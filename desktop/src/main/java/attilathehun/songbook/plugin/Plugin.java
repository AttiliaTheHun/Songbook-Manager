package attilathehun.songbook.plugin;


import java.util.HashMap;

public abstract class Plugin {

    public abstract String getName();

    public abstract int execute();

    public abstract PluginSettings getSettings();

    public static class PluginSettings extends HashMap<String, Object> {

    }
}
