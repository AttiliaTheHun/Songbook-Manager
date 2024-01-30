package attilathehun.songbook.plugin;

public class Frontpage extends Plugin {

    private static final Frontpage instance = new Frontpage();
    private PluginSettings settings = null;

    private Frontpage() {

    }

    public static Plugin getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return Frontpage.class.getSimpleName();
    }

    @Override
    public Object execute() {
        return null;
    }

    @Override
    public void register() {
        PluginManager.registerPlugin(this);
    }

    @Override
    public PluginSettings getDefaultSettings() {
        PluginSettings settings = new PluginSettings();
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

}

