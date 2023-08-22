package attilathehun.songbook.environment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Upon loading this class performs a verification of the environment, eventually notifying the user what is wrong.
 */
public class EnvironmentVerificator {

    private EnvironmentVerificator() {

        if (!new File(Environment.getInstance().settings.COLLECTION_FILE_PATH).exists()) {
            verificationFail("No song collection found!");
        }
        if (!(new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH).exists() && new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH).isDirectory())) {
            verificationFail("No song data folder found!");
        }
        if (!(new File(Environment.getInstance().settings.CSS_RESOURCES_FILE_PATH).exists() && new File(Environment.getInstance().settings.CSS_RESOURCES_FILE_PATH).isDirectory())) {
            verificationFail("No CSS resource folder found!");
        }
        if (!(new File(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH).isDirectory())) {
            verificationFail("No HTML template folder found!");
        }
        if (!(new File(Environment.getInstance().settings.SCRIPTS_FILE_PATH).exists() && new File(Environment.getInstance().settings.SCRIPTS_FILE_PATH).isDirectory())) {
            Environment.showWarningMessage("Warning", "No scripts folder found. Some features might not work!");
        }
        if (Environment.getInstance().settings.IS_IT_EASTER_ALREADY) {
            verifyEaster();
        }
        try {
            verifyTemp();
        } catch (IOException e) {
            verificationFail("Could not initialize temp folder!");
        }

    }

    public static void verify() {
        new EnvironmentVerificator();
    }

    private static void verificationFail(String message) {
        Environment.showErrorMessage("Environment verification failed", message);
    }


    private void verifyEaster() {
        if(!(new File(Environment.getInstance().settings.EGG_DATA_FILE_PATH).exists() && new File(Environment.getInstance().settings.EGG_DATA_FILE_PATH).isDirectory())) {
            Environment.showMessage("Warning", "No easter egg folder found!");
        }
    }

    private void verifyTemp() throws IOException {
        if(!(new File(Environment.getInstance().settings.TEMP_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMP_FILE_PATH).isDirectory())) {
            new File(Environment.getInstance().settings.TEMP_FILE_PATH).mkdirs();
            PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH), false));
            printWriter.write(String.valueOf(Environment.Settings.SESSION_TIMESTAMP));
            printWriter.close();
        } else {
            if (new File(Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH).exists() && new File(Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH).length() != 0) {
                if (Long.parseLong(String.join("", Files.readAllLines(Paths.get(Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH)))) != Environment.Settings.SESSION_TIMESTAMP) {
                    for (File f : Objects.requireNonNull(new File(Environment.getInstance().settings.TEMP_FILE_PATH).listFiles())) {
                        f.delete();
                    }
                }
            }

            PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.TEMP_TIMESTAMP_FILE_PATH), false));
            printWriter.write(String.valueOf(Environment.Settings.SESSION_TIMESTAMP));
            printWriter.close();

        }
    }

}
