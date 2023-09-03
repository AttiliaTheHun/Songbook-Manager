package attilathehun.songbook.plugin;

import java.io.Serializable;

public abstract class Plugin {

    public abstract String getName();

    public abstract int execute();

    public abstract PluginSettings getSettings();

    public static class PluginSettings implements Serializable {
        public final boolean enabled;

        protected PluginSettings() {
            enabled = true;
        }
    }
}
