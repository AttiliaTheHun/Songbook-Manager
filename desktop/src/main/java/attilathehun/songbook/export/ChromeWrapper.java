package attilathehun.songbook.export;

public class ChromeWrapper extends BrowserWrapper {
    @Override
    public void print(String inputPath, String outputPath) {
        // chrome --headless --print-to-pdf="C:\Users\Jaroslav\Desktop\sb_test\out.pdf" C:\Users\Jaroslav\Desktop\sb_test\segment0.html

    }
}
