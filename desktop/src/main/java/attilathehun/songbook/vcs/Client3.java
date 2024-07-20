package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Deprecated
public class Client3 {
    private static final Logger logger = LogManager.getLogger(Client.class);
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_ZIP = "application/zip; charset=utf-8";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String JSON_EXTENSION = ".json";
    public static final int ACCESS_DENIED = 401;
    @Deprecated
    public static final int FORBIDDEN = 403;
    @Deprecated
    public static final int NOT_FOUND = 404;
    @Deprecated
    public static final int METHOD_NOT_ALLOWED = 405;
    @Deprecated
    public static final int SERVICE_UNAVAILABLE = 503;


    public Result http(final String url, final String method, final String token, final Object data, final String metadata) throws IOException {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("invalid url");
        }

        if (method == null || !(method.equals(HTTP_GET) || method.equals(HTTP_POST) || method.equals(HTTP_PUT) || method.equals(HTTP_DELETE))) {
            throw new IllegalArgumentException("invalid method");
        }
        final OkHttpClient httpClient = new OkHttpClient();
        final Request.Builder requestBuilder = new Request.Builder().url(url);

        if (token != null && token.length() != 0) {
            requestBuilder.addHeader(AUTHORIZATION_HEADER, String.format(AUTHORIZATION_HEADER_VALUE, token));
        }

        if (data != null) {

            if (data instanceof File) {
                if (((File) data).getName().endsWith(ZIP_EXTENSION)) {
                    requestBuilder.addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_ZIP);
                } else if (((File) data).getName().endsWith(JSON_EXTENSION)) {
                    requestBuilder.addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
                } else {
                    requestBuilder.addHeader(CONTENT_TYPE_HEADER, "application/custom; charset=utf-8");
                }
                if (metadata != null && metadata.length() != 0) {
                    requestBuilder.addHeader(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", metadata));
                } else {
                    requestBuilder.addHeader(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", ((File) data).getName()));
                }

                final InputStream fileInputStream = new FileInputStream((File) data);
                final RequestBody requestBody = RequestBody.create(fileInputStream.readAllBytes());
                fileInputStream.close();
                requestBuilder.method(method, requestBody);

            } else {

                requestBuilder.addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
                if (metadata != null && metadata.length() != 0) {
                    requestBuilder.addHeader(CONTENT_DISPOSITION_HEADER, String.format("attachments; filename=%s", metadata));
                }

                RequestBody requestBody;

                if (data instanceof String) {
                    requestBody = RequestBody.create(((String) data).getBytes(StandardCharsets.UTF_8));
                } else {
                    final String jsonRequestBody = new Gson().toJson(data);
                    requestBody = RequestBody.create(jsonRequestBody.getBytes(StandardCharsets.UTF_8));
                }

                requestBuilder.method(method, requestBody);

            }
        } else {
            requestBuilder.method(method, null);
        }

        try (final Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            String filename = null;
            if (response.header(CONTENT_DISPOSITION_HEADER, null) != null) {
                filename = response.header(CONTENT_DISPOSITION_HEADER, null).replace("attachment; filename=", "")
                        .replace("attachment;filename=", "");
                final InputStream in = response.body().byteStream();
                filename = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"), filename).toString();
                Files.copy(in, Path.of(filename), StandardCopyOption.REPLACE_EXISTING);
                in.close();
            }
            String content = null;
            String error = null;
            if (response.code() >= 300 || response.code() < 200) {
                error = response.body().string();
            } else {
                content = response.body().string();
            }

            return new Result(response.code(), content, error, filename);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            return new Result(-1, null,e.getLocalizedMessage(), null);
        }
    }

    public record Result(int responseCode, String response, String error, String metadata) {
       /* public Result(int responseCode, byte[] response, byte[] error, String metadata) {
            this.responseCode = responseCode;
            this.response = response;
            this.error = error;
            this.metadata = metadata;
            System.out.println(new Gson().toJson(this));
        }*/
    }

}
