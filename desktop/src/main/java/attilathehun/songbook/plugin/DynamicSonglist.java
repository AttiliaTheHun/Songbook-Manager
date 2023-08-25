package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.HTMLGenerator;

public class DynamicSonglist {

    public static final int MAX_SONG_PER_COLUMN = 38;
    public static final int MAX_SONG_PER_PAGE = 2 * MAX_SONG_PER_COLUMN;

    public DynamicSonglist() {
        if (Environment.getInstance().settings.DISABLE_DYNAMIC_SONGLIST) {
            throw new RuntimeException("Plugin is disabled");
        }
    }

    public static boolean getEnabled() {
        return !Environment.getInstance().settings.DISABLE_DYNAMIC_SONGLIST;
    }

    public int generateSonglist() {
        boolean isLastPageSignleColumn = false;
        int songlistParts = Environment.getInstance().getCollectionManager().getDisplayCollection().size() / MAX_SONG_PER_PAGE;
        if (Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONG_PER_PAGE != 0) {
            songlistParts += 1;
            isLastPageSignleColumn = Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONG_PER_PAGE <= MAX_SONG_PER_COLUMN;
        }

        HTMLGenerator generator = new HTMLGenerator();
        if (songlistParts == 0) {
            generator.generateSonglistSegmentFile(0, 0, 1);
            return 1;
        }

        int startIndex = 0;
        int endIndex = (songlistParts == 1 && MAX_SONG_PER_PAGE > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) ? Environment.getInstance().getCollectionManager().getDisplayCollection().size() : MAX_SONG_PER_PAGE;

        for (int i = 0; i < songlistParts; i++) {
            generator.generateSonglistSegmentFile(startIndex, endIndex, i);
            startIndex += MAX_SONG_PER_PAGE;
            endIndex = (endIndex + MAX_SONG_PER_PAGE > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) ? Environment.getInstance().getCollectionManager().getDisplayCollection().size() :
                        endIndex + MAX_SONG_PER_PAGE;
        }
        return songlistParts;
    }
}
