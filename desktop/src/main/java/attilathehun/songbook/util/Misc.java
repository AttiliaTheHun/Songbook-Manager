package attilathehun.songbook.util;

public class Misc {

    public static String toTitleCase(String word){
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

}
