package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Deprecated
public class Client {
    private static final Logger logger = LogManager.getLogger(Client.class);
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";
    private static final String HTTP_PUT = "PUT";
    private static final String HTTP_DELETE = "DELETE";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_ZIP = "application/zip";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int ACCESS_DENIED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int NA = -1;


    /**
     * Sends a GET request to the specified endpoint using the specified authorization token and if the response is satisfactory, saves it
     * to the temp path and returns path to the response file. The response file is supposed to contain the entire remote songbook data.
     *
     * @param endpoint the URL to send the request to (download endpoint)
     * @param token the authorization token (needs READ perms)
     * @return {@link Result} with response file path as a message or with error (the other value is always null)
     * @throws IOException upon connection or file write error
     */
    public Result getCompleteLoadRequest(final String endpoint, final String token) throws IOException {
        return getLoadRequest(endpoint, token, null);
    }

    /**
     * Sends a POST request to the specified endpoint using the specified authorization token and if the response is satisfactory, saves it
     * to the temp path and returns path to the response file. The response file is supposed to contain only the files specified by the request
     * index.
     *
     * @param endpoint the URL to send the request to (download endpoint)
     * @param token the authorization token (needs READ perms)
     * @param index load request index
     * @return {@link Result} with response file path as a message or with error (the other value is always null)
     * @throws IOException upon connection or file write error
     */
    public Result getPartialLoadRequest(final String endpoint, final String token, final LoadIndex index) throws IOException {
        return getLoadRequest(endpoint, token, index);
    }

    private Result getLoadRequest(final String endpoint, final String token, final LoadIndex index) throws IOException {
        if (endpoint == null || endpoint.length() == 0) {
            throw new IllegalArgumentException("endpoint must be specified");
        }
        if (token == null || token.length() == 0) {
            throw new IllegalArgumentException("token must be specified");
        }

        final URL url = new URL(endpoint);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(HTTP_GET);
        conn.setDoOutput(true);
        conn.setRequestProperty(AUTHORIZATION_HEADER, "Bearer " + token);
        if (index != null) {
            // send over the index (will change the request to POST tho)
            conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
            conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, "attachment;filename=index.json");

            final OutputStream outputStream = conn.getOutputStream();
            outputStream.write(new Gson().toJson(index).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        }

        conn.connect();
        if (conn.getResponseCode() == OK) {
            final String contentType = conn.getHeaderField(CONTENT_TYPE_HEADER);
            final String contentDisposition = conn.getHeaderField(CONTENT_DISPOSITION_HEADER);
            if (contentType == null || contentDisposition == null) {
                return new Result(null, "invalid response header values");
            }
            if (!contentType.equalsIgnoreCase(CONTENT_TYPE_ZIP)) {
                return new Result(null, "invalid response header values");
            }
            if (!contentDisposition.startsWith("attachment") || !contentDisposition.contains("filename=")) {
                return new Result(null, "invalid response header values");
            }

            final String filename = contentDisposition.substring(contentDisposition.indexOf("=") + 1);
            final Path outputPath = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH"), filename);

            final InputStream in = conn.getInputStream();
            Files.copy(in, outputPath);

            in.close();
            conn.disconnect();
            return new Result(outputPath.toString(), null);
        } else {
            new AlertDialog.Builder().setTitle("Could not download the data").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("HTTP response code: %s", conn.getResponseCode()))
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        }
        conn.disconnect();

        return new Result(null, (conn.getErrorStream() != null) ? new String(conn.getErrorStream().readAllBytes()) : String.format("invalid response code: %d", conn.getResponseCode()));

    }

    public Result postSaveRequestFile(final String endpoint, final String token, final String requestFilePath) throws IOException {
        if (endpoint == null || endpoint.length() == 0) {
            throw new IllegalArgumentException("endpoint must be specified");
        }
        if (token == null || token.length() == 0) {
            throw new IllegalArgumentException("token must be specified");
        }
        if (requestFilePath == null || requestFilePath.length() == 0) {
            throw new IllegalArgumentException("request file path must be specified");
        }

        final URL url = new URL(endpoint);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(HTTP_POST);
        conn.setDoOutput(true);
        conn.setRequestProperty(AUTHORIZATION_HEADER, "Bearer " + token);

        conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_ZIP);
        conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, "attachment;filename=save_request.zip");

        // send over the request file
        try (final OutputStream outputStream = conn.getOutputStream()) {
            Files.copy(Path.of(requestFilePath), outputStream);
            outputStream.flush();
        }

        conn.connect();
        conn.disconnect();
        if (conn.getResponseCode() == CREATED) {
            return new Result("success", null);
        }
        new AlertDialog.Builder().setTitle("Could not download the data").setIcon(AlertDialog.Builder.Icon.ERROR)
                .setMessage(String.format("HTTP response code: %s", conn.getResponseCode()))
                .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        return new Result(null, (conn.getErrorStream() != null) ? String.format("%d %s", conn.getResponseCode(), new String(conn.getErrorStream().readAllBytes())) : String.format("invalid response code: %d", conn.getResponseCode()));
    }

    /**
     * The {@link Client} data structure.
     *
     * @param message any kind of {@link String} output (can be null)
     * @param error any kind of error message (can be null)
     * @param data data Object (can be null)
     */
    public record Result(String message, String error, Object data) {
        public Result(final String message, final String error) {
            this(message, error, null);
        }
    }




    private Status status = null;

    public void postRequestFile(String targetUrl, String requestFileUrl, String authToken) throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "Application/zip; charset=utf-8");
        conn.setRequestProperty("Content-Disposition", "attachments; filename=request.zip");
        conn.setRequestProperty("Authorization", "Bearer " + authToken);

        conn.connect();
        if (conn.getResponseCode() == 400) {
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(requestFileUrl)));
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            conn.disconnect();
        } else {
            new AlertDialog.Builder().setTitle("Could not upload the data").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("HTTP response code: %s", conn.getResponseCode()))
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        }
        setStatus(new Status(conn.getResponseCode(), (conn.getErrorStream() != null) ? new String(conn.getErrorStream().readAllBytes()) : ""));
    }

    public String getResponseFile(String targetUrl, String indexFileUrl, String authToken) throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "Application/json; charset=utf-8");
        conn.setRequestProperty("Content-Disposition", "attachments; filename=index.json");
        conn.setRequestProperty("Authorization", "Bearer " + authToken);

        conn.connect();
        if (conn.getResponseCode() == OK) {
            Path outputPath = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH"), "response.zip");
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(indexFileUrl)));
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            InputStream in = conn.getInputStream();
            Files.copy(in, outputPath);
            in.close();
            conn.disconnect();
            return outputPath.toString();
        } else {
            new AlertDialog.Builder().setTitle("Could not upload the data").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("HTTP response code: %s", conn.getResponseCode()))
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        }
        setStatus(new Status(conn.getResponseCode(), (conn.getErrorStream() != null) ? new String(conn.getErrorStream().readAllBytes()) : ""));
        return null;
    }

    public String httpGet(String urlToRead) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        setStatus(new Status(conn.getResponseCode(), (conn.getErrorStream() != null) ? new String(conn.getErrorStream().readAllBytes()) : ""));
        return result.toString();
    }

    public String httpGet(String urlToRead, String token) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        setStatus(new Status(conn.getResponseCode(), (conn.getErrorStream() != null) ? new String(conn.getErrorStream().readAllBytes()) : ""));
        return result.toString();
    }

    public Status getStatus() {
        return status;
    }

    private void setStatus(Status s) {
        this.status = s;
    }

    public static class Status {
        public static final int SUCCESS = 200;
        public static final int NO_CONTENT = 204;
        public static final int ACCESS_DENIED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int I_AM_A_TEAPOT = 418;
        public static final int NA = -248;
        private final int state;
        private String error = null;

        public Status(int state, String e) {
            this.state = state;
            this.error = e;
        }

        public int getCode() {
            return state;
        }

        public String getError() {
            return error;
        }
    }
}
