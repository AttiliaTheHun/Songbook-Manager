package attilathehun.songbook.plugin;

import java.io.Serializable;

public class Export extends Plugin {

    private static final String DEFAULT_EXPORT_NAME = "ExportDefault.pdf";
    private static final String SINGLEPAGE_EXPORT_NAME = "ExportSinglepage.pdf";
    private static final String PRINTABLE_EXPORT_NAME = "ExportPrintable.pdf";

    private static final Export instance = new Export();

    private String name = Export.class.getSimpleName();

    private Export() {
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
        //public final boolean enabled;
        public final ConversionMethod conversionMethod;
        public final String conversionURL;
        public final String defaultExportName;
        public final String defaultSinglepageName;
        public final String defaultPrintableName;

        protected PluginSettings() {
            super();
           // enabled = true;
            conversionMethod = ConversionMethod.THIRD_PARTY_API;
            conversionURL = "";
            defaultExportName = DEFAULT_EXPORT_NAME;
            defaultSinglepageName = SINGLEPAGE_EXPORT_NAME;
            defaultPrintableName = PRINTABLE_EXPORT_NAME;
        }

        public static enum ConversionMethod implements Serializable {
            REMOTE {
                @Override
                public String toString() {
                    return "REMOTE";
                }
            },
            THIRD_PARTY_API {
                @Override
                public String toString() {
                    return "THIRD_PARTY_API";
                }
            },
            LOCAL_HEADLESS {
                @Override
                public String toString() {
                    return "LOCAL_HEADLESS";
                }
            },
            LOCAL_DEFAULT {
                @Override
                public String toString() {
                    return "LOCAL_DEFAULT";
                }
            }
        }
    }
}
