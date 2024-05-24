package attilathehun.songbook.plugin;

import attilathehun.songbook.util.DynamicSonglist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Deprecated
public final class PluginManager {

    private static final Logger logger = LogManager.getLogger(PluginManager.class);

    private static final PluginManager instance = new PluginManager();

    private final Map<String, Plugin> plugins = new HashMap<>();


    private PluginManager() {

    }







    public static PluginManager getInstance() {
        return instance;
    }

    /**
     * Adds a plugin to the plugin collection. Unless registered, a plugin will not be recognized my the most part of the
     * program as plugins are supposed to be accessed through {@link #getPlugin(String)}. A plugin can not be unregistered,
     * but can be set as disabled which the rest of the program should abide by not using this plugin.
     *
     * @param plugin the wannabe registered plugin
     */
    public void registerPlugin(final Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
        logger.info("Plugin registered: " + plugin.getName());
    }

    /**
     * Returns a plugin, if such a plugin is registered.
     *
     * @param name target plugin name
     * @return the plugin or null
     */
    public Plugin getPlugin(final String name) {
        return plugins.get(name);
    }

    /**
     * Collects settings of all the registered plugins to a name-settings mapping. This mapping can then be serialised and
     * saved.
     *
     * @return plugin settings collection
     */
    public HashMap<String, Plugin.PluginSettings> getSettingsMap() {
        HashMap<String, Plugin.PluginSettings> map = new HashMap<>();
        for (Plugin p : plugins.values()) {
            map.put(p.getName(), p.getSettings());
        }
        return map;
    }

    /**
     * Injects settings to the registered plugins. Settings are provided as a pluginName-pluginSettings mapping.
     *
     * @param settingsMap the new plugin settings
     */
    public void loadPluginSettings(final HashMap<String, Plugin.PluginSettings> settingsMap) {
        if (settingsMap == null) {
            throw new IllegalArgumentException("plugin settings map must never be null");
        }
        for (Plugin p : plugins.values()) {
            if (settingsMap.get(p.getName()) != null) {
                p.setSettings(settingsMap.get(p.getName()));
            }

        }
    }

}
