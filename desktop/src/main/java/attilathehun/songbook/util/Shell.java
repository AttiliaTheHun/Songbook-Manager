package attilathehun.songbook.util;

import java.awt.*;
import java.util.Scanner;

import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.window.SongbookApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Shell {
    private static final Logger logger = LogManager.getLogger(Shell.class);

    public static void main(final String[] args) {
        System.out.println("SongbookManager recovery shell");
        System.out.println("Check the documentation for help");
        final Scanner sc = new Scanner(System.in);
        final Shell shell = new Shell();
        String input;
        String[] cmd;
        while ((input = sc.nextLine()) != null) {
            if (input.trim().isEmpty()) {
                continue;
            }
            cmd = input.split(" ");
            try {
                switch (cmd[0]) {
                    case "exit" -> System.exit(0);
                    case "backup" -> shell.backup(cmd);
                    case "restore" -> shell.restore(cmd);
                    case "clear" -> shell.clear(cmd);
                    case "wipe", "destroy" -> shell.wipe(cmd);
                    case "launch" -> shell.launch(cmd);
                }
            } catch(final Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage(), e);
            }

        }
    }

    public static void start() {
        new Thread(() -> Shell.main(new String[]{})).start();
    }

    public void backup(final String[] cmd) {
        // allow to specify filename
    }

    public void restore(final String[] cmd) {
        // allow to specify filename
    }

    public void clear(final String[] cmd) {
        if (cmd.length == 1) {
            System.out.println("additional arguments (1) required");
            return;
        }


    }

    public void wipe(final String[] cmd) {
        if (cmd.length == 1) {
            System.out.println("additional arguments (1) required");
            return;
        }
        switch(cmd[1]) {
            case "all", "clean" -> wipeClean();
            case "settings" -> wipeSettings();
            case "resources" -> wipeResources();
            default -> wipeCollection(cmd[1]);
        }
    }

    private void wipeClean() {
        // delete all but .zip files and the .exe/.jar file from the work dir
    }

    private void wipeSettings() {

    }

    private void wipeResources() {

    }

    private void wipeCollection(final String collectionName) {

    }

    public void launch(final String[] cmd) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("GUI not supported");
        } else {
            SongbookApplication.main(cmd);
        }
    }

}
