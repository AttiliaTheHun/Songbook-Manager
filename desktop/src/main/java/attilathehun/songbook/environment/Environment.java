package attilathehun.songbook.environment;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.util.Client;
import attilathehun.songbook.collection.CollectionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import static javax.swing.JOptionPane.showMessageDialog;

public final class Environment {

    public static class Settings implements Serializable {

        public static final String SETTINGS_FILE_PATH = "settings.json";
        public static final String EASTER_EXE_FILE_PATH = "easter.exe";
        public static final long SESSION_TIMESTAMP = System.currentTimeMillis();
        public final String COLLECTION_FILE_PATH;
        public final String EASTER_COLLECTION_FILE_PATH;
        public final String SONG_DATA_FILE_PATH;
        public final String EGG_DATA_FILE_PATH;
        public final String RESOURCE_FILE_PATH;
        public final String CSS_RESOURCES_FILE_PATH;
        public final String TEMPLATE_RESOURCES_FILE_PATH;
        public final String DATA_ZIP_FILE_PATH;
        public final String EDIT_LOG_FILE_PATH;
        public final boolean AUTO_LOAD_DATA;
        public final boolean REMOTE_SAVE_LOAD_ENABLED;
        public final String TEMP_FILE_PATH;
        public final String ASSETS_RESOURCES_FILE_PATH;
        public final String OUTPUT_FILE_PATH;
        public final transient boolean IS_IT_EASTER_ALREADY = new File(EASTER_EXE_FILE_PATH).exists() && new File(EASTER_EXE_FILE_PATH).length() == 0;
        ;
        public final String DATA_FILE_PATH;
        public final String TEMP_TIMESTAMP_FILE_PATH;
        public final String REMOTE_DATA_ZIP_FILE_DOWNLOAD_URL;
        public final String REMOTE_DATA_ZIP_FILE_UPLOAD_URL;
        public final String REMOTE_DATA_FILE_HASH_URL;
        public final String REMOTE_DATA_FILE_LAST_EDITED_URL;
        private final String DEFAULT_READ_TOKEN;
        private String AUTH_FILE_PATH;
        public final String LOG_FILE_PATH;

        public final Environment.Settings.AuthType AUTH_TYPE;

        public final boolean LOG_ENABLED;
        public final String SCRIPTS_FILE_PATH;
        public final boolean DISABLE_FRONTPAGE;
        public final boolean DISABLE_DYNAMIC_SONGLIST;
        public final boolean BIND_SONG_TITLES;


        private Settings() {
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
            AUTO_LOAD_DATA = false;
            REMOTE_SAVE_LOAD_ENABLED = false;
            TEMP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/temp/").toString();
            TEMP_TIMESTAMP_FILE_PATH = Paths.get(TEMP_FILE_PATH + "/session_timestamp.txt").toString();
            ASSETS_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/assets/").toString();
            OUTPUT_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/pdf/").toString();
            REMOTE_DATA_ZIP_FILE_DOWNLOAD_URL = "http://hrabozpevnik.clanweb.eu/api/data/download/";
            REMOTE_DATA_ZIP_FILE_UPLOAD_URL = "http://hrabozpevnik.clanweb.eu/api/data/upload/";
            REMOTE_DATA_FILE_HASH_URL = "http://hrabozpevnik.clanweb.eu/api/data/hash/";
            REMOTE_DATA_FILE_LAST_EDITED_URL = "http://hrabozpevnik.clanweb.eu/api/data/modify-date/";
            DEFAULT_READ_TOKEN = "SHJhYm/FoWkgTGV0J3MgRnVja2luZyAgR29vb28h";
            AUTH_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/.auth").toString();
            LOG_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/log.txt").toString();
            AUTH_TYPE = Environment.Settings.AuthType.TOKEN;
            LOG_ENABLED = true;
            SCRIPTS_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/scripts/").toString();
            DISABLE_FRONTPAGE = false;
            DISABLE_DYNAMIC_SONGLIST = false;
            BIND_SONG_TITLES = true;
        }

        static Environment.Settings getSettings() {
            try {
                Path path = Paths.get(Environment.Settings.SETTINGS_FILE_PATH);

                String json = String.join("\n", Files.readAllLines(path));

                Type targetClassType = new TypeToken<Environment.Settings>() {
                }.getType();
                Environment.Settings settings = new Gson().fromJson(json, targetClassType);
                return settings;
            } catch (NoSuchFileException nsf) {

            } catch (Exception e) {
                e.printStackTrace();

            }
            Environment.Settings settings = new Environment.Settings();
            if (!Environment.fileExists(SETTINGS_FILE_PATH)) {
                save(settings);
            }

            return settings;
        }

        public static boolean save(Settings settings) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileWriter writer = new FileWriter(SETTINGS_FILE_PATH);
                gson.toJson(settings, writer);
                writer.close();
            } catch (IOException e) {
                showWarningMessage("Warning", "Can not export the settings!");
                return false;
            }
            return true;
        }

        public enum AuthType implements Serializable {
            TOKEN {
                @Override
                public String toString() {
                    return "token";
                }
            },
            PHRASE {
                @Override
                public String toString() {
                    return "phrase";
                }
            }
        }

    }

    public final Environment.Settings settings = Environment.Settings.getSettings();

    private static final Environment instance = new Environment();

    private PrintStream logOutputStream = null;

    private CollectionManager collectionManager;

    private String tokenInMemory = null;

    private Environment() {
        if (settings.LOG_ENABLED) {
            logOutputStream = openFileOutputStream(settings.LOG_FILE_PATH);
        }
        refresh();
    }

    public static Environment getInstance() {
        return instance;
    }

    private PrintStream openFileOutputStream(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            return new PrintStream(new FileOutputStream(file), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public PrintStream getLogPrintStream() {
        return logOutputStream;
    }

    private void writeLogStream(String message) {
        if (logOutputStream == null) {
            return;
        }
        try {
            logOutputStream.write(message.getBytes(StandardCharsets.UTF_8));
            logOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void logTimestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        writeLogStream("[" + dtf.format(now) + "] ");
    }

    public void log(String message) {
        log(message, true);
    }

    public void log(String message, boolean timestamp) {
        if (timestamp) {
            logTimestamp();
        }
        writeLogStream(message + "\n");
    }

    public String acquireToken(Client.Certificate certificate) {
        if (tokenInMemory != null) {
            return tokenInMemory;
        }

        if (fileExists(settings.AUTH_FILE_PATH)) {
            try {
                return String.join("", Files.readAllLines(Path.of(settings.AUTH_FILE_PATH)));
            } catch (IOException e) {
                e.printStackTrace();
                e.printStackTrace(getLogPrintStream());
                showWarningMessage("Warning", "Error reading auth file, continuing with default token.");
            }

        }

        return settings.DEFAULT_READ_TOKEN;
    }


    public static void showErrorMessage(String title, String message) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.ERROR_MESSAGE);
        getInstance().exit();
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
            for (File f : new File(settings.TEMP_FILE_PATH).listFiles()) {
                if (f.getName().equals("session_timestamp.txt")) {
                    continue;
                }
                if (!f.delete()) {
                    showErrorMessage("Refreshing error", "Can not clean the temp folder!");
                }
            }
        } catch (NullPointerException npe) {
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
        if (logOutputStream != null) {
            logOutputStream.close();
        }
        System.exit(0);
    }

    public int getFormalCollectionSongIndex(Song s) {
        if (s.id() < 0) {
            throw new IllegalArgumentException();
        }
        ArrayList<Song> formalCollection = collectionManager.getFormalCollection();
        for (int i = 0; i < formalCollection.size(); i++) {
            if (formalCollection.get(i).equals(s)) {
                return i;
            }
        }
        return -1;
    }

    public int getFormalCollectionSongIndex(int id) {
        if (id < 0) {
            throw new IllegalArgumentException();
        }
        ArrayList<Song> formalCollection = collectionManager.getFormalCollection();
        for (int i = 0; i < formalCollection.size(); i++) {
            if (formalCollection.get(i).id() == id) {
                return i;
            }
        }
        return -1;
    }

    public static JDialog getAlwaysOnTopJDialog() {
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        dialog.toFront();
        dialog.requestFocusInWindow();
        return dialog;
    }

}
