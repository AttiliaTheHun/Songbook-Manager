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

    @Nested
    public class CollectionSettingsTest {


        @Test
        public void testGetCollectionFilePath_whenSettingNotDefined() {
            CollectionManager.CollectionSettings settings = new CollectionManager.CollectionSettings();
            assertNull(settings.getCollectionFilePath());
        }

        @Test
        public void testGetCollectionFilePath_whenSettingDefined() {
            String settingName = "COLLECTION_FILE_PATH";
            String path = "path";
            CollectionManager.CollectionSettings settings = new CollectionManager.CollectionSettings();
            settings.put(settingName, path);
            assertEquals(path, settings.getCollectionFilePath());
        }

        @Test
        public void testGetCollectionFilePath_whenSettingRedefined() {
            String settingName = "COLLECTION_FILE_PATH";
            String path1 = "path";
            String path2 = "path2";
            String path3 = "path3";
            CollectionManager.CollectionSettings settings = new CollectionManager.CollectionSettings();
            settings.put(settingName, path1);
            assertEquals(path1, settings.getCollectionFilePath());
            settings.put(settingName, path2);
            assertEquals(path2, settings.getCollectionFilePath());
            settings.put(settingName, path2);
            assertEquals(path2, settings.getCollectionFilePath());
        }

        @Test
        public void testGetSongDataFilePath_whenSettingNotDefined() {
            CollectionManager.CollectionSettings settings = new CollectionManager.CollectionSettings();
            assertNull(settings.getSongDataFilePath());
        }

        @Test
        public void testGetSongDataFilePath_whenSettingDefined() {
            String settingName = "SONG_DATA_FILE_PATH";
            String path = "path";
            CollectionManager.CollectionSettings settings = new CollectionManager.CollectionSettings();
            settings.put(settingName, path);
            assertEquals(path, settings.getSongDataFilePath());
        }

        @Test
        public void testGetSongDataFilePath_whenSettingRedefined() {
            String settingName = "SONG_DATA_FILE_PATH";
            String path1 = "path";
            String path2 = "path2";
            String path3 = "path3";
            CollectionManager.CollectionSettings settings = new CollectionManager.CollectionSettings();
            settings.put(settingName, path1);
            assertEquals(path1, settings.getSongDataFilePath());
            settings.put(settingName, path2);
            assertEquals(path2, settings.getSongDataFilePath());
            settings.put(settingName, path3);
            assertEquals(path3, settings.getSongDataFilePath());
        }

    }
}