package attilathehun.songbook.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A utility class for creation and extraction of ZIP archive files.
 */
public class ZipBuilder implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(ZipBuilder.class);
    /**
     * When true, the zip file will contain a folder of the same name
     * as the target folder and all the content will be in this folder. When false, the target folder content
     * will be put directly into the zip file root.
     */
    private static boolean INCLUDE_SOURCE_FOLDER = true;
    private String zipFilePath = "";
    private static final int BUFFER_SIZE = 4096;
    private String sourceFolderName;
    private ZipOutputStream zos;
    private boolean initialized = false;

    /**
     * A no context constructor. Will not throw any exceptions but requires initialisation in the form of {@link #setOutputPath(String)} method before the Builder can be used.
     */
    public ZipBuilder() {

    }

    /**
     *Automatically initialised constructor.
     * @param outputPath
     * @throws FileNotFoundException
     */
    public ZipBuilder(String outputPath) throws FileNotFoundException {
        init(outputPath);
    }

    /**
     * Constructor that allows simple zip file creation by chaining with the {@link #close()} method. Behavior of this constructor can be changed by the {@link #INCLUDE_SOURCE_FOLDER} flag.
     * @param target the folder/file to be zipped
     * @param outputPath path of the output archive
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ZipBuilder(File target, String outputPath) throws FileNotFoundException, IOException {
        init(outputPath);
        if (target == null) {
            throw new IllegalArgumentException("target file must not be null");
        }

        if (target.isDirectory()) {
            if (!INCLUDE_SOURCE_FOLDER) {
                sourceFolderName = target.getName();
                addFolderContentToArchive(target, target.getName());
            } else {
                addFolderToArchive(target, target.getName());
            }
        } else {
            addFileToArchive(target);
        }
    }

    /**
     * Initialises the builder by creating the output stream. Every functional method relies on this stream and will throw {@link IllegalStateException} if the stream is null.
     * @param outputPath
     * @throws FileNotFoundException
     */
    private void init(String outputPath) throws FileNotFoundException {
        if (initialized) {
            return;
        }
        if (zipFilePath == null || zipFilePath.length() == 0) {
            if (outputPath == null || outputPath.length() == 0) {
                throw new IllegalStateException("Output path not specified!");
            }
            zipFilePath = outputPath;
        }
        zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
        initialized = true;
    }

    /**
     * Change the {@link #INCLUDE_SOURCE_FOLDER} flag.
     * @param value desired value of the flag
     */
    public static void includeSourceFolder(boolean value) {
        INCLUDE_SOURCE_FOLDER = value;
    }

    /**
     * Returns the current value of the flag {@link #INCLUDE_SOURCE_FOLDER}.
     * @return flag state
     */
    public static boolean includesSourceFolder() {
        return INCLUDE_SOURCE_FOLDER;
    }

    /**
     * Set path for the output ZIP file.
     * @param targetPath zip file path
     * @return this
     */
    public ZipBuilder setOutputPath(String targetPath) throws FileNotFoundException {
        init(targetPath);
        return this;
    }

    public String getOutputPath() {
        return zipFilePath;
    }

    /**
     * Adds a file to the root of the ZIP file.
     * @param file the file to be added
     * @return this
     * @throws IOException
     */
    public ZipBuilder addFile(File file) throws IOException {
        init(null);
        return addFileToArchive(file);
    }

    /**
     * Adds a file to the specified path within the ZIP file.
     * @param file the file to be added
     * @return this
     * @throws IOException
     */
    public ZipBuilder addFile(File file, String parentFolder) throws IOException {
        init(null);
        return addFileToArchive(file, parentFolder);
    }

    /**
     * Adds a file to the root of the ZIP file.
     * @param file the file to be added
     * @throws FileNotFoundException
     * @throws IOException
     */
    private ZipBuilder addFileToArchive(File file)
            throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException();
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (file.isDirectory()) {
            addFolder(file, sourceFolderName);
            return this;
        }
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
        return this;
    }

    /**
     * Adds a file to the specified path within the ZIP file.
     * @param file the file to be added
     * @throws FileNotFoundException
     * @throws IOException
     */
    private ZipBuilder addFileToArchive(File file, String parentFolder)
            throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException();
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (file.isDirectory()) {
            addFolder(file, sourceFolderName);
            return this;
        }

        if (parentFolder == null || parentFolder.length() == 0) {
            return addFile(file);
        }
        if (parentFolder.startsWith("/")) {
            parentFolder = parentFolder.substring(1);
        }
        if (parentFolder.endsWith("/")) {
            parentFolder = parentFolder.substring(0, parentFolder.length() - 1);
        }
        zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
        return this;
    }

    /**
     * Adds a folder to the root of the ZIP file. The folder content will be inside a folder of the same name as source.
     * @param folder the folder to add
     * @return this
     * @throws IOException
     */
    public ZipBuilder addFolder(File folder) throws IOException {
        return addFolder(folder, folder.getName());
    }

    /**
     * Adds a folder to the ZIP file. The folder content will be inside written at the specified path within the file.
     * @param folder the folder to add
     * @return this
     * @throws IOException
     */
    public ZipBuilder addFolder(File folder, String parentFolder) throws IOException {
        init(null);
        return addFolderToArchive(folder, parentFolder);
    }

    /**
     * Adds a directory to the ZIP file.
     * @param folder       the directory to be added
     * @param parentFolder the path of parent directory
     * @throws FileNotFoundException
     * @throws IOException
     */
    private ZipBuilder addFolderToArchive(File folder, String parentFolder) throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException("The builder is not initialised");
        }
        if (folder == null || parentFolder == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        if (parentFolder.startsWith("/")) {
            parentFolder = parentFolder.substring(1);
        }
        if (parentFolder.endsWith("/")) {
            parentFolder = parentFolder.substring(0, parentFolder.length() - 1);
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolder(file, parentFolder + "/" + file.getName());
                continue;
            }
            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            zos.closeEntry();
        }
        return this;
    }

    /**
     * Adds content of the folder to the root of the ZIP file.
     * @param folder the folder whose content is added
     * @return this
     * @throws IOException
     */
    public ZipBuilder addFolderContent(File folder) throws IOException {
        return addFolderContent(folder, "");
    }

    /**
     * Adds a folder to the ZIP file. The content of the folder will be written at the specified path within the file. Does the same thing as {@link #addFolder(File, String)}, but uses
     * different internal methods.
     * @param folder the folder to be added
     * @param parentFolder path within the ZIP file
     * @return this
     * @throws IOException
     */
    public ZipBuilder addFolderContent(File folder, String parentFolder) throws IOException {
        init(null);
        return addFolderContentToArchive(folder, parentFolder);
    }


    /**
     * Adds the content of a directory to the ZIP file under a specific parent directory already present in the zip file.
     *
     * @param folder       the directory to be added from
     * @param parentFolder the path of parent directory
     * @throws FileNotFoundException
     * @throws IOException
     */
    private ZipBuilder addFolderContentToArchive(File folder, String parentFolder) throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException("The builder is not initialised");
        }
        if (folder == null || parentFolder == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        if (parentFolder.startsWith("/")) {
            parentFolder = parentFolder.substring(1);
        }
        if (parentFolder.endsWith("/")) {
            parentFolder = parentFolder.substring(0, parentFolder.length() - 1);
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolder(file, file.getName());
                continue;
            }
            if (parentFolder.equals(sourceFolderName) || parentFolder.length() == 0) {
                zos.putNextEntry(new ZipEntry(file.getName()));
            } else {
                zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            }

            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            zos.closeEntry();
        }
        return this;
    }


    /**
     * Finalize the build by closing the streams.
     * @throws IOException in the case of I/O error
     */
    @Override
    public void close() throws IOException {
        if (zos != null) {
            zos.flush();
            zos.close();
        }
    }

    /**
     * No idea why I thought this would be useful. Its functionality is now replaced by {@link #ZipBuilder(String)}.
     * @param path arbitrary String object
     * @return null
     */
    @Deprecated
    public static ZipBuilder fromPath(String path) {
        return null;
    }

    public static void extract(String zipFilePath) throws IOException {
        String dirName;
        if (zipFilePath.endsWith(".zip")) {
            dirName = zipFilePath.substring(0, zipFilePath.lastIndexOf(".zip"));
        } else {
            dirName = zipFilePath;
        }
        extract(zipFilePath, dirName);
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if it does not exist)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void extract(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (single file) from the zip file.
     * @param zipIn target zip input stream
     * @param filePath where to extract the file to
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        new File(filePath).getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath, false));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

}
