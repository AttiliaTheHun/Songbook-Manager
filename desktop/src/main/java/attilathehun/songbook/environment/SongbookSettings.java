package attilathehun.songbook.environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

public final class SongbookSettings extends HashMap<String, Object> implements Serializable {
    private static final Logger logger = LogManager.getLogger(SongbookSettings.class);

    //TODO: make another title-author display options
}