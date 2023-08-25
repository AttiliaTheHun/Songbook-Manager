package attilathehun.songbook.collection;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CollectionManager {

    private static CollectionManager instance;
    private Collection<Song> collection;

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

    public abstract void createShadowSong();
}
