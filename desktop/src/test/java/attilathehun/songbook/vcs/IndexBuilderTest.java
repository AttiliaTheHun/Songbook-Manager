package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.vcs.index.Index;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexBuilderTest {

    Index createTestLocalIndex(int timestamp) {
        Index local = Index.empty();
        HashMap<String, String> standardSongs = new HashMap<>();
        standardSongs.put("song1", "abc");
        standardSongs.put("song2", "acb");
        standardSongs.put("song3", "abc");
        local.getData().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(standardSongs.keySet().toArray())));
        local.getHashes().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(standardSongs.values()));
        HashMap<String, String> easterSongs = new HashMap<>();
        easterSongs.put("song1", "acb");
        easterSongs.put("song2", "abc");
        local.getData().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(easterSongs.keySet().toArray())));
        local.getHashes().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>(easterSongs.values()));
        local.getCollections().put("standard", "abc");
        local.getCollections().put("easter", "acb");
        local.setVersionTimestamp(timestamp);
        return local;
    }

    Index createTestRemoteIndex(int timestamp) {
        Index local = Index.empty();
        HashMap<String, String> standardSongs = new HashMap<>();
        standardSongs.put("song1", "abc");
        standardSongs.put("song2", "abc");
        local.getData().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(standardSongs.keySet().toArray())));
        local.getHashes().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(standardSongs.values()));
        HashMap<String, String> easterSongs = new HashMap<>();
        easterSongs.put("song1", "abc");
        easterSongs.put("song2", "abc");
        easterSongs.put("song3", "abc");
        local.getData().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(easterSongs.keySet().toArray())));
        local.getHashes().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>(easterSongs.values()));
        local.getCollections().put("standard", "abc");
        local.getCollections().put("easter", "abc");
        local.setVersionTimestamp(timestamp);
        return local;
    }

    @Test
    void createLoadIndexTest() {
        IndexBuilder builder = new IndexBuilder();
        Index local = createTestLocalIndex(12);
        Index remote = createTestRemoteIndex(27);
        LoadIndex index = builder.createLoadIndex(local, remote);

        assertEquals(((ArrayList<String>) index.getMissing().get(StandardCollectionManager.getInstance().getCollectionName())).size(), 0);
        assertEquals(((ArrayList<String>) index.getMissing().get(EasterCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getMissing().get(EasterCollectionManager.getInstance().getCollectionName())).get(0), "song3");
        assertEquals(((ArrayList<String>) index.getOutdated().get(StandardCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getOutdated().get(StandardCollectionManager.getInstance().getCollectionName())).get(0), "song2");
        assertEquals(((ArrayList<String>) index.getOutdated().get(EasterCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getOutdated().get(EasterCollectionManager.getInstance().getCollectionName())).get(0), "song1");
        assertEquals(index.getCollections().size(), 1);
        assertEquals(((ArrayList<String>) index.getCollections()).get(0), EasterCollectionManager.getInstance().getCollectionName());

    }

    @Test
    void createSaveIndexTest() {
        IndexBuilder builder = new IndexBuilder();
        Index local = createTestLocalIndex(27);
        Index remote = createTestRemoteIndex(12);
        SaveIndex index = builder.createSaveIndex(local, remote);
        assertEquals(((ArrayList<String>) index.getAdditions().get(StandardCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getAdditions().get(StandardCollectionManager.getInstance().getCollectionName())).get(0), "song3");
        assertEquals(((ArrayList<String>) index.getAdditions().get(EasterCollectionManager.getInstance().getCollectionName())).size(), 0);
        assertEquals(((ArrayList<String>) index.getDeletions().get(StandardCollectionManager.getInstance().getCollectionName())).size(), 0);
        assertEquals(((ArrayList<String>) index.getDeletions().get(EasterCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getDeletions().get(EasterCollectionManager.getInstance().getCollectionName())).get(0), "song3");
        assertEquals(((ArrayList<String>) index.getChanges().get(StandardCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getChanges().get(StandardCollectionManager.getInstance().getCollectionName())).get(0), "song2");
        assertEquals(((ArrayList<String>) index.getChanges().get(EasterCollectionManager.getInstance().getCollectionName())).size(), 1);
        assertEquals(((ArrayList<String>) index.getChanges().get(EasterCollectionManager.getInstance().getCollectionName())).get(0), "song1");
        assertEquals(index.getCollections().size(), 1);
        assertEquals(((ArrayList<String>) index.getCollections()).get(0), EasterCollectionManager.getInstance().getCollectionName());
        assertTrue(index.getVersionTimestamp() > -1);
    }

}