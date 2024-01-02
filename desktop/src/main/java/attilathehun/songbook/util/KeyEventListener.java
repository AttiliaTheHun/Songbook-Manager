package attilathehun.songbook.util;

import attilathehun.songbook.collection.Song;

/**
 * This interface provides a communication about specific key presses.
 */
@Deprecated(forRemoval = true)
public interface KeyEventListener {

    /**
     * Notifies the listener that left arrow key has been pressed.
     */
    public void onLeftArrowPressed();

    /**
     * Notifies the listener that right arrow key has been pressed.
     */
    public void onRightArrowPressed();
    public void onControlPlusRPressed();
    public void onDeletePressed();

    public void onControlPlusSPressed();

    public void onImaginarySongOneKeyPressed(Song s);

    public void onImaginarySongTwoKeyPressed(Song s);

    public boolean onImaginaryIsTextFieldFocusedKeyPressed();
}
