package attilathehun.songbook;

import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.window.SongbookApplication;

public class Main {

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", Main.class.getResource("log4j2.yaml").toString());
        /*PluginManager.loadPlugins();
        SongbookApplication.main(args);*/
    }
}
