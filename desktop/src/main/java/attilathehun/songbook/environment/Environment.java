package attilathehun.songbook.environment;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.vcs.Client;
import attilathehun.songbook.collection.CollectionManager;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import attilathehun.songbook.vcs.VCSAdmin;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static javax.swing.JOptionPane.showMessageDialog;

public final class Environment {

    private static final Logger logger = LogManager.getLogger(Environment.class);

    public static boolean FLAG_IGNORE_SEGMENTS = false;

    public final Settings settings = Settings.getSettings();

    private static final Environment instance = new Environment();

    private CollectionManager collectionManager;

    private String tokenInMemory = null;

    private long songbookVersionTimestamp = -1;

    private Environment() {
        refresh();
        logger.info("Environment instantiated");
    }

    public long getSongbookVersionTimestamp() {
        return songbookVersionTimestamp;
    }

    public void setSongbookVersionTimestamp(long songbookVersionTimestamp) {
        this.songbookVersionTimestamp = songbookVersionTimestamp;
    }

    public static Environment getInstance() {
        return instance;
    }


    public String acquireToken(VCSAdmin.Certificate certificate) {
        if (tokenInMemory != null) {
            return tokenInMemory;
        }

        if (fileExists(settings.user.getAuthFilePath(new Certificate()))) {
            try {
                return String.join("", Files.readAllLines(Path.of(settings.user.getAuthFilePath(new Certificate()))));
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
                showErrorMessage("Error", "Error reading auth file, continuing with default token.");
            }

        }

        return settings.user.getDefaultReadToken(new Certificate());
    }


    public static void showErrorMessage(String title, String message) {
        showErrorMessage(title, message, false);
    }

    public static void showErrorMessage(String title, String message, boolean fatal) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.ERROR_MESSAGE);
        if (fatal) {
            getInstance().exit();
        }
    }

    public static void showMessage(String title, String message) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarningMessage(String title, String message) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.WARNING_MESSAGE);
    }

    public void refresh() {
        try {
            for (File f : new File(settings.environment.TEMP_FILE_PATH).listFiles()) {
                if (f.getName().equals("session_timestamp.txt") || (FLAG_IGNORE_SEGMENTS && f.getName().startsWith("segment"))) {
                    continue;
                }
                if (!f.delete()) {
                    showErrorMessage("Refreshing error", "Can not clean the temp folder!", true);
                }
            }
        } catch (NullPointerException npe) {
            logger.error(npe.getMessage(), npe);
        }

    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    /**
     * Perform action(s) depending on the arguments. Handles command line arguments, but can be used on runtime as well.
     * Unrecognized commands are ignored
     *
     * @param args series of commands
     * @return false when any error occurs
     */
    //TODO
    public Result[] perform(String[] args) {
        Result[] output = new Result[args.length];
        boolean performSave = false, performLoad = false, targetRemote = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-song1":
                    break;
                case "-song2":
                    break;
                case "--remote":
                    targetRemote = true;
                    output[i] = Result.SUCCESS;
                    break;
                case "save":
                    performSave = true;
                    output[i] = Result.SUCCESS;
                    break;
                case "load":
                    performLoad = true;
                    output[i] = Result.SUCCESS;
                    break;
                case "-token":
                    break;
                default:
                    output[i] = Result.IGNORED;
            }
        }

        return output;
    }

    public enum Result {
        SUCCESS,
        FAILURE,
        IGNORED
    }

    /**
     * Returns the default Collection Manager.
     * @return default Collection Manager (Standard Collection Manager if null)
     */
    public CollectionManager getCollectionManager() {
        if (collectionManager == null) {
            return StandardCollectionManager.getInstance();
        }
        return collectionManager;
    }

    public void setCollectionManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void loadTokenToMemory(String token, EnvironmentManager.Certificate certificate) {
        tokenInMemory = token;
    }

    public void exit() {
        Settings.save(settings);
        System.exit(0);
    }


    public static JDialog getAlwaysOnTopJDialog() {
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        dialog.toFront();
        dialog.requestFocusInWindow();
        return dialog;
    }

    public static void navigateWebViewToSong(Song s) {
        int index = getInstance().getCollectionManager().getFormalCollectionSongIndex(s);
        if (index % 2 == 0) {
            SongbookApplication.dialImaginarySongOneKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index));
            if (index + 1 >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
                SongbookApplication.dialImaginarySongTwoKeyPressed(CollectionManager.getShadowSong());
            } else {
                SongbookApplication.dialImaginarySongTwoKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index + 1));
            }
        } else {
            SongbookApplication.dialImaginarySongTwoKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index));
            if (index - 1 < 0) {
                SongbookApplication.dialImaginarySongOneKeyPressed(CollectionManager.getShadowSong());
            } else {
                SongbookApplication.dialImaginarySongOneKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index - 1));
            }
        }
    }

    public static class EnvironmentSettings implements Serializable {
        private static final Logger logger = LogManager.getLogger(attilathehun.songbook.environment.Environment.EnvironmentSettings.class);

        public static final String SETTINGS_FILE_PATH = "settings.json";
        public static final String EASTER_EXE_FILE_PATH = "easter.exe";
        public static final long SESSION_TIMESTAMP = System.currentTimeMillis();
        public final transient boolean IS_IT_EASTER_ALREADY = new File(EASTER_EXE_FILE_PATH).exists() && new File(EASTER_EXE_FILE_PATH).length() == 0;
        public final String COLLECTION_FILE_PATH;
        public final String EASTER_COLLECTION_FILE_PATH;
        public final String SONG_DATA_FILE_PATH;
        public final String EGG_DATA_FILE_PATH;
        public final String RESOURCE_FILE_PATH;
        public final String CSS_RESOURCES_FILE_PATH;
        public final String TEMPLATE_RESOURCES_FILE_PATH;
        public final String DATA_ZIP_FILE_PATH;
        public final String EDIT_LOG_FILE_PATH;
        public final String TEMP_FILE_PATH;
        public final String ASSETS_RESOURCES_FILE_PATH;
        public final String OUTPUT_FILE_PATH;
        public final String DATA_FILE_PATH;
        public final String TEMP_TIMESTAMP_FILE_PATH;
        public final String LOG_FILE_PATH;
        public final String SCRIPTS_FILE_PATH;


        public EnvironmentSettings() {
            DATA_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/data/").toString();
            COLLECTION_FILE_PATH = Paths.get(DATA_FILE_PATH + "/collection.json").toString();
            EASTER_COLLECTION_FILE_PATH = Paths.get(DATA_FILE_PATH + "/easter_collection.json").toString();
            SONG_DATA_FILE_PATH = Paths.get(DATA_FILE_PATH + "/songs/html/").toString();
            EGG_DATA_FILE_PATH = Paths.get(DATA_FILE_PATH + "/songs/egg/").toString();
            RESOURCE_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/resources/").toString();
            CSS_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/css/").toString();
            TEMPLATE_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/templates/").toString();
            DATA_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/data.zip").toString();
            EDIT_LOG_FILE_PATH = Paths.get(DATA_FILE_PATH + "/last_modified_by.txt").toString();
            TEMP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/temp/").toString();
            TEMP_TIMESTAMP_FILE_PATH = Paths.get(TEMP_FILE_PATH + "/session_timestamp.txt").toString();
            ASSETS_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/assets/").toString();
            OUTPUT_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/pdf/").toString();
            LOG_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/log.txt").toString();
            SCRIPTS_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/scripts/").toString();
        }


    }

    public static final class Certificate {
        private Certificate() {

        }
    }

}
