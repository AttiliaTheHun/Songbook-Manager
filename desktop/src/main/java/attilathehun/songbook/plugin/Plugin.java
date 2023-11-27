package attilathehun.songbook.plugin;


import java.util.HashMap;

/**
 * Base class for all Songbook Manager plugins.
 */
public abstract class Plugin {

    public abstract String getName();

    public abstract int execute();

    /**
     * Returns default settings for the particular plugin. These settings are applied when
     * environment settings are being generated anew and do not take effect when the user has
     * already a custom settings.json file.
     * @return
     */
    public abstract PluginSettings getSettings();

    public static class PluginSettings extends HashMap<String, Object> {

    }
}
