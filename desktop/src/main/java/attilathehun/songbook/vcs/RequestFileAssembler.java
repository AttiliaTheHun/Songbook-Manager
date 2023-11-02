package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.index.SaveIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private String outputFilePath = null;

    /**
     * Create a request.zip file to the temp folder. The file contains all songbook files that changes have been made to,
     * the songbook changelog and the save request index.
     * @param index the save request index
     * @return this
     * @throws IOException
     */
    public RequestFileAssembler assembleSaveFile(SaveIndex index, List<String> collections) throws IOException {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        outputFilePath = Environment.getInstance().settings.vcs.REQUEST_ZIP_TEMP_FILE_PATH;
        ZipBuilder builder = new ZipBuilder()
                .setOutputPath(outputFilePath);

        for (Object s : (Collection) index.getAdditions().get("standard")) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/html");
        }
        for (Object s : (Collection) index.getAdditions().get("easter")) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/egg");
        }

        for (Object s : (Collection) index.getChanges().get("standard")) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/html");
        }
        for (Object s : (Collection) index.getChanges().get("easter")) {
            builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/egg");
        }
        for (String collection : collections) {
            builder.addFile(new File(Environment.getInstance().settings.collections.get(collection).getCollectionFilePath()), "/data");
        }

        builder.addFile(new File(Environment.getInstance().settings.vcs.CHANGE_LOG_FILE_PATH), "/");
        builder.addFile(CacheManager.getInstance().getCachedIndexFile(), "/");
        builder.finish();
        return this;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}
