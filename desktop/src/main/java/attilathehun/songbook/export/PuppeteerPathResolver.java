package attilathehun.songbook.export;

import java.io.IOException;

public class PuppeteerPathResolver extends BrowserPathResolver {
    // it is possible that the user already has puppeteer installed so no reason to do it twice
    @Override
    public String resolve() throws IOException {
        return null;
    }
}
