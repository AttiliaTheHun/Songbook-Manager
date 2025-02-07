package attilathehun.songbook;

import attilathehun.songbook.util.LoggerOutputStream;
import attilathehun.songbook.window.SongbookApplication;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.io.PrintStream;

public class Main {

    static {
        System.setProperty("log4j2.configurationFile", Main.class.getResource("log4j2.yaml").toString());
        System.setErr(new PrintStream(new LoggerOutputStream(LogManager.getLogger(Main.class)))); // redirect standard error to log file
    }

    public static void main(final String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("GUI not supported");
            System.exit(0);
        }
        SongbookApplication.main(args);
    }
}
