package attilathehun.songbook.environment;

import attilathehun.songbook.collection.Song;

public interface EnvironmentStateListener {
    void onRefresh();
    void onPageTurnedBack();
    void onPageTurnedForward();
    void onSongOneSet(Song s);
    void onSongTwoSet(Song s);
}
