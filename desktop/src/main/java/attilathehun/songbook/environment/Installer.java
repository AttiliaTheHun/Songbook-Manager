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
    private static final String REMOTE_RESOURCES_ZIP_FILE = "http://hrabozpevnik.clanweb.eu/resources.zip";
    private static final String REMOTE_SCRIPTS_ZIP_FILE = "";
    private static final String RESOURCES_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/resources.zip").toString();
    private static final String SCRIPTS_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/scripts.zip").toString();
    private static boolean IS_TEMP_RESOURCES_ZIP_FILE = false;
    private static boolean IS_TEMP_SCRIPTS_ZIP_FILE = false;

    public static void runDiagnostics() {
        logger.info("Running installer diagnostics...");
        Installer installer = new Installer();
        EnvironmentVerificator verificator = new EnvironmentVerificator();
        if (!verificator.verifyResources()) {
            installer.installResources();
        }
        if (!verificator.verifyScripts()) {
            installer.installScripts();
        }
        logger.info("Installer diagnostics finished");
    }

    private static long downloadRemoteFile(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }

    private void installResources() {
        logger.info("Installing resources....");
        try {
            if (!new File(RESOURCES_ZIP_FILE_PATH).exists()) {
                if (REMOTE_RESOURCES_ZIP_FILE.equals("")) {
                    new AlertDialog.Builder().setTitle("Installation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .setMessage("Please provide a 'resources.zip' file!").addOkButton().build().open();
                    System.exit(0);
                }
                IS_TEMP_RESOURCES_ZIP_FILE = true;
                downloadRemoteFile(REMOTE_RESOURCES_ZIP_FILE, "resources.zip");
            }
            ZipBuilder.extract(RESOURCES_ZIP_FILE_PATH, (String) Environment.getInstance().getDefaultSettings().get("RESOURCE_FILE_PATH"));
            if (IS_TEMP_RESOURCES_ZIP_FILE) {
                new File(RESOURCES_ZIP_FILE_PATH).delete();
            }
            logger.info("Finished installing resources");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Installation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Cannot install resources!").addOkButton().build().open();
            System.exit(0);
        }
    }

    private void installScripts() {
        logger.info("Installing scripts....");
        try {
            if (!new File(SCRIPTS_ZIP_FILE_PATH).exists()) {
                if (REMOTE_SCRIPTS_ZIP_FILE.equals("")) {
                    new AlertDialog.Builder().setTitle("Installation Error").setIcon(AlertDialog.Builder.Icon.WARNING)
                            .setMessage("Please provide a 'scripts.zip' file!").addOkButton().build().open();
                    return;
                }
                IS_TEMP_SCRIPTS_ZIP_FILE = true;
                downloadRemoteFile(REMOTE_SCRIPTS_ZIP_FILE, "scripts.zip");
            }
            if (IS_TEMP_SCRIPTS_ZIP_FILE) {
                ZipBuilder.extract(SCRIPTS_ZIP_FILE_PATH, (String) Environment.getInstance().getSettings().get("SCRIPTS_FILE_PATH"));
            }
            logger.info("Finished installing scripts");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Installation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Cannot install scripts!").addOkButton().build().open();
        }
    }
}
