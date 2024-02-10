package attilathehun.songbook.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.Preferences;

public abstract class BrowserWrapper implements AutoCloseable {
    public static final String OS_WINDOWS = "Windows";
    public static final String OS_LINUX = "Linux";
    private static final Logger logger = LogManager.getLogger(BrowserWrapper.class);
    private static final String DEFAULT_OPTION = "export.browser.default";
    private static final String OPTION_CHROME = "export.browser.chrome";
    private static final String OPTION_EDGE = "export.browser.edge";
    private static final String OPTION_CHROMIUM = "export.browser.chromium";
    private static final String OPTION_PUPPETEER = "export.browser.puppeteer";
    private static final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());

    static {
        try {
            // TODO only if export enabled
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final String[] DEFAULT_PRINT_ARGS = {"--headless", "--print-to-pdf=\"%s\"", "--scale=\"0.8\"", "--no-pdf-header-footer", "%s"};

    /**
     * Attempts to find one of the supported browser and if successful, saves it as the default option.
     *
     * @throws IOException when one of the path resolutions fails really miserably
     */
    private static void init() throws IOException, InterruptedException {
        String browser = preferences.get(DEFAULT_OPTION, null);

        if (browser != null) {
            logger.info("Init: default export browser value " + browser);
            switch (browser) {
                case OPTION_CHROME -> {
                    if (new ChromePathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                }
                case OPTION_EDGE -> {
                    if (new EdgePathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                }
                case OPTION_CHROMIUM -> {
                    if (new ChromiumPathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                }
                case OPTION_PUPPETEER -> {
                    if (new PuppeteerPathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                }
            }
        } else {
            if (new ChromePathResolver().resolve() != null) {
                preferences.put(DEFAULT_OPTION, OPTION_CHROME);
                logger.info("init: default export browser set to " + OPTION_CHROME);
                return;
            }
            if (new EdgePathResolver().resolve() != null) {
                preferences.put(DEFAULT_OPTION, OPTION_EDGE);
                logger.info("init: default export browser set to " + OPTION_EDGE);
                return;
            }
            if (new ChromiumPathResolver().resolve() != null) {
                preferences.put(DEFAULT_OPTION, OPTION_CHROMIUM);
                logger.info("init: default export browser set to " + OPTION_CHROMIUM);
                return;
            }
            if (new PuppeteerPathResolver().resolve() != null) {
                preferences.put(DEFAULT_OPTION, OPTION_PUPPETEER);
                logger.info("init: default export browser set to " + OPTION_PUPPETEER);
                return;
            }
        }
    }

    /**
     * Returns an instance of the {@link BrowserWrapper} subclass corresponding to the default browser for exporting. The default browser
     * is determined automatically by the {@link BrowserWrapper#init()} method. You can change it by locating the Java preferences API storage
     * and manually changing the value.
     *
     * @return a {@link BrowserWrapper} subclass or null
     */
    public static BrowserWrapper getInstance() throws IOException {
        final String browser = preferences.get(DEFAULT_OPTION, "");
        switch (browser) {
            case OPTION_CHROME -> {
                return new ChromeWrapper();
            }
            case OPTION_EDGE -> {
                return new EdgeWrapper();
            }
            case OPTION_CHROMIUM -> {
                return new ChromiumWrapper();
            }
            case OPTION_PUPPETEER -> {
                return new PuppeteerWrapper();
            }
        }
        return null;
    }

    /**
     * Determines whether the host operating system is Windows or something else (most probably Linux of some kind).
     *
     * @return {@link BrowserWrapper#OS_WINDOWS} if Windows-based, {@link BrowserWrapper#OS_LINUX} otherwise
     */
    public static String getOS() {
        final String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return OS_WINDOWS;
        }
        return OS_LINUX;
    }

    /**
     * Converts target file to PDF using the corresponding browser.
     *
     * @param inputPath  full path to target HTML file
     * @param outputPath full path to the output PDF file
     */
    public abstract void print(String inputPath, String outputPath) throws IOException;

    /**
     * Frees up the resources in use by this particular wrapper. Using the wrapper after calling {@link BrowserWrapper#close()} will throw exceptions.
     */
    @Override
    public abstract void close() throws IOException;
}
