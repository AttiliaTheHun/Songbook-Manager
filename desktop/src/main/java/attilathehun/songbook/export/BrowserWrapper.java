package attilathehun.songbook.export;

public abstract class BrowserWrapper {
    protected final String[] DEFAULT_ARGS = {"--headless"};
    protected final String PRINT_OPTION_TEMPLATE = "--print-to-pdf=\"%s\"";

    public abstract void print(String inputPath, String outputPath);
}
