package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
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
import java.util.Collection;
import java.util.List;

public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private static final String REQUEST_ZIP_TEMP_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH"), "save_request.zip").toString();
    private String outputFilePath = null;
    private boolean disassemblySuccessful = false;
    private LoadIndex loadIndex = null;

    public static RequestFileAssembler disassemble(final String filePath) throws IOException {
        final RequestFileAssembler assembler = new RequestFileAssembler();
        if (filePath == null || filePath.length() == 0) {
            throw new IllegalArgumentException("Invalid response file path for disassembly!");
        }
        final File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("Could not find the response file");
        }
        final String responseFilesPath = ZipBuilder.extract(filePath);
        final File indexFile = new File(Paths.get(responseFilesPath, "index.json").toString());
        if (!indexFile.exists()) {
            throw new FileNotFoundException("Could not find the response load index file");
        }
        final String json = String.join("", Files.readAllLines(indexFile.toPath()));
        final Type targetClassType = new TypeToken<LoadIndex>(){}.getType();

        assembler.loadIndex = new Gson().fromJson(json, targetClassType);
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
    public RequestFileAssembler assembleSaveFile(final SaveIndex index, final List<String> collections) throws IOException {
        if (index == null) {
            throw new IllegalArgumentException("index can not be null");
        }
        outputFilePath = REQUEST_ZIP_TEMP_FILE_PATH;
        try (final ZipBuilder builder = new ZipBuilder()
                .setOutputPath(outputFilePath)) {

            for (final CollectionManager manager : Environment.getInstance().getRegisteredManagers().values()) {
                for (final Object s : (Collection) index.getAdditions().get(manager.getCollectionName())) {
                    System.out.println(new File(Paths.get(manager.getSongDataFilePath(), (String) s).toString()));
                    System.out.println(manager.getRelativeFilePath());
                    builder.addFile(new File(Paths.get(manager.getSongDataFilePath(), (String) s).toString()), manager.getRelativeFilePath());
                }
                for (final Object s : (Collection) index.getChanges().get(manager.getCollectionName())) {
                    builder.addFile(new File(Paths.get(manager.getSongDataFilePath(), (String) s).toString()), manager.getRelativeFilePath());
                }
            }

            for (final String collection : collections) {
                builder.addFile(new File(Environment.getInstance().getRegisteredManagers().get(collection).getCollectionFilePath()), "");
            }

            builder.addFile(new File(VCSAdmin.CHANGE_LOG_FILE_PATH), "");
            final File saveIndexTemp = new File(Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH"), "index.json").toString());
            Misc.saveObjectToFileInJSON(index, saveIndexTemp);
            builder.addFile(saveIndexTemp, "");
        }

        return this;
    }

    /**
     * Returns the path to the file that was created through the {@link #assembleSaveFile(SaveIndex, List)} method. Returns null if no such file was created.
     *
     * @return path to the output file or null
     */
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
