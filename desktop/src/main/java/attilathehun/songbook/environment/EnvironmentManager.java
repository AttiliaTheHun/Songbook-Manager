package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.plugin.Plugin;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.VCSAdmin;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
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


    public SongbookSettings getDefaultSongbookSettings() {
        SongbookSettings settings = new SongbookSettings();
        settings.put("BIND_SONG_TITLES", Boolean.TRUE);
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
            new AlertDialog.Builder().setTitle("Add a song").setMessage("Do you want to add your first song?").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .setParent(SongbookApplication.getMainWindow())
                    .addOkButton("Add")
                    .addCloseButton("Cancel")
                    .build().awaitResult().thenAccept((result) -> {
                        if (result == AlertDialog.RESULT_OK) {
                            Environment.getInstance().getCollectionManager().addSongDialog();
                        }
                    });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not create a new songbook!").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .addOkButton().build().open();
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
        new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                .setMessage("Songbook loaded successfully.")
                .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        logger.debug("Songbook loaded");
    }

    public void autoLoad() {
        if ((Boolean) userSettings.get("AUTO_LOAD_DATA")) {
            load();
        }
    }

}
