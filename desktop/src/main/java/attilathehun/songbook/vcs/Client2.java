package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Client2 {
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

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int ACCESS_DENIED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int NA = -1;


    public Result http(final String url, final String method, final String token, final Object data, final String metadata) throws IOException {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("invalid url");
        }

        if (method == null || !(method.equals(HTTP_GET) || method.equals(HTTP_POST) || method.equals(HTTP_PUT) || method.equals(HTTP_DELETE))) {
            throw new IllegalArgumentException("invalid method");
        }

        final URL endpoint = new URL(url);
        final HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod(method);
        if (token != null && token.length() != 0) {
            conn.setRequestProperty(AUTHORIZATION_HEADER, String.format(AUTHORIZATION_HEADER_VALUE, token));
        }

        if (data != null) {
            conn.setDoOutput(true);
            if (data instanceof File) {
                if (((File) data).getName().endsWith(ZIP_EXTENSION)) {
                    conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_ZIP);
                } else if (((File) data).getName().endsWith(JSON_EXTENSION)) {
                    conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
                } else {
                    conn.setRequestProperty(CONTENT_TYPE_HEADER, "application/custom; charset=utf-8");
                }
                if (metadata != null && metadata.length() != 0) {
                    conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", metadata));
                } else {
                    conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", ((File) data).getName()));
                }

                final OutputStream outputStream = conn.getOutputStream();
                final InputStream fileInputStream = new FileInputStream((File) data);
                outputStream.write(fileInputStream.readAllBytes());
                outputStream.flush();
                outputStream.close();

                /*
                if (conn.getResponseCode() == OK) {
                } else {

                    byte[] response = (conn.getErrorStream() == null) ? null : conn.getInputStream().readAllBytes();
                    byte[] error = (conn.getErrorStream() == null) ? null : conn.getErrorStream().readAllBytes();
                    conn.disconnect();
                    return new Result(conn.getResponseCode(), response, error, null);
                }*/


            } else {
                conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
                if (metadata != null && metadata.length() != 0) {
                    conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, String.format("attachments; filename=%s", metadata));
                }


                if (conn.getResponseCode() == OK) {
                    final OutputStream outputStream = conn.getOutputStream();
                    if (data instanceof String) {
                        outputStream.write(((String) data).getBytes(StandardCharsets.UTF_8));
                    } else {
                        final String jsonRequestBody = new Gson().toJson(data);
                        outputStream.write(jsonRequestBody.getBytes(StandardCharsets.UTF_8));
                    }
                    outputStream.flush();
                    outputStream.close();

                } else {
                    byte[] response = (conn.getErrorStream() == null) ? null : conn.getInputStream().readAllBytes();
                    byte[] error = (conn.getErrorStream() == null) ? null : conn.getErrorStream().readAllBytes();
                    conn.disconnect();
                    return new Result(conn.getResponseCode(), response, error, null);
                }


            }
        }

        byte[] response = null;
        String filename = null;

        if (conn.getResponseCode() < 400) { // prevent {@link HttpURLConnection#getInputStream()} from throwing exception
            if (conn.getHeaderFields().get(CONTENT_DISPOSITION_HEADER) != null) {
                filename = conn.getHeaderFields().get(CONTENT_DISPOSITION_HEADER).get(0).replace("attachment; filename=", "")
                        .replace("attachment;filename=", "");
                final InputStream in = conn.getInputStream();
                filename = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"), filename).toString();
                Files.copy(in, Path.of(filename), StandardCopyOption.REPLACE_EXISTING);
                in.close();

            } else {
                response = (conn.getInputStream() == null) ? null : conn.getInputStream().readAllBytes();
            }
        }


        byte[] error = (conn.getErrorStream() == null) ? null : conn.getErrorStream().readAllBytes();
        conn.disconnect();
        return new Result(conn.getResponseCode(), response, error, filename);

    }

    public record Result(int responseCode, byte[] response, byte[] error, String metadata) {
       /* public Result(int responseCode, byte[] response, byte[] error, String metadata) {
            this.responseCode = responseCode;
            this.response = response;
            this.error = error;
            this.metadata = metadata;
            System.out.println(new Gson().toJson(this));
        }*/
    }

}
