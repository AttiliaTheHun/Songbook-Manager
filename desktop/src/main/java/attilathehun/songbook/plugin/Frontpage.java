package attilathehun.songbook.plugin;


import java.io.Serializable;

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

    @Override
    public PluginSettings getSettings() {
        return new PluginSettings();
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static class PluginSettings extends Plugin.PluginSettings {

        protected PluginSettings() {
            put("enabled", Boolean.TRUE);
        }


    }

}

