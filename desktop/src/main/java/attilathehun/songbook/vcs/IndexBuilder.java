package attilathehun.songbook.vcs;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A convenience class for generation of Index objects for the Version Control System.
 */
public class IndexBuilder {

    private static final Logger logger = LogManager.getLogger(IndexBuilder.class);

    public SaveIndex createSaveIndex(Index local, Index remote) {
        Collection standardAdditions = getExtraItems((Collection) remote.getData().getContent().get("standard").getContent(), (Collection) local.getData().getContent().get("standard").getContent());
        Collection easterAdditions = getExtraItems((Collection) remote.getData().getContent().get("easter").getContent(), (Collection) local.getData().getContent().get("easter").getContent());

        return null;
    }

    public LoadIndex createLoadIndex(Index local, Index remote) {
        return null;
    }

    /**
     * Generates a complete index of the local songbook data. Performs blocking operations.
     * @return local songbook index
     */
    public Index createLocalIndex() throws IOException, NoSuchAlgorithmException {
        BuildWorker.spamCPUAndMemoryWithThreads(listFiles(Environment.getInstance().settings.environment.SONG_DATA_FILE_PATH));
        Map standardSongs = BuildWorker.map;
        BuildWorker.spamCPUAndMemoryWithThreads(listFiles(Environment.getInstance().settings.environment.EGG_DATA_FILE_PATH));
        Map easterSongs = BuildWorker.map;
        SHA256HashGenerator hashGenerator = new SHA256HashGenerator();
        String standardCollectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.environment.COLLECTION_FILE_PATH));
        String easterCollectionHash = hashGenerator.getHash(new File(Environment.getInstance().settings.environment.EASTER_COLLECTION_FILE_PATH));
        Index index = new Index(null);
        CompoundProperty data = new CompoundProperty();
        data.put("standard", new SimpleProperty<>(standardSongs.keySet().toArray()));
        data.put("easter", new SimpleProperty<>(easterSongs.keySet().toArray()));
        index.setData(data);
        CompoundProperty hashes = new CompoundProperty();
        hashes.put("standard", new SimpleProperty<>(standardSongs.values().toArray()));
        hashes.put("easter", new SimpleProperty<>(easterSongs.values().toArray()));
        index.setHashes(hashes);
        index.setMetadata(null);
        CompoundProperty collections = new CompoundProperty();
        data.put("standard", new SimpleProperty<>(standardCollectionHash));
        data.put("easter", new SimpleProperty<>(easterCollectionHash));
        index.setCollections(collections);
        index.setDefaultSettings(null);
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
    private Collection getMissingItems(Collection old, Collection current) {
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
    private Collection getExtraItems(Collection old, Collection current) {
        return getMissingItems(current, old);
    }

    /**
     * A specially configured class for obtaining song file hashes.
     */
    private static class BuildWorker implements Runnable {
        private static final Logger logger = LogManager.getLogger(BuildWorker.class);
        static ConcurrentLinkedDeque<File> deque;
        static Map<String, String> map;
        static volatile int activeThreads;

        /**
         * Generates hash for the file that is in the bottom of the deck and registers the hash in the map.
         */
        @Override
        public void run() {
            activeThreads++;
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
            activeThreads--;
        }

        /**
         * Initialization method to perform the necessary task setup. Internal method, do not use outside class.
         * @param dequeData task input data
         */
        private static void init(Collection<File> dequeData) {
            activeThreads = 0;
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
            if (activeThreads != 0) {
                throw  new IllegalStateException();
            }
        }

        /**
         * PerformBuildTask method. Executes the preconfigured task upon a given data set. Should block the thread until the task
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
            while (activeThreads > 0) {
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
