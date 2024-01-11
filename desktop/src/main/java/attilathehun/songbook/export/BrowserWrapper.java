package attilathehun.songbook.export;

import java.io.IOException;
import java.util.prefs.Preferences;

abstract class BrowserWrapper {
    protected final String[] DEFAULT_ARGS = {"--headless"};
    protected final String PRINT_OPTION_TEMPLATE = "--print-to-pdf=\"%s\"";

    private static final String DEFAULT_OPTION = "export.browser.default";
    private static final String OPTION_CHROME = "export.browser.chrome";
    private static final String OPTION_EDGE = "export.browser.edge";
    private static final String OPTION_CHROMIUM = "export.browser.chromium";
    private static final String OPTION_PUPPETEER = "export.browser.puppeteer";

    private static final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());

    public abstract void print(String inputPath, String outputPath);

    static {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void init() throws IOException {
        String browser = preferences.get(DEFAULT_OPTION, null);
        String path;

        if (browser != null) {
            switch (browser) {
                case OPTION_CHROME -> {
                    if (new ChromePathResolver().resolve() != null) {
                        return;
                    }
                    preferences.remove(DEFAULT_OPTION);
                }
            }
        } else {
            if (new ChromePathResolver().resolve() != null) {
                preferences.put(DEFAULT_OPTION, OPTION_CHROME);
                return;
            }
        }
    }


    public static BrowserWrapper getInstance() {
        String browser = preferences.get(DEFAULT_OPTION, "");
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
}
