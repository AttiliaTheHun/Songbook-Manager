package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.Misc;
import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class is used for assembly and disassembly of request files that are sent or received from the remote server.
 */
public class RequestFileAssembler {
    private static final Logger logger = LogManager.getLogger(RequestFileAssembler.class);
    private static final String REQUEST_ZIP_TEMP_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH"), "save_request.zip").toString();
    private String outputFilePath = null;

    /**
     * Verifies a load request file and if accepted, applies the changes from this request to the local songbook. It is necessary to call {@link CollectionManager#init()} on every
     * affected collection manager before any local changes are made to the songbook, otherwise the data that was just loaded will be discarded when the manager calls {@link CollectionManager#save()}.
     *
     * @param filePath path to the request zip file
     * @param index the load index of the request
     * @return return true if successful, false otherwise
     * @throws IOException
     */
    public static boolean disassemble(final String filePath, final LoadIndex index) throws IOException {
        logger.debug("disassembly file path: " + filePath);

        if (filePath == null || filePath.length() == 0) {
            throw new IllegalArgumentException("Invalid response file path for disassembly!");
        }
        final File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("Could not find the response file");
        }
        try (final ZipFile zipFile = new ZipFile(filePath)) {
            if (!verifyFilesReceived(zipFile, index)) {
                throw new RuntimeException("the load request zip file does not comply to the load request index");
            }
            if (!verifyCollectionIntegrity(zipFile, index)) {
                throw new RuntimeException("the loaded collection files are corrupted");
            }

            unloadLoadedFiles(zipFile, index);
        }
        
        return true;
    }

    /**
     * Verifies that all the files that were requested in the load index are present in the zip file.
     *
     * @param zipFile the zip load request zip file to be disassembled
     * @param loadIndex the load request index that was used to fetch the zip file
     * @return true if all files ar present, false otherwise
     */
    private static boolean verifyFilesReceived(final ZipFile zipFile, final LoadIndex loadIndex) {
        for (final CollectionManager manager : Environment.getInstance().getRegisteredManagers().values()) {
            if (loadIndex.getMissing().get(manager.getCollectionName()) == null) { // this field should exist even if none songs are missing from this collection
                continue;
            }
            for (final String file : (Collection<String>) loadIndex.getMissing().get(manager.getCollectionName())){
                if (zipFile.getEntry(Paths.get(manager.getRelativeFilePath(), file).toString().replace("\\", "/")) == null) { // replace because of Windows
                    System.out.println(file);
                    System.out.println(manager.getRelativeFilePath().replace("\\", "/") + file);
                    return false;
                }
            }
            for (final String file : (Collection<String>) loadIndex.getOutdated().get(manager.getCollectionName())){
                if (zipFile.getEntry(Paths.get(manager.getRelativeFilePath(), file).toString().replace("\\", "/")) == null) {
                    System.out.println(file);
                    return false;
                }
            }
            if (loadIndex.getCollections().contains(manager.getCollectionName()) && zipFile.getEntry(new File(manager.getCollectionFilePath()).getName()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Attempts to deserialize collection json files in the load request.
     *
     * @param zipFile the load request zip file
     * @param loadIndex the load request load index
     * @return true if the collections are valid, false otherwise
     * @throws IOException
     */
    private static boolean verifyCollectionIntegrity(final ZipFile zipFile, final LoadIndex loadIndex) throws IOException {
        for (final String collection : loadIndex.getCollections()) {
            final ZipEntry entry = zipFile.getEntry(new File(Environment.getInstance().getRegisteredManagers().get(collection).getCollectionFilePath()).getName());
            try (final InputStream inputStream = zipFile.getInputStream(entry)) {
                new Gson().fromJson(new InputStreamReader(inputStream), new TypeToken<ArrayList<Song>>(){});
            } catch (final JsonSyntaxException j) {
                return false;
            }
        }
        return true;
    }

    /**
     * Incorporates the files from the zip file to the local songbook, eventually overwriting any files necessary. The new files are then marked as if modified on the date of the
     * remote version timestamp.
     *
     * @param zipFile the laod request zip file
     * @param loadIndex the load request load index
     * @throws IOException
     */
    private static void unloadLoadedFiles(final ZipFile zipFile, final LoadIndex loadIndex) throws IOException {
        ZipEntry entry;
        for (final CollectionManager manager : Environment.getInstance().getRegisteredManagers().values()) {
            if (loadIndex.getMissing().get(manager.getCollectionName()) == null) {
                continue;
            }
            for (final String file : (Collection<String>) loadIndex.getMissing().get(manager.getCollectionName())){
                entry = zipFile.getEntry(Paths.get(manager.getRelativeFilePath(), file).toString().replace("\\", "/"));
                final Path path = Paths.get(manager.getSongDataFilePath(), file);
                try (final InputStream inputStream = zipFile.getInputStream(entry)) {
                    Files.copy(inputStream, path);
                }
                if (!Misc.setLastModifiedDate(path.toString(), CacheManager.getInstance().getCachedSongbookVersionTimestamp())) {
                    new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setMessage("File lastModifiedDate can not be modified. The Version Control System is not guaranteed to work correctly from now on.")
                            .addOkButton().setParent(SongbookApplication.getMainWindow()).build().open();
                }
            }
            for (final String file : (Collection<String>) loadIndex.getOutdated().get(manager.getCollectionName())){
                entry = zipFile.getEntry(Paths.get(manager.getRelativeFilePath(), file).toString().replace("\\", "/"));
                final Path path = Paths.get(manager.getSongDataFilePath(), file);
                try (final InputStream inputStream = zipFile.getInputStream(entry)) {
                    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
                }
                if (!Misc.setLastModifiedDate(path.toString(), CacheManager.getInstance().getCachedSongbookVersionTimestamp())) {
                    new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setMessage("File lastModifiedDate can not be modified. The Version Control System is not guaranteed to work correctly from now on.")
                            .addOkButton().setParent(SongbookApplication.getMainWindow()).build().open();
                }
            }
            if (loadIndex.getCollections().contains(manager.getCollectionName())) {
                entry = zipFile.getEntry(new File(manager.getCollectionFilePath()).getName());
                try (final InputStream inputStream = zipFile.getInputStream(entry)) {
                    Files.copy(inputStream, Path.of(manager.getCollectionFilePath()), StandardCopyOption.REPLACE_EXISTING);
                }
                if (!Misc.setLastModifiedDate(manager.getCollectionFilePath(), CacheManager.getInstance().getCachedSongbookVersionTimestamp())) {
                    new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setMessage("File lastModifiedDate can not be modified. The Version Control System is not guaranteed to work correctly from now on.")
                            .addOkButton().setParent(SongbookApplication.getMainWindow()).build().open();
                }
            }
        }
    }

    /**
     * Creates a request file to the {@link #REQUEST_ZIP_TEMP_FILE_PATH} location. The file contains all songbook files that changes have been made to,
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
    

}
