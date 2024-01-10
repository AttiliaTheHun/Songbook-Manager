package attilathehun.songbook.vcs;

import attilathehun.annotation.TODO;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.Misc;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private String outputFilePath = null;
    private boolean disassemblySuccessful = false;
    private LoadIndex loadIndex = null;

    /**
     * Create a request.zip file to the temp folder. The file contains all songbook files that changes have been made to,
     * the songbook changelog and the save request index.
     * @param index the save request index
     * @param collections collections whose .json files should be sent along the request
     * @return this
     * @throws IOException
     */
    public RequestFileAssembler assembleSaveFile(SaveIndex index, List<String> collections) throws IOException {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        outputFilePath = Environment.getInstance().settings.vcs.REQUEST_ZIP_TEMP_FILE_PATH;
        try (ZipBuilder builder = new ZipBuilder()
                .setOutputPath(outputFilePath)) {

            for (Object s : (Collection) index.getAdditions().get(StandardCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/html");
            }
            for (Object s : (Collection) index.getAdditions().get(EasterCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/egg");
            }

            for (Object s : (Collection) index.getChanges().get(StandardCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/html");
            }
            for (Object s : (Collection) index.getChanges().get(EasterCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), (String) s).toUri()), "/data/songs/egg");
            }
            for (String collection : collections) {
                builder.addFile(new File(Environment.getInstance().settings.collections.get(collection).getCollectionFilePath()), "/data");
            }

            builder.addFile(new File(Environment.getInstance().settings.vcs.CHANGE_LOG_FILE_PATH), "");
            File saveIndexTemp = new File(Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH, "index.json").toString());
            Misc.saveObjectToFileInJSON(index, saveIndexTemp);
            builder.addFile(saveIndexTemp, "");
        }
        return this;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public static RequestFileAssembler disassemble(String filePath) throws IOException {
        RequestFileAssembler assembler = new RequestFileAssembler();
        if (filePath == null || filePath.length() == 0) {
            throw new IllegalArgumentException("Invalid response file path for disassembly!");
        }
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("Could not find the response file");
        }
        String responseFilesPath = ZipBuilder.extract(filePath);
        File indexFile = new File(Paths.get(responseFilesPath, "index,json").toString());
        if (!indexFile.exists()) {
            throw new FileNotFoundException("Could not find the response load index file");
        }
        String json = String.join("", Files.readAllLines(indexFile.toPath()));
        Type targetClassType = new TypeToken<LoadIndex>() {
        }.getType();
        LoadIndex index = new Gson().fromJson(json, targetClassType);


        assembler.loadIndex = index;
        assembler.disassemblySuccessful = true;
        return assembler;
    }

    public boolean success() {
        return disassemblySuccessful;
    }

    public LoadIndex index() {
        return loadIndex;
    }
}
