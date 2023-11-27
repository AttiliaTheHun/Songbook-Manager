package attilathehun.songbook.plugin;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Songbook Manager Song Mark-up Language utility provider. Provides a simple API to convert
 * SMSML into HTML and vice versa utilising the Songbook Manager resource templates.
 */
public class SMSML extends Plugin {
    private static final SMSML instance = new SMSML();

    private final String name = SMSML.class.getSimpleName();

    private static final String[] languageTokens = {"#song", "#name", "#author", "#active", "#url", "#endheader", "#column", "#line", "#chords", "#lyrics"};
    private static final String[] classes = {"ultra-small-br", "very-small-br", "small-br", "smaller-br", "little-bigger-br", "bigger-br", "big-br", "very-big-br", "ultra-big-br"};
    private static final String[] specialTokens = {"#style"};
    private static final String[] types = {"div", "h1", "h4", "meta", "meta", "div", "pre", "span", "span", "span"};
    private static final String[][] defaultClasses = {{"song"}, {"song-title"}, {"song-author"}, null, null, {"song-text-wrapper", "short-song"}, {"song-text"}, {"line"}, {"chords"}, {"lyrics"}};
    private static final Pair<String, String>[][] defaultAttributes = new Pair[][]{null, null, null, new Pair[]{new Pair("name", "active"), new Pair("value", "true")}, new Pair[]{new Pair("name", "url"), new Pair("value", "")}, null, null, null, null, null};
    private static final HashMap<String, String> templates = new HashMap<>();
    private static final String STRING_PLACEHOLDER = "%s";

    private SMSML() {
        if (languageTokens.length == classes.length && classes.length == types.length && types.length == defaultClasses.length && defaultClasses.length == defaultAttributes.length) {
            initTemplateMap();
            PluginManager.registerPlugin(this);
        } else {
            throw new IllegalStateException("The SMSML plugin is broken!");
        }
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int execute() {
        return 0;
    }

    //TODO
    public String parseHTML(String input) {
        return null;
    }

    //TODO
    public String parseSMSML(String html) {
        if (html == null) {
            throw new IllegalArgumentException();
        }
        String[] lines = html.split("\n");
        String[][] tokens = new String[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            tokens[i] = lines[i].split(" ");
        }
        String output = "";
        for (String[] line : tokens) {

        }


        return null;
    }

    private void initTemplateMap() {
        templates.put("div", "<div%s>%s</div>\n");
        templates.put("h1", "<h1%s>%s</h1>\n");
        templates.put("h4", "<h4%s>%s</h4>\n");
        templates.put("meta", "<meta%s>\n");
        templates.put("pre", "<pre%s>%s</pre>\n");
        templates.put("span", "<span%s>%s</span>\n");
        templates.put("%class", "class=\"%s\"");
        templates.put("%attr", "%=\"%\"");
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
