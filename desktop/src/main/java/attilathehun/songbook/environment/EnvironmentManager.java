package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.VCSAdmin;
import attilathehun.songbook.window.CollectionEditor;
import attilathehun.songbook.SongbookApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.io.*;
import java.text.NumberFormat;

public class EnvironmentManager {

    private static final Logger logger = LogManager.getLogger(EnvironmentManager.class);

    private static final int ACTION_EDIT = 0;
    private static final int ACTION_ADD = 1;


    public void load() {
        if (Environment.getInstance().settings.vcs.REMOTE_SAVE_LOAD_ENABLED) {
            VCSAdmin.getInstance().pull();
            return;
        }
        if (!new File(Environment.getInstance().settings.environment.DATA_ZIP_FILE_PATH).exists()) {
            Environment.showErrorMessage("Error", "Could not file a local data zip file.");
            return;
        }
        if (!extractLocalDataFile()) {
            return;
        }
        Environment.showMessage("Success", "Data loaded successfully");
    }

    private boolean extractLocalDataFile() {
        try {
            ZipBuilder.extract(Environment.getInstance().settings.environment.DATA_ZIP_FILE_PATH, Environment.getInstance().settings.environment.DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Could not extract the data");
            return false;
        }
        return true;
    }

    public void save() {
        if (Environment.getInstance().settings.vcs.REMOTE_SAVE_LOAD_ENABLED) {
            VCSAdmin.getInstance().push();
            return;
        }
        if (!archiveDataToLocalFile()) {
            return;
        }

    }

    private boolean archiveDataToLocalFile() {
        try {
            ZipBuilder builder = new ZipBuilder();
            builder.setIncludeSourceFolder(false);
            builder.setOutputPath(Environment.getInstance().settings.environment.DATA_ZIP_FILE_PATH);
            builder.addFolder(new File(Environment.getInstance().settings.environment.DATA_FILE_PATH), "/");
            builder.finish();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Could not archive the data");
            return false;
        }
        return true;
    }


    public void createNewSongbook() {
        try {
            File songDataFolder = new File(Environment.getInstance().settings.environment.SONG_DATA_FILE_PATH);
            songDataFolder.mkdirs();
            File collectionJSONFile = new File(Environment.getInstance().settings.environment.COLLECTION_FILE_PATH);
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
            Environment.showErrorMessage("Error", "Could not create a new songbook!", true);
        }
    }

    @Deprecated
    public void loadSongbook() {
        if (Environment.getInstance().settings.vcs.REMOTE_SAVE_LOAD_ENABLED) {
            VCSAdmin.getInstance().pull();
            return;
        }
        extractLocalDataFile();
        Environment.getInstance().refresh();
        Environment.getInstance().getCollectionManager().init();
        SongbookApplication.dialControlPLusRPressed();
        Environment.showMessage("Success", "Songbook loaded successfully.");
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
                CollectionEditor.forceRefreshInstance();
                return song;
            }
        } else {
            if (option == JOptionPane.OK_OPTION) {
                Song song = new Song(songNameField.getText(), s.id());
                song.setUrl(songURLField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.updateSongRecord(song);
                CollectionEditor.forceRefreshInstance();
                return song;
            }
        }
        return null;

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
                    Environment.showErrorMessage("Error", "Invalid Id value!");
                    return null;
                }

                Song song = new Song(songNameField.getText(), Integer.parseInt(songIdField.getText()));
                song.setUrl(songURLField.getText());
                song.setAuthor(songAuthorField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.addSong(song);
                CollectionEditor.forceRefreshInstance();
                return song;
            }
        } else {
            if (option == JOptionPane.OK_OPTION) {
                if (songIdField.getText().equals("")) {
                    Environment.showErrorMessage("Error", "Invalid Id value!");
                    return null;
                }

                Song song = new Song(songNameField.getText(), Integer.parseInt(songIdField.getText()));
                song.setFormerId(s.id());
                song.setUrl(songURLField.getText());
                song.setActive(songActiveSwitch.isSelected());
                song = manager.updateSongRecord(song);
                CollectionEditor.forceRefreshInstance();
                return song;
            }
        }
        return null;

    }

    public void autoLoad() {
        if (Environment.getInstance().settings.user.AUTO_LOAD_DATA) {
            load();
        }
    }


}
