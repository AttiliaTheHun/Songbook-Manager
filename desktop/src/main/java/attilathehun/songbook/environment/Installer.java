package attilathehun.songbook.environment;

import attilathehun.songbook.util.ZipGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Installer {
    private static boolean IS_TEMP_RESOURCES_ZIP_FILE = false;

    private static boolean IS_TEMP_SCRIPTS_ZIP_FILE = false;

    private static final String REMOTE_RESOURCES_ZIP_FILE = "http://hrabozpevnik.clanweb.eu/resources.zip";
    private static final String REMOTE_SCRIPTS_ZIP_FILE = "";

    private static final String RESOURCES_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/resources.zip").toString();
    private static final String SCRIPTS_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/scripts.zip").toString();

    public static void runDiagnostics() {
        Installer installer = new Installer();
        EnvironmentVerificator verificator = new EnvironmentVerificator();
        if (!verificator.verifyResources()) {
            installer.installResources();
        }
        if (!verificator.verifyScripts()) {
            installer.installScripts();
        }
    }

    private void installResources() {
        try {
            if (!new File(RESOURCES_ZIP_FILE_PATH).exists()) {
                if (REMOTE_RESOURCES_ZIP_FILE.equals("")) {
                    Environment.showErrorMessage("Installation Error", "Please provide a 'resources.zip' file!");
                }
                IS_TEMP_RESOURCES_ZIP_FILE = true;
                downloadRemoteFile(REMOTE_RESOURCES_ZIP_FILE, "resources.zip");
            }
            new ZipGenerator().extractZip(RESOURCES_ZIP_FILE_PATH, Environment.getInstance().settings.RESOURCE_FILE_PATH);
            new File(RESOURCES_ZIP_FILE_PATH).delete();
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("Installation Error", "Cannot install resources!");
        }
    }

    private void installScripts() {
        try {
            if (!new File(SCRIPTS_ZIP_FILE_PATH).exists()) {
                if (REMOTE_SCRIPTS_ZIP_FILE.equals("")) {
                    Environment.showWarningMessage("Installation Error", "Please provide a 'scripts.zip' file!");
                    return;
                }
                IS_TEMP_SCRIPTS_ZIP_FILE = true;
                downloadRemoteFile(REMOTE_SCRIPTS_ZIP_FILE, "scripts.zip");
            }
            new ZipGenerator().extractZip(SCRIPTS_ZIP_FILE_PATH, Environment.getInstance().settings.SCRIPTS_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Installation Error", "Cannot install scripts!");
        }
    }

    private  static long downloadRemoteFile(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }
}
