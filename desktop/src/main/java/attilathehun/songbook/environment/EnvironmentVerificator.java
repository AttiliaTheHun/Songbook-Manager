package attilathehun.songbook.environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Upon loading this class performs a verification of the environment, eventually notifying the user what is wrong.
 */
public class EnvironmentVerificator {

    private static final Logger logger = LogManager.getLogger(EnvironmentVerificator.class);

    public static boolean SUPPRESS_WARNINGS = false;

    private static boolean FATAL_FAIL = false;

    private boolean automated = false;

    public EnvironmentVerificator() {

    }

    private EnvironmentVerificator(boolean automated) {
        this.automated = automated;

        verifyResources();

        verifyTemplates();

        verifyCSS();

        verifyScripts();

        verifyTemp();

        verifyCollection();

        verifyData();

        verifyEaster();

    }

    public static void automated() {
        new EnvironmentVerificator(true);
        logger.info("Automated environment verification successful");
    }

    private static void verificationFail(String message) {
        Environment.showErrorMessage("Environment verification failed", message, FATAL_FAIL);
    }


    public boolean verifyEaster() {
        if (Environment.getInstance().settings.IS_IT_EASTER_ALREADY) {
            if (!(new File(Environment.getInstance().settings.EGG_DATA_FILE_PATH).exists() && new File(Environment.getInstance().settings.EGG_DATA_FILE_PATH).isDirectory())) {
                if (automated && !SUPPRESS_WARNINGS) {
                    Environment.showMessage("Warning", "No easter egg folder found!");
                }
                return false;
            }
        }
        return true;
    }

    public boolean verifyData() {
        if (!(new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH).exists() && new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH).isDirectory())) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No song data folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCollection() {
        if (!new File(Environment.getInstance().settings.COLLECTION_FILE_PATH).exists()) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No song collection found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyResources() {
        if (!(new File(Environment.getInstance().settings.RESOURCE_FILE_PATH).exists() && new File(Environment.getInstance().settings.RESOURCE_FILE_PATH).isDirectory())) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCSS() {
        if (!(new File(Environment.getInstance().settings.CSS_RESOURCES_FILE_PATH).exists() && new File(Environment.getInstance().settings.CSS_RESOURCES_FILE_PATH).isDirectory())) {
            verificationFail("No CSS resource folder found!");
            if (automated) {
                verificationFail("No CSS resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyTemplates() {
        if (!(new File(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH).isDirectory())) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No HTML template folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyScripts() {
        if (!(new File(Environment.getInstance().settings.SCRIPTS_FILE_PATH).exists() && new File(Environment.getInstance().settings.SCRIPTS_FILE_PATH).isDirectory())) {
            if (automated) {
                Environment.showWarningMessage("Warning", "No scripts folder found. Some features might not work!");
            }
            return false;
        }
        return true;
    }

    public void verifyTemp() {
        try {

            if (!(new File(Environment.getInstance().settings.TEMP_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMP_FILE_PATH).isDirectory())) {
                new File(Environment.getInstance().settings.TEMP_FILE_PATH).mkdirs();
            } else {
                if (new File(Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH).length() != 0) {
                    if (Long.parseLong(String.join("", Files.readAllLines(Paths.get(Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH)))) != Environment.Settings.SESSION_TIMESTAMP) {
                        Environment.getInstance().refresh();
                    }
                } else {
                    Environment.getInstance().refresh();
                }

                PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH), false));
                printWriter.write(String.valueOf(Environment.Settings.SESSION_TIMESTAMP));
                printWriter.close();

            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            FATAL_FAIL = true;
            verificationFail("Could not initialize temp folder!");
        }
    }

}
