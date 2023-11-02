package attilathehun.songbook.plugin;

public class Frontpage extends Plugin {

    private static final Frontpage instance = new Frontpage();

    private final String name = Frontpage.class.getSimpleName();

    private Frontpage() {
        PluginManager.registerPlugin(this);
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public int execute() {
        return 0;
    }

    @Override
    public PluginSettings getSettings() {
        PluginSettings settings = new PluginSettings();
        settings.put("enabled", Boolean.TRUE);
        return settings;
    }

    public static Plugin getInstance() {
        return instance;
    }

}

