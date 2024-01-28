package attilathehun.songbook.export;

import java.io.IOException;

public abstract class BrowserPathResolver {
    public static final String SHELL_LOCATION_WINDOWS = "C:/Windows/System32/cmd.exe";
    public static final String SHELL_DELIMETER_WINDOWS = ">";
    public static final String SHELL_LOCATION_LINUX = "/bin/bash";
    public static final String SHELL_DELIMETER_LINUX = "$"; // if this thing runs as su, we better deploy some ransomware

    /**
     * Searches for the browser's executable, hopefully returning its full path. The method should also save the path
     * within the {@link BrowserWrapper} preferences, so it can simply be looked up the next time. The save pattern is
     * supposed to follow this format `export.browser.$browsername.path`.
     *
     * @return path to browser executable's parent or null
     * @throws IOException yeah, this happens
     */
    public abstract String resolve() throws IOException, InterruptedException;
}
