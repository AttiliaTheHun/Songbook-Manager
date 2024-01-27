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

        try {
            String inputPath = "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\temp\\current_page.html";
            String outputPath = "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\temp\\test.pdf";
            BrowserWrapper wrapper = new EdgeWrapper();
            wrapper.print(inputPath, outputPath);
            wrapper.close();
            throw new RuntimeException();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }


        PluginManager.loadPlugins();
        SongbookApplication.main(args);
    }
}
