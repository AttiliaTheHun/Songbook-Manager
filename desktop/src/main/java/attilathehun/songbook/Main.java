package attilathehun.songbook;

import attilathehun.songbook.export.ChromePathResolver;
import attilathehun.songbook.export.EdgePathResolver;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.window.SongbookApplication;

public class Main {

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\src\\main\\resources\\log4j2.yaml");
        try {
            System.out.println(new EdgePathResolver().resolve());
            throw new RuntimeException();
        } catch (Exception e) {
            System.exit(0);
        }


        PluginManager.loadPlugins();
        SongbookApplication.main(args);
    }
}
