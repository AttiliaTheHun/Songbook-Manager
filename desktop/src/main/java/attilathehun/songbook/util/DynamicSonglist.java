package attilathehun.songbook.util;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DynamicSonglist{
    private static final Logger logger = LogManager.getLogger(DynamicSonglist.class);

    public static int generateSonglist() {
        final int MAX_SONGS_PER_COLUMN = SettingsManager.getInstance().getValue("DYNAMIC_SONGLIST_SONGS_PER_COLUMN");
        final int MAX_SONGS_PER_PAGE = 2 * MAX_SONGS_PER_COLUMN;
        boolean isLastPageSingleColumn = false;
        int songlistParts = Environment.getInstance().getCollectionManager().getDisplayCollection().size() / MAX_SONGS_PER_PAGE;
        if (Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONGS_PER_PAGE != 0) {
            songlistParts += 1;
            isLastPageSingleColumn = Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONGS_PER_PAGE <= MAX_SONGS_PER_COLUMN;
        }

        HTMLGenerator generator = new HTMLGenerator();
        if (songlistParts == 0) {
            generator.generateSonglistSegmentFile(0, 0, 0);
            return 1;
        }

        int startIndex = 0;
        int endIndex = (songlistParts == 1 && MAX_SONGS_PER_PAGE > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) ? Environment.getInstance().getCollectionManager().getDisplayCollection().size() : MAX_SONGS_PER_PAGE;

        for (int i = 0; i < songlistParts; i++) {
            generator.generateSonglistSegmentFile(startIndex, endIndex, i);
            startIndex += MAX_SONGS_PER_PAGE;
            endIndex = Math.min(endIndex + MAX_SONGS_PER_PAGE, Environment.getInstance().getCollectionManager().getDisplayCollection().size());
        }
        logger.debug("Dynamic songlist generated");
        return songlistParts;
    }

}
