package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

@Deprecated
public class Export extends Plugin {
    private static final Logger logger = LogManager.getLogger(Export.class);

    private static final String DEFAULT_EXPORT_NAME = "DefaultExport.pdf";
    private static final String SINGLEPAGE_EXPORT_NAME = "SinglepageExport.pdf";
    private static final String PRINTABLE_EXPORT_NAME = "PrintableExport.pdf";

    private static final Export instance = new Export();

    private final String name = Export.class.getSimpleName();

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
        switch ((PluginSettings.ConversionMethod)((PluginSettings) Environment.getInstance().settings.plugins.get(name)).get("conversionMethod")) {
            //TODO: ask for the remote conversion url the way you ask for token
            case REMOTE:
                break;
            case THIRD_PARTY_API:
                break;
            case LOCAL_HEADLESS:
                break;
            case LOCAL_DEFAULT:
            default:
        }
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
            put("conversionMethod", ConversionMethod.THIRD_PARTY_API);
            put("localConversionThreadCount", 20);
            put("conversionURL", "");
            put("defaultExportName", DEFAULT_EXPORT_NAME);
            put("defaultSinglepageName", SINGLEPAGE_EXPORT_NAME);
            put("defaultPrintableName", PRINTABLE_EXPORT_NAME);
        }


        @Deprecated
        public enum ConversionMethod implements Serializable {
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
