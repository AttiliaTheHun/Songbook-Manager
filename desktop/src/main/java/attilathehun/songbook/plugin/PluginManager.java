package attilathehun.songbook.plugin;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginManager {

    private static final Logger logger = LogManager.getLogger(PluginManager.class);

    private static final PluginManager instance = new PluginManager();

    private static final Map<String, Plugin> plugins = new HashMap<String, Plugin>();


    private PluginManager() {

    }

    public static PluginManager getInstance() {
        return instance;
    }

    public static void registerPlugin(Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
        Settings.getInstance().registerPlugin(plugin);
        logger.debug("Plugin registered: " + plugin.getName());
    }

    public Plugin getPlugin(String name) {
        return plugins.get(name);
    }

    public Settings getSettings() {
        return Settings.getInstance();
    }

    public void setSettings(Settings s) {
        Settings.getInstance().update(s);
    }


    public static class Settings extends HashMap<String, Plugin.PluginSettings> implements Serializable {

        private static final Logger logger = LogManager.getLogger(Settings.class);

        private static final Map<String, Plugin.PluginSettings> instance = new Settings();

        private Settings() {

        }

        private static Settings getInstance() {
            return (Settings) instance;
        }

        public void registerPlugin(Plugin plugin) {
            if (get(plugin.getName()) == null) {
                put(plugin.getName(), plugin.getSettings());
            }
        }

        public void update(Settings s) {
            clear();
            putAll(s);
        }

        public boolean getEnabled(String pluginName) {
            if (get(pluginName) == null) {
                return false;
            }
            return get(pluginName).enabled;
        }





    }

}