package attilathehun.songbook.misc;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.window.AlertDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(target);
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

    public static Set<Class> findAllClassesUsingClassLoader(final String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private static Class getClass(final String className, final String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }

}
