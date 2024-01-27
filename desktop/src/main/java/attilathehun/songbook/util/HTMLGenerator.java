package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.export.PDFGenerator;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.plugin.DynamicSonglist;
import attilathehun.songbook.plugin.PluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * This class is used to create HTML files from templates. The product HTML is then fed to the WebView or converted into a PDF.
 */
public class HTMLGenerator {

    private static final Logger logger = LogManager.getLogger(HTMLGenerator.class);

    private static final String SONGLIST_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/songlist.html").toString();
    private static final String HEAD_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/head.html").toString();
    private static final String FRONTPAGE_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/frontpage.html").toString();
    private static final String PAGEVIEW_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/pageview.html").toString();
    private static final String SONG_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/song.html").toString();
    private static final String SONG_WRAPPER_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/song_wrapper.html").toString();
    private static final String TEMP_SONGLIST_PART_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/songlist_part_%d.html").toString();
    private static final String TEMP_FRONTPAGE_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/frontpage.html").toString();
    private static final String TEMP_PAGEVIEW_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/current_page.html").toString();
    private static final String BASE_STYLE_FILE_PATH = Paths.get(Environment.getInstance().settings.environment.CSS_RESOURCES_FILE_PATH + "/style.css").toString();
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
    private static final String FRONTPAGE_PICTURE_PATH = Paths.get(Environment.getInstance().settings.environment.ASSETS_RESOURCES_FILE_PATH + "/frontpage.png").toString();

    private static final String SHADOW_SONG_PATH = Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/shadow_song.html").toString();

    /**
     * The default constructor. Upon instantiation the default style file and a shadow song file are created in the temp folder.
     */
    public HTMLGenerator() {
        try {
            Files.copy(Paths.get(BASE_STYLE_FILE_PATH), Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/style.css"), StandardCopyOption.REPLACE_EXISTING);
            File shadowSongFile = new File(SHADOW_SONG_PATH);
            shadowSongFile.createNewFile();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Can not instantiate the HTML generator", true);
        }
    }

    /**
     * Generates HTML file with the list of songs from a specified portion of the formal collection to the temp folder. The default Collection Manager is used.
     * This method is part of the DynamicSonglist plugin.
     * @param startIndex formal collection first song index (inclusive)
     * @param endIndex formal collection last song index (exclusive)
     * @param segmentNumber number of the part of the songlist
     */
    public void generateSonglistSegmentFile(int startIndex, int endIndex, int segmentNumber) {
        try {
            String path = String.format(TEMP_SONGLIST_PART_PATH, segmentNumber);
            if (Misc.fileExists(path)) {
                return;
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generateSonglistSegment(startIndex, endIndex, segmentNumber));
            printWriter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate the songlist.");
        }

    }

    /**
     * Returns an HTML string  with the list of songs from a specified portion of the formal collection.The default Collection Manager is used.
     * This method is part of the DynamicSonglist plugin.
     * @param startIndex formal collection first song index (inclusive)
     * @param endIndex formal collection last song index (exclusive)
     * @param segmentNumber number of the part of the songlist
     * @return HTML string of a songlist part file of the specified number
     * @throws IOException
     */
    private String generateSonglistSegment(int startIndex, int endIndex, int segmentNumber) throws IOException {
        if (startIndex < 0 || startIndex > Environment.getInstance().getCollectionManager().getDisplayCollection().size() || endIndex < 0 ||endIndex > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) {
            throw new IllegalArgumentException();
        }

        Path path = Paths.get(SONGLIST_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        ArrayList<Song> collection = Environment.getInstance().getCollectionManager().getDisplayCollection();
        StringBuilder payload = new StringBuilder();
        int columns = (endIndex - startIndex > DynamicSonglist.MAX_SONG_PER_COLUMN) ? 2 : 1;
        int columnStartIndex = startIndex;
        int columnEndIndex = Math.min(startIndex + DynamicSonglist.MAX_SONG_PER_COLUMN, endIndex);
        for (int j = 1; j < columns + 1; j++) {

            payload.append("<div>\n");
            payload.append("<td class=\"songlist-column-wrapper\"><ul>\n");

            for (int i = columnStartIndex; i < columnEndIndex; i++) {
                payload.append("<li>");
                if (collection.get(i).id() == 96) {
                    payload.append("<b><u>");
                }
                payload.append(collection.get(i).name());
                if (collection.get(i).id() == 96) {
                    payload.append("</b></u>");
                }

                payload.append("</li>\n");
            }

            payload.append("</ul></td>\n");
            payload.append("</div>\n");

            columnStartIndex = columnEndIndex;
            columnEndIndex = Math.min(columnEndIndex + DynamicSonglist.MAX_SONG_PER_COLUMN, endIndex);
        }

        html = html.replace(SONG_LIST_REPLACE_MARK, payload.toString());
        return html;
    }

    /**
     * Returns the HTML string of the premade <head> element.
     * @return HTML <head> string
     * @throws IOException
     */
    private String getHead() throws IOException {
        Path path = Paths.get(HEAD_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        return html.replace(BASE_STYLE_PATH_REPLACE_MARK, BASE_STYLE_HTML_LINK);
    }

    /**
     * Returns an HTML string of a page of the songbook.
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @param manager the collection manager to use (default if null)
     * @return songbook page HTML string
     * @throws IOException
     */
    private String generatePage(Song songOne, Song songTwo, CollectionManager manager) throws IOException {
        if (songOne == null && songTwo == null) {
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
            int songlistPartNumber = Integer.parseInt(songOne.name().substring("songlist".length()));
            songOnePath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
        } else if (songOne.name().equals(CollectionManager.SHADOW_SONG_NAME)) {
            songOnePath = SHADOW_SONG_PATH;
        } else if (songOne.id() != CollectionManager.INVALID_SONG_ID) {
            songOnePath = manager.getSongFilePath(songOne.id());
        }

        if (songTwo.name().equals("frontpage")) {
            songTwoPath = TEMP_FRONTPAGE_PATH;
        } else if (songTwo.name().startsWith("songlist")) {
            int songlistPartNumber = Integer.parseInt(songTwo.name().substring("songlist".length()));
            songTwoPath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
        } else if (songTwo.name().equals(CollectionManager.SHADOW_SONG_NAME)) {
            songTwoPath = SHADOW_SONG_PATH;
        } else if (songTwo.id() != CollectionManager.INVALID_SONG_ID) {
            songTwoPath = manager.getSongFilePath(songTwo.id());
        }


        if ((songOne.name().equals("frontpage") || songTwo.name().equals("frontpage")) && !new File(TEMP_FRONTPAGE_PATH).exists()) {
            generateFrontpageFile();
        }
        if (songOne.name().startsWith("songlist")) {
            int songlistPartNumber = Integer.parseInt(songOne.name().substring("songlist".length()));
            if (!new File(String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber)).exists()) {
                PluginManager.getInstance().getPlugin(DynamicSonglist.class.getSimpleName()).execute();
            }
        }

        if (songTwo.name().startsWith("songlist")) {
            int songlistPartNumber = Integer.parseInt(songTwo.name().substring("songlist".length()));
            if (!new File(String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber)).exists()) {
                PluginManager.getInstance().getPlugin(DynamicSonglist.class.getSimpleName()).execute();
            }
        }

        Path templatePath = Paths.get(PAGEVIEW_TEMPLATE_PATH);
        String songOneHTML = String.join("\n", Files.readAllLines(Paths.get(songOnePath)));
        String songTwoHTML = String.join("\n", Files.readAllLines(Paths.get(songTwoPath)));
        String html = String.join("\n", Files.readAllLines(templatePath));

        html = html.replace(NAVBAR_REPLACE_MARK, ""); //navbar is for the web version, we have UI controls
        html = html.replace(HEAD_REPLACE_MARK, getHead());
        html = html.replace(SONG_ONE_REPLACE_MARK, songOneHTML);
        html = html.replace(SONG_TWO_REPLACE_MARK, songTwoHTML);
        html = html.replace(LANGUAGE_REPLACE_MARK, Environment.getInstance().settings.songbook.language.getDisplayName());
        return html;
    }

    /**
     * Generates an HTML file with a page of the songbook to the temp folder.
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @return path of the temp page file
     */
    public String generatePageFile(Song songOne, Song songTwo) {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter((TEMP_PAGEVIEW_PATH), false));
            printWriter.write(generatePage(songOne, songTwo, Environment.getInstance().getCollectionManager()));
            printWriter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate current page file.");
        }
        return TEMP_PAGEVIEW_PATH;
    }

    /**
     * Generates a new song file from the template to the data folder.
     * @param s target song
     * @param manager the collection manager to use (default if null)
     * @return true if the file has been created
     */
    public boolean generateSongFile(Song s, CollectionManager manager) {
        if (s == null || s.id() < 0) {
            throw new IllegalArgumentException();
        }

        if (manager == null) {
            manager = Environment.getInstance().getCollectionManager();
        }

        try {
            File songFile;
            songFile = new File(manager.getSongFilePath(s));

            File songTemplate = new File(Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH + "/song.html");
            if (!songFile.createNewFile() && songFile.length() != 0) {
                Environment.showWarningMessage("Data Loss Prevented", "File exists but is not empty: " + songFile);
                return false;
            }
            String songHTML = String.join("\n", Files.readAllLines(songTemplate.toPath()));
            songHTML = songHTML.replace(SONG_NAME_REPLACE_MARK, s.name());
            songHTML = songHTML.replace(SONG_AUTHOR_REPLACE_MARK, s.getAuthor());
            songHTML = songHTML.replace(SONG_URL_REPLACE_MARK, s.getUrl());
            songHTML = songHTML.replace(SONG_ACTIVE_REPLACE_MARK, String.valueOf(s.isActive()));

            PrintWriter printWriter = new PrintWriter(new FileWriter((songFile), false));
            printWriter.write(songHTML);
            printWriter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate a song file.");
            return false;
        }
        return true;
    }

    /**
     * Generates a new song file from the template to the data folder. Uses the default Collection Manager.
     * @param s target song
     * @return true if the file has been created
     */
    public boolean generateSongFile(Song s) {
        return generateSongFile(s, Environment.getInstance().getCollectionManager());
    }

    /**
     * Returns HTML string with the frontpage.
     * This method is part of the Frontpage plugin.
     * @return frontpage HTML string
     * @throws IOException
     */
    private String generateFrontpage() throws IOException {
        Path path = Paths.get(FRONTPAGE_TEMPLATE_PATH);

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
            Files.copy(Paths.get(FRONTPAGE_PICTURE_PATH), Paths.get(Environment.getInstance().settings.environment.TEMP_FILE_PATH + "/frontpage.png"), StandardCopyOption.REPLACE_EXISTING);
            String path = TEMP_FRONTPAGE_PATH;
            if (Misc.fileExists(path)) {
                return;
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generateFrontpage());
            printWriter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate the frontpage file.");
        }
    }

    /**
     * Returns HTML string of a page of the songbook. Though similar to #generatePageFile(), it is suited for exporting as you can choose name of the output file.
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @param number number of the segment (serves as file name)
     * @param manager the collection manager to use (default if null)
     * @return songbook page file HTML
     */
    public String generateSegmentFile(Song songOne, Song songTwo, int number, CollectionManager manager) {
        if (number != -1 && number < 0) {
            throw  new IllegalArgumentException();
        }

        String path;
        if (number == PDFGenerator.PREVIEW_SEGMENT_NUMBER) {
            path = String.format(PDFGenerator.PREVIEW_SEGMENT_PATH, PDFGenerator.EXTENSION_HTML);
        } else {
            path = String.format(PDFGenerator.DEFAULT_SEGMENT_PATH, number, PDFGenerator.EXTENSION_HTML);
        }
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generatePage(songOne, songTwo, manager));
            printWriter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate current segment file.");
        }
        return path;
    }

    /**
     * Returns an HTML string of a page of the songbook. Though similar to #generatePageFile(), it is suited for exporting as you can choose
     * name of the output file. Uses the default Collection Manager.
     * @param songOne first song on the page (on the left)
     * @param songTwo second song on the page (on the right)
     * @param number number of the segment (serves as file name)
     * @return songbook page file HTML
     */
    public String generateSegmentFile(Song songOne, Song songTwo, int number) {
        return generateSegmentFile(songOne, songTwo, number, Environment.getInstance().getCollectionManager());
    }

    /**
     * Returns an HTML string of a standalone song file wrapped in the template.
     * @param s target song
     * @param manager Collection Manager to use (default if null)
     * @return standalone song file HTML string
     * @throws IOException
     */
    private String generatePrintableSong(Song s, CollectionManager manager) throws  IOException {
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
            int songlistPartNumber = Integer.parseInt(s.name().substring("songlist".length()));
            songPath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
        } else if (s.name().equals(CollectionManager.SHADOW_SONG_NAME)) {
            return null;
        } else if (s.id() != CollectionManager.INVALID_SONG_ID) {
            songPath = manager.getSongFilePath(s.id());
        }


        if (s.name().equals("frontpage") && !new File(TEMP_FRONTPAGE_PATH).exists()) {
            generateFrontpageFile();
        }
        if (s.name().startsWith("songlist")) {
            int songlistPartNumber = Integer.parseInt(s.name().substring("songlist".length()));
            if (!new File(String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber)).exists()) {
                PluginManager.getInstance().getPlugin(DynamicSonglist.class.getSimpleName()).execute();
            }
        }

        Path templatePath = Paths.get(SONG_WRAPPER_TEMPLATE_PATH);
        String songHTML = String.join("\n", Files.readAllLines(Paths.get(songPath)));
        String html = String.join("\n", Files.readAllLines(templatePath));

        html = html.replace(HEAD_REPLACE_MARK, getHead());
        html = html.replace(SONG_ONE_REPLACE_MARK, songHTML);
        return html;
    }

    /**
     * Generates a standalone HTML file from template for a target song to the temp folder.
     * @param s target song
     * @param number number of the song file (servers as a file name)
     * @param manager the Collection Manager to use (default if null)
     * @return path to the file
     */
    public String generatePrintableSongFile(Song s, int number, CollectionManager manager) {
        if (number != -1 && number < 0) {
            throw  new IllegalArgumentException();
        }

        String path;
        if (number == PDFGenerator.PREVIEW_SEGMENT_NUMBER) {
            path = String.format(PDFGenerator.PREVIEW_SEGMENT_PATH, PDFGenerator.EXTENSION_HTML);
        } else {
            path = String.format(PDFGenerator.DEFAULT_SEGMENT_PATH, number, PDFGenerator.EXTENSION_HTML);
        }
        try {
            String html = generatePrintableSong(s, manager);
            if (html == null) {
                return null;
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(html);
            printWriter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate current segment file.");
        }
        return path;
    }

    /**
     * Generates a standalone HTML file from template for a target song to the temp folder. Uses default Collection Manager.
     * @param s target song
     * @param number number of the song file (servers as a file name)
     * @return path to the file
     */
    public String generatePrintableSongFile(Song s, int number) {
        return generatePrintableSongFile(s, number, Environment.getInstance().getCollectionManager());
    }
}
