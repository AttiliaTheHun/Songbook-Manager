package attilathehun.songbook.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SongTest {

    @Test
    public void testGetDisplayId_whenIdIsValid() {
        int validId = 24;
        Song s = new Song("Song", validId);
        assertEquals(String.valueOf(validId), s.getDisplayId());
    }

    @Test
    public void testGetDisplayId_whenIdIsInvalid() {
        int invalidId = -24;
        String name = "Song";
        Song s = new Song(name, invalidId);
        assertEquals(name, s.getDisplayId());
    }

    @Test
    public void testEquals_whenNameAndIdDefined_shouldEqual() {
        String name = "Song";
        int id = 12;
        Song s1 = new Song(name, id);
        Song s2 = new Song(name, id);
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdDefined_shouldNotEqual() {
        String name = "Song";
        Song s1 = new Song(name, 12);
        Song s2 = new Song(name, 13);
        assertNotEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndActiveAndURLAndAuthorDefined_authorMismatch_shouldEqual() {
        String name = "Song";
        int id = 12;
        boolean active = true;
        String url = "url";
        Song s1 = new Song(id, name, active, url);
        s1.setAuthor("John Smith");
        Song s2 = new Song(id, name, active, url);
        s2.setAuthor("Captain Jack Sparrow");
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndActiveAndURLAndFormerIdDefined_formerIdMismatch_shouldEqual() {
        String name = "Song";
        int id = 12;
        boolean active = true;
        String url = "url";
        Song s1 = new Song(id, name, active, url);
        s1.setFormerId(25);
        Song s2 = new Song(id, name, active, url);
        s2.setFormerId(35);
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndActiveAndURLDefined_URLMismatch_shouldEqual() {
        String name = "Song";
        int id = 12;
        boolean active = true;
        Song s1 = new Song(id, name, active, "example.com");
        Song s2 = new Song(id, name, active, "cyber.mil");
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndManagerDefined_managerOneIsNull_shouldEqual() {
        String name = "Song";
        int id = 12;
        Song s1 = new Song(name, id);
        s1.setManager(new TestCollectionManager());
        Song s2 = new Song(name, id);
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndManagerDefined_managerTwoIsNull_shouldEqual() {
        String name = "Song";
        int id = 12;
        Song s1 = new Song(name, id);
        Song s2 = new Song(name, id);
        s2.setManager(new TestCollectionManager());
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndManagerDefined_shouldEqual() {
        String name = "Song";
        int id = 12;
        CollectionManager manager = new TestCollectionManager();
        Song s1 = new Song(name, id);
        s1.setManager(manager);
        Song s2 = new Song(name, id);
        s2.setManager(manager);
        assertEquals(s1, s2);
    }

    @Test
    public void testEquals_whenNameAndIdAndManagerDefined_managerMismatch_shouldNotEqual() {
        String name = "Song";
        int id = 12;
        Song s1 = new Song(name, id);
        s1.setManager(new TestCollectionManager());
        Song s2 = new Song(name, id);
        s2.setManager(new TestCollectionManager());
        assertNotEquals(s1, s2);
    }

}