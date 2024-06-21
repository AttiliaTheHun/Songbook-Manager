package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;

public interface EnvironmentStateListener {

    /**
     * Emitted when {@link Environment#refresh()} or {@link Environment#hardRefresh()} is called.
     */
    void onRefresh();

    /**
     * Emitted through {@link Environment#notifyOnPageTurnedBack()}. It is supposed to notify that the page has been turned to the left.
     */
    void onPageTurnedBack();

    /**
     * Emitted through {@link Environment#notifyOnPageTurnedForward()}. It is supposed to notify that the page has been turned to the right.
     */
    void onPageTurnedForward();

    /**
     * Emitted through {@link Environment#notifyOnSongOneSet(Song)}. It is supposed to notify that the song on the left of the page has been artificially (outside page turning)
     * updated.
     *
     * @param s the song that has been set
     */
    void onSongOneSet(Song s);


    /**
     * Emitted through {@link Environment#notifyOnSongTwoSet(Song)}. It is supposed to notify that the song on the right of the page has been artificially (outside page turning)
     * updated.
     *
     * @param s the song that has been set
     */
    void onSongTwoSet(Song s);

    /**
     * Emitted when the default CollectionManager is changed using {@link Environment#setCollectionManager(CollectionManager)}.
     */
    void onCollectionManagerChanged(CollectionManager m);
}
