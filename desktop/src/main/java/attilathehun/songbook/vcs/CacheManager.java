package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class CacheManager {
    private static final Logger logger = LogManager.getLogger(CacheManager.class);

    private static final String CACHED_INDEX_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"), "local", "index.json").toString();
    private static final String VERSION_TIMESTAMP_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"), "local", "version_timestamp.txt").toString();
    private static final String CACHED_REMOTE_INDEX_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"),  "index.json").toString();
    private static long localVersionTimestamp = -1;


    private static final CacheManager instance = new CacheManager();

    private CacheManager() {
    }

    public static CacheManager getInstance() {
        return instance;
    }

    /**
     * Clears Version Control System cache folder.
     */
    public void clearCache() {
        localVersionTimestamp = -1;
        clearDirectory(new File((String) SettingsManager.getInstance().getValue("VCS_CACHE_PATH")));
    }

    /**
     * Recursively deletes all files in a directory and subdirectories and then tries to delete these directories, too
     *
     * @param directory the directory to clean
     */
    private void clearDirectory(final File directory) {
        try {
            for (final File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    clearDirectory(f);
                    f.delete();
                } else {
                    f.delete();
                }
            }
        } catch (final NullPointerException npe) {
            logger.error(npe.getMessage(), npe);
        }
    }

    /**
     * Get cached index or null if there is none.
     *
     * @return cached Index or null
     */
    public Index getCachedIndex() {
        try {
            final File file = getCachedIndexFile();
            if (!file.exists()) {
                return null;
            }

            final String json = String.join("", Files.readAllLines(file.toPath()));
            final Type targetClassType = new TypeToken<Index>(){}.getType();
            return new Gson().fromJson(json, targetClassType);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public File getCachedIndexFile() {
        return new File(CACHED_INDEX_FILE_PATH);
    }

    public Index getCachedRemoteIndex() {
        try {
            final File file = getCachedRemoteIndexFile();
            if (!file.exists()) {
                return null;
            }

            final String json = String.join("", Files.readAllLines(file.toPath()));
            final Type targetClassType = new TypeToken<Index>(){}.getType();
            return new Gson().fromJson(json, targetClassType);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public File getCachedRemoteIndexFile() {
        return new File(CACHED_REMOTE_INDEX_FILE_PATH);
    }

    /**
     * Save an index to the vcs cache folder.
     *
     * @param index the index to save
     */
    void cacheIndex(final Index index) {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            new File(CACHED_INDEX_FILE_PATH).getParentFile().mkdirs();
            final FileWriter writer = new FileWriter(CACHED_INDEX_FILE_PATH, false);
            gson.toJson(index, writer);
            writer.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public long getCachedSongbookVersionTimestamp() {
        if (localVersionTimestamp != -1) {
            return  localVersionTimestamp;
        }
        try {
            final File file = getCachedSongbookVersionTimestampFile();
            if (!file.exists()) {
                cacheSongbookVersionTimestamp();
            }
            final long timestamp = Long.parseLong(Files.readAllLines(file.toPath()).get(0));
            localVersionTimestamp = timestamp;
            return timestamp;
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public File getCachedSongbookVersionTimestampFile() {
        return new File(VERSION_TIMESTAMP_FILE_PATH);
    }

    void cacheSongbookVersionTimestamp(final long versionTimestamp) {
        if (versionTimestamp < 0) {
            throw new IllegalArgumentException();
        }
        try {
            localVersionTimestamp = versionTimestamp;
            new File(VERSION_TIMESTAMP_FILE_PATH).getParentFile().mkdirs();
            final PrintWriter printWriter = new PrintWriter(new FileWriter(VERSION_TIMESTAMP_FILE_PATH, false));
            printWriter.write(String.valueOf(versionTimestamp));
            printWriter.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Scans all the songbook files to find the newest modify date which becomes the new version timestamp. This timestamp is then stored in a local file.
     */
    public void cacheSongbookVersionTimestamp() throws IOException {
        long timestamp = -1;
        long tempStamp;

        for (final CollectionManager collectionManager : Environment.getInstance().getRegisteredManagers().values()) {
            for (final File file : Stream.of(new File(collectionManager.getSongDataFilePath()).listFiles())
                    .filter(file -> !file.isDirectory())
                    .toList()) {
                tempStamp = Files.getLastModifiedTime(file.toPath()).to(TimeUnit.SECONDS);
                if (tempStamp > timestamp) {
                    timestamp = tempStamp;
                }
            }
        }

        for (final File file : Stream.of(new File((String) SettingsManager.getInstance().getValue("DATA_FILE_PATH")).listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().endsWith(".json"))
                .toList()) {
            tempStamp = Files.getLastModifiedTime(file.toPath()).to(TimeUnit.SECONDS);
            if (tempStamp > timestamp) {
                timestamp = tempStamp;
            }
        }

        cacheSongbookVersionTimestamp(timestamp);
    }


}
