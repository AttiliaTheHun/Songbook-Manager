package attilathehun.songbook.environment;

import attilathehun.songbook.Client;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EnvironmentManager {

    public void loadData() {
        try {
            if (Environment.getInstance().settings.REMOTE_SAVE_LOAD_ENABLED) {
                File dataFile = new File(Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
                Client client = new Client();
                if (dataFile.exists()) {
                    String content = String.join("\n", Files.readAllLines(dataFile.toPath()));
                    String localHash = createSHAHash(content);
                    String remoteHash = client.httpGet(Environment.getInstance().settings.REMOTE_DATA_FILE_HASH_URL);
                    if (localHash.equals(remoteHash)) {
                        Environment.showMessage("Success", "The local version of data is up to date with the remote one");
                        return;
                    }
                }
                client.downloadData();

            } else if (!new File(Environment.getInstance().settings.DATA_ZIP_FILE_PATH).exists()) {
                //Does not exist, create new collection for a fresh start?
                return;
            }
            unzipData();
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not load the data");
        }
        Environment.showMessage("Success", "Data loaded successfully");
    }

    public void saveData() {
        try {
            zipData();
            if (Environment.getInstance().settings.REMOTE_SAVE_LOAD_ENABLED) {
                File dataFile = new File(Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
                Client client = new Client();
                if (dataFile.exists()) {
                    String content = String.join("\n", Files.readAllLines(dataFile.toPath()));
                    String localHash = createSHAHash(content);
                    String remoteHash = client.httpGet(Environment.getInstance().settings.REMOTE_DATA_FILE_HASH_URL);
                    if (localHash.equals(remoteHash)) {
                        Environment.showMessage("Success", "The remote version of data is up to date with the local one");
                        return;
                    }
                    client.uploadData();
                }
                Environment.showMessage("No way", "This should never have happened");
                return;

            }

        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not save the data");
        }
        Environment.showMessage("Success", "Data saved successfully");
    }

    private void unzipData(){
        try {
            String fileZip = Environment.getInstance().settings.DATA_ZIP_FILE_PATH;
            File destDir = new File(Environment.getInstance().settings.DATA_FILE_PATH);

            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Data problem", "Cannot decompress the data!");
        }
    }

    public void zipData() {
        try {
            logEditingUser();
            String sourceFile = Environment.getInstance().settings.DATA_FILE_PATH;
            FileOutputStream fos = new FileOutputStream(Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            File fileToZip = new File(sourceFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

            zipOut.close();
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Data problem", "Cannot compress the data!");
        }

    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public String createSHAHash(String input) throws NoSuchAlgorithmException {

        String hashtext = null;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageDigest =
                md.digest(input.getBytes(StandardCharsets.UTF_8));

        hashtext = convertToHex(messageDigest);
        return hashtext;
    }

    private String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }

    public void logEditingUser() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        final String date = dtf.format(now);
        final String username = System.getProperty("user.name");
        PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.EDIT_LOG_FILE_PATH), true));
        printWriter.write(date + " " + username + "\n");
        printWriter.close();
    }
}
