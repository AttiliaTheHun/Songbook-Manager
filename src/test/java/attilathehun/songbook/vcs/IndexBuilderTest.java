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

    Index createTestLocalIndex(final int timestamp) {
        final Index local = Index.empty();
        final HashMap<String, String> standardSongs = new HashMap<>();
        standardSongs.put("song1", "abc");
        standardSongs.put("song2", "acb");
        standardSongs.put("song3", "abc");
        local.getData().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(standardSongs.keySet().toArray())));
        local.getHashes().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(standardSongs.values()));
        final HashMap<String, String> easterSongs = new HashMap<>();
        easterSongs.put("song1", "acb");
        easterSongs.put("song2", "abc");
        local.getData().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(easterSongs.keySet().toArray())));
        local.getHashes().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>(easterSongs.values()));
        local.getCollections().put("standard", "abc");
        local.getCollections().put("easter", "acb");
        local.setVersionTimestamp(timestamp);
        return local;
    }

    Index createTestRemoteIndex(final int timestamp) {
        final Index local = Index.empty();
        final HashMap<String, String> standardSongs = new HashMap<>();
        standardSongs.put("song1", "abc");
        standardSongs.put("song2", "abc");
        local.getData().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(List.of(standardSongs.keySet().toArray())));
        local.getHashes().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>(standardSongs.values()));
        final HashMap<String, String> easterSongs = new HashMap<>();
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
        final IndexBuilder builder = new IndexBuilder();
        final Index local = createTestLocalIndex(12);
        final Index remote = createTestRemoteIndex(27);

        // init the mocks
        final Environment environmentMock = mock(Environment.class);
        final HashMap<String, CollectionManager> fakeRegisteredManagers = new HashMap<>();
        fakeRegisteredManagers.put(STANDARD_COLLECTION_NAME, new TestCollectionManager());
        fakeRegisteredManagers.put(EASTER_COLLECTION_NAME, new TestCollectionManager());
        when(environmentMock.getRegisteredManagers()).thenReturn(fakeRegisteredManagers);

        try (final MockedStatic<Environment> staticEnvironmentMock = mockStatic(Environment.class)) {
            staticEnvironmentMock.when(Environment::getInstance).thenReturn(environmentMock);

            // what we were waiting for all the time
            final LoadIndex index = builder.createLoadIndex(local, remote);

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
        final IndexBuilder builder = new IndexBuilder();
        final Index local = createTestLocalIndex(27);
        final Index remote = createTestRemoteIndex(12);

        // init the mocks
        final Environment environmentMock = mock(Environment.class);
        final HashMap<String, CollectionManager> fakeRegisteredManagers = new HashMap<>();
        fakeRegisteredManagers.put(STANDARD_COLLECTION_NAME, new TestCollectionManager());
        fakeRegisteredManagers.put(EASTER_COLLECTION_NAME, new TestCollectionManager());
        when(environmentMock.getRegisteredManagers()).thenReturn(fakeRegisteredManagers);
        final CacheManager cacheManagerMock = mock(CacheManager.class);
        when(cacheManagerMock.getCachedSongbookVersionTimestamp()).thenReturn(CACHED_TIMESTAMP);

        try (
                final MockedStatic<Environment> staticEnvironmentMock = mockStatic(Environment.class);
                final MockedStatic<CacheManager> staticCacheManagerMock = mockStatic(CacheManager.class);
                ) {
            staticEnvironmentMock.when(Environment::getInstance).thenReturn(environmentMock);
            staticCacheManagerMock.when(CacheManager::getInstance).thenReturn(cacheManagerMock);

            final SaveIndex index = builder.createSaveIndex(local, remote);

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