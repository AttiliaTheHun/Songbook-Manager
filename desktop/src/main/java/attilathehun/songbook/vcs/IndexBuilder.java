package attilathehun.songbook.vcs;

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
    //TODO rewrite using loops and environment registered managers api
    public SaveIndex createSaveIndex(Index local, Index remote)  throws IOException, NoSuchAlgorithmException  {
        Collection<String> standardAdditions = getExtraItems((Collection) remote.getData().get("standard"), (Collection) local.getData().get("standard"));
        Collection<String> easterAdditions = getExtraItems((Collection) remote.getData().get("easter"), (Collection) local.getData().get("easter"));
        Collection<String> standardDeletions = getMissingItems((Collection) remote.getData().get("standard"), (Collection) local.getData().get("standard"));
        Collection<String> easterDeletions = getMissingItems((Collection) remote.getData().get("easter"), (Collection) local.getData().get("easter"));
        Collection<String> standardChanges = new ArrayList<String>();
        Collection<String> easterChanges = new ArrayList<String>();
        SHA256HashGenerator hashGenerator = new SHA256HashGenerator();
        String standardCollectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getCollectionFilePath()));
        String easterCollectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getCollectionFilePath()));
        SaveIndex index = new SaveIndex(CacheManager.getInstance().getCachedSongbookVersionTimestamp());
        index.getAdditions().put("standard", standardAdditions);
        index.getAdditions().put("easter", easterAdditions);
        index.getDeletions().put("standard", standardDeletions);
        index.getDeletions().put("easter", easterDeletions);
        index.getChanges().put("standard", standardChanges);
        index.getChanges().put("easter", easterChanges);
        index.getCollections().put("standard", standardCollectionHash);
        index.getCollections().put("easter", easterCollectionHash);
        return index;
    }

    /**
     * Generates a load request index mapping the files we need to obtain from the server.
     * //TODO make the collections part of the index, to be only received when there are changes to them (same for saving) to save bandwidth
     * @param local local songbook index
     * @param remote remote songbook index
     * @return load index of the differences
     */
    @Deprecated
    public LoadIndex createLoadIndex(Index local, Index remote) {
        Collection<String> standardMissing = getExtraItems((Collection) remote.getData().get("standard"), (Collection) local.getData().get("standard"));
        Collection<String> easterMissing = getExtraItems((Collection) remote.getData().get("easter"), (Collection) local.getData().get("easter"));
        Collection<String> standardOutdated = getChanges();
        Collection<String> easterOutdated = getChanges();
        LoadIndex index = new LoadIndex();
        index.getMissing().put("standard", standardMissing);
        index.getMissing().put("easter", easterMissing);
        index.getOutdated().put("standard", standardOutdated);
        index.getOutdated().put("easter", easterOutdated);
        return index;
    }

    /**
     * Generates a complete index of the local songbook data. Performs blocking operations.
     * @return local songbook index
     */
    public Index createLocalIndex() throws IOException, NoSuchAlgorithmException {
        BuildWorker.spamCPUAndMemoryWithThreads(listFiles(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()));
        Map standardSongs = BuildWorker.map;
        BuildWorker.spamCPUAndMemoryWithThreads(listFiles(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()));
        Map easterSongs = BuildWorker.map;
        SHA256HashGenerator hashGenerator = new SHA256HashGenerator();
        String standardCollectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getCollectionFilePath()));
        String easterCollectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getCollectionFilePath()));
        Index index = new Index(null);
        Property data = new Property();
        data.put("standard", standardSongs.keySet());
        data.put("easter", easterSongs.keySet());
        index.setData(data);
        Property hashes = new Property();
        hashes.put("standard", standardSongs.values());
        hashes.put("easter", easterSongs.values());
        index.setHashes(hashes);
        Property metadata = new Property();
        index.setMetadata(metadata);
        Property collections = new Property();
        collections.put("standard", standardCollectionHash);
        collections.put("easter", easterCollectionHash);
        index.setCollections(collections);
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
        Collection missing = new ArrayList<>();
        for (Object o : old) {
            if (!current.contains(o)) {
                missing.add(o);
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

    private Collection<String> getChanges() {

        //TODO
        return new ArrayList<String>();
    }

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
         * Initialization method to perform the necessary task setup. Internal method, do not use outside class.
         * @param dequeData task input data
         */
        private static void init(Collection<File> dequeData) {
            activeThreads = new AtomicInteger(0);
            deque = new ConcurrentLinkedDeque<File>(dequeData);
            map = Collections.synchronizedSortedMap(new TreeMap<String, String>());
        }

        /**
         * A verification method to be called when the task execution is finished. Internal method, do not use outside class.
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
