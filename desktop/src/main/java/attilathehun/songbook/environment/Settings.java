package attilathehun.songbook.environment;

import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.vcs.VCSAdmin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings implements Serializable {

    private static final Logger logger = LogManager.getLogger(Settings.class);

    public final Environment.EnvironmentSettings environment;
    public final UserSettings user;
    public final SongbookSettings songbook;
    public final PluginManager.Settings plugins;
    public final VCSAdmin.VCSSettings vcs;


    private Settings() {
        environment = new Environment.EnvironmentSettings();
        user = new UserSettings();
        songbook = new SongbookSettings();
        plugins = PluginManager.getInstance().getSettings();
        vcs = new VCSAdmin.VCSSettings();
    }

    static Settings getSettings() {
        try {
            Path path = Paths.get(Environment.EnvironmentSettings.SETTINGS_FILE_PATH);

            String json = String.join("\n", Files.readAllLines(path));

            Type targetClassType = new TypeToken<Settings>() {
            }.getType();
            Settings settings = new Gson().fromJson(json, targetClassType);
            PluginManager.getInstance().setSettings(settings.plugins);
            logger.info("Loaded local environment settings");
            return settings;
        } catch (NoSuchFileException nsf) {

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Settings settings = new Settings();
        if (!Environment.fileExists(Environment.EnvironmentSettings.SETTINGS_FILE_PATH)) {
            save(settings);
        }

        return settings;
    }

    public static boolean save(Settings settings) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Environment.EnvironmentSettings.SETTINGS_FILE_PATH, false);
            gson.toJson(settings, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Can not export the settings!");
            return false;
        }
        return true;
    }



    public static class UserSettings implements Serializable {
        private static final Logger logger = LogManager.getLogger(UserSettings.class);
        private final String AUTH_FILE_PATH;
        public final boolean AUTO_LOAD_DATA;
        public final AuthType AUTH_TYPE;
        private final String DEFAULT_READ_TOKEN;


        public UserSettings() {
            AUTO_LOAD_DATA = false;
            DEFAULT_READ_TOKEN = "SHJhYm/FoWkgTGV0J3MgRnVja2luZyAgR29vb28h";
            AUTH_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/.auth").toString();
            AUTH_TYPE = AuthType.TOKEN;
        }

        public String getDefaultReadToken(Environment.Certificate certificate) {
            return DEFAULT_READ_TOKEN;
        }

        public String getAuthFilePath(Environment.Certificate certificate) {
            return AUTH_FILE_PATH;
        }

        public enum AuthType implements Serializable {
            TOKEN {
                @Override
                public String toString() {
                    return "TOKEN";
                }
            },
            PHRASE {
                @Override
                public String toString() {
                    return "PHRASE";
                }
            }
        }


    }

    public static class SongbookSettings implements Serializable {
        private static final Logger logger = LogManager.getLogger(SongbookSettings.class);
        public final boolean BIND_SONG_TITLES;

        public SongbookSettings() {
            BIND_SONG_TITLES = true;
        }

        //TODO: make another title-author display options
    }

}
