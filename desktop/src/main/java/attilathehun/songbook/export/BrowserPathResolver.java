package attilathehun.songbook.export;

import java.io.IOException;

public abstract class BrowserPathResolver {
    public abstract String resolve() throws IOException;
}
