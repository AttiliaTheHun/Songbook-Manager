package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.collection.TestCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.vcs.index.Index;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndexBuilderTest {
    final String STANDARD_COLLECTION_NAME = "standard";
    final String EASTER_COLLECTION_NAME = "easter";

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

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

        // init the mock
        Environment environmentMock = mock(Environment.class);
        HashMap<String, CollectionManager> fakeRegisteredManagers = new HashMap<>();
        fakeRegisteredManagers.put(STANDARD_COLLECTION_NAME, new TestCollectionManager());
        fakeRegisteredManagers.put(EASTER_COLLECTION_NAME, new TestCollectionManager());
        when(environmentMock.getRegisteredManagers()).thenReturn(fakeRegisteredManagers);

        try (MockedStatic<Environment> staticEnvironmentMock = mockStatic(Environment.class)) {
            staticEnvironmentMock.when(Environment::getInstance).thenReturn(environmentMock);

            // what we were waiting for all the time
            LoadIndex index = builder.createLoadIndex(local, remote);

            assertEquals(((ArrayList<String>) index.getMissing().get(STANDARD_COLLECTION_NAME)).size(), 0);
            assertEquals(((ArrayList<String>) index.getMissing().get(EASTER_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getMissing().get(EASTER_COLLECTION_NAME)).get(0), "song3");
            assertEquals(((ArrayList<String>) index.getOutdated().get(STANDARD_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getOutdated().get(STANDARD_COLLECTION_NAME)).get(0), "song2");
            assertEquals(((ArrayList<String>) index.getOutdated().get(EASTER_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getOutdated().get(EASTER_COLLECTION_NAME)).get(0), "song1");
            assertEquals(index.getCollections().size(), 1);
            assertEquals(((ArrayList<String>) index.getCollections()).get(0), EASTER_COLLECTION_NAME);
        }

    }

    @Test
    void createSaveIndexTest() {
        final long CACHED_TIMESTAMP = 227;
        IndexBuilder builder = new IndexBuilder();
        Index local = createTestLocalIndex(27);
        Index remote = createTestRemoteIndex(12);

        // init the mocks
        Environment environmentMock = mock(Environment.class);
        HashMap<String, CollectionManager> fakeRegisteredManagers = new HashMap<>();
        fakeRegisteredManagers.put(STANDARD_COLLECTION_NAME, new TestCollectionManager());
        fakeRegisteredManagers.put(EASTER_COLLECTION_NAME, new TestCollectionManager());
        when(environmentMock.getRegisteredManagers()).thenReturn(fakeRegisteredManagers);
        CacheManager cacheManagerMock = mock(CacheManager.class);
        when(cacheManagerMock.getCachedSongbookVersionTimestamp()).thenReturn(CACHED_TIMESTAMP);

        try (
                MockedStatic<Environment> staticEnvironmentMock = mockStatic(Environment.class);
                MockedStatic<CacheManager> staticCacheManagerMock = mockStatic(CacheManager.class);
                ) {
            staticEnvironmentMock.when(Environment::getInstance).thenReturn(environmentMock);
            staticCacheManagerMock.when(CacheManager::getInstance).thenReturn(cacheManagerMock);

            SaveIndex index = builder.createSaveIndex(local, remote);

            assertEquals(((ArrayList<String>) index.getAdditions().get(STANDARD_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getAdditions().get(STANDARD_COLLECTION_NAME)).get(0), "song3");
            assertEquals(((ArrayList<String>) index.getAdditions().get(EASTER_COLLECTION_NAME)).size(), 0);
            assertEquals(((ArrayList<String>) index.getDeletions().get(STANDARD_COLLECTION_NAME)).size(), 0);
            assertEquals(((ArrayList<String>) index.getDeletions().get(EASTER_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getDeletions().get(EASTER_COLLECTION_NAME)).get(0), "song3");
            assertEquals(((ArrayList<String>) index.getChanges().get(STANDARD_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getChanges().get(STANDARD_COLLECTION_NAME)).get(0), "song2");
            assertEquals(((ArrayList<String>) index.getChanges().get(EASTER_COLLECTION_NAME)).size(), 1);
            assertEquals(((ArrayList<String>) index.getChanges().get(EASTER_COLLECTION_NAME)).get(0), "song1");
            assertEquals(index.getCollections().size(), 1);
            assertEquals(((ArrayList<String>) index.getCollections()).get(0), EasterCollectionManager.getInstance().getCollectionName());
            assertTrue(index.getVersionTimestamp() > -1);
        }
    }

}