package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This class is used to create HTML files from templates. The product HTML is then fed to the WebView or converted into a PDF.
 */
public class HTMLGenerator {

    private static final Logger logger = LogManager.getLogger(HTMLGenerator.class);

    private static final String SONGLIST_TEMPLATE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH") + "/songlist.html").toString();
    private static final String HEAD_TEMPLATE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH") + "/head.html").toString();
    private static final String FRONTPAGE_TEMPLATE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH") + "/frontpage.html").toString();
    private static final String PAGEVIEW_TEMPLATE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH") + "/pageview.html").toString();
    private static final String SONG_WRAPPER_TEMPLATE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH") + "/song_wrapper.html").toString();
    private static final String TEMP_SONGLIST_PART_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/songlist_part_%d.html").toString();
    private static final String TEMP_FRONTPAGE_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/frontpage.html").toString();
    private static final String TEMP_PAGEVIEW_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/current_page.html").toString();
    private static final String BASE_STYLE_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("CSS_RESOURCES_FILE_PATH") + "/style.css").toString();
    private static final String SHADOW_SONG_HTML = "<div class=\"song\"></div>";
    private static final String NAVBAR_REPLACE_MARK = "<replace \"navbar\">";
    private static final String HEAD_REPLACE_MARK = "<replace \"head\">";
    private static final String BASE_STYLE_PATH_REPLACE_MARK = "<replace \"basecss\">"; //style.css
    private static final String LANGUAGE_REPLACE_MARK = "<replace \"language\">";
    private static final String SONG_ONE_REPLACE_MARK = "<replace \"song1\">";
    private static final String SONG_TWO_REPLACE_MARK = "<replace \"song2\">";
    private static final String BASE_STYLE_HTML_LINK = "<link rel=\"stylesheet\" href=\"" + "style.css" + "\" />";
    private static final String SONG_AUTHOR_REPLACE_MARK = "<replace \"songauthor\">";
    private static final String SONG_NAME_REPLACE_MARK = "<replace \"songname\">";
    private static final String SONG_URL_REPLACE_MARK = "<replace \"songurl\">";
    private static final String SONG_ACTIVE_REPLACE_MARK = "<replace \"songactive\">";
    private static final String SONG_LIST_REPLACE_MARK = "<replace \"unorderedlist\">";
    private static final String FRONTPAGE_PICTURE_PATH_REPLACE_MARK = "<replace \"frontpagepic\">";
    private static final String FRONTPAGE_PICTURE_PATH = Paths.get(SettingsManager.getInstance().getValue("ASSET_RESOURCES_FILE_PATH") + "/frontpage.png").toString();

    private static final String SHADOW_SONG_PATH = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/shadow_song.html").toString();

    private static String tempStylesheetFileChecksum = "";

    static {
        init();
    }

    /**
     * The default constructor.
     */
    public HTMLGenerator() {

    }

    /**
     * Ensures the stylesheet in the temp folder is up-to-date and creates the shadow song file.
     */
    public static void init() {
        try {
            initTempFiles();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Cannot instantiate the HTML generator").addOkButton().build().open();
            Environment.getInstance().exit();
        }
    }

    /**
     * Ensures the stylesheet in the temp folder is up-to-date and creates the shadow song file.
     */
    private static void initTempFiles() throws IOException, NoSuchAlgorithmException {
        createShadowSongFile();

        final File tempStylesheet = new File(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/style.css");
        String currentChecksum;

        if (tempStylesheet.exists()) {
            currentChecksum = new SHA256HashGenerator().getHash(tempStylesheet);
            if (currentChecksum.equals(tempStylesheetFileChecksum)) {
                return;
            }
        }
        Files.copy(Paths.get(BASE_STYLE_FILE_PATH), tempStylesheet.toPath(), StandardCopyOption.REPLACE_EXISTING);
        currentChecksum = new SHA256HashGenerator().getHash(tempStylesheet);
        tempStylesheetFileChecksum = currentChecksum;
    }

    /**
     * Create an empty song file that can be injected into the current page file as a fill-up song.
     *
     * @throws IOException
     */
    private static void createShadowSongFile() throws IOException {
        final File shadowSongFile = new File(SHADOW_SONG_PATH);
        if (shadowSongFile.exists()) {
            return;
        }
        shadowSongFile.createNewFile();
        final PrintWriter printWriter = new PrintWriter(new FileWriter((shadowSongFile), false));
        printWriter.write(SHADOW_SONG_HTML);
        printWriter.close();
    }

    /**
     * Generates HTML file with the list of songs from a specified portion of the formal collection to the temp folder. The default Collection Manager is used.
     * This method is part of the DynamicSonglist plugin.
     *
     * @param startIndex    formal collection first song index (inclusive)
     * @param endIndex      formal collection last song index (exclusive)
     * @param segmentNumber number of the part of the songlist
     */
    public void generateSonglistSegmentFile(final int startIndex, final int endIndex, final int segmentNumber) {
        try {
            final String path = String.format(TEMP_SONGLIST_PART_PATH, segmentNumber);
            if (Misc.fileExists(path)) {
                return;
            }
            final PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generateSonglistSegment(startIndex, endIndex));
            printWriter.close();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Unable to generate the songlist.").addOkButton().build().open();
        }

    }

    /**
     * Returns an HTML string  with the list of songs from a specified portion of the formal collection.The default Collection Manager is used.
     * This method is part of the DynamicSonglist plugin.
     *
     * @param startIndex    formal collection first song index (inclusive)
     * @param endIndex      formal collection last song index (exclusive)
     * @return HTML string of a songlist part file of the specified number
     * @throws IOException
     */
    private String generateSonglistSegment(final int startIndex, final int endIndex) throws IOException {
        if (startIndex < 0 || startIndex > Environment.getInstance().getCollectionManager().getDisplayCollection().size() || endIndex < 0 || endIndex > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) {
            throw new IllegalArgumentException();
        }

        final int MAX_SONGS_PER_COLUMN = SettingsManager.getInstance().getValue("DYNAMIC_SONGLIST_SONGS_PER_COLUMN");

        final Path path = Paths.get(SONGLIST_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        final ArrayList<Song> collection = Environment.getInstance().getCollectionManager().getDisplayCollection();
        final StringBuilder payload = new StringBuilder();
        final int columns = (endIndex - startIndex > MAX_SONGS_PER_COLUMN) ? 2 : 1;
        int columnStartIndex = startIndex;
        int columnEndIndex = Math.min(startIndex + MAX_SONGS_PER_COLUMN, endIndex);
        for (int j = 1; j < columns + 1; j++) {

            payload.append("<div class=\"songlist-column\">\n<ul class=\"songlist-formatting\">");

            // This is implementation-specific, public users do not need this
            for (int i = columnStartIndex; i < columnEndIndex; i++) {
                payload.append("<li>");
                if (collection.get(i).name().equals("Píseň Hraboše")) {
                    payload.append("<b><u>");
                }
                payload.append(collection.get(i).name());
                if (collection.get(i).name().equals("Píseň Hraboše")) {
                    payload.append("</b></u>");
                }

                payload.append("</li>\n");
            }

            payload.append("</ul></div>\n");

            columnStartIndex = columnEndIndex;
            columnEndIndex = Math.min(columnEndIndex + MAX_SONGS_PER_COLUMN, endIndex);
        }

        html = html.replace(SONG_LIST_REPLACE_MARK, payload.toString());
        return html;
    }

    /**
     * Returns the HTML string of the premade <head> element.
     *
     * @return HTML <head> string
     * @throws IOException
     */
    private String getHead() throws IOException {
        final Path path = Paths.get(HEAD_TEMPLATE_PATH);

        final String html = String.join("\n", Files.readAllLines(path));
        return html.replace(BASE_STYLE_PATH_REPLACE_MARK, BASE_STYLE_HTML_LINK);
    }

    /**
     * Returns an HTML string of a page of the songbook.
     *
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @param manager the collection manager to use (default if null)
     * @return songbook page HTML string
     * @throws IOException
     */
    private String generatePage(final Song songOne, final Song songTwo, CollectionManager manager) throws IOException {
        if (songOne == null || songTwo == null) {
            throw new IllegalArgumentException();
        }

        if (manager == null) {
            manager = Environment.getInstance().getCollectionManager();
        }

        String songOnePath = null;
        String songTwoPath = null;

        if (songOne.name().equals("frontpage")) {
            songOnePath = TEMP_FRONTPAGE_PATH;
        } else if (songOne.name().startsWith("songlist")) {
            final int songlistPartNumber = Integer.parseInt(songOne.name().substring("songlist".length()));
            songOnePath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
        } else if (songOne.name().equals(CollectionManager.SHADOW_SONG_NAME)) {
            songOnePath = SHADOW_SONG_PATH;
        } else if (songOne.id() != CollectionManager.INVALID_SONG_ID) {
            songOnePath = manager.getSongFilePath(songOne.id());
        }

        if (songTwo.name().equals("frontpage")) {
            songTwoPath = TEMP_FRONTPAGE_PATH;
        } else if (songTwo.name().startsWith("songlist")) {
            final int songlistPartNumber = Integer.parseInt(songTwo.name().substring("songlist".length()));
            songTwoPath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
        } else if (songTwo.name().equals(CollectionManager.SHADOW_SONG_NAME)) {
            songTwoPath = SHADOW_SONG_PATH;
        } else if (songTwo.id() != CollectionManager.INVALID_SONG_ID) {
            songTwoPath = manager.getSongFilePath(songTwo.id());
        }


        if ((songOne.name().equals("frontpage") || songTwo.name().equals("frontpage")) && !new File(TEMP_FRONTPAGE_PATH).exists()) {
            generateFrontpageFile();
        }

        final Path templatePath = Paths.get(PAGEVIEW_TEMPLATE_PATH);
        final String songOneHTML = String.join("\n", Files.readAllLines(Paths.get(songOnePath)));
        final String songTwoHTML = String.join("\n", Files.readAllLines(Paths.get(songTwoPath)));
        String html = String.join("\n", Files.readAllLines(templatePath));

        html = html.replace(NAVBAR_REPLACE_MARK, ""); //navbar is for the web version, we have UI controls
        html = html.replace(HEAD_REPLACE_MARK, getHead());
        html = html.replace(SONG_ONE_REPLACE_MARK, songOneHTML);
        html = html.replace(SONG_TWO_REPLACE_MARK, songTwoHTML);
        html = html.replace(LANGUAGE_REPLACE_MARK, Locale.of(SettingsManager.getInstance().getValue("SONGBOOK_LANGUAGE")).getDisplayName());
        return html;
    }

    /**
     * Generates an HTML file with a page of the songbook to the temp folder.
     *
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @return path of the temp page file
     */
    public String generatePageFile(final Song songOne, final Song songTwo) {
        try {
            final PrintWriter printWriter = new PrintWriter(new FileWriter((TEMP_PAGEVIEW_PATH), false));
            printWriter.write(generatePage(songOne, songTwo, Environment.getInstance().getCollectionManager()));
            printWriter.close();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Unable to generate current page file.").addOkButton().build().open();
        }
        return TEMP_PAGEVIEW_PATH;
    }

    /**
     * Generates a new song file from the template to the data folder.
     *
     * @param s       target song
     * @param manager the collection manager to use (default if null)
     * @return true if the file has been created
     */
    public boolean generateSongFile(final Song s, CollectionManager manager) {
        if (s == null || s.id() < 0) {
            throw new IllegalArgumentException();
        }

        if (manager == null) {
            manager = Environment.getInstance().getCollectionManager();
        }

        try {
            File songFile;
            songFile = new File(manager.getSongFilePath(s));

            final File songTemplate = new File(SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH") + "/song.html");
            if (!songFile.createNewFile() && songFile.length() != 0) {
                new AlertDialog.Builder().setTitle("Data Loss Prevented").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage("File already exists but is not empty.").addOkButton().build().open();
                return false;
            }
            String songHTML = String.join("\n", Files.readAllLines(songTemplate.toPath()));
            songHTML = songHTML.replace(SONG_NAME_REPLACE_MARK, s.name());
            songHTML = songHTML.replace(SONG_AUTHOR_REPLACE_MARK, s.getAuthor());
            songHTML = songHTML.replace(SONG_URL_REPLACE_MARK, s.getUrl());
            songHTML = songHTML.replace(SONG_ACTIVE_REPLACE_MARK, String.valueOf(s.isActive()));

            final PrintWriter printWriter = new PrintWriter(new FileWriter((songFile), false));
            printWriter.write(songHTML);
            printWriter.close();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Unable to generate a song file.").addOkButton().build().open();
            return false;
        }
        return true;
    }

    /**
     * Generates a new song file from the template to the data folder. Uses the default Collection Manager.
     *
     * @param s target song
     * @return true if the file has been created
     */
    public boolean generateSongFile(final Song s) {
        return generateSongFile(s, Environment.getInstance().getCollectionManager());
    }

    /**
     * Returns HTML string with the frontpage.
     * This method is part of the Frontpage plugin.
     *
     * @return frontpage HTML string
     * @throws IOException
     */
    private String generateFrontpage() throws IOException {
        final Path path = Paths.get(FRONTPAGE_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        html = html.replace(FRONTPAGE_PICTURE_PATH_REPLACE_MARK, /*FRONTPAGE_PICTURE_PATH*/ "frontpage.png");

        return html;
    }

    /**
     * Generates an HTML file with the songbook frontpage to the temp folder.
     * This method is part of the Frontpage plugin.
     */
    public void generateFrontpageFile() {
        try {
            Files.copy(Paths.get(FRONTPAGE_PICTURE_PATH), Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH") + "/frontpage.png"), StandardCopyOption.REPLACE_EXISTING);
            final String path = TEMP_FRONTPAGE_PATH;
            if (Misc.fileExists(path)) {
                return;
            }
            final PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generateFrontpage());
            printWriter.close();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Unable to generate the frontpage file.").addOkButton().build().open();
        }
    }

    /**
     * Returns HTML string of a page of the songbook. Though similar to #generatePageFile(), it is suited for exporting as you can choose name of the output file.
     *
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @param number  number of the segment (serves as file name)
     * @param manager the collection manager to use (default if null)
     * @return songbook page file HTML
     */
    public String generateSegmentFile(final Song songOne, final Song songTwo, final int number, final CollectionManager manager) {
        if (number != -1 && number < 0) {
            throw new IllegalArgumentException();
        }

        String path;
        if (number == PDFGenerator.PREVIEW_SEGMENT_NUMBER) {
            path = String.format(PDFGenerator.PREVIEW_SEGMENT_PATH, PDFGenerator.EXTENSION_HTML);
        } else {
            path = String.format(PDFGenerator.DEFAULT_SEGMENT_PATH, number, PDFGenerator.EXTENSION_HTML);
        }
        try {
            final PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generatePage(songOne, songTwo, manager));
            printWriter.close();
        } catch (final Exception e) {
            logger.debug(String.format("song1: %s (%d) song2: %s (%d) segmentNumber: %d", songOne.name(), songOne.id(), songTwo.name(), songTwo.id(), number));
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Unable to generate current segment file.").addOkButton().build().open();
        }
        return path;
    }

    /**
     * Returns an HTML string of a page of the songbook. Though similar to #generatePageFile(), it is suited for exporting as you can choose
     * name of the output file. Uses the default Collection Manager.
     *
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @param number  number of the segment (serves as file name)
     * @return songbook page file HTML
     */
    public String generateSegmentFile(final Song songOne, final Song songTwo, final int number) {
        return generateSegmentFile(songOne, songTwo, number, Environment.getInstance().getCollectionManager());
    }

    /**
     * Returns an HTML string of a standalone song file wrapped in the template.
     *
     * @param s       target song
     * @param manager Collection Manager to use (default if null)
     * @return standalone song file HTML string
     * @throws IOException
     */
    private String generatePrintableSong(final Song s, CollectionManager manager) throws IOException {
        if (s == null) {
            throw new IllegalArgumentException();
        }

        if (manager == null) {
            manager = Environment.getInstance().getCollectionManager();
        }

        String songPath = null;

        if (s.name().equals("frontpage")) {
            songPath = TEMP_FRONTPAGE_PATH;
        } else if (s.name().startsWith("songlist")) {
            final int songlistPartNumber = Integer.parseInt(s.name().substring("songlist".length()));
            songPath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
        } else if (s.name().equals(CollectionManager.SHADOW_SONG_NAME)) {
            return null;
        } else if (s.id() != CollectionManager.INVALID_SONG_ID) {
            songPath = manager.getSongFilePath(s.id());
        }


        if (s.name().equals("frontpage") && !new File(TEMP_FRONTPAGE_PATH).exists()) {
            generateFrontpageFile();
        }


        final Path templatePath = Paths.get(SONG_WRAPPER_TEMPLATE_PATH);
        final String songHTML = String.join("\n", Files.readAllLines(Paths.get(songPath)));
        String html = String.join("\n", Files.readAllLines(templatePath));

        html = html.replace(HEAD_REPLACE_MARK, getHead());
        html = html.replace(SONG_ONE_REPLACE_MARK, songHTML);
        return html;
    }

    /**
     * Generates a standalone HTML file from template for a target song to the temp folder.
     *
     * @param s       target song
     * @param number  number of the song file (servers as a file name)
     * @param manager the Collection Manager to use (default if null)
     * @return path to the file
     */
    public String generatePrintableSongFile(final Song s, final int number, final CollectionManager manager) {
        if (number != -1 && number < 0) {
            throw new IllegalArgumentException();
        }

        String path;
        if (number == PDFGenerator.PREVIEW_SEGMENT_NUMBER) {
            path = String.format(PDFGenerator.PREVIEW_SEGMENT_PATH, PDFGenerator.EXTENSION_HTML);
        } else {
            path = String.format(PDFGenerator.DEFAULT_SEGMENT_PATH, number, PDFGenerator.EXTENSION_HTML);
        }
        try {
            final String html = generatePrintableSong(s, manager);
            if (html == null) {
                return null;
            }
            final PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(html);
            printWriter.close();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Generation Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Unable to generate printable song file").addOkButton().build().open();
        }
        return path;
    }

    /**
     * Generates a standalone HTML file from template for a target song to the temp folder. Uses default Collection Manager.
     *
     * @param s      target song
     * @param number number of the song file (servers as a file name)
     * @return path to the file
     */
    public String generatePrintableSongFile(final Song s, final int number) {
        return generatePrintableSongFile(s, number, Environment.getInstance().getCollectionManager());
    }
}
