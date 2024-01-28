package attilathehun.songbook.collection;

public interface CollectionListener {

    /**
     * This event should be cast by the {@link CollectionManager#removeSong(Song)} method, however managers may choose
     * not to emit any events.
     *
     * @param s the song that is deleted
     * @param m corresponding CollectionManager
     */
    void onSongRemoved(Song s, CollectionManager m);

    /**
     * This event can be cast by the {@link CollectionManager#updateSongRecord(Song)}, {@link CollectionManager#updateSongRecordTitleFromHTML(Song)},
     * {@link CollectionManager#activateSong(Song)} and {@link CollectionManager#deactivateSong(Song)} methods, however managers may choose not to
     * emit any events.
     *
     * @param s new state of the song
     * @param m corresponding CollectionManager
     */
    void onSongUpdated(Song s, CollectionManager m);

    /**
     * This event should be cast by the {@link CollectionManager#addSong(Song)} method, however managers may choose
     * not to emit any events.
     *
     * @param s the song that is added
     * @param m corresponding CollectionManager
     */
    void onSongAdded(Song s, CollectionManager m);

}
