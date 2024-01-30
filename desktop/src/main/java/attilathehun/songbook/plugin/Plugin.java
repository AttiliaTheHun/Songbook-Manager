package attilathehun.songbook.plugin;


import java.util.HashMap;

/**
 * Base class for all Songbook Manager plugins.
 */
public abstract class Plugin {

    /**
     * The plugin name should match the class name.
     *
     * @return plugin name
     */
    public abstract String getName();

    /**
     * A conventional plugin entry point other entities are free to call. Some plugins however may not find this integration
     * sufficient and are free to create custom methods; these however will make the plugin usage more difficult.
     *
     * @return implementation dependent (null we go)
     */
    public abstract Object execute();

    /**
     * This method is called by {@link PluginManager} when the class is marked with @{@link AutoRegister} annotation.
     */
    public abstract void register();

    /**
     * Returns default settings for the particular plugin. These settings are used when a settings.json
     * file is not present.
     *
     * @return plugin default {@link PluginSettings}
     */
    public abstract PluginSettings getDefaultSettings();

    /**
     * Returns the actual plugin settings in effect.
     *
     * @return plugin settings
     */
    public abstract PluginSettings getSettings();

    /**
     * Replaces the current plugin settings with another. Plugin settings must never be null to ensure stability of the
     * program.
     *
     * @param p new plugin settings
     */
    public abstract void setSettings(final PluginSettings p);

    /**
     * This class servers as a plugin settings collection. Plugin settings are stored as key-value pairs.
     */
    public static class PluginSettings extends HashMap<String, Object> {

        /**
         * Returns the enabled property of the plugin. All plugins should have this property.
         *
         * @return whether the plugin is enabled
         */
        public boolean getEnabled() {
            return get("enabled") == Boolean.TRUE;
        }
    }
}
