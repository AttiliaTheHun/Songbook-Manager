package attilathehun.songbook.environment;

import attilathehun.songbook.util.Client;
import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.util.ZipGenerator;
import javafx.util.Pair;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
                Environment.showWarningMessage("Warning", "Could not file a local data zip file.");
                return;
            }
            if (!unzipData()) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not load the data");
            return;
        }
        Environment.showMessage("Success", "Data loaded successfully");
    }

    public void loadData(String remoteApiEndpointURL) {
        try {
            if (Environment.getInstance().settings.REMOTE_SAVE_LOAD_ENABLED) {
                File dataFile = new File(Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
                Client client = new Client();
                if (dataFile.exists()) {
                    String content = String.join("\n", Files.readAllLines(dataFile.toPath()));
                    String localHash = createSHAHash(content);
                    String remoteHash = client.httpGet(remoteApiEndpointURL.endsWith("/") ? remoteApiEndpointURL + "hash/" : remoteApiEndpointURL + "/hash/");
                    if (localHash.equals(remoteHash)) {
                        Environment.showMessage("Success", "The local version of data is up to date with the remote one");
                        return;
                    }
                }
                client.downloadData(remoteApiEndpointURL.endsWith("/") ? remoteApiEndpointURL + "download/" : remoteApiEndpointURL + "/download/");

            } else if (!new File(Environment.getInstance().settings.DATA_ZIP_FILE_PATH).exists()) {
                Environment.showWarningMessage("Warning", "Could not load a local data zip file.");
                return;
            }
            if (!unzipData()) {
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not load the data");
            return;
        }
        Environment.showMessage("Success", "Data loaded successfully");
    }

    public void saveData() {
        try {
            logEditingUser();
            if (!zipData()) {
                return;
            }

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
            return;
        }
        Environment.showMessage("Success", "Data saved successfully");
    }

    private boolean unzipData() {
        try {
            new ZipGenerator().extractZip(Environment.getInstance().settings.DATA_ZIP_FILE_PATH, Environment.getInstance().settings.DATA_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not unzip the data");
            return false;
        }
        return true;
    }


    private boolean zipData() {
        try {
            new ZipGenerator(false).createZip(Environment.getInstance().settings.DATA_FILE_PATH, Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not zip the data");
            return false;
        }
        return true;
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        final String date = dtf.format(now);
        final String username = System.getProperty("user.name");
        PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.EDIT_LOG_FILE_PATH), true));
        printWriter.write(date + " " + username + "\n");
        printWriter.close();
    }

    public void createNewSongbook() {
        try {
            File songDataFolder = new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH);
            songDataFolder.mkdirs();
            Environment.getInstance().getCollectionManager().createShadowSong();
            Environment.getInstance().getCollectionManager().createShadowSong();
            File collectionJSONFile = new File(Environment.getInstance().settings.COLLECTION_FILE_PATH);
            collectionJSONFile.createNewFile();
            PrintWriter printWriter = new PrintWriter(new FileWriter(collectionJSONFile));
            printWriter.write("[]");
            printWriter.close();
            //TODO: ask if the user wants to create a song right away
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Could not create a new songbook!");
        }
    }

    public void loadSongbook() {
        if (Environment.getInstance().settings.REMOTE_SAVE_LOAD_ENABLED) {
            Pair<String, String> input = loadSongbookInputDialog();
            if (input.getKey() == null) {
                Environment.showWarningMessage("Warning", "Songbook loading aborted");
                return;
            }
            Environment.getInstance().loadTokenToMemory(input.getValue(), new Certificate());
            loadData(input.getKey());

            return;
        }
        unzipData();
        Environment.getInstance().refresh();
        Environment.getInstance().getCollectionManager().init();
        SongbookApplication.dialControlPLusRPressed();
        Environment.showMessage("Success", "Songbook loaded successfully.");
    }

    private Pair<String, String> loadSongbookInputDialog() {
        JLabel label = new JLabel("Leave the field blank to use the default value.");
        JTextField remoteApiEndpointURL = new JTextField();
        remoteApiEndpointURL.setToolTipText("For example http://example.org/api/data/");
        JTextField token = new JPasswordField();
        token.setToolTipText("The token must have READ permission.");
        Object[] message = {
                label,
                "Remote API Endpoint:", remoteApiEndpointURL,
                "Token:", token
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Load Remote Songbook", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return new Pair<String, String>(remoteApiEndpointURL.getText(), token.getText());
        }
        return new Pair<>(null, null);
    }

    public static class Certificate {
        private Certificate() {
        }
    }
}
