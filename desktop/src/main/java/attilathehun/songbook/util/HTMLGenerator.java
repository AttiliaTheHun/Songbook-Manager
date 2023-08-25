package attilathehun.songbook.util;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.plugin.DynamicSonglist;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * This class is used to create HTML files from templates. The product HTML is then fed to the WebView or converted into a PDF.
 */
public class HTMLGenerator {

    private static final String SONGLIST_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH + "/songlist.html").toString();
    private static final String HEAD_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH + "/head.html").toString();
    private static final String FRONTPAGE_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH + "/frontpage.html").toString();
    private static final String PAGEVIEW_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH + "/pageview.html").toString();
    private static final String SONG_TEMPLATE_PATH = Paths.get(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH + "/song.html").toString();
    private static final String TEMP_SONGLIST_PART_PATH = Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/songlist_part_%d.html").toString();
    private static final String TEMP_FRONTPAGE_PATH = Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/frontpage.html").toString();
    private static final String TEMP_PAGEVIEW_PATH = Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/current_page.html").toString();
    private static final String BASE_STYLE_FILE_PATH = Paths.get(Environment.getInstance().settings.CSS_RESOURCES_FILE_PATH + "/style.css").toString();
    private static final String HEAD_REPLACE_MARK = "<replace \"head\">";
    private static final String BASE_STYLE_PATH_REPLACE_MARK = "<replace \"basecss\">"; //style.css
    private static final String SONG_ONE_REPLACE_MARK = "<replace \"song1\">";
    private static final String SONG_TWO_REPLACE_MARK = "<replace \"song2\">";
    private static final String BASE_STYLE_HTML_LINK = "<link rel=\"stylesheet\" href=\"" + "style.css" + "\"></link>";
    private static final String SONG_AUTHOR_REPLACE_MARK = "<replace \"songauthor\">";
    private static final String SONG_NAME_REPLACE_MARK = "<replace \"songname\">";
    private static final String SONG_LIST_REPLACE_MARK = "<replace \"unorderedlist\">";
    private static final String FRONTPAGE_PICTURE_PATH_REPLACE_MARK = "<replace \"frontpagepic\">";
    private static final String FRONTPAGE_PICTURE_PATH = Paths.get(Environment.getInstance().settings.ASSETS_RESOURCES_FILE_PATH + "/frontpage.png").toString();
    public static final String FRONTPAGE = "frontpage.html";
    private static final String PAGEVIEW = "current_page.html";
    private static final String DEFAULT_SEGMENT_PATH = Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/segment").toString();

    public HTMLGenerator() {
        try {
            Files.copy(Paths.get(BASE_STYLE_FILE_PATH), Paths.get(Environment.getInstance().settings.TEMP_FILE_PATH + "/style.css"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("Error", "Can not instantiate the HTML generator");
        }
    }


    public void generateSonglistSegmentFile(int startIndex, int endIndex, int segmentNumber) {
        if (startIndex < 0 || startIndex > Environment.getInstance().getCollectionManager().getDisplayCollection().size() || endIndex > Environment.getInstance().getCollectionManager().getDisplayCollection().size()) {
            throw new IllegalArgumentException();
        }

        try {
            String path = String.format(TEMP_SONGLIST_PART_PATH, segmentNumber);
            if (Environment.fileExists(path)) {
                return;
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generateSonglistSegment(startIndex, endIndex, segmentNumber));
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate the songlist.");
        }

    }

    private String generateSonglistSegment(int startIndex, int endIndex, int segmentNumber) throws IOException {
        Path path = Paths.get(SONGLIST_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        ArrayList<Song> collection = Environment.getInstance().getCollectionManager().getDisplayCollection();
        StringBuilder payload = new StringBuilder();
        int columns = (endIndex - startIndex > DynamicSonglist.MAX_SONG_PER_COLUMN) ? 2 : 1;
        int columnStartIndex = startIndex;
        int columnEndIndex = (startIndex + DynamicSonglist.MAX_SONG_PER_COLUMN > endIndex) ? endIndex : startIndex + DynamicSonglist.MAX_SONG_PER_COLUMN;
        for (int j = 1; j < columns + 1; j++) {

            payload.append("<div class=\"divvy\">\n");
            payload.append("<ul>\n");

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

            payload.append("</ul>\n");
            payload.append("</div>\n");

            columnStartIndex = columnEndIndex;
            columnEndIndex = (columnEndIndex + DynamicSonglist.MAX_SONG_PER_COLUMN > endIndex) ? endIndex : columnEndIndex + DynamicSonglist.MAX_SONG_PER_COLUMN;
        }

        html = html.replace(SONG_LIST_REPLACE_MARK, payload.toString());
        return html;
    }

    private String getHead() throws IOException {
        Path path = Paths.get(HEAD_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        return html.replace(BASE_STYLE_PATH_REPLACE_MARK, BASE_STYLE_HTML_LINK);
    }

    private String generatePage(Song songOne, Song songTwo) throws IOException {
        String songOnePath = null, songTwoPath = null;
        if (songOne.id() != -1) {
            songOnePath = Paths.get(Environment.getInstance().settings.SONG_DATA_FILE_PATH + "/" + songOne.id() + ".html").toString();
            //generateSongFile(songOne.id());
        } else {
                if (songOne.name().equals("frontpage")) {
                    songOnePath = TEMP_FRONTPAGE_PATH;
                } else if (songOne.name().startsWith("songlist")) {
                    int songlistPartNumber = Integer.parseInt(songOne.name().substring("songlist".length()));
                    songOnePath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
                }
        }

        if (songTwo.id() != -1) {
            songTwoPath = Paths.get(Environment.getInstance().settings.SONG_DATA_FILE_PATH + "/" + songTwo.id() + ".html").toString();
            //generateSongFile(songTwo.id());
        } else {
            if (songTwo.name().equals("frontpage")) {
                songTwoPath = TEMP_FRONTPAGE_PATH;
            } else if (songTwo.name().startsWith("songlist")) {
                int songlistPartNumber = Integer.parseInt(songTwo.name().substring("songlist".length()));
                songTwoPath = String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber);
            }
        }

        if ((songOne.name().equals("frontpage") || songTwo.name().equals("frontpage")) && !new File(TEMP_FRONTPAGE_PATH).exists()) {
            generateFrontpageFile();
        }
        if (songOne.name().startsWith("songlist")) {
            int songlistPartNumber = Integer.parseInt(songOne.name().substring("songlist".length()));
            if (!new File(String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber)).exists()) {
                new DynamicSonglist().generateSonglist();
            }
        }

        if (songTwo.name().startsWith("songlist")) {
            int songlistPartNumber = Integer.parseInt(songTwo.name().substring("songlist".length()));
            if (!new File(String.format(TEMP_SONGLIST_PART_PATH, songlistPartNumber)).exists()) {
                new DynamicSonglist().generateSonglist();
            }
        }

        Path path = Paths.get(PAGEVIEW_TEMPLATE_PATH);
        // System.out.println(songOnePath);
        // System.out.println(songTwoPath);
        String songOneHTML = String.join("\n", Files.readAllLines(Paths.get(songOnePath)));
        String songTwoHTML = String.join("\n", Files.readAllLines(Paths.get(songTwoPath)));
        String html = String.join("\n", Files.readAllLines(path));

        html = html.replace(HEAD_REPLACE_MARK, getHead());
        html = html.replace(SONG_ONE_REPLACE_MARK, songOneHTML);
        html = html.replace(SONG_TWO_REPLACE_MARK, songTwoHTML);
        return html;
    }

    public String generatePageFile(Song songOne, Song songTwo) {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter((TEMP_PAGEVIEW_PATH), false));
            printWriter.write(generatePage(songOne, songTwo));
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate current page file.");
        }
        return TEMP_PAGEVIEW_PATH;
    }

    public boolean generateSongFile(Song s) {
        try {
            File songFile = null;
            if (Environment.getInstance().getCollectionManager().equals(EasterCollectionManager.getInstance())) {
                songFile = new File(String.format(EasterCollectionManager.EASTER_SONG_DATA_FILE_PATH + "/%d.html", s.id()));
            } else {
                songFile = new File(String.format(Environment.getInstance().settings.SONG_DATA_FILE_PATH + "/%d.html", s.id()));
            }

            File songTemplate = new File(Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH + "/song.html");
            if (!songFile.createNewFile() && songFile.length() != 0) {
                Environment.showWarningMessage("Data Loss Prevented", "File exists but is not empty: " + songFile);
                return false;
            }
            String songHTML = String.join("\n", Files.readAllLines(songTemplate.toPath()));
            songHTML = songHTML.replace(SONG_NAME_REPLACE_MARK, s.name());
            songHTML = songHTML.replace(SONG_AUTHOR_REPLACE_MARK, s.getAuthor());

            PrintWriter printWriter = new PrintWriter(new FileWriter((songFile), false));
            printWriter.write(songHTML);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("HTML Generation Error", "Unable to generate a song file.");
            return false;
        }
        return true;
    }

    private String generateFrontpage() throws IOException {
        Path path = Paths.get(FRONTPAGE_TEMPLATE_PATH);

        String html = String.join("\n", Files.readAllLines(path));
        html = html.replace(FRONTPAGE_PICTURE_PATH_REPLACE_MARK, /*FRONTPAGE_PICTURE_PATH*/"../resources/assets/frontpage.png");
        //System.out.println(html);
        return html;
    }

    public void generateFrontpageFile() {
        try {
            String path = TEMP_FRONTPAGE_PATH;
            if (Environment.fileExists(path)) {
                return;
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generateFrontpage());
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate the frontpage file.");
        }
    }


    public String generateSegmentFile(Song songOne, Song songTwo, int number) {
        String path = DEFAULT_SEGMENT_PATH + number + ".html";
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter((path), false));
            printWriter.write(generatePage(songOne, songTwo));
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("HTML Generation Error", "Unable to generate current segment file.");
        }
        return path;
    }
}
