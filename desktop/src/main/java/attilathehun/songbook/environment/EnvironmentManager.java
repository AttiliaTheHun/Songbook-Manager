package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.plugin.Plugin;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.VCSAdmin;
import attilathehun.songbook.window.AlertDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;

public class EnvironmentManager {

    private static final Logger logger = LogManager.getLogger(EnvironmentManager.class);

    private static final EnvironmentManager INSTANCE = new EnvironmentManager();

    private SongbookSettings songbookSettings = getDefaultSongbookSettings();
    private UserSettings userSettings = getDefaultUserSettings();

    private EnvironmentManager() {}

    public static EnvironmentManager getInstance() {
        return INSTANCE;
    }

    @Deprecated
    private static final int ACTION_EDIT = 0;
    @Deprecated
    private static final int ACTION_ADD = 1;

    public SongbookSettings getDefaultSongbookSettings() {
        SongbookSettings settings = new SongbookSettings();
        settings.put("BIND_SONG_TITLE", Boolean.TRUE);
        settings.put("LANGUAGE", Locale.ENGLISH);
        return settings;
    }


    public SongbookSettings getSongbookSettings() {
        return songbookSettings;
    }


    public void setSongbookSettings(final SongbookSettings p) {
        if (p == null) {
            return;
        }
        songbookSettings = p;
    }

    public UserSettings getDefaultUserSettings() {
        UserSettings settings = new UserSettings();
        settings.put("AUTO_LOAD_DATA", Boolean.FALSE);
        settings.put("DEFAULT_READ_TOKEN", "SHJhYm/FoWkgTGV0J3MgRnVja2luZyAgR29vb28h");
        settings.put("AUTH_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/.auth").toString());
        return settings;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(final UserSettings u) {
        if (u == null) {
            return;
        }
        userSettings = u;
    }



    @Deprecated
    public static Song addSongDialog(CollectionManager manager) {
        if (StandardCollectionManager.getInstance().equals(manager)) {
            return addStandardSongDialog(manager);
        } else if (EasterCollectionManager.getInstance().equals(manager)) {
            return addEasterSongDialog(manager);
        }
        return null;
    }

    @Deprecated
    public static Song editSongDialog(Song s, CollectionManager manager) {
        if (StandardCollectionManager.getInstance().equals(manager)) {
            return editStandardSongDialog(s, manager);
        } else if (EasterCollectionManager.getInstance().equals(manager)) {
            return editEasterSongDialog(s, manager);
        }
        return null;
    }

    @Deprecated
    private static Song addStandardSongDialog(CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Add");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return songActionDialog(manager.getPlaceholderSong(), manager, ACTION_ADD);
    }

    @Deprecated
    private static Song editStandardSongDialog(Song s, CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Save Changes");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return songActionDialog(s, manager, ACTION_EDIT);
    }

    @Deprecated
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

    }

    @Deprecated
    private static Song addEasterSongDialog(CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Add");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return easterSongActionDialog(manager.getPlaceholderSong(), manager, ACTION_ADD);
    }

    @Deprecated
    public static Song addEasterSongFromTemplateDialog(Song s, CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Add");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        if (s == null) {
            return addEasterSongDialog(manager);
        }
        return easterSongActionDialog(s, manager, ACTION_ADD);
    }

    @Deprecated
    private static Song editEasterSongDialog(Song s, CollectionManager manager) {
        UIManager.put("OptionPane.okButtonText", "Save Changes");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        return easterSongActionDialog(s, manager, ACTION_EDIT);
    }

    @Deprecated
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
                //TODO CollectionEditor.forceRefreshInstance();
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
                //TODO CollectionEditor.forceRefreshInstance();
                return song;
            }
        }
        return null;

    }

    public void load() {
        if ((Boolean) VCSAdmin.getInstance().getSettings().get("REMOTE_SAVE_LOAD_ENABLED")) {
            VCSAdmin.getInstance().pull();
            return;
        }
        logger.info("Importing a local data zip file...");
        if (!new File((String) Environment.getInstance().getSettings().get("DATA_ZIP_FILE_PATH")).exists()) {
            logger.info("Import aborted: file not found!");
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not find a local data zip file. Make sure it is in the same directory as the program.")
            .setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton().build().open();
            return;
        }
        if (!extractLocalDataFile()) {
            logger.info("Import failed!");
            new AlertDialog.Builder().setTitle("Error").setMessage("Extracting data from the zip failed.")
                    .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
            return;
        }
        logger.info("Import successful!");
        new AlertDialog.Builder().setTitle("Success").setMessage("Data loaded successfully.")
                .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
    }

    private boolean extractLocalDataFile() {
        try {
            ZipBuilder.extract((String) Environment.getInstance().getSettings().get("DATA_ZIP_FILE_PATH"), (String) Environment.getInstance().getDefaultSettings().get("DATA_FILE_PATH"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not extract the data. For complete error message view the log file.")
                    .setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton().build().open();
            return false;
        }
        return true;
    }

    public void save() {
        if ((Boolean) VCSAdmin.getInstance().getSettings().get("REMOTE_SAVE_LOAD_ENABLED")) {
            VCSAdmin.getInstance().push();
            return;
        }
        logger.info("Exporting data to local zip file...");
        if (!archiveDataToLocalFile()) {
            logger.info("Export failed!");
            new AlertDialog.Builder().setTitle("Error").setMessage("Error creating data zip file.")
                    .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
            return;
        }
        logger.info("Export successful!");
        new AlertDialog.Builder().setTitle("Success").setMessage("data saved successfully.")
                .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
    }

    private boolean archiveDataToLocalFile() {
        try (ZipBuilder builder = new ZipBuilder()) {
            builder.setOutputPath((String) Environment.getInstance().getSettings().get("DATA_ZIP_FILE_PATH"));
            builder.addFolderContent(new File((String) Environment.getInstance().getSettings().get("DATA_FILE_PATH"), ""));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not archive the data.")
                    .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
            return false;
        }
        return true;
    }

    @Deprecated
    public void createNewSongbook() {
        try {
            File songDataFolder = new File(StandardCollectionManager.getInstance().getSettings().getSongDataFilePath());
            songDataFolder.mkdirs();
            File collectionJSONFile = new File(StandardCollectionManager.getInstance().getSettings().getCollectionFilePath());
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
            Environment.showErrorMessage("Error", "Could not create a new songbook!");
        }
    }

    @Deprecated
    public void loadSongbook() {
        if ((Boolean) VCSAdmin.getInstance().getSettings().get("REMOTE_SAVE_LOAD_ENABLED")) {
            VCSAdmin.getInstance().pull();
            return;
        }
        extractLocalDataFile();
        Environment.getInstance().refresh();
        Environment.getInstance().getCollectionManager().init();
        Environment.showMessage("Success", "Songbook loaded successfully.");
        logger.debug("Songbook loaded");
    }

    public void autoLoad() {
        if ((Boolean) userSettings.get("AUTO_LOAD_DATA")) {
            load();
        }
    }

}
