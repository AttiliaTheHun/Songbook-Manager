package attilathehun.songbook.environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;

@Deprecated
public final class UserSettings extends HashMap<String, Object> implements Serializable {
    private static final Logger logger = LogManager.getLogger(UserSettings.class);


}
