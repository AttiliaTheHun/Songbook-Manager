package attilathehun.songbook;

import attilathehun.songbook.plugin.PluginManager;

public class Main {

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\src\\main\\resources\\log4j2.yaml");
        PluginManager.loadPlugins();
        SongbookApplication.main(args);
    }
}
