package attilathehun.songbook.util;

import attilathehun.songbook.collection.Song;

/**
 * This interface provides a communication about specific key presses.
 */
public interface KeyEventListener {

    /**
     * Notifies the listener that left arrow key has been pressed.
     */
    public void onLeftArrowPressed();

    /**
     * Notifies the listener that rigth arrow key has been pressed.
     */
    public void onRightArrowPressed();

    /**
     * Notifies the listeners that Ctrl + R keyboard shortcut has been utilised.
     */
    public void onControlPlusRPressed();

    public void onImaginarySongOneKeyPressed(Song s);

    public void onImaginarySongTwoKeyPressed(Song s);

    public boolean onImaginaryIsTextFieldFocusedKeyPressed();
}
