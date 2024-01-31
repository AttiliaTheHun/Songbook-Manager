package attilathehun.songbook.environment;

import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.window.AlertDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This class performs a verification check of the environment, eventually notifying the user what is wrong.
 */
public class EnvironmentVerificator {

    private static final Logger logger = LogManager.getLogger(EnvironmentVerificator.class);

    public static boolean SUPPRESS_WARNINGS = false;

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
        new AlertDialog.Builder().setTitle("Environment Verification Failed").setIcon(AlertDialog.Builder.Icon.ERROR)
                .setMessage(message).addOkButton().build().open();
        Environment.getInstance().exit();
    }

    public boolean verifyData() {
        if (!(new File(StandardCollectionManager.getInstance().getSettings().getSongDataFilePath()).exists() && new File(StandardCollectionManager.getInstance().getSettings().getSongDataFilePath()).isDirectory())) {
            if (automated) {
                verificationFail("No song data folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCollection() {
        if (!new File(StandardCollectionManager.getInstance().getSettings().getCollectionFilePath()).exists()) {
            if (automated) {
                verificationFail("No song collection found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyResources() {
        if (!(new File((String) Environment.getInstance().getSettings().get("RESOURCES_FILE_PATH")).exists() && new File((String) Environment.getInstance().getSettings().get("RESOURCES_FILE_PATH")).isDirectory())) {
            if (automated) {
                verificationFail("No resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCSS() {
        if (!(new File((String) Environment.getInstance().getSettings().get("CSS_RESOURCES_FILE_PATH")).exists() && new File((String) Environment.getInstance().getSettings().get("CSS_RESOURCES_FILE_PATH")).isDirectory())) {
            verificationFail("No CSS resource folder found!");
            if (automated) {
                verificationFail("No CSS resource folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyTemplates() {
        if (!(new File((String) Environment.getInstance().getSettings().get("TEMPLATE_RESOURCES_FILE_PATH")).exists() && new File((String) Environment.getInstance().getSettings().get("TEMPLATE_RESOURCES_FILE_PATH")).isDirectory())) {
            if (automated) {
                verificationFail("No HTML template folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyScripts() {
        if (!(new File((String) Environment.getInstance().getSettings().get("SCRIPTS_FILE_PATH")).exists() && new File((String) Environment.getInstance().getSettings().get("SCRIPTS_FILE_PATH")).isDirectory())) {
            if (automated) {
                new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                        .setMessage("Scripts folder not found. Some features might not work!").addOkButton().build().open();
            }
            return false;
        }
        return true;
    }

    public void verifyTemp() {
        if (!(new File((String) Environment.getInstance().getSettings().get("TEMP_FILE_PATH")).exists() && new File((String) Environment.getInstance().getSettings().get("TEMP_FILE_PATH")).isDirectory())) {
            new File((String) Environment.getInstance().getSettings().get("TEMP_FILE_PATH")).mkdirs();
        } else {
            Environment.getInstance().refresh();
        }
    }

}
