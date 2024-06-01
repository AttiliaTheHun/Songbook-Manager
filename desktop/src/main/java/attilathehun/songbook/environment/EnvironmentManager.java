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
import javafx.application.Platform;
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

public final class EnvironmentManager {
    private static final Logger logger = LogManager.getLogger(EnvironmentManager.class);
    private static final EnvironmentManager INSTANCE = new EnvironmentManager();
    private EnvironmentManager() {}
    public static EnvironmentManager getInstance() {
        return INSTANCE;
    }


    /**
     * The platform API method for performing data loading. Based on the REMOTE_SAVE_LOAD_ENABLED setting either tries to contact the VCS server via
     * {@link VCSAdmin} or tries to load a local zip archive file. This method provides visual feedback through {@link AlertDialog}s and so is not
     * a good pick for background work.
     */
    public void load() {
        if ((Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
            VCSAdmin.getInstance().pull();
            return;
        }
        logger.info("Importing a local data zip file...");
        if (!new File((String) SettingsManager.getInstance().getValue("DATA_ZIP_FILE_PATH")).exists()) {
            logger.info("Import aborted: file not found!");
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not find a local data zip file. Make sure it is in the same directory as the program.")
            .setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton().build().open();
            return;
        }
        if (!extractLocalDataFile()) {
            logger.info("Import failed!");
            return;
        }
        Environment.getInstance().hardRefresh();
        logger.info("Import successful!");
        new AlertDialog.Builder().setTitle("Success").setMessage("Data loaded successfully.")
                .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
    }

    /**
     * This method extracts local zip archive file to import songbook data and returns the result of the operation. Upon failure, it presents the user with
     * visual feedback through an {@link AlertDialog}.
     *
     * @return whether the file was correctly extracted
     */
    private boolean extractLocalDataFile() {
        try {
            ZipBuilder.extract(SettingsManager.getInstance().getValue("DATA_ZIP_FILE_PATH"), SettingsManager.getInstance().getValue("DATA_FILE_PATH"));
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not extract the data. For complete error message view the log file.")
                    .setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton().build().open();
            return false;
        }
        return true;
    }

    /**
     * The platform API method for performing data saving. Based on the REMOTE_SAVE_LOAD_ENABLED setting either tries to contact the VCS server via
     * {@link VCSAdmin} or attempts to save the data into a local zip archive file. This method provides visual feedback through {@link AlertDialog}s and so is not
     * a good pick for background work.
     */
    public void save() {
        if ((Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
            VCSAdmin.getInstance().push();
            return;
        }
        logger.info("Exporting data to local zip file...");
        if (!archiveDataToLocalFile()) {
            logger.info("Export failed!");
            return;
        }
        logger.info("Export successful!");
        new AlertDialog.Builder().setTitle("Success").setMessage("Data saved successfully to " + SettingsManager.getInstance().getValue("DATA_ZIP_FILE_PATH"))
                .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
    }

    /**
     * This method saves the songbook data to a local zip archive file and returns the result of the operation. Upon failure, it presents the user with
     * visual feedback through an {@link AlertDialog}.
     *
     * @return whether the file was correctly compiled
     */
    private boolean archiveDataToLocalFile() {
        try (final ZipBuilder builder = new ZipBuilder()) {
            builder.setOutputPath(SettingsManager.getInstance().getValue("DATA_ZIP_FILE_PATH"));
            builder.addFolderContent(new File((String) SettingsManager.getInstance().getValue("DATA_FILE_PATH"), ""));
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Error creating data zip file.")
                    .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
            return false;
        }
        return true;
    }

    /**
     * Initializes the songbook by creating the necessary files and folder structure for the {@link StandardCollectionManager}. Upon failure, it presents the user with
     * a visual feedback through an {@link AlertDialog} and closes the application.
     */
    public void createNewSongbook() {
        try {
            final File songDataFolder = new File(StandardCollectionManager.getInstance().getSongDataFilePath());
            songDataFolder.mkdirs();
            final File collectionJSONFile = new File(StandardCollectionManager.getInstance().getCollectionFilePath());
            collectionJSONFile.createNewFile();
            final PrintWriter printWriter = new PrintWriter(new FileWriter(collectionJSONFile));
            printWriter.write("[]");
            printWriter.close();
            new AlertDialog.Builder().setTitle("Add a song").setMessage("Do you want to add your first song?").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .setParent(SongbookApplication.getMainWindow())
                    .addOkButton("Add")
                    .addCloseButton("Cancel")
                    .build().awaitResult().thenAccept((result) -> {
                        if (result == AlertDialog.RESULT_OK) {
                            Environment.getInstance().getCollectionManager().addSongDialog();
                        }
                    });
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not create a new songbook!").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .addOkButton().build().open();
            Platform.exit();
        }
    }

    /**
     * Loads a songbook either from remote or local source, based on settings. This method is designed to be used on the program startup and follows with
     * some additional initialization. The method opens {@link AlertDialog}s to notify the user. Upon failure, it closes the program. For regular data loading use {@link #load()}.
     */
    public void loadSongbook() {
        if ((Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
            VCSAdmin.getInstance().pull();
            return;
        }
        if (!extractLocalDataFile()) {
            logger.debug("Songbook loading failed");
            Platform.exit();
            return;
        }
        Environment.getInstance().hardRefresh();
        new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                .setMessage("Songbook loaded successfully.")
                .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        logger.debug("Songbook loaded");
    }

    public void autoLoad() {
        if ((Boolean) SettingsManager.getInstance().getValue("AUTO_LOAD_DATA")) {
            load();
        }
    }

}
