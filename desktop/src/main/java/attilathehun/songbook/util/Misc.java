package attilathehun.songbook.util;

import attilathehun.songbook.window.AlertDialog;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Random;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class Misc {
    private static final Logger logger = LogManager.getLogger(Misc.class);

    public static String toTitleCase(final String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static int indexOf(final String[] array, final String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean fileExists(final String path) {
        return new File(path).exists();
    }

    public static void saveObjectToFileInJSON(final Serializable s, final File target) {
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final FileWriter writer = new FileWriter(target);
            gson.toJson(s, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("Cannot save object to file: %s. File: %s", e.getLocalizedMessage(), target.toPath())).addOkButton().build().open();
        }
    }

    public static <T> T loadObjectFromFileInJSON(final TypeToken<T> targetType, final File target) {
        try {
            System.out.println(targetType.getClass().getSimpleName());
            String json = String.join("", Files.readAllLines(target.toPath()));

            Type targetClassType = targetType.getType();
            T result = new Gson().fromJson(json, targetClassType);

            return result;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("Cannot load object from file: %s. File: %s", e.getLocalizedMessage(), target.toPath())).addOkButton().build().open();
        }
        return null;
    }

    public static void randomException() {
        final int random = new Random().nextInt(10);
        switch (random) {
            case 0 -> throw new IllegalArgumentException();
            case 1 -> throw new NullPointerException();
            case 2 -> throw new IllegalStateException();
            case 3 -> throw new IndexOutOfBoundsException();
            case 4 -> throw new ClassCastException();
            case 5 -> throw new UnsupportedOperationException();
            case 6 -> throw new LayerInstantiationException();
            case 7 -> throw new ArithmeticException();
            case 8 -> throw new IllegalCallerException();
            case 9 -> throw new SecurityException();
        }
    }

    public static boolean setLastModifiedDate(final String filePath, long timestamp) {
        final File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        try {
            final BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(filePath), BasicFileAttributeView.class);
            final FileTime time = FileTime.from(timestamp, TimeUnit.SECONDS);
            attributes.setTimes(time, time, time);
            if (file.lastModified() == timestamp) {
                return true;
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * A very memory-inefficient way to format a JSON string as pretty printing.
     *
     * @param jsonString the json string
     * @return the formatted json string
     */
    public static String formatJSON(final String jsonString) {
        if (jsonString == null || jsonString.length() == 0) {
            throw new IllegalArgumentException("the string must not be empty");
        }
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (jsonString.startsWith("[") && jsonString.endsWith("]")) { // arrays need special treatment
            final JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
            return gson.toJson(jsonArray);
        }
        final JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
        return gson.toJson(jsonObject);
    }

}
