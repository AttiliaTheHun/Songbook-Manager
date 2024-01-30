package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.HTMLGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DynamicSonglist extends Plugin {

    //TODO move MAX_SONG_PER_COLUMN to settings?
    public static final int MAX_SONG_PER_COLUMN = 38;
    public static final int MAX_SONG_PER_PAGE = 2 * MAX_SONG_PER_COLUMN;
    private static final Logger logger = LogManager.getLogger(DynamicSonglist.class);
    private static final DynamicSonglist instance = new DynamicSonglist();

    private PluginSettings settings = null;

    private DynamicSonglist() {
        PluginManager.registerPlugin(this);
    }

    public static Plugin getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return DynamicSonglist.class.getSimpleName();
    }

    @Override
    public Object execute() {
        return generateSonglist();
    }

    @Override
    public void register() {

    }

    @Override
    public Plugin.PluginSettings getDefaultSettings() {
        Plugin.PluginSettings settings = new Plugin.PluginSettings();
        settings.put("enabled", Boolean.TRUE);
        return settings;
    }

    @Override
    public PluginSettings getSettings() {
        return (settings == null) ? getDefaultSettings() : settings;
    }

    @Override
    public void setSettings(final PluginSettings p) {
        if (p == null) {
            throw new IllegalArgumentException("plugin settings cannot be null");
        }
        settings = p;
    }

    private int generateSonglist() {
        boolean isLastPageSingleColumn = false;
        int songlistParts = Environment.getInstance().getCollectionManager().getDisplayCollection().size() / MAX_SONG_PER_PAGE;
        if (Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONG_PER_PAGE != 0) {
            songlistParts += 1;
            isLastPageSingleColumn = Environment.getInstance().getCollectionManager().getDisplayCollection().size() % MAX_SONG_PER_PAGE <= MAX_SONG_PER_COLUMN;
        }

        HTMLGenerator generator = new HTMLGenerator();
        if (songlistParts == 0) {
            generator.generateSonglistSegmentFile(0, 0, 0);
            return 1;
        }

        int startIndex = 0;
        int endIndex = (songlistParts == 1 && MAX_SONG_PER_PAGE > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) ? Environment.getInstance().getCollectionManager().getDisplayCollection().size() : MAX_SONG_PER_PAGE;

        for (int i = 0; i < songlistParts; i++) {
            generator.generateSonglistSegmentFile(startIndex, endIndex, i);
            startIndex += MAX_SONG_PER_PAGE;
            endIndex = Math.min(endIndex + MAX_SONG_PER_PAGE, Environment.getInstance().getCollectionManager().getDisplayCollection().size());
        }
        logger.debug("Dynamic songlist generated");
        return songlistParts;
    }

}
