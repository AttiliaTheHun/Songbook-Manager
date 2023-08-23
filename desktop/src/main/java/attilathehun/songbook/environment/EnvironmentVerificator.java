package attilathehun.songbook.environment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Upon loading this class performs a verification of the environment, eventually notifying the user what is wrong.
 */
public class EnvironmentVerificator {
    private boolean automated = false;

    public EnvironmentVerificator() {

    }

    public EnvironmentVerificator(boolean automated) {
        this.automated = automated;

        verifyCollection();

        verifyData();

        verifyResources();

        verifyCSS();

        verifyTemplates();

        verifyScripts();

        verifyEaster();

        verifyTemp();

    }

    public static void automated() {
        new EnvironmentVerificator(true);
    }

    private static void verificationFail(String message) {
        Environment.showErrorMessage("Environment verification failed", message);
    }


    public boolean verifyEaster() {
        if (Environment.getInstance().settings.IS_IT_EASTER_ALREADY) {
            if (!(new File(Environment.getInstance().settings.EGG_DATA_FILE_PATH).exists() && new File(Environment.getInstance().settings.EGG_DATA_FILE_PATH).isDirectory())) {
                if (automated) {
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
                verificationFail("No song data folder found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyCollection() {
        if (!new File(Environment.getInstance().settings.COLLECTION_FILE_PATH).exists()) {
            if (automated) {
                verificationFail("No song collection found!");
            }
            return false;
        }
        return true;
    }

    public boolean verifyResources() {
        if (!(new File(Environment.getInstance().settings.RESOURCE_FILE_PATH).exists() && new File(Environment.getInstance().settings.RESOURCE_FILE_PATH).isDirectory())) {
            if (automated) {
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

            if(!(new File(Environment.getInstance().settings.TEMP_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMP_FILE_PATH).isDirectory())) {
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
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            verificationFail("Could not initialize temp folder!");
        }
    }

}
