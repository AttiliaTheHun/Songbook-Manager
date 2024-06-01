package attilathehun.songbook.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionManagerTest {

    @Test
    public void testGetFrontpageSongIdNotValid() {
        Song s = CollectionManager.getFrontpageSong();
        assertFalse(CollectionManager.isValidId(s.id()));
    }

    @Test
    public void testGetSonglistSong_whenListPartIdIsNegative() {
        int listPartId = -5;
        assertThrows(IllegalArgumentException.class, () -> CollectionManager.getSonglistSong(listPartId));
    }

    @Test
    public void testGetSonglistSong_whenListPartIdIsPositive() {
        int listPartId = 5;
        assertDoesNotThrow(() -> CollectionManager.getSonglistSong(listPartId));
    }

    @Test
    public void testGetSonglistSong_whenListPartIdIsZero() {
        int listPartId = 0;
        assertDoesNotThrow(() -> CollectionManager.getSonglistSong(listPartId));
    }

    @Test
    public void testGetSonglistSongIdNotValid() {
        Song s = CollectionManager.getSonglistSong(2);
        assertFalse(CollectionManager.isValidId(s.id()));
    }

    @Test
    public void testGetShadowSongIdNotValid() {
        Song s = CollectionManager.getShadowSong();
        assertFalse(CollectionManager.isValidId(s.id()));
    }

    @Test
    public void testIsValidId_whenIdIsValid() {
        int id = 15;
        boolean result = CollectionManager.isValidId(id);
        assertTrue(result);
    }

    @Test
    public void testIsValidId_whenIdIsNotValid() {
        int id = -15;
        boolean result = CollectionManager.isValidId(id);
        assertFalse(result);
    }

}