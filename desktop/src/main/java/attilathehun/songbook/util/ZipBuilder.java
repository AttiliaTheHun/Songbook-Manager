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
public class ZipBuilder {
    private static final Logger logger = LogManager.getLogger(ZipBuilder.class);

    private String zipFilePath = "";
    private static final int BUFFER_SIZE = 4096;
    private boolean INCLUDE_SOURCE_FOLDER = true;
    private String SOURCE_FOLDER_NAME;

    private ZipOutputStream zos;


    public ZipBuilder() {

    }

    /**
     * Standard constructor.
     * @param targetPath the folder to be zipped
     * @param outputPath path of the output archive
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ZipBuilder(String targetPath, String outputPath) throws FileNotFoundException, IOException {
        this.zipFilePath = outputPath;
        File sourceFile = new File(targetPath);
        zos = new ZipOutputStream(new FileOutputStream(outputPath));
        if (sourceFile.isDirectory()) {

            if (!INCLUDE_SOURCE_FOLDER) {
                SOURCE_FOLDER_NAME = sourceFile.getName();
                addFolderContent(sourceFile, sourceFile.getName());
            } else {
                addFolder(sourceFile, sourceFile.getName());
            }

        } else {
            addFile(sourceFile);
        }
    }

    /**
     * Change the INCLUDE_SOURCE_FOLDER flag. When true, the zip file will contain a folder of the same name
     * as the target folder and all the content will be in this folder. When false, the target folder content
     * will be put directly into the zip file root.
     * @param value desired value of the flag
     */
    public void setIncludeSourceFolder(boolean value) {
        INCLUDE_SOURCE_FOLDER = value;
    }

    /**
     * Set path for the output ZIP file.
     * @param targetPath zip file path
     * @return this
     */
    public ZipBuilder setOutputPath(String targetPath) throws FileNotFoundException {
        if (targetPath == null || targetPath.equals("")) {
            throw new IllegalArgumentException();
        }
        this.zipFilePath = targetPath;
        zos = new ZipOutputStream(new FileOutputStream(targetPath));
        return this;
    }

    public String getOutputPath() {
        return zipFilePath;
    }

    /**
     * Adds a file to the root of the ZIP file.
     * @param file the file to be added
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ZipBuilder addFile(File file)
            throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException();
        }
        if (file.isDirectory()) {
            addFolder(file, SOURCE_FOLDER_NAME);
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
     * Adds a file to the ZIP file.
     * @param file the file to be added
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ZipBuilder addFile(File file, String parentFolder)
            throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException();
        }
        if (file.isDirectory()) {
            addFolder(file, SOURCE_FOLDER_NAME);
            return this;
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
     * Adds a directory to the ZIP file.
     *
     * @param folder       the directory to be added
     * @param parentFolder the path of parent directory
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ZipBuilder addFolder(File folder, String parentFolder) throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException();
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
     * Adds the content of a directory to the ZIP file.
     *
     * @param folder       the directory to be added from
     * @param parentFolder the path of parent directory
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void addFolderContent(File folder, String parentFolder) throws FileNotFoundException, IOException {
        if (zos == null) {
            throw  new IllegalStateException();
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolder(file, file.getName());
                continue;
            }
            if (parentFolder.equals(SOURCE_FOLDER_NAME)) {
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
    }


    /**
     * Finalize the build and close the streams.
     * @return this
     * @throws IOException
     */
    public ZipBuilder finish() throws IOException {
        if (zipFilePath == null || zipFilePath.equals("")) {
            throw new IllegalStateException();
        }
        if (zos != null) {
            zos.flush();
            zos.close();
        }
        return this;
    }

    public static ZipBuilder fromPath(String path) {
        return null;
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
     * @throws IOException the thingy you gotta catch
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        new File(filePath).getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }


}
