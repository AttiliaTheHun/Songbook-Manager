package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Environment;

public class DynamicSongbook {

    private static final int MAX_SONG_PER_COLUMN = 38;
    private static final int MAX_SONG_PER_PAGE = 2 * MAX_SONG_PER_COLUMN;

    public DynamicSongbook() {
        if (Environment.getInstance().settings.DISABLE_DYNAMIC_SONGLIST) {
            throw new RuntimeException("Plugin is disabled");
        }
    }

    public static boolean getEnabled() {
        return !Environment.getInstance().settings.DISABLE_DYNAMIC_SONGLIST;
    }

    public void generateSonglist() {

    }
}
