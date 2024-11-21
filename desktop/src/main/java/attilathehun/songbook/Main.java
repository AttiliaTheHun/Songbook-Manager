package attilathehun.songbook;

import attilathehun.songbook.util.Shell;
import attilathehun.songbook.window.SongbookApplication;
import javafx.application.Application;

import java.awt.*;

public class Main {

    static {
        System.setProperty("log4j2.configurationFile", Main.class.getResource("log4j2.yaml").toString());
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
