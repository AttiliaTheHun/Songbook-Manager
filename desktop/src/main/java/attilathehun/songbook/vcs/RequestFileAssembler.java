package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private String outputFilePath = null;

    public RequestFileAssembler assembleSaveFile(SaveIndex index) throws IOException {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        outputFilePath = Environment.getInstance().settings.vcs.REQUEST_ZIP_FILE_PATH;
        ZipBuilder builder = new ZipBuilder()
                .setOutputPath(outputFilePath);

        for (Object s : (Collection) index.getAdditions().get("standard").getContent()) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.environment.SONG_DATA_FILE_PATH, (String) s).toUri()), "/data/songs/html");
        }
        for (Object s : (Collection) index.getAdditions().get("easter").getContent()) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.environment.EGG_DATA_FILE_PATH, (String) s).toUri()), "/data/songs/egg");
        }

        for (Object s : (Collection) index.getChanges().get("standard").getContent()) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.environment.SONG_DATA_FILE_PATH, (String) s).toUri()), "/data/songs/html");
        }
        for (Object s : (Collection) index.getChanges().get("easter").getContent()) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.environment.EGG_DATA_FILE_PATH, (String) s).toUri()), "/data/songs/egg");
        }
        builder.addFile(new File(Environment.getInstance().settings.environment.COLLECTION_FILE_PATH), "/data");
        builder.addFile(new File(Environment.getInstance().settings.environment.EASTER_COLLECTION_FILE_PATH), "/data");
        builder.addFile(new File(Environment.getInstance().settings.vcs.CHANGE_LOG_FILE_PATH), "/");
        builder.addFile(CacheManager.getInstance().getCachedIndexFile(), "/");
        return this;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}
