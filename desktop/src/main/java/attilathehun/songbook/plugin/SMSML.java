package attilathehun.songbook.plugin;

import attilathehun.songbook.environment.Settings;
import attilathehun.songbook.util.Misc;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Songbook Manager Song Mark-up Language utility provider. Provides a simple API to convert
 * SMSML into HTML and vice versa utilising the Songbook Manager resource templates.
 */
public class SMSML extends Plugin {
    private static final Logger logger = LogManager.getLogger(SMSML.class);
    private static final SMSML instance = new SMSML();

    private final String name = SMSML.class.getSimpleName();
    protected static final String STRING_PLACEHOLDER = "%s";

    private SMSML() {
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

    @Override
    public PluginSettings getSettings() {
        PluginSettings settings = new PluginSettings();
        settings.put("enabled", Boolean.TRUE);
        settings.put("engine", new StreamLooper().getName());
        return settings;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static abstract class SMSMLEngine {
        private static final Logger logger = LogManager.getLogger(SMSMLEngine.class);
        protected static final String[] languageTokens = {"#song", "#name", "#author", "#active", "#url", "#endheader", "#column", "#line", "#chords", "#lyrics", "#emptyline"};
        protected static final String[] marginClasses = {"ultra-small-br", "very-small-br", "small-br", "smaller-br", "little-bigger-br", "bigger-br", "big-br", "very-big-br", "ultra-big-br"};
        protected static String[] fontSizes = {"default", "small", "very-small", "ultra-small"};
        protected static final String[] specialTokens = {"#style"};
        protected static final String[] types = {"div", "h1", "h4", "meta", "meta", "div", "pre", "span", "span", "span", "br"};
        protected static final String[][] defaultClasses = {{"song"}, {"song-title"}, {"song-author"}, null, null, {"song-text-wrapper", "short-song"}, {"song-text"}, {"line"}, null, {"lyrics"}, null};
        protected static final Pair<String, String>[][] defaultAttributes = new Pair[][]{null, null, null, new Pair[]{new Pair("name", "active"), new Pair("value", "true")}, new Pair[]{new Pair("name", "url"), new Pair("value", "")}, null, null, null, null, null, null};
        protected static final String LONG_SONG_CLASS = "long-song";
        protected static final HashMap<String, String> templates = getTemplates();
        protected static final HashMap<String, Pair<String, String>> fontSizeClasses = getFontSizeClasses();

        static {
            if (!(languageTokens.length  == types.length && types.length == defaultClasses.length && defaultClasses.length == defaultAttributes.length)) {
                PluginManager.getInstance().getSettings().get(SMSML.getInstance().getName()).put("enabled", Boolean.FALSE);
                logger.error("SMSML Plugin disabled because of interpreter engine misconfiguration");
                throw new IllegalStateException("The SMSML plugin is broken: Cannot load the engine!");
            }
        }

        protected static HashMap<String, String> getTemplates() {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put("div", "<div%s>%s</div>\n");
            temp.put("h1", "<h1%s>%s</h1>\n");
            temp.put("h4", "<h4%s>%s</h4>\n");
            temp.put("meta", "<meta%s>\n");
            temp.put("pre", "<pre%s>%s</pre>\n");
            temp.put("span", "<span%s>%s</span>\n");
            temp.put("br", "<br>\n");
            temp.put("%class", "class=\"%s\"");
            temp.put("%attr", "%s=\"%s\"");
            temp.put("%style", "style=\"%s\"");
            return temp;
        }

        protected static HashMap<String, Pair<String, String>> getFontSizeClasses() {
            HashMap<String, Pair<String, String>> temp = new HashMap<String, Pair<String, String>>();
            temp.put("default", new Pair<String, String>("song-text", "chords"));
            temp.put("small", new Pair<String, String>("song-text-long", "chords-long"));
            temp.put("very-small", new Pair<String, String>("song-text-long-plus-one", "chords-long-plus-one"));
            temp.put("ultra-small", new Pair<String, String>("song-text-slaboch", "chords-slaboch"));
            return temp;
        }


        public abstract String interpret(String s) throws ParseException;
        public abstract String getName();
    }

    private static class DemoDOM extends SMSMLEngine {

        @Override
        public String interpret(String s) throws ParseException {
            Element root = parse(s);
            return root.toString();
        }

        private Element parse(String s) throws ParseException {
            if (s == null) {
                throw new IllegalArgumentException();
            }
            String[] lines = s.split("\n");
            String[][] tokens = new String[lines.length][];
            for (int i = 0; i < lines.length; i++) {
                tokens[i] = lines[i].split(" ");
            }
            String fontSize = null;
            boolean isLine = false;
            boolean isColumn = false;
            boolean multiColumn = (s.indexOf(languageTokens[6]) == s.lastIndexOf(languageTokens[6]) && s.contains(languageTokens[6]));
            Element root = null;
            Element parent = null;
            for (String[] line : tokens) {
                Element node = null;
                isLine = false;
                isColumn = false;
                if (line != null) {
                    for (int i = 0; i < line.length; i++) {
                        if (Misc.indexOf(languageTokens, line[i]) != -1) {
                            node = new Element(templates.get(languageTokens[Misc.indexOf(languageTokens, line[i])]));
                            switch (Misc.indexOf(languageTokens, line[i])) {
                                case 0 -> root = node;
                                case 6 -> {
                                    isColumn = true;
                                   // parent = null;
                                }
                                case 7 -> isLine = true;
                            }
                            if (parent == null) {
                                parent = root;
                            }

                        }
                    }

                }
            }
            if (root == null) {
                throw new ParseException("Could not find the root element!", 0);
            }
            return root;
        }

        @Override
        public String getName() {
            return DemoDOM.class.getSimpleName();
        }

        private static class Element {
            private final String template;
            private String classes = "";
            private String style = "";
            private String attributes = "";
            private ArrayList<Object> innerHTML = new ArrayList<>();
            private String closure = "";

            private Element parent = null;

            public Element(String template) {
                if (!template.contains("</")) {
                    this.template = template;
                } else {
                    this.template = template.substring(0, template.indexOf("</"));
                    this.closure = template.substring(template.indexOf("</"));
                }
            }

            public void addClasses(String s) {
                if (s == null || s.length() == 0) {
                    return;
                }
                if (!this.classes.startsWith("class=\"")) {
                    this.classes = String.format(templates.get("%class"), s);
                } else {
                    this.classes = classes.substring(0, classes.length() - 1).concat(String.valueOf(classes.charAt(classes.length() - 1)));
                }
            }

            public void addStyle(String s) {
                if (s == null || s.length() == 0) {
                    return;
                }
                if (!this.style.startsWith("style=\"")) {
                    this.style = String.format(templates.get("%style"), s);
                } else {
                    this.style = style.substring(0, style.length() - 1).concat(String.valueOf(style.charAt(style.length() - 1)));
                }
            }

            public void addAttributes(String s) {
                this.attributes = this.attributes.concat(" ").concat(s);
            }

            public void addElement(Object o) {
                if (o != null) {
                    innerHTML.add(o);
                }
            }

            public void setParent(Element e) {
                this.parent = e;
            }

            public Element getParent() {
                return parent;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append(this.template.concat(String.format(" %s %s %s", classes, style, attributes).trim()));
                for (Object item : this.innerHTML) {
                    if (item != null) {
                        builder.append(item.toString());
                    }
                }
                builder.append(this.closure);
                return builder.toString();
            }

        }


    }

    private static class StreamLooper extends SMSMLEngine {

        @Override
        public String interpret(String s) {
            return null;
        }

        @Override
        public String getName() {
            return StreamLooper.class.getSimpleName();
        }
    }

}
