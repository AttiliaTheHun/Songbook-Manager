package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {

    private static final Logger logger = LogManager.getLogger(Client.class);
    private Status status = null;


    public void postFile(String targetUrl, String fileUrl, String authToken) throws Exception {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "Application/zip; charset=utf-8");
        conn.setRequestProperty("Content-Disposition", "attachments; filename=data.zip");
        conn.setRequestProperty("Authorization", "Bearer " + authToken);

        conn.connect();
        if (conn.getResponseCode() == 400) {
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(Environment.getInstance().settings.environment.DATA_ZIP_FILE_PATH)));
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            conn.disconnect();
        } else {
            //Environment.getInstance().log("Error uploading data: Response code " + conn.getResponseCode());
            Environment.showMessage("Could not upload the data", "HTTP response code: " + conn.getResponseCode());
        }

    }

    public String getFile(String targetUrl, String fileUrl, String authToken) throws Exception {
        try {
            String fileContent = getFile(remoteDataZipFileDownloadURL, Environment.getInstance().acquireToken(new Certificate()));
            File file = new File(Environment.getInstance().settings.environment.DATA_ZIP_FILE_PATH);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file, false);
            outputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            Environment.showMessage("Success", "Data downloaded successfully from the server.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String httpGet(String urlToRead) throws Exception {
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
        return result.toString();
    }

    public Status getStatus() {
        return status;
    }

    public static class Status {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;
        public static final int OTHER = 2;
        private int state;
        private Exception error = null;

        public Status(int state, Exception e) {
            this.state = state;
            this.error = e;
        }

        public int getCode() {
            return state;
        }

        public Exception getError() {
            return error;
        }
    }
}
