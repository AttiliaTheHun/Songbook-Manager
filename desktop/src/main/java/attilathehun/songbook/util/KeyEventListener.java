package attilathehun.songbook.util;

import attilathehun.songbook.collection.Song;

/**
 * This interface provides a communication about specific key presses.
 */
public interface KeyEventListener {

    public void onLeftArrowPressed();

    public void onRightArrowPressed();

    public void onControlPlusRPressed();

    public void onImaginarySongOneKeyPressed(Song s);

    public void onImaginarySongTwoKeyPressed(Song s);
}
