package attilathehun.songbook.environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsManager {
    private static final Logger logger = LogManager.getLogger(SettingsManager.class);

    private static final SettingsManager INSTANCE = new SettingsManager();

    private SettingsManager() {}

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    public void save() {

    }

    public void load() {

    }
}
