package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.Song;
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
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CacheManager {
    private static final Logger logger = LogManager.getLogger(CacheManager.class);

    private static final String CACHED_INDEX_FILE_PATH = Paths.get(Environment.getInstance().settings.vcs.VCS_CACHE_FILE_PATH, "index.json").toString();

    private static final CacheManager instance = new CacheManager();

    public static CacheManager getInstance() {
        return instance;
    }

    public void clearCache() {

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



}
