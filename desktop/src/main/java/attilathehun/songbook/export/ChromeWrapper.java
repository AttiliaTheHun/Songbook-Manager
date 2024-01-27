package attilathehun.songbook.export;

import java.util.prefs.Preferences;

public class ChromeWrapper extends BrowserWrapper {
    private static final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());
    private static final String CHROME_PARENT_PATH = preferences.get(ChromePathResolver.CHROME_PATH_VARIABLE, null);
    @Override
    public void print(String inputPath, String outputPath) {
        // chrome --headless --print-to-pdf="C:\Users\Jaroslav\Desktop\sb_test\out.pdf" C:\Users\Jaroslav\Desktop\sb_test\segment0.html

    }
}
