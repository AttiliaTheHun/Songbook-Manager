package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Environment;

public class Frontpage {

    public Frontpage() {
        if (Environment.getInstance().settings.DISABLE_FRONTPAGE) {
            throw new RuntimeException("Plugin is disabled");
        }
    }

    public static boolean getEnabled() {
        return !Environment.getInstance().settings.DISABLE_FRONTPAGE;
    }

    public void generateSonglist() {

    }
}

