package attilathehun.songbook.util;

import java.io.File;

public class Misc {

    public static String toTitleCase(String word){
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static int indexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return  i;
            }
        }
        return -1;
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

}
