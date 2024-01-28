package attilathehun.songbook.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Export extends Plugin {
    private static final Logger logger = LogManager.getLogger(Export.class);

    private static final String DEFAULT_EXPORT_NAME = "DefaultExport.pdf";
    private static final String SINGLEPAGE_EXPORT_NAME = "SinglepageExport.pdf";
    private static final String PRINTABLE_EXPORT_NAME = "PrintableExport.pdf";

    private static final Export instance = new Export();

    private Export() {
        PluginManager.registerPlugin(this);
    }

    public static Plugin getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return Export.class.getSimpleName();
    }

    @Override
    public int execute() {

        return 0;
    }

    @Override
    public PluginSettings defaultSettings() {
        PluginSettings settings = new PluginSettings();
        settings.put("enabled", Boolean.TRUE);
        settings.put("conversionThreadCount", 20d);
        settings.put("defaultExportName", DEFAULT_EXPORT_NAME);
        settings.put("singlepageExportName", SINGLEPAGE_EXPORT_NAME);
        settings.put("printableExportName", PRINTABLE_EXPORT_NAME);
        return settings;
    }

}
