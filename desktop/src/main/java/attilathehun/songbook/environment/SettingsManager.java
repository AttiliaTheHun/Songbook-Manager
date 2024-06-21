package attilathehun.songbook.environment;

import attilathehun.songbook.util.Misc;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public final class SettingsManager {
    private static final Logger logger = LogManager.getLogger(SettingsManager.class);
    private static final SettingsManager INSTANCE = new SettingsManager();
    private HashMap<String, Setting<?>> settings = new HashMap<>();

    private SettingsManager() {

    }

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the manager by loading the local settings to memory. It is necessary to call this method before using the manager at all.
     */
    public static void init() {
        getInstance().load();
        if (!Misc.fileExists(Environment.SETTINGS_FILE_PATH)) {
            getInstance().save();
        }
    }

    /**
     * Returns the complete representation of the setting. It is not recommended to modify settings directly through its methods from {@link Setting}, because the manager can not
     * perform a save operation after such modifications are made. use {@link #set(String, Object)} instead.
     *
     * @param name target setting name
     * @return the setting object or null
     */
    public Setting<?> get(final String name) {
        return settings.get(name);
    }

    /**
     * Returns the value of a setting. If the name of the setting is incorrect (nonexistent) this method will throw an exception.
     *
     * @param setting target setting name
     * @return value of the setting
     * @param <T> type of the setting value, usually String, Boolean or Integer
     * @throws NullPointerException if the setting does not exist
     */
    public <T> T getValue(final String setting) {
        return (T) settings.get(setting).getValue();
    }

    /**
     * Changes the value of the setting to the value provided. If the new value is of incorrect type, this method will throw an exception. Successful changing of a setting
     * will make the manager save the current state of the settings.
     *
     * @param setting target setting name
     * @param value new value for the setting
     * @param <T> type of the setting value, usually String, Boolean or Integer
     * @throws ClassCastException if the new value does not match the setting value type
     */
    public <T> void set(final String setting, final T value) {
        if (settings.get(setting) == null) {
            return;
        }
        settings.get(setting).set(value);
        logger.debug(setting + " set to " + value);
        save();
    }

    /**
     * Locally saves the current state of the settings.
     */
    public void save() {
        Misc.saveObjectToFileInJSON(new ArrayList<>(settings.values()), new File (Environment.SETTINGS_FILE_PATH));
    }

    /**
     * Loads the settings from a local file if such a file exists or loads default settings if it does not.
     */
    public void load() {
        final ArrayList<Setting<?>> values = loadSavedValues();
        final HashMap<String, Setting<?>> settings = getDefaultSettings();
        if (values != null) {
            for (final Setting<?> s : values) {
                settings.get(s.getName()).set(s.getValue());
            }
            logger.debug("loaded local settings");
        }
        this.settings = settings;

    }

    /**
     * Loads locally saved settings to an {@link ArrayList}. Returns null if there is no save file.
     *
     * @return list of settings or null
     */
    private ArrayList<Setting<?>> loadSavedValues() {

        try {
            final Path path = Paths.get(Environment.SETTINGS_FILE_PATH);

            final String json = String.join("\n", Files.readAllLines(path));

            final Type type = new TypeToken<ArrayList<Setting<?>>>(){}.getType();

            final ArrayList<Setting<?>> values = new Gson().fromJson(json, type);
            return values;
        } catch (final NoSuchFileException nsf) {

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Creates a setting {@link java.util.HashMap} with the default values.
     *
     * @return hashmap of default settings
     */
    private static HashMap<String, Setting<?>> getDefaultSettings() {
        final HashMap<String, Setting<?>> map = new HashMap<>();
        // environment settings
        map.put("DATA_FILE_PATH", new Setting<String>("DATA_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/data/").toString(), Paths.get(System.getProperty("user.dir") + "/data/").toString(), Setting.TYPE_URL, "Location of the data folder", "Insert a full file path to the folder"));
        map.put("SONGS_FILE_PATH", new Setting<String>("SONGS_FILE_PATH", Paths.get(map.get("DATA_FILE_PATH").getValue() + "/songs/").toString(), Paths.get(map.get("DATA_FILE_PATH").getValue() + "/songs/").toString(), Setting.TYPE_URL, "Location of the songs files", "Insert a full file path to the folder"));
        map.put("RESOURCES_FILE_PATH", new Setting<String>("RESOURCES_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/resources/").toString(), Paths.get(System.getProperty("user.dir") + "/resources/").toString(), Setting.TYPE_URL, "Location of the songbook resources", "Insert a full file path to the folder"));
        map.put("CSS_RESOURCES_FILE_PATH", new Setting<String>("CSS_RESOURCES_FILE_PATH", Paths.get(map.get("RESOURCES_FILE_PATH").getValue() + "/css/").toString(), Paths.get(map.get("RESOURCES_FILE_PATH").getValue() + "/css/").toString(), Setting.TYPE_URL, "Location of the songbook stylesheets", "Insert a full file path to the folder"));
        map.put("TEMPLATE_RESOURCES_FILE_PATH", new Setting<String>("TEMPLATE_RESOURCES_FILE_PATH", Paths.get(map.get("RESOURCES_FILE_PATH").getValue() + "/templates/").toString(), Paths.get(map.get("RESOURCES_FILE_PATH").getValue() + "/templates/").toString(), Setting.TYPE_URL, "Location of the songbook resource templates", "Insert a full file path to the folder"));
        map.put("ASSET_RESOURCES_FILE_PATH", new Setting<String>("ASSET_RESOURCES_FILE_PATH", Paths.get(map.get("RESOURCES_FILE_PATH").getValue() + "/assets/").toString(), Paths.get(map.get("RESOURCES_FILE_PATH").getValue() + "/assets/").toString(), Setting.TYPE_URL, "Location of the songbook resource assets", "Insert a full file path to the folder"));
        map.put("DATA_ZIP_FILE_PATH", new Setting<String>("DATA_ZIP_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/data.zip").toString(), Paths.get(System.getProperty("user.dir") + "/data.zip").toString(), Setting.TYPE_URL, "Location of the zip archive for local import/export", "Insert a full file path to the file"));
        map.put("TEMP_FILE_PATH", new Setting<String>("TEMP_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/temp/").toString(), Paths.get(System.getProperty("user.dir") + "/temp/").toString(), Setting.TYPE_URL, "Location of the working files", "Insert a full file path to the folder"));
        map.put("EXPORT_FILE_PATH", new Setting<String>("EXPORT_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/pdf/").toString(), Paths.get(System.getProperty("user.dir") + "/pdf/").toString(), Setting.TYPE_URL, "Location of the export output files", "Insert a full file path to the folder"));
        map.put("LOG_FILE_PATH", new Setting<String>("LOG_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/log.txt").toString(), Paths.get(System.getProperty("user.dir") + "/log.txt").toString(), Setting.TYPE_URL, "Location of the application log file", "Insert a full file path to the file"));
        map.put("SCRIPTS_FILE_PATH", new Setting<String>("SCRIPTS_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/scripts/").toString(), Paths.get(System.getProperty("user.dir") + "/scripts/").toString(), Setting.TYPE_URL, "Location of the executable scripts", "Insert a full file path to the folder"));
        // VCS settings
        map.put("REMOTE_SAVE_LOAD_ENABLED", new Setting<Boolean>("REMOTE_SAVE_LOAD_ENABLED", Boolean.FALSE, Boolean.FALSE, Setting.TYPE_URL, "Whether the client should upload/download the data to/from the server", "true or false"));
        map.put("REMOTE_DATA_DOWNLOAD_URL", new Setting<String>("REMOTE_DATA_DOWNLOAD_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/download/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote download endpoint", "Insert a full URL address"));
        map.put("REMOTE_DATA_UPLOAD_URL", new Setting<String>("REMOTE_DATA_UPLOAD_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/upload/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote upload endpoint", "Insert a full URL address"));
        map.put("REMOTE_DATA_INDEX_URL", new Setting<String>("REMOTE_DATA_INDEX_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/index/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote index download endpoint", "Insert a full URL address"));
        map.put("REMOTE_DATA_VERSION_TIMESTAMP_URL", new Setting<String>("REMOTE_DATA_VERSION_TIMESTAMP_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/version-timestamp/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote version timestamp download endpoint", "Insert a full URL address"));
        map.put("VCS_CACHE_PATH", new Setting<String>("VCS_CACHE_PATH", Paths.get(System.getProperty("user.dir"), "/vcs/").toString(), Paths.get(System.getProperty("user.dir"), "/vcs/").toString(), Setting.TYPE_URL, "Location of the VCS cache files", "Insert a full file path to the folder"));
        map.put("VCS_THREAD_COUNT", new Setting<Integer>("VCS_THREAD_COUNT", 5, 5, Setting.TYPE_POSITIVE_INTEGER, "How many threads can the VCS use to process the data", "a positive integer"));
        // songbook settings
        map.put("BIND_SONG_TITLES", new Setting<Boolean>("BIND_SONG_TITLES", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Should the client keep the song name in the collection the same as in the file", "true or false"));
        map.put("SONGBOOK_LANGUAGE", new Setting<String>("SONGBOOK_LANGUAGE", Locale.ENGLISH.toString(), Locale.ENGLISH.toString(), Setting.TYPE_NON_EMPTY_STRING, "What language are the songs in", "en, fr"));
        map.put("ENABLE_FRONTPAGE", new Setting<Boolean>("ENABLE_FRONTPAGE", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Should the songbook have a special page on the beginning", "true or false"));
        map.put("ENABLE_DYNAMIC_SONGLIST", new Setting<Boolean>("ENABLE_DYNAMIC_SONGLIST", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Should the songbook have the list of songs on the beginning", "true or false"));
        map.put("DYNAMIC_SONGLIST_SONGS_PER_COLUMN", new Setting<Integer>("DYNAMIC_SONGLIST_SONGS_PER_COLUMN", 38, 38, Setting.TYPE_POSITIVE_INTEGER, "How many songs will be displayed in a single column of the song list", "38"));
        // user settings
        map.put("AUTO_LOAD_DATA", new Setting<Boolean>("AUTO_LOAD_DATA", Boolean.FALSE, Boolean.FALSE, Setting.TYPE_BOOLEAN, "Should the automatically load remote data on start up", "true or false"));
        map.put("DEFAULT_READ_TOKEN", new Setting<String>("DEFAULT_READ_TOKEN", "SHJhYm/FoWkgTGV0J3MgRnVja2luZyAgR29vb28h", "SHJhYm/FoWkgTGV0J3MgRnVja2luZyAgR29vb28h", Setting.TYPE_STRING, "This setting will be removed", "Deprecated, use for testing only"));
        map.put("AUTH_FILE_PATH", new Setting<String>("AUTH_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/.auth").toString(), Paths.get(System.getProperty("user.dir") + "/.auth").toString(), Setting.TYPE_URL_ALLOW_EMPTY, "Location of the file with remote authentication tokens", "Insert a full file path to the file"));
        // export settings
        map.put("EXPORT_ENABLED", new Setting<Boolean>("EXPORT_ENABLED", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Whether exporting of the songbook is desirable", "true or false"));
        map.put("EXPORT_BROWSER_EXECUTABLE_PATH", new Setting<String>("EXPORT_BROWSER_EXECUTABLE_PATH", "", "", Setting.TYPE_URL_ALLOW_EMPTY, "Path to the headless browser executable file", "true or false"));
        map.put("EXPORT_KEEP_BROWSER_INSTANCE", new Setting<Boolean>("EXPORT_KEEP_BROWSER_INSTANCE", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Speeds up export and preview but uses more memory", "true or false"));
        map.put("EXPORT_DEFAULT_FILE_NAME", new Setting<String>("EXPORT_DEFAULT_FILE_NAME", "ExportDefault.pdf", "ExportDefault.pdf", Setting.TYPE_NON_EMPTY_STRING, "Name of the default export file", "Insert a file name"));
        map.put("EXPORT_PRINTABLE_FILE_NAME", new Setting<String>("EXPORT_PRINTABLE_FILE_NAME", "ExportPrintable.pdf", "ExportPrintable.pdf", Setting.TYPE_NON_EMPTY_STRING, "Name of the print-format export file", "Insert a file name"));
        map.put("EXPORT_SINGLEPAGE_FILE_NAME", new Setting<String>("EXPORT_SINGLEPAGE_FILE_NAME", "ExportSinglepage.pdf", "ExportSinglepage.pdf", Setting.TYPE_NON_EMPTY_STRING, "Name of the singlepage-format export file", "Insert a file name"));
        return map;
    }
}
