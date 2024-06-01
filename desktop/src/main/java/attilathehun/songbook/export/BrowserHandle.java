package attilathehun.songbook.export;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.window.AlertDialog;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.prefs.Preferences;

// TODO maybe allow in settings to use playwright's browser directly in case local chrome/edge breaks?
@Deprecated
public class BrowserHandle {
    private static final Logger logger = LogManager.getLogger(BrowserHandle.class);
    public static final String OS_WINDOWS = "Windows";
    public static final String OS_LINUX = "Linux";
    private static final String DEFAULT_OPTION = "export.browser.default";
    private static final String OPTION_CHROME = "export.browser.chrome";
    private static final String OPTION_EDGE = "export.browser.edge";
    private static final String OPTION_CHROMIUM = "export.browser.chromium";
    private static final String OPTION_CHROMIUM_PLAYWRIGHT = "export.browser.chromium_playwright";
    private static final Preferences preferences = Preferences.userRoot().node(BrowserHandle.class.getName());
    private static Browser browserInstance;

    public static void init() {
        if ((Boolean) SettingsManager.getInstance().getValue("EXPORT_ENABLED")) {
            try {
                initializeDefaultBrowserPath();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                new AlertDialog.Builder().setTitle("Error").setMessage(e.getLocalizedMessage()).setIcon(AlertDialog.Builder.Icon.ERROR)
                            .addOkButton().setCancelable(false).build().open();
                Environment.getInstance().exit();
            }
            if ((Boolean) SettingsManager.getInstance().getValue("EXPORT_KEEP_BROWSER_INSTANCE")) {
                browserInstance = createBrowserInstance();
            }
        }
    }


    /**
     * Attempts to find one of the supported browser and if successful, saves it as the default option. Otherwise, asks the user whether to install a browser.
     *
     * @throws IOException when one of the path resolutions fails really miserably
     */
    private static void initializeDefaultBrowserPath() throws IOException, InterruptedException {
        final String browser = preferences.get(DEFAULT_OPTION, null);

        if (browser != null) {
            logger.info("default export browser value " + browser);
            switch (browser) {
                case OPTION_CHROME -> {
                    if (new ChromePathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                    logger.info("default export browser removed");
                }
                case OPTION_EDGE -> {
                    if (new EdgePathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                    logger.info("default export browser removed");
                }
                case OPTION_CHROMIUM -> {
                    if (new ChromiumPathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                    logger.info("default export browser removed");
                }
                case OPTION_CHROMIUM_PLAYWRIGHT -> {
                    if (new PlaywrightChromiumPathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                    logger.info("default export browser removed");
                }
            }
        }

        if (new ChromePathResolver().resolve() != null) {
            preferences.put(DEFAULT_OPTION, OPTION_CHROME);
            logger.info("default export browser set to " + OPTION_CHROME);
            return;
        }
        if (new EdgePathResolver().resolve() != null) {
            preferences.put(DEFAULT_OPTION, OPTION_EDGE);
            logger.info("default export browser set to " + OPTION_EDGE);
            return;
        }
        if (new ChromiumPathResolver().resolve() != null) {
            preferences.put(DEFAULT_OPTION, OPTION_CHROMIUM);
            logger.info("default export browser set to " + OPTION_CHROMIUM);
            return;
        }

        if (new PlaywrightChromiumPathResolver().resolve() != null) {
            preferences.put(DEFAULT_OPTION, OPTION_CHROMIUM_PLAYWRIGHT);
            logger.info("default export browser set to " + OPTION_CHROMIUM_PLAYWRIGHT);
            return;
        }
        // no browser seems to be installed, given we've got this far
        requestBrowserInstall();

    }

    public static Browser createBrowserInstance() {
        final HashMap<String, String> playwrightEnv = new HashMap<>();
        playwrightEnv.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
        try (final Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(playwrightEnv))) {
            final BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
            if (!OPTION_CHROMIUM_PLAYWRIGHT.equals(preferences.get(DEFAULT_OPTION, null))) {
                options.setExecutablePath(Path.of(preferences.get(DEFAULT_OPTION + ".path", null)));
            }
            final Browser browser = playwright.chromium().launch(options);
            return browser;

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public static Browser getPreviewBrowserInstance() {
        return (browserInstance == null) ? createBrowserInstance() : browserInstance;
    }

    public static Page.PdfOptions getPrintOptions() {
        return new Page.PdfOptions().setDisplayHeaderFooter(false).setLandscape(true)
                .setMargin(new Margin().setRight("0").setTop("0").setBottom("0").setLeft("0")).setPrintBackground(false).setFormat("A4");
    }

    private static void requestBrowserInstall() {
        final AlertDialog dialog = new AlertDialog.Builder().setTitle("Install browser").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .setMessage("Exporting is done through a headless chromium-based web browser. Since none has been found on your computer, it is necessary to install it to make the exporting work. This may take a few minutes and around 200MB of disk space. Do you wish to proceed?")
                    .setCancelable(false).addOkButton("Install").addCloseButton("Disable exporting").build();
        dialog.awaitResult().thenAccept(result -> {
            if (result == AlertDialog.RESULT_OK) {
                // TODO install chromium somehow
            } else {
                SettingsManager.getInstance().set("EXPORT_ENABLED", false);
                throw new RuntimeException("browser installation refused");
            }
        });

    }

    public static void close() {
        if (browserInstance != null) {
            browserInstance.close();
        }
    }

    /**
     * Determines whether the host operating system is Windows or something else (most probably Linux of some kind).
     *
     * @return {@link BrowserHandle#OS_WINDOWS} if Windows-based, {@link BrowserHandle#OS_LINUX} otherwise
     */
    public static String getOS() {
        final String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return OS_WINDOWS;
        }
        return OS_LINUX;
    }

}
