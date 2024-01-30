package attilathehun.songbook.plugin;

import attilathehun.songbook.misc.Misc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PluginManager {

    private static final Logger logger = LogManager.getLogger(PluginManager.class);

    private static final PluginManager instance = new PluginManager();

    private static final Map<String, Plugin> plugins = new HashMap<>();


    private PluginManager() {
        autoRegisterPlugins();
    }

    /**
     * Automatically registers {@link Plugin}s that are marked for registration. This action is meant to be performed only
     * once, at startup.
     */
    private void autoRegisterPlugins() {
        Set<Class> pluginClasses = Misc.findAllClassesUsingClassLoader(this.getClass().getPackageName());
        for (Class c : pluginClasses) {
            if (c.getSuperclass().equals(Plugin.class) && c.isAnnotationPresent(AutoRegister.class)) {
                try {
                    ((Plugin) c.getMethod("getInstance").invoke(null)).register();
                } catch (Exception e) {
                    logger.error(String.format("automatic registering of the %s plugin failed", c.getSimpleName()));
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    @Deprecated(forRemoval = true)
    public static void loadPlugins() {
        DynamicSonglist.getInstance();
        Frontpage.getInstance();
        Export.getInstance();
        SML.getInstance();
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
    public static void registerPlugin(final Plugin plugin) {
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
            throw new IllegalArgumentException("plugin settings must never be null");
        }
        for (Plugin p : plugins.values()) {
            if (settingsMap.get(p.getName()) != null) {
                p.setSettings(settingsMap.get(p.getName()));
            }

        }
    }

}
