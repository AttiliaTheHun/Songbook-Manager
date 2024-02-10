package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.misc.Misc;
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
import java.util.Collection;
import java.util.List;

public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private String outputFilePath = null;
    private boolean disassemblySuccessful = false;
    private LoadIndex loadIndex = null;

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
        File indexFile = new File(Paths.get(responseFilesPath, "index.json").toString());
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

    /**
     * Create a request.zip file to the temp folder. The file contains all songbook files that changes have been made to,
     * the songbook changelog and the save request index.
     *
     * @param index       the save request index
     * @param collections collections whose .json files should be sent along the request
     * @return this
     * @throws IOException
     */
    public RequestFileAssembler assembleSaveFile(SaveIndex index, List<String> collections) throws IOException {
        if (index == null) {
            throw new IllegalArgumentException();
        }
        outputFilePath = (String) VCSAdmin.getInstance().getSettings().get("REQUEST_ZIP_TEMP_FILE_PATH");
        try (ZipBuilder builder = new ZipBuilder()
                .setOutputPath(outputFilePath)) {

            for (Object s : (Collection) index.getAdditions().get(StandardCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(StandardCollectionManager.getInstance().getSettings().getSongDataFilePath(), (String) s).toUri()), StandardCollectionManager.getInstance().getSettings().getRelativeFilePath());
            }
            for (Object s : (Collection) index.getAdditions().get(EasterCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(EasterCollectionManager.getInstance().getSettings().getSongDataFilePath(), (String) s).toUri()), EasterCollectionManager.getInstance().getSettings().getRelativeFilePath());
            }

            for (Object s : (Collection) index.getChanges().get(StandardCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(StandardCollectionManager.getInstance().getSettings().getSongDataFilePath(), (String) s).toUri()), StandardCollectionManager.getInstance().getSettings().getRelativeFilePath());
            }
            for (Object s : (Collection) index.getChanges().get(EasterCollectionManager.getInstance().getCollectionName())) {
                builder.addFile(new File(Paths.get(EasterCollectionManager.getInstance().getSettings().getSongDataFilePath(), (String) s).toUri()), EasterCollectionManager.getInstance().getSettings().getRelativeFilePath());
            }
            for (String collection : collections) {
                builder.addFile(new File(Environment.getInstance().getRegisteredManagers().get(collection).getSettings().getCollectionFilePath()), "");
            }

            builder.addFile(new File((String) VCSAdmin.getInstance().getSettings().get("CHANGE_LOG_FILE_PATH")), "");
            File saveIndexTemp = new File(Paths.get((String) Environment.getInstance().getSettings().get("TEMP_FILE_PATH"), "index.json").toString());
            Misc.saveObjectToFileInJSON(index, saveIndexTemp);
            builder.addFile(saveIndexTemp, "");
        }
        return this;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public boolean success() {
        return disassemblySuccessful;
    }

    public LoadIndex index() {
        return loadIndex;
    }
}
