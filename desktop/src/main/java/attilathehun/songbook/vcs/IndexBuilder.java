package attilathehun.songbook.vcs;

import attilathehun.annotation.TODO;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.SHA256HashGenerator;
import attilathehun.songbook.vcs.index.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A convenience class for generation of Index objects for the Version Control System.
 */
public class IndexBuilder {
    private static final Logger logger = LogManager.getLogger(IndexBuilder.class);

    /**
     * Generates a save request index mapping all the files and data we need to pass to the server.
     * @param local local songbook index
     * @param remote remote songbook index
     * @return save index of the differences
     */
    public SaveIndex createSaveIndex(Index local, Index remote) {
        SaveIndex index = new SaveIndex(CacheManager.getInstance().getCachedSongbookVersionTimestamp());
        index.setAdditions(new Property());
        index.setDeletions(new Property());
        Collection<String> collectionAdditions;
        Collection<String> collectionDeletions;
        for (String collection : Environment.getInstance().getRegisteredManagers().keySet()) {
            collectionAdditions = getExtraItems((Collection) remote.getData().get(collection), (Collection) local.getData().get(collection));
            index.getAdditions().put(collection, collectionAdditions);
            collectionDeletions = getMissingItems((Collection) remote.getData().get(collection), (Collection) local.getData().get(collection));
            index.getDeletions().put(collection, collectionDeletions);
        }
        Property changes = new Property(getChanges(local, remote));

        index.setChanges(changes);
        index.setCollections(new ArrayList<>(compareCollections(local, remote)));
        return index;
    }

    /**
     * Generates a load request index mapping the files we need to obtain from the server.
     * @param local local songbook index
     * @param remote remote songbook index
     * @return load index of the differences
     */
    public LoadIndex createLoadIndex(Index local, Index remote) {
        Property missing = new Property();
        Collection<String> collectionMissing;
        for (String collection : Environment.getInstance().getRegisteredManagers().keySet()) {
            collectionMissing = getMissingItems((Collection) remote.getData().get(collection), (Collection) local.getData().get(collection));
            missing.put(collection, collectionMissing);
        }
        Property outdated = new Property(getChanges(local, remote));
        LoadIndex index = new LoadIndex();
        index.setMissing(missing);
        index.setOutdated(outdated);
        index.setCollections(new ArrayList<>(compareCollections(local, remote)));
        return index;
    }

    /**
     * Generates a complete index of the local songbook data. Performs blocking operations.
     * @return local songbook index
     */
    public Index createLocalIndex() throws IOException, NoSuchAlgorithmException {
        SHA256HashGenerator hashGenerator = new SHA256HashGenerator();
        Index index = new Index();
        index.setData(new Property());
        index.setHashes(new Property());
        index.setCollections(new Property());
        index.setMetadata(new Property());

        Map<String, String> collectionSongs;
        String collectionHash;
        for (String collection : Environment.getInstance().getRegisteredManagers().keySet()) {
            BuildWorker.spamCPUAndMemoryWithThreads(listFiles(Environment.getInstance().settings.collections.get(collection).getSongDataFilePath()));
            collectionSongs = BuildWorker.map;
            collectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.collections.get(collection).getCollectionFilePath()));
            index.getData().put(collection, collectionSongs.keySet());
            index.getHashes().put(collection, collectionSongs.values());
            index.getCollections().put(collection, collectionHash);
        }

        index.setVersionTimestamp(CacheManager.getInstance().getCachedSongbookVersionTimestamp());
        return index;
    }

    /**
     * Obtain a list of file names inside the specified directory. Should throw exceptions when the path is invalid.
     * @param dir target directory
     * @return list of file names
     */
    private List<String> listFileNames(String dir) {
        if (dir == null) {
            throw new IllegalArgumentException();
        }
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    /**
     * Obtain a list of file objects inside the specified directory. Should throw exceptions when the path is invalid.
     * @param dir target directory
     * @return list of file objects
     */
    private List<File> listFiles(String dir) {
        if (dir == null) {
            throw new IllegalArgumentException();
        }
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .collect(Collectors.toList());
    }

    /**
     * Returns a collection of items from the first collection that are absent in the second one.
     * @param old the collection to compare against
     * @param current to collection to compare
     * @return elements from old absent in current
     */
    private Collection<String> getMissingItems(Collection<String> old, Collection<String> current) {
        Collection<String> missing = new ArrayList<>();
        for (String s : old) {
            if (!current.contains(s)) {
                missing.add(s);
            }
        }
       return missing;
    }

    /**
     * Returns a collection of items from the second collection that are not present in the first one.
     * @param old the collection to compare against
     * @param current to collection to compare
     * @return elements from current absent in old
     */
    private Collection<String> getExtraItems(Collection<String> old, Collection<String> current) {
        return getMissingItems(current, old);
    }

    /**
     * Returns a HashMap mapping songs present in both indices whose hashes do not match for every collection that is registered within the environment.
     * See {@link Environment#registerCollectionManager(CollectionManager)}.
     * @param localIndex the index to compare against
     * @param remoteIndex the index to be compared
     * @return a map of collections containing the actual changes
     */
    private HashMap<String, Collection<String>> getChanges(Index localIndex, Index remoteIndex) {
        HashMap<String, Collection<String>> changes = new HashMap<>();

        for (String collection : Environment.getInstance().getRegisteredManagers().keySet()) {

            ArrayList<String> localSongs = (ArrayList<String>) localIndex.getData().get(collection);
            ArrayList<String> localSongHashes = (ArrayList<String>) localIndex.getHashes().get(collection);
            if (localSongs.size() != localSongHashes.size()) {
                throw new MalformedIndexException("Song count and song hash count do not match!");
            }
            ArrayList<String> remoteSongs = (ArrayList<String>) remoteIndex.getData().get(collection);
            ArrayList<String> remoteSongHashes = (ArrayList<String>) remoteIndex.getHashes().get(collection);
            if (remoteSongs.size() != remoteSongHashes.size()) {
                throw new MalformedIndexException("Song count and song hash count do not match!");
            }

            HashMap<String, String> localSongData = new HashMap<>();

            for (int i = 0; i < localSongs.size(); i++) {
                localSongData.put(localSongs.get(i), localSongHashes.get(i));
            }

            HashMap<String, String> remoteSongData = new HashMap<>();

            for (int i = 0; i < remoteSongs.size(); i++) {
                remoteSongData.put(remoteSongs.get(i), remoteSongHashes.get(i));
            }

            Collection<String> collectionChanges = new ArrayList<>();

            for (String song : localSongData.keySet()) {
                if (remoteSongData.get(song) == null) {
                    continue;
                }

                if (!localSongData.get(song).equals(remoteSongData.get(song))) {
                    collectionChanges.add(song);
                }
            }

            changes.put(collection, collectionChanges);

        }

        return changes;
    }

    /**
     * Compares collection hashes in the indexes and returns a lists collections that have been modified (whose hashes do not match)
     * @param localIndex the index to compare against
     * @param remoteIndex the index to be compared
     * @return modified collection names list
     */
    public static List<String> compareCollections(Index localIndex, Index remoteIndex) {
        List<String> modifiedCollections = new ArrayList<>();
        if (!localIndex.getCollections().get(StandardCollectionManager.getInstance().getCollectionName()).equals(remoteIndex.getCollections().get(StandardCollectionManager.getInstance().getCollectionName()))) {
            modifiedCollections.add(StandardCollectionManager.getInstance().getCollectionName());
        }
        if (!localIndex.getCollections().get(EasterCollectionManager.getInstance().getCollectionName()).equals(remoteIndex.getCollections().get(EasterCollectionManager.getInstance().getCollectionName()))) {
            modifiedCollections.add(EasterCollectionManager.getInstance().getCollectionName());
        }
        return modifiedCollections;
    }

    /**
     * A specially configured class for obtaining song file hashes.
     */
    private static class BuildWorker implements Runnable {
        private static final Logger logger = LogManager.getLogger(BuildWorker.class);
        static ConcurrentLinkedDeque<File> deque;
        static Map<String, String> map;
        static AtomicInteger activeThreads;

        /**
         * Generates hash for the file that is in the bottom of the deck and registers the hash in the map.
         */
        @Override
        public void run() {
                activeThreads.incrementAndGet();
            try {
                SHA256HashGenerator hashGenerator = new SHA256HashGenerator();
                File file;
                while (deque.size() > 0) {
                    file = deque.pollLast();
                    map.put(file.getName(), hashGenerator.getHash(file));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            activeThreads.decrementAndGet();
        }

        /**
         * Initialization method to perform the necessary task setup.
         * @param dequeData task input data
         */
        private static void init(Collection<File> dequeData) {
            activeThreads = new AtomicInteger(0);
            deque = new ConcurrentLinkedDeque<File>(dequeData);
            map = Collections.synchronizedSortedMap(new TreeMap<String, String>());
        }

        /**
         * A verification method to be called when the task execution is finished.
         */
        private static void postExecution() {
            if (deque.size() > 0) {
                throw  new IllegalStateException();
            }
            if (activeThreads.intValue() != 0) {
                throw  new IllegalStateException();
            }
        }

        /**
         * PerformBuildTask method. Executes the preconfigured task upon a given data set. Blocks the thread until the task
         * is finished.
         * @param collection task input data
         */
        private static void spamCPUAndMemoryWithThreads(Collection<File> collection) {
            init(collection);
            Thread[] threads = new Thread[Environment.getInstance().settings.vcs.VCS_THREAD_COUNT];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(new BuildWorker());
                threads[i].start();
            }
            while (activeThreads.intValue() > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            postExecution();
        }

    }

}
