package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


class Client {

    private static final Logger logger = LogManager.getLogger(Client.class);
    private Status status = null;

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";


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
            Environment.showMessage("Could not upload the data", "HTTP response code: " + conn.getResponseCode());
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
        if (conn.getResponseCode() == 400) {
            Path outputPath = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH, "response.zip");
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
            Environment.showMessage("Could not upload the data", "HTTP response code: " + conn.getResponseCode());
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
