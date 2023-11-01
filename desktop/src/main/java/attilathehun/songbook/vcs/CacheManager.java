package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.vcs.index.Index;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class CacheManager {
    private static final Logger logger = LogManager.getLogger(CacheManager.class);

    private static final String CACHED_INDEX_FILE_PATH = Paths.get(Environment.getInstance().settings.vcs.VCS_CACHE_FILE_PATH, "index.json").toString();

    private static final CacheManager instance = new CacheManager();

    private CacheManager() {}

    public static CacheManager getInstance() {
        return instance;
    }

    /**
     * Clears Version Control System cache folder.
     */
    public void clearCache() {
        try {
            for (File f : new File(Environment.getInstance().settings.vcs.VCS_CACHE_FILE_PATH).listFiles()) {
                f.delete();
            }
        } catch (NullPointerException npe) {
            logger.error(npe.getMessage(), npe);
        }
    }

    /**
     * Get cached index or null if there is none.
     * @return cached Index or null
     */
    public Index getCachedIndex() {
        try {
            File file = new File(CACHED_INDEX_FILE_PATH);
            if (!file.exists()) {
                return null;
            }

            String json = String.join("", Files.readAllLines(file.toPath()));
            Type targetClassType = new TypeToken<Index>() {
            }.getType();
            Index index = new Gson().fromJson(json, targetClassType);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public File getCachedIndexFile() {
        return new File(CACHED_INDEX_FILE_PATH);
    }

    /**
     * Save an index to the vcs cache folder.
     * @param index the index to save
     */
    public void cacheIndex(Index index) {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(CACHED_INDEX_FILE_PATH, false);
            gson.toJson(index, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public long getCachedSongbookVersionTimestamp() {
        try {
            File file = getCachedSongbookVersionTimestampFile();
            if (!file.exists()) {
                cacheSongbookVersionTimestamp();
            }
            return Long.parseLong(Files.readAllLines(file.toPath()).get(0));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public File getCachedSongbookVersionTimestampFile() {
        return new File(Environment.getInstance().settings.vcs.VERSION_TIMESTAMP_FILE_PATH);
    }

    private void cacheSongbookVersionTimestamp(long versionTimestamp) {
        if (versionTimestamp < 0) {
            throw new IllegalArgumentException();
        }
        try {
            new File(Environment.getInstance().settings.vcs.VCS_CACHE_FILE_PATH).mkdir();
            PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.vcs.VERSION_TIMESTAMP_FILE_PATH), false));
            printWriter.write(String.valueOf(versionTimestamp));
            printWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Scans all the songbook files to find the newest modify date which becomes the new version timestamp.
     */
    public void cacheSongbookVersionTimestamp() throws IOException {
        long timestamp = -1;
        long tempStamp;

        for (File file : Stream.of(new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()).listFiles())
                .filter(file -> !file.isDirectory())
                .toList()) {
            tempStamp = Files.getLastModifiedTime(file.toPath()).toMillis();
            if (tempStamp > timestamp) {
                timestamp = tempStamp;
            }
        }

        for (File file : Stream.of(new File(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()).listFiles())
                .filter(file -> !file.isDirectory())
                .toList()) {
            tempStamp = Files.getLastModifiedTime(file.toPath()).toMillis();
            if (tempStamp > timestamp) {
                timestamp = tempStamp;
            }
        }

        for (File file : Stream.of(new File(Environment.getInstance().settings.environment.DATA_FILE_PATH).listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().endsWith(".json"))
                .toList()) {
            tempStamp = Files.getLastModifiedTime(file.toPath()).toMillis();
            if (tempStamp > timestamp) {
                timestamp = tempStamp;
            }
        }


        cacheSongbookVersionTimestamp(timestamp);
    }



}
