package attilathehun.songbook.environment;

import attilathehun.songbook.util.ZipBuilder;
import attilathehun.songbook.window.AlertDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Installer {

    private static final Logger logger = LogManager.getLogger(Installer.class);
    private static final String REMOTE_RESOURCES_ZIP_FILE = "https://github.com/AttiliaTheHun/Songbook-Manager/releases/download/v0.0.3-alpha/resources.zip";
    private static final String REMOTE_SCRIPTS_ZIP_FILE = "";
    private static final String RESOURCES_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/resources.zip").toString();
    private static final String SCRIPTS_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/scripts.zip").toString();
    private static boolean IS_TEMP_RESOURCES_ZIP_FILE = false;
    private static boolean IS_TEMP_SCRIPTS_ZIP_FILE = false;

    /**
     * Checks the existence of resource and script folders and in the case of their absence attempts to download and extract them.
     */
    public static void runTasks() {
        final Installer installer = new Installer();
        final EnvironmentVerificator verificator = new EnvironmentVerificator();
        installer.initTemp();
        if (!verificator.verifyResources()) {
            installer.installResources();
        }/*
        if (!verificator.verifyScripts()) {
            installer.installScripts();
        }*/
        logger.debug("finished installer tasks");
    }

    /**
     * Attempts to install resources either from local or remote archive file.
     */
    private void installResources() {
        logger.info("installing resources...");
        try {
            if (!new File(RESOURCES_ZIP_FILE_PATH).exists()) {
                if (REMOTE_RESOURCES_ZIP_FILE.equals("")) {
                    throw new RuntimeException("Please provide a 'resources.zip' file!");
                }
                IS_TEMP_RESOURCES_ZIP_FILE = true;
                downloadRemoteFile(REMOTE_RESOURCES_ZIP_FILE, "resources.zip");
            }
            ZipBuilder.extract(RESOURCES_ZIP_FILE_PATH, SettingsManager.getInstance().getValue("RESOURCES_FILE_PATH"));
            if (IS_TEMP_RESOURCES_ZIP_FILE) {
                new File(RESOURCES_ZIP_FILE_PATH).delete();
            }
            logger.info("finished installing resources");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Cannot install resources");
        }
    }

    /**
     * Attempts to install scripts either from local or remote archive file.
     */
    private void installScripts() {
        logger.info("installing scripts...");
        try {
            if (!new File(SCRIPTS_ZIP_FILE_PATH).exists()) {
                if (REMOTE_SCRIPTS_ZIP_FILE.equals("")) {
                    //throw new RuntimeException("Installation error: Please provide a 'scripts.zip' file!");
                }
                IS_TEMP_SCRIPTS_ZIP_FILE = true;
                downloadRemoteFile(REMOTE_SCRIPTS_ZIP_FILE, "scripts.zip");
            }
            if (IS_TEMP_SCRIPTS_ZIP_FILE) {
                ZipBuilder.extract(SCRIPTS_ZIP_FILE_PATH, SettingsManager.getInstance().getValue("SCRIPTS_FILE_PATH"));
            }
            logger.info("finished installing scripts");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Cannot install scripts.");
        }
    }

    private void initTemp() {
        if (new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).exists() && !new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).isDirectory()) {
            if (!new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).delete()) {
                throw new IllegalStateException("cannot initialize temp folder: already exists as a file");
            }
        }
        new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).mkdirs();
    }

    /**
     * Downloads a file from remote location to the local disk.
     *
     * @param url full remote file URL
     * @param fileName target file path on the local machine
     * @return number of bytes downloaded
     * @throws IOException when things go wrong
     */
    private static long downloadRemoteFile(final String url, final String fileName) throws IOException {
        try (final InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }
}