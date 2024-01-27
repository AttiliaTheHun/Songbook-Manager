package attilathehun.songbook.export;

import java.io.IOException;

public abstract class BrowserPathResolver {

    /**
     * Searches for the browser's executable, hopefully returning its full path. The method should also save the path
     * within the {@link BrowserWrapper} preferences, so it can simply be looked up the next time. The save pattern is
     * supposed to follow this format `export.browser.$browsername.path`.
     * @return path to browser executable's parent or null
     * @throws IOException yeah, this happens
     */
    public abstract String resolve() throws IOException, InterruptedException;
}
