package attilathehun.songbook.environment;

import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.window.AlertDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This class performs a verification check of the environment. Should the verification fail, the program will crash anyway, but as we know what is
 * wrong beforehand, we can notify the user.
 */
public class EnvironmentVerificator {
    private static final Logger logger = LogManager.getLogger(EnvironmentVerificator.class);

    private boolean automated = false;

    /**
     * The constructor to obtain an instance for customized verification. For a complete automatic verification use {@link #automated()}.
     */
    public EnvironmentVerificator() {

    }

    /**
     * Internal constructor that performs automatic verification upon instantiation.
     */
    private EnvironmentVerificator(boolean b) {
        this.automated = true;

        verifyResources();

        verifyTemplates();

        verifyCSS();

        verifyScripts();

        verifyTemp();

        verifyCollection();

        verifyData();

    }

    /**
     * Automatically checks the environment. In case of failure, an {@link AlertDialog} will be shown to the user.
     */
    public static void automated() {
        new EnvironmentVerificator(true);
        logger.info("Environment verification successful");
    }

    private static void verificationFail(final String message) {
        new AlertDialog.Builder().setTitle("Environment Verification Failed").setIcon(AlertDialog.Builder.Icon.ERROR)
                .setMessage(message).addOkButton().build().open();
        Environment.getInstance().exit();
        logger.error("Environment verification failed: %s", message);
    }

    public boolean verifyData() {
        if (!(new File(StandardCollectionManager.getInstance().getSongDataFilePath()).exists() && new File(StandardCollectionManager.getInstance().getSongDataFilePath()).isDirectory())) {
            if (automated) {
                verificationFail("No song data folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCollection() {
        if (!new File(StandardCollectionManager.getInstance().getCollectionFilePath()).exists()) {
            if (automated) {
                verificationFail("No song collection found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyResources() {
        if (!(new File((String) SettingsManager.getInstance().getValue("RESOURCES_FILE_PATH")).exists() && new File((String) SettingsManager.getInstance().getValue("RESOURCES_FILE_PATH")).isDirectory())) {
            if (automated) {
                verificationFail("No resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCSS() {
        if (!(new File((String) SettingsManager.getInstance().getValue("CSS_RESOURCES_FILE_PATH")).exists() && new File((String) SettingsManager.getInstance().getValue("CSS_RESOURCES_FILE_PATH")).isDirectory())) {
            verificationFail("No CSS resource folder found!");
            if (automated) {
                verificationFail("No CSS resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyTemplates() {
        if (!(new File((String) SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH")).exists() && new File((String) SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH")).isDirectory())) {
            if (automated) {
                verificationFail("No HTML template folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyScripts() {
        if (!(new File((String) SettingsManager.getInstance().getValue("SCRIPTS_FILE_PATH")).exists() && new File((String) SettingsManager.getInstance().getValue("SCRIPTS_FILE_PATH")).isDirectory())) {
            if (automated) {
                new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                        .setMessage("Scripts folder not found. Some features might not work!").addOkButton().build().open();
            }
            return false;
        }
        return true;
    }

    public void verifyTemp() {
        if (!(new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).exists() && new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).isDirectory())) {
            new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).mkdirs();
        } else {
            Environment.getInstance().refresh();
        }
    }

}
