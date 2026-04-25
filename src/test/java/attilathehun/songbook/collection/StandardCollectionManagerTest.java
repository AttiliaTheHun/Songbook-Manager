package attilathehun.songbook.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardCollectionManagerTest {

    @Test
    public void testGetInstance_thatIsNotNull() {
        assertNotNull(StandardCollectionManager.getInstance());
    }

}