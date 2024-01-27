package attilathehun.songbook;

import attilathehun.songbook.export.BrowserWrapper;
import attilathehun.songbook.export.ChromePathResolver;
import attilathehun.songbook.export.EdgePathResolver;
import attilathehun.songbook.export.EdgeWrapper;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.window.SongbookApplication;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\src\\main\\resources\\log4j2.yaml");

        PluginManager.loadPlugins();
        SongbookApplication.main(args);
    }
}
