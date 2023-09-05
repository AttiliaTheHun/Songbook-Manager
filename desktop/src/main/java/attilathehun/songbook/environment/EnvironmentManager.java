package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.util.Client;
import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.util.ZipGenerator;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EnvironmentManager {

    private static final Logger logger = LogManager.getLogger(EnvironmentManager.class);

    private static final int ACTION_EDIT = 0;
    private static final int ACTION_ADD = 1;

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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            Environment.showWarningMessage("Warning", "Could not save the data");
            return;
        }
        Environment.showMessage("Success", "Data saved successfully");
    }

    private boolean unzipData() {
        try {
            new ZipGenerator().extractZip(Environment.getInstance().settings.DATA_ZIP_FILE_PATH, Environment.getInstance().settings.DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showWarningMessage("Warning", "Could not unzip the data");
            return false;
        }
        return true;
    }


    private boolean zipData() {
        try {
            new ZipGenerator(false).createZip(Environment.getInstance().settings.DATA_FILE_PATH, Environment.getInstance().settings.DATA_ZIP_FILE_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
            //Environment.getInstance().getCollectionManager().createShadowSong();
            //Environment.getInstance().getCollectionManager().createShadowSong();
            File collectionJSONFile = new File(Environment.getInstance().settings.COLLECTION_FILE_PATH);
            collectionJSONFile.createNewFile();
            PrintWriter printWriter = new PrintWriter(new FileWriter(collectionJSONFile));
            printWriter.write("[]");
            printWriter.close();
            EnvironmentVerificator.SUPPRESS_WARNINGS = true;

            UIManager.put("OptionPane.okButtonText", "Add");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");

            int option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to add your first song?", "Add a Song?", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                addSongDialog(Environment.getInstance().getCollectionManager());
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
        //Environment.showMessage("Success", "Songbook loaded successfully.");
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

        int option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), message, "Load Remote Songbook", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return new Pair<String, String>(remoteApiEndpointURL.getText(), token.getText());
        }
        return new Pair<>(null, null);
    }


    public static Song addSongDialog(CollectionManager manager) {
        if (StandardCollectionManager.getInstance().equals(manager)) {
            return addStandardSongDialog(manager);
        } else if (EasterCollectionManager.getInstance().equals(manager)) {
            return addEasterSongDialog(manager);
        }
        return null;
    }

    public static Song editSongDialog(Song s, CollectionManager manager) {
        if (StandardCollectionManager.getInstance().equals(manager)) {
            return editStandardSongDialog(s, manager);
        } else if (EasterCollectionManager.getInstance().equals(manager)) {
            return editEasterSongDialog(s, manager);
        }
        return null;
    }

    private static Song addStandardSongDialog(CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Add");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
         return songActionDialog(manager.getPlaceholderSong(), manager, ACTION_ADD);
    }

    private static Song editStandardSongDialog(Song s, CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Save Changes");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return songActionDialog(s, manager, ACTION_EDIT);
    }

    private static Song songActionDialog(Song s, CollectionManager manager, int action) {

        if (action < ACTION_EDIT || action > ACTION_ADD) {
            throw new IllegalArgumentException();
        }

        JTextField songNameField = new JTextField();
        songNameField.setToolTipText("Name of the song. For example 'I Will Always Return'.");
        JTextField songAuthorField = new JTextField();
        songAuthorField.setToolTipText("Author or interpret of the song. For example 'Leonard Cohen'.");
        JTextField songURLField = new JTextField();
        songURLField.setToolTipText("Link to a video performance of the song.");
        JCheckBox songActiveSwitch = new JCheckBox("Active");
        songActiveSwitch.setToolTipText("When disabled, the song will not be included in the songbook.");

        songNameField.setText(s.name());
        songAuthorField.setText(s.getAuthor());
        songURLField.setText(s.getUrl());
        songActiveSwitch.setSelected(true);

        Object[] message;

        if (action == ACTION_ADD) {
            message = new Object[]{
                    "Name:", songNameField,
                    "Author:", songAuthorField,
                    "URL:", songURLField,
                    songActiveSwitch
            };
        } else {
            message = new Object[]{
                    "Name:", songNameField,
                    "URL:", songURLField,
                    songActiveSwitch
            };
        }

        int option;

        if (action == ACTION_ADD) {
            option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), message, "Add a Song", JOptionPane.OK_CANCEL_OPTION);
        } else {
            option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), message, "Edit Song id: " + s.id(), JOptionPane.OK_CANCEL_OPTION);
        }

        UIManager.put("OptionPane.okButtonText", "Ok");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        if (action == ACTION_ADD) {
            if (option == JOptionPane.OK_OPTION) {
                Song song = new Song(songNameField.getText(), -1);
                song.setUrl(songURLField.getText());
                song.setAuthor(songAuthorField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.addSong(song);
                return song;
            }
        } else {
            if (option == JOptionPane.OK_OPTION) {
                Song song = new Song(songNameField.getText(), s.id());
                song.setUrl(songURLField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.updateSongRecord(song);
                return song;
            }
        }
        return null;
//TODO: Refresh CollectionEditor list if an instance exists

    }

    private static Song addEasterSongDialog(CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Add");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return easterSongActionDialog(manager.getPlaceholderSong(), manager, ACTION_ADD);
    }

    public static Song addEasterSongFromTemplateDialog(Song s, CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Add");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        if (s == null) {
            return addEasterSongDialog(manager);
        }
        return easterSongActionDialog(s, manager, ACTION_ADD);
    }

    private static Song editEasterSongDialog(Song s, CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Save Changes");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return easterSongActionDialog(s, manager, ACTION_EDIT);
    }

    private static Song easterSongActionDialog(Song s, CollectionManager manager, int action) {

        if (action < ACTION_EDIT || action > ACTION_ADD) {
            throw new IllegalArgumentException();
        }

        NumberFormat longFormat = NumberFormat.getIntegerInstance();

        NumberFormatter numberFormatter = new NumberFormatter(longFormat);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setMinimum(0l);

        JTextField songNameField = new JTextField();
        songNameField.setToolTipText("Name of the song. For example 'I Will Always Return'.");
        JFormattedTextField songIdField = new JFormattedTextField(numberFormatter);
        songIdField.setToolTipText("Identificator of the song. Do not confuse with collection index (n).");
        JTextField songAuthorField = new JTextField();
        songAuthorField.setToolTipText("Author or interpret of the song. For example 'Leonard Cohen'.");
        JTextField songURLField = new JTextField();
        songURLField.setToolTipText("Link to a video performance of the song.");
        JCheckBox songActiveSwitch = new JCheckBox("Active");
        songActiveSwitch.setToolTipText("When disabled, the song will not be included in the songbook.");

        songNameField.setText(s.name());
        songIdField.setText(String.valueOf(s.id()));
        songAuthorField.setText(s.getAuthor());
        songURLField.setText(s.getUrl());
        songActiveSwitch.setSelected(true);

        Object[] message;

        if (action == ACTION_ADD) {
            message = new Object[]{
                    "Name:", songNameField,
                    "Id:", songIdField,
                    "Author:", songAuthorField,
                    "URL:", songURLField,
                    songActiveSwitch
            };
        } else {
            message = new Object[]{
                    "Name:", songNameField,
                    "Id:", songIdField,
                    "URL:", songURLField,
                    songActiveSwitch
            };
        }

        int option;

        if (action == ACTION_ADD) {
            option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), message, "Add Easter Song", JOptionPane.OK_CANCEL_OPTION);
        } else {
            option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), message, "Edit Easter Song id: " + s.id(), JOptionPane.OK_CANCEL_OPTION);
        }

        UIManager.put("OptionPane.okButtonText", "Ok");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");



        if (action == ACTION_ADD) {
            if (option == JOptionPane.OK_OPTION) {
                if (songIdField.getText().equals("")) {
                    Environment.showWarningMessage("Warning", "Invalid Id value!");
                    return null;
                }

                Song song = new Song(songNameField.getText(), Integer.parseInt(songIdField.getText()));
                song.setUrl(songURLField.getText());
                song.setAuthor(songAuthorField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.addSong(song);
                return song;
            }
        } else {
            if (option == JOptionPane.OK_OPTION) {
                if (songIdField.getText().equals("")) {
                    Environment.showWarningMessage("Warning", "Invalid Id value!");
                    return null;
                }

                Song song = new Song(songNameField.getText(), Integer.parseInt(songIdField.getText()));
                song.setFormerId(s.id());
                song.setUrl(songURLField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.updateSongRecord(song);
                return song;
            }
        }
        return null;
//TODO: Refresh CollectionEditor list if an instance exists

    }




    public static class Certificate {
        private Certificate() {
        }
    }
}
