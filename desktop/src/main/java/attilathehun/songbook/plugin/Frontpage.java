package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Environment;

public class Frontpage extends Plugin {

    private static final Frontpage instance = new Frontpage();

    private String name = Frontpage.class.getSimpleName();

    private Frontpage() {
        super();
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

    public PluginSettings getSettings() {
        return new Plugin.PluginSettings();
    }

    public static Plugin getInstance() {
        return instance;
    }
}
