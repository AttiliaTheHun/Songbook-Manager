package attilathehun.songbook;

import attilathehun.songbook.util.LoggerOutputStream;
import attilathehun.songbook.util.Shell;
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
            Shell.main(args);
        }
        if (args.length > 0 && args[0].equals("--headless")) {
            Shell.main(args);
        }
        try {
            SongbookApplication.main(args);
        } catch (final Exception e) {
          e.printStackTrace();
          Shell.main(args);
        }

    }
}
