package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionListener;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DynamicSonglist implements EnvironmentStateListener, CollectionListener {
    private static final Logger logger = LogManager.getLogger(DynamicSonglist.class);

    private static final DynamicSonglist INSTANCE = new DynamicSonglist();
    private int current_list_pages = -1;

    private DynamicSonglist() {

    }

    /**
     * Registers a listener to the current {@link CollectionManager}. Calling this method more than once per runtime may lead to undefined behavior.
     */
    public static void init() {
        Environment.addListener(INSTANCE);
        Environment.getInstance().getCollectionManager().addListener(INSTANCE);
        getInstance().generateSonglist();
    }

    public static DynamicSonglist getInstance() {
        return INSTANCE;
    }

    /**
     * Generates the HTML files of the songlist based on the default manager's {@link CollectionManager#getFormalCollection()} output. The file is generated from a template.
     */
    private void generateSonglist() {
        final int MAX_SONGS_PER_COLUMN = SettingsManager.getInstance().getValue("DYNAMIC_SONGLIST_SONGS_PER_COLUMN");
        final int MAX_SONGS_PER_PAGE = 2 * MAX_SONGS_PER_COLUMN;

        int songlistParts = Environment.getInstance().getCollectionManager().getDisplayCollection().size() / MAX_SONGS_PER_PAGE;
        if (Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONGS_PER_PAGE != 0) {
            songlistParts += 1;
        }

        final HTMLGenerator generator = new HTMLGenerator();
        if (songlistParts == 0) {
            generator.generateSonglistSegmentFile(0, 0, 0);
            current_list_pages = 1;
            return;
        }

        int startIndex = 0;
        int endIndex = (songlistParts == 1 && MAX_SONGS_PER_PAGE > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) ? Environment.getInstance().getCollectionManager().getDisplayCollection().size() : MAX_SONGS_PER_PAGE;

        for (int i = 0; i < songlistParts; i++) {
            generator.generateSonglistSegmentFile(startIndex, endIndex, i);
            startIndex += MAX_SONGS_PER_PAGE;
            endIndex = Math.min(endIndex + MAX_SONGS_PER_PAGE, Environment.getInstance().getCollectionManager().getDisplayCollection().size());
        }
        logger.debug("dynamic songlist generated");
        current_list_pages = songlistParts;
    }

    public int getListPages() {
        return current_list_pages;
    }

    /**
     * Regenerates the songlist when the {@link Environment} is refreshed. This is necessary, because refreshing cleans the temp folder,
     * so the songlist files need to be created anew. This creates a race condition, because both the WebView and the DynamicSonglist are
     * are refreshed via environment event and if the files are not generated before the webview refreshes, it will throw an error as it will
     * not find the files (when a part of the songlist is actively being displayed).
     */
    @Override
    public void onRefresh() {
        generateSonglist();
    }

    @Override
    public void onPageTurnedBack() {

    }

    @Override
    public void onPageTurnedForward() {

    }

    @Override
    public void onSongOneSet(final Song s) {

    }

    @Override
    public void onSongTwoSet(final Song s) {

    }

    /**
     * Executed upon receiving the {@link Environment} event. Registers a listener to the current collection manager
     * and unregisters from the previous one to only receive changes that might affect the songlist.
     *
     * @param m the new {@link CollectionManager} in use
     * @param old the previous manager
     */
    @Override
    public void onCollectionManagerChanged(final CollectionManager m, final CollectionManager old) {
        if (old != null) {
            old.removeListener(this);
        }
        if (m == null) {
            logger.error("supplied a null manager, highly unexpected");
            return;
        }
        m.addListener(this);
        generateSonglist();
    }

    @Override
    public void onSongRemoved(final Song s, final CollectionManager m) {
        generateSonglist();
    }

    @Override
    public void onSongUpdated(final Song s, final CollectionManager m) {
        generateSonglist();
    }

    @Override
    public void onSongAdded(final Song s, final CollectionManager m) {
        generateSonglist();
    }
}
