package attilathehun.songbook.collection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class every collection manager should implement. Moreover, every collection manager should follow the singleton pattern for compatibility,
 * as the environment is set up to work with this pattern.
 */
public abstract class CollectionManager {
    public static final int INVALID_SONG_ID = -1;

    public static final String FRONTPAGE_SONG_NAME = "frontpage";
    public static final int FRONTPAGE_SONG_ID = -2;

    public static final String SONGLIST_SONG_NAME = "songlist%d";
    public static final int SONGLIST_SONG_ID = -3;

    public static final String SHADOW_SONG_NAME = "Shadow Song";
    public static final int SHADOW_SONG_ID = -4;


    /**
     * Initializes the collection. Should be call in advance to any collection-related logic.
     */
    public abstract void init();

    /**
     * Creates a Collection Manager with copy of the collection. This Collection Manager should be used when changes to the collection are undesirable
     * e.g. exporting. It is discouraged to ever set it as default Collection Manager because many methods use the Collection Managers directly and not
     * through the Environment API, thus a data loss is probable.
     * @return Collection Manager with a copy of the collection
     */
    public abstract CollectionManager copy();

    /**
     * Saves current state of the collection to its respective file. Any data manipulation should be saved automatically by the manager though.
     */
    public abstract void save();

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
     * Returns a collection modified to a presentable form for displaying purposes. Deactivated songs are excluded.
     * It is the collection format used for dynamic songlist generation.
     *
     * @return
     */
    public abstract ArrayList<Song> getDisplayCollection();

    /**
     * Returns a collection modified to a presentable form for output purposes. Deactivated songs are excluded,
     * but some other pages may be added depending on plugins. The WebView driver as well as the PDFGenerator class
     * work with this collection format.
     *
     * @return production form of the collection
     */

    public abstract ArrayList<Song> getFormalCollection();

    /**
     * Add a song to the collection. Song id and other properties may be managed by the manager implementation.
     * @param s the song data object to be added
     * @return the song that was added to the collection (may differ from input)
     */
    public abstract Song addSong(Song s);

    /**
     * Remove a song from the collection. Whether the song is identified by strict matching or by some property (e. g. id) depends on the implementation.
     * @param s the song to be deleted from the collection
     */
    public abstract void removeSong(Song s);

    /**
     * Activate a song (set `active` to true) in the collection.  Whether the song is identified by strict matching or by some property (e. g. id)
     * depends on the implementation.
     * @param s the song to be activated
     */
    public abstract void activateSong(Song s);

    /**
     * Deactivate a song (set `active` to false) in the collection.  Whether the song is identified by strict matching or by some property (e. g. id)
     * depends on the implementation.
     * @param s the song to be deactivated
     */
    public abstract void deactivateSong(Song s);

    /**
     * Edit song's properties. The collection manager must be able to link the new song to the old one.
     * @param s new song data
     * @return actual record of the song from the collection
     */
    public abstract Song updateSongRecord(Song s);

    /**
     * Matches song's title in the collection to the one set in its HTML file. `BIND_SONG_TITLES` setting must be active (true).
     * @param s target song
     */
    public abstract void updateSongRecordTitleFromHTML(Song s);

    /**
     * Matches song's title in the HTML file to the one set in the collection. `BIND_SONG_TITLES` setting must be active (true).
     * @param s target song
     */
    public abstract void updateSongHTMLTitleFromRecord(Song s);

    /**
     * Saves additional song's metadata to the HTML file as a backup. This metadata is used upon repairing a collection and has no effect in any other
     * functioning of the client.
     * @param s target song
     */
    public abstract void updateSongHTMLFromRecord(Song s);

    /**
     * Returns path of the song HTML file. Song is identified by its id.
     * @param s target song
     * @return song HTML file path or null
     */
    public abstract String getSongFilePath(Song s);

    /**
     * Returns path of the song HTML file.
     * @param id target song id
     * @return song HTML file path or null
     */
    public abstract String getSongFilePath(int id);

    /**
     * Returns index of the song in the raw collection. Song is identified by strict comparison (all properties).
     * @param s target song
     * @return index of the song in the raw collection; -1 if not found
     */
    public abstract int getCollectionSongIndex(Song s);

    /**
     * Returns index of the song under the specified id in the raw collection.
     * @param songId target song id
     * @return index of the song in the raw collection; -1 if not found
     */
    public abstract int getCollectionSongIndex(int songId);

    /**
     * Returns index of the song in the sorted collection. Song is identified by strict comparison (all properties).
     * @param s target song
     * @return index of the song in the sorted collection; -1 if not found
     */
    public abstract int getSortedCollectionSongIndex(Song s);

    /**
     * Returns index of the song under the specified id in the sortedcollection.
     * @param songId target song id
     * @return index of the song in the sorted collection; -1 if not found
     */
    public abstract int getSortedCollectionSongIndex(int songId);

    /**
     * Returns index of the song in the display collection. Song is identified by strict comparison (all properties).
     * @param s target song
     * @return index of the song in the display collection; -1 if not found
     */
    public abstract int getDisplayCollectionSongIndex(Song s);

    /**
     * Returns index of the song under the specified id in the display collection.
     * @param songId target song id
     * @return index of the song in the display collection; -1 if not found
     */
    public abstract int getDisplayCollectionSongIndex(int songId);

    /**
     * Returns index of the song in the formal collection. Song is identified by strict comparison (all properties).
     * @param s target song
     * @return index of the song in the formal collection; -1 if not found
     */
    public abstract int getFormalCollectionSongIndex(Song s);

    /**
     * Returns index of the song under the specified id in the formal collection.
     * @param songId target song id
     * @return index of the song in the formal collection; -1 if not found
     */
    public abstract int getFormalCollectionSongIndex(int songId);

    /**
     * Creates a placeholder song with prefilled data and invalid id. The content of this song depends purely on the manager implementation.
     * @return a placeholder song object
     */
    public abstract Song getPlaceholderSong();

    /**
     * Creates a song representing the frontpage of the songbook generated by the Frontpage plugin.
     * @return song object representing the frontpage
     */
    public static Song getFrontpageSong() {
        return new Song(FRONTPAGE_SONG_NAME, FRONTPAGE_SONG_ID);
    }

    /**
     * Creates a song that represents one page of the songlist generated by the DynamicSonglist plugin.
     * @param listPartId number of the part of the songlist
     * @return song object representing a part of the songlist
     */
    public static Song getSonglistSong(int listPartId) {
        if (listPartId < 0) {
            throw new IllegalArgumentException();
        }
        return new Song(String.format(SONGLIST_SONG_NAME, listPartId), SONGLIST_SONG_ID);
    }

    /**
     * Creates a shadow song that can be used to fill in blank space. Shadow songs should never become a part of any collection for they
     * are not compatible with the majority of the CollectionManager API.
     * @return shadow song object
     */
    public static Song getShadowSong() {
        return new Song(SHADOW_SONG_NAME, SHADOW_SONG_ID);
    };


}
