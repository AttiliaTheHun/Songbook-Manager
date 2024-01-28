package attilathehun.songbook.misc;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.window.AlertDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class Misc {
    private static final Logger logger = LogManager.getLogger(Misc.class);

    public static String toTitleCase(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static int indexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    public static void saveObjectToFileInJSON(Serializable s, File target) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(target);
            gson.toJson(s, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.WARNING)
                    .setMessage("Cannot save object to file!").addOkButton().build().open();
        }
    }

}
