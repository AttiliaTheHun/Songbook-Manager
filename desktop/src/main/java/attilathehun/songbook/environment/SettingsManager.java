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
import java.util.List;
import java.util.Locale;

public final class SettingsManager {
    private static final Logger logger = LogManager.getLogger(SettingsManager.class);
    private static final List<SettingsListener> listeners = new ArrayList<SettingsListener>();
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
     * will make the manager save the current state of the settings. Emits the {@link SettingsListener#onSettingChanged(String, Setting, Setting)} event.
     *
     * @param setting target setting name
     * @param value new value for the setting
     * @param <T> type of the setting value, usually String, Boolean or Integer
     * @throws ClassCastException if the new value does not match the setting value type
     */
    public <T> void set(final String setting, final T value) {
        final Setting<?> current = settings.get(setting);
        setSilent(setting, value);
        notifySettingChanged(setting, current, settings.get(setting));
    }

    /**
     * <p>Changes the value of the setting to the value provided without triggering any events. If the new value is of incorrect type, this method will throw an exception. Successful changing of a setting
     * will make the manager save the current state of the settings.</p>
     *
     * <p>This method is meant to be used for automatic procedures and configuration to avoid producing unnecessary events. For regular runtime and most application components the
     * {@link #set(String, Object)} method should be used.</p>
     *
     * @param setting target setting name
     * @param value new value for the setting
     * @param <T> type of the setting value, usually String, Boolean or Integer
     * @throws ClassCastException if the new value does not match the setting value type
     */
    public <T> void setSilent(final String setting, final T value) {
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

            return new Gson().fromJson(json, type);
        } catch (final NoSuchFileException nsf) {
            // this is not really an exception from the program's point of view
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
        map.put("REMOTE_DATA_URL", new Setting<String>("REMOTE_DATA_URL", "http://localhost/api/data/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote download endpoint", "Insert a full URL address"));
        map.put("REMOTE_TOKENS_URL", new Setting<String>("REMOTE_TOKENS_URL", "http://localhost/api/tokens/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote tokens endpoint", "Insert a full URL address"));
        map.put("REMOTE_BACKUPS_URL", new Setting<String>("REMOTE_BACKUPS_URL", "http://localhost/api/backups/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote backups endpoint", "Insert a full URL address"));
        map.put("REMOTE_INDEX_URL", new Setting<String>("REMOTE_INDEX_URL", "http://localhost/api/index/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote index download endpoint", "Insert a full URL address"));
        map.put("REMOTE_VERSION_TIMESTAMP_URL", new Setting<String>("REMOTE_VERSION_TIMESTAMP_URL", "http://localhost/api/version-timestamp/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote version timestamp download endpoint", "Insert a full URL address"));
        map.put("REMOTE_ACTION_LOG_URL", new Setting<String>("REMOTE_ACTION_LOG_URL", "http://localhost/api/action-log/", "", Setting.TYPE_URL_ALLOW_EMPTY, "Address of the remote version timestamp download endpoint", "Insert a full URL address"));
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
        map.put("AUTH_FILE_PATH", new Setting<String>("AUTH_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/.auth").toString(), Paths.get(System.getProperty("user.dir") + "/.auth").toString(), Setting.TYPE_URL_ALLOW_EMPTY, "Location of the file with remote authentication tokens", "Insert a full file path to the file"));
        map.put("USE_DEFAULT_ENCRYPTION_PASSWORD", new Setting<Boolean>("USE_DEFAULT_ENCRYPTION_PASSWORD", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Do you want to use default encryption password", ""));
        // export settings
        map.put("EXPORT_ENABLED", new Setting<Boolean>("EXPORT_ENABLED", Boolean.TRUE, Boolean.TRUE, Setting.TYPE_BOOLEAN, "Whether exporting of the songbook is desirable", "true or false"));
        map.put("EXPORT_BROWSER_EXECUTABLE_PATH", new Setting<String>("EXPORT_BROWSER_EXECUTABLE_PATH", "", "", Setting.TYPE_URL_ALLOW_EMPTY, "Path to the headless browser executable file", "true or false"));
        map.put("EXPORT_KEEP_BROWSER_INSTANCE", new Setting<Boolean>("EXPORT_KEEP_BROWSER_INSTANCE", Boolean.FALSE, Boolean.FALSE, Setting.TYPE_BOOLEAN, "Speeds up export and preview but uses more memory", "true or false"));
        map.put("EXPORT_DEFAULT_FILE_NAME", new Setting<String>("EXPORT_DEFAULT_FILE_NAME", "Default.pdf", "Default.pdf", Setting.TYPE_NON_EMPTY_STRING, "Name of the default export file", "Insert a file name"));
        map.put("EXPORT_PRINTABLE_FILE_NAME", new Setting<String>("EXPORT_PRINTABLE_FILE_NAME", "Print.pdf", "Print.pdf", Setting.TYPE_NON_EMPTY_STRING, "Name of the print-format export file", "Insert a file name"));
        map.put("EXPORT_SINGLEPAGE_FILE_NAME", new Setting<String>("EXPORT_SINGLEPAGE_FILE_NAME", "Singlepage.pdf", "Singlepage.pdf", Setting.TYPE_NON_EMPTY_STRING, "Name of the singlepage-format export file", "Insert a file name"));
        return map;
    }

    /**
     * Registers a {@link SettingsListener} to receive settings-related events.
     *
     * @param listener the listener (not null)
     */
    public static void addListener(final SettingsListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can not be null");
        }
        listeners.add(listener);
    }

    /**
     * Unregisters a {@link SettingsListener} so it no longer receives settings-related events.
     *
     * @param listener the listener (not null)
     */
    public static void removeListener(final SettingsListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can not be null");
        }
        listeners.remove(listener);
    }

    /**
     * Sends the {@link SettingsListener#onSettingChanged(String, Setting, Setting)} event to all the registered listeners.
     *
     * @param name name of the setting affected
     * @param old state of the setting begore update
     * @param _new state of the setting after update
     */
    private void notifySettingChanged(final String name, final Setting old, final Setting _new) {
        for (final SettingsListener listener : listeners) {
            listener.onSettingChanged(name, old, _new);
        }
    }
}
