package attilathehun.songbook;

import attilathehun.songbook.plugin.DynamicSonglist;
import attilathehun.songbook.plugin.Frontpage;

public class Main {

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\src\\main\\resources\\log4j2.yaml");
        DynamicSonglist.getInstance();
        Frontpage.getInstance();
        SongbookApplication.main(args);
    }
}
