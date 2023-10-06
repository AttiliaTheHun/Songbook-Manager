package attilathehun.songbook.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.zip.*;
import java.io.*;

/**
 * Based on https://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
 * and https://www.baeldung.com/java-compress-and-uncompress
 * and https://www.codejava.net/java-se/file-io/zip-directories
 *
 * @author halex
 * @author AttilaTheHun
 */
public class ZipUtil {

    private static final Logger logger = LogManager.getLogger(ZipUtil.class);

    private static final int BUFFER_SIZE = 4096;

    private boolean INCLUDE_SOURCE_FOLDER;

    private String SOURCE_FOLDER_NAME;

    public ZipUtil() {
        this(true);
    }

    public ZipUtil(Boolean includeTargetFolder) {
        this.INCLUDE_SOURCE_FOLDER = includeTargetFolder;
    }

    public void createZip(String sourceFilePath, String targetFilePath) throws FileNotFoundException, IOException {
        File sourceFile = new File(sourceFilePath);

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFilePath));

        if (sourceFile.isDirectory()) {

            if (!INCLUDE_SOURCE_FOLDER) {
                SOURCE_FOLDER_NAME = sourceFile.getName();
                addFolderContentToZip(sourceFile, sourceFile.getName(), zos);
            } else {
                addFolderToZip(sourceFile, sourceFile.getName(), zos);
            }
            
        } else {
            addFileToZip(sourceFile, zos);
        }
        
        zos.flush();
        zos.close();

    }


    /**
     * Adds a directory to the current zip output stream
     *
     * @param folder       the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos          the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZip(file, parentFolder + "/" + file.getName(), zos);
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
    }

    /**
     * Adds a directory to the current zip output stream
     *
     * @param folder       the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos          the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void addFolderContentToZip(File folder, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZip(file, file.getName(), zos);
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
     * Adds a file to the current zip output stream
     * @param file the file to be added
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void addFileToZip(File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
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
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public void extractZip(String zipFilePath, String destDirectory) throws IOException {
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
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
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
