package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;

public interface EnvironmentStateListener {

    /**
     * Emited when {@link Environment#refresh()} or {@link Environment#hardRefresh()}  is called.
     */
    void onRefresh();

    void onPageTurnedBack();

    void onPageTurnedForward();

    void onSongOneSet(Song s);

    void onSongTwoSet(Song s);

    /**
     * Emited when the default CollectionManager is changed using {@link Environment#setCollectionManager(CollectionManager)}.
     */
    void onCollectionManagerChanged(CollectionManager m);
}
