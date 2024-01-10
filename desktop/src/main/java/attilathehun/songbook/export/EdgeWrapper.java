package attilathehun.songbook.export;

public class EdgeWrapper extends BrowserWrapper {
    @Override
    public void print(String inputPath, String outputPath) {
        // msedge --headless --print-to-pdf="C:\Users\Jaroslav\Desktop\sb_test\out2.pdf" C:\Users\Jaroslav\Desktop\sb_test\segment1.html

    }
}
