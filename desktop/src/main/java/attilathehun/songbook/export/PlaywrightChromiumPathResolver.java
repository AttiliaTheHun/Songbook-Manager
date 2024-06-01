package attilathehun.songbook.export;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
public class PlaywrightChromiumPathResolver extends BrowserPathResolver {
    private static final Logger logger = LogManager.getLogger(PlaywrightChromiumPathResolver.class);
    public static final String PLAYWRIGHT_CHROMIUM_PATH_VARIABLE = "export.browser.chromium_playwright.path";

    private static final String FOLDER_NAME_LIKE = "chromium";

    private static final String PATH_WINDOWS = Paths.get(System.getenv("%USERPROFILE%"), "\\AppData\\Local\\ms-playwright").toString();
    private static final String PATH_MAC_OS = "~/Library/Caches/ms-playwright";
    private static final String PATH_LINUX = "~/.cache/ms-playwright";

    private final Preferences preferences = Preferences.userRoot().node(BrowserHandle.class.getName());


    @Override
    public String resolve() throws IOException, InterruptedException {
        String[] paths;
        if (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) {
            paths = new String[]{PATH_WINDOWS};
        } else {
            paths = new String[]{PATH_LINUX, PATH_MAC_OS};
        }
        for (final String path : paths) {

            try {
                for (final File f : new File(path).listFiles()) {
                    if (f.isDirectory() && f.getName().startsWith(FOLDER_NAME_LIKE) && f.listFiles().length != 0) {
                        save();
                        return "true";
                    }
                }
            } catch (final NullPointerException e) {
                logger.error(e.getMessage(), e);
            }


        }
        unsave();
        return null;
    }

    private void save() {
        preferences.put(PLAYWRIGHT_CHROMIUM_PATH_VARIABLE, "exists");
    }

    private void unsave() {
        preferences.remove(PLAYWRIGHT_CHROMIUM_PATH_VARIABLE);
    }
}
