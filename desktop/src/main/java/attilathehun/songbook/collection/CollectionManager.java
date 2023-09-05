package attilathehun.songbook.collection;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CollectionManager {
    public static final int INVALID_SONG_ID = -1;

    public static final String FRONTPAGE_SONG_NAME = "frontpage";
    public static final int FRONTPAGE_SONG_ID = -2;

    public static final String SONGLIST_SONG_NAME = "songlist%d";
    public static final int SONGLIST_SONG_ID = -3;

    public static final String SHADOW_SONG_NAME = "Shadow Song";
    public static final int SHADOW_SONG_ID = -4;

    /**
     * Returns the raw collection the manager manages without any data manipulation.
     *
     * @return manager's raw collection
     */

    public abstract Collection<Song> getCollection();

    /**
     * Return the raw collection without any data manipulation, sorted by song name.
     *
     * @return raw collection sorted by name
     */

    public abstract ArrayList<Song> getSortedCollection();

    /**
     * Returns a collection modified to a presentable form for output purposes. The WebView driver as well as the PDFGenerator class work with this collection format.
     *
     * @return production form of the collection
     */

    public abstract ArrayList<Song> getFormalCollection();

    /**
     * Returns a collection modified to a presentable form for displaying purposes. It is the collection format used for dynamic songlist generation.
     *
     * @return
     */
    public abstract ArrayList<Song> getDisplayCollection();

    public abstract void init();

    public abstract String getSongFilePath(Song s);

    public abstract String getSongFilePath(int id);

    public abstract Song updateSongRecord(Song s);

    public abstract void updateSongRecordFromHTML(Song s);

    public abstract void save();

    public abstract Song addSong(Song s);

    public abstract void removeSong(Song s);

    public abstract void deactivateSong(Song s);

    public abstract void activateSong(Song s);

    public abstract Song getPlaceholderSong();
    public static Song getFrontpageSong() {
        return new Song(FRONTPAGE_SONG_NAME, FRONTPAGE_SONG_ID);
    }

    public static Song getSonglistSong(int listPartId) {
        if (listPartId < 0) {
            throw new IllegalArgumentException();
        }
        return new Song(String.format(SONGLIST_SONG_NAME, listPartId), SONGLIST_SONG_ID);
    }

    public static Song getShadowSong() {
        return new Song(SHADOW_SONG_NAME, SHADOW_SONG_ID);
    };
}
