package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.vcs.VCSAdmin;
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
import java.util.HashMap;

public final class SettingsManager {
    private static final Logger logger = LogManager.getLogger(SettingsManager.class);

    private static final SettingsManager INSTANCE = new SettingsManager();

    private SettingsManager() {

    }

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        getInstance().load();
    }

    public void save() {
        Settings settings = new Settings();
        settings.environment = Environment.getInstance().getSettings();
        HashMap<String, CollectionManager.CollectionSettings> c = new HashMap<>();
        ((HashMap<String, CollectionManager>) ((HashMap<String, CollectionManager>) Environment.getInstance().getRegisteredManagers()).clone()).forEach( (key, value) -> c.put(key, value.getSettings()));
        settings.collections = c;
        settings.user = EnvironmentManager.getInstance().getUserSettings();
        settings.songbook = EnvironmentManager.getInstance().getSongbookSettings();
        settings.plugins = PluginManager.getInstance().getSettingsMap();
        settings.vcs = VCSAdmin.getInstance().getSettings();
        Misc.saveObjectToFileInJSON(settings, new File(Environment.EnvironmentSettings.SETTINGS_FILE_PATH));
        System.out.println("saved");
    }

    public void load() {
        try {
            Path path = Paths.get(Environment.EnvironmentSettings.SETTINGS_FILE_PATH);

            String json = String.join("\n", Files.readAllLines(path));

            Type targetClassType = new TypeToken<Settings>() {
            }.getType();
            Settings settings = new Gson().fromJson(json, targetClassType);
            Environment.getInstance().setSettings(settings.environment);
            StandardCollectionManager.getInstance().setSettings(settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()));
            StandardCollectionManager.getInstance().init();
            EasterCollectionManager.getInstance().setSettings(settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()));
            EasterCollectionManager.getInstance().init();
            EnvironmentManager.getInstance().setUserSettings(settings.user);
            EnvironmentManager.getInstance().setSongbookSettings(settings.songbook);
            PluginManager.getInstance().loadPluginSettings(settings.plugins);
            VCSAdmin.getInstance().setSettings(settings.vcs);
            logger.debug("Loaded local settings");

        } catch (NoSuchFileException nsf) {

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (!Misc.fileExists(Environment.EnvironmentSettings.SETTINGS_FILE_PATH)) {
            save();
        }
    }
}
