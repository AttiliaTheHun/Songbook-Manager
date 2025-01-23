package attilathehun.songbook.util;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper around {@link Logger} that poses as {@link OutputStream}.
 */
public class LoggerOutputStream extends OutputStream {
    private final Logger logger;

    public LoggerOutputStream(final Logger logger) {
        this.logger = logger;
    }
    @Override
    public final void write(int b) throws IOException {
        logger.error((char) b);
    }

    @Override
    public final void write(@NotNull byte[] b, int off, int len) {
        logger.error(new String(b, off ,len));
    }
}
