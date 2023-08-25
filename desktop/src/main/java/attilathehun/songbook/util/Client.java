package attilathehun.songbook.util;

import attilathehun.songbook.environment.Environment;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {

    public void downloadData() {
        downloadData(Environment.getInstance().settings.REMOTE_DATA_ZIP_FILE_DOWNLOAD_URL);
    }

    public void downloadData(String remoteDataZipFileDownloadURL) {
        try {
            String fileContent = getFile(remoteDataZipFileDownloadURL, Environment.getInstance().acquireToken(new Certificate()));
            File file = new File(Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file, false);
            outputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            //TODO: remove for production
            Environment.showMessage("Success", "Data downloaded successfully from the server.");
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
        }
    }

    public void uploadData() {
        uploadData(Environment.getInstance().settings.REMOTE_DATA_ZIP_FILE_UPLOAD_URL);
    }

    public void uploadData(String remoteDataZipFileUploadURL) {
        try {
            postFile(remoteDataZipFileUploadURL, Environment.getInstance().acquireToken(new Certificate()));
            Environment.showMessage("Success", "Data uploaded successfully to the server.");
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
        }

    }


    private void postFile(String targetUrl, String authToken) throws Exception {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "Application/zip; charset=utf-8");
        conn.setRequestProperty("Content-Disposition", "attachments; filename=data.zip");
        conn.setRequestProperty("Authorization", "Bearer " + authToken);

        conn.connect();
        if (conn.getResponseCode() == 400) {
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(Environment.getInstance().settings.DATA_ZIP_FILE_PATH)));
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            conn.disconnect();
        } else {
            Environment.getInstance().log("Error uploading data: Response code " + conn.getResponseCode());
            Environment.showMessage("Could not upload the data", "HTTP response code: " + conn.getResponseCode());
        }

    }

    private String getFile(String targetUrl, String authToken) throws Exception {

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

    public static class Certificate {
        private Certificate() {
        }
    }
}
