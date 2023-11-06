package attilathehun.songbook.environment;

import attilathehun.songbook.collection.StandardCollectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class performs a verification check of the environment, eventually notifying the user what is wrong.
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

    }

    public static void automated() {
        new EnvironmentVerificator(true);
        logger.info("Automated environment verification successful");
    }

    private static void verificationFail(String message) {
        Environment.showErrorMessage("Environment verification failed", message, FATAL_FAIL);
    }

    public boolean verifyData() {
        if (!(new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()).exists() && new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()).isDirectory())) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No song data folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCollection() {
        if (!new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getCollectionFilePath()).exists()) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No song collection found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyResources() {
        if (!(new File(Environment.getInstance().settings.environment.RESOURCE_FILE_PATH).exists() && new File(Environment.getInstance().settings.environment.RESOURCE_FILE_PATH).isDirectory())) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCSS() {
        if (!(new File(Environment.getInstance().settings.environment.CSS_RESOURCES_FILE_PATH).exists() && new File(Environment.getInstance().settings.environment.CSS_RESOURCES_FILE_PATH).isDirectory())) {
            verificationFail("No CSS resource folder found!");
            if (automated) {
                verificationFail("No CSS resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyTemplates() {
        if (!(new File(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH).exists() && new File(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH).isDirectory())) {
            if (automated) {
                FATAL_FAIL = true;
                verificationFail("No HTML template folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyScripts() {
        if (!(new File(Environment.getInstance().settings.environment.SCRIPTS_FILE_PATH).exists() && new File(Environment.getInstance().settings.environment.SCRIPTS_FILE_PATH).isDirectory())) {
            if (automated) {
                Environment.showWarningMessage("Warning", "No scripts folder found. Some features might not work!");
            }
            return false;
        }
        return true;
    }

    public void verifyTemp() {

        if (!(new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH).exists() && new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH).isDirectory())) {
            new File(Environment.getInstance().settings.environment.TEMP_FILE_PATH).mkdirs();
        } else {
                Environment.getInstance().refresh();
        }
    }

}
