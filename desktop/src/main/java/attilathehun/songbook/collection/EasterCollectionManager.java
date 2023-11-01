package attilathehun.songbook.collection;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.EnvironmentVerificator;
import attilathehun.songbook.window.CodeEditor;
import attilathehun.songbook.window.CollectionEditor;
import attilathehun.songbook.util.HTMLGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class EasterCollectionManager extends CollectionManager {

    private static final Logger logger = LogManager.getLogger(EasterCollectionManager.class);
    private static final List<CollectionListener> listeners = new ArrayList<>();

    public static final String EASTER_SONG_DATA_FILE_PATH = Paths.get(Environment.getInstance().settings.environment.DATA_FILE_PATH + "/songs/egg/").toString();
    private static final EasterCollectionManager instance;

    private final String collectionName = "easter";

    static {
        instance = new EasterCollectionManager();
        instance.init();
    }

    private ArrayList<Song> collection;

    private EasterCollectionManager() {

    }

    private EasterCollectionManager(ArrayList<Song> collection) {
        this.collection = new ArrayList<Song>(collection);
    }

    public static EasterCollectionManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        Environment.getInstance().registerCollectionManager(this);
        try {
            File collectionJSONFile = new File(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getCollectionFilePath());
            if (!collectionJSONFile.exists()) {
                File songDataFolder = new File(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getSongDataFilePath());
                if (songDataFolder.exists() && songDataFolder.isDirectory()) {
                    repairCollectionDialog();
                } else {
                    createCollectionDialog();
                }
                return;
            }

            String json = String.join("", Files.readAllLines(collectionJSONFile.toPath()));

            Type targetClassType = new TypeToken<ArrayList<Song>>() {
            }.getType();
            collection = new Gson().fromJson(json, targetClassType);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Easter Collection Initialisation error", "Can not load the easter song collection!");
        }
        logger.info("EasterCollectionManager initialized");
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public CollectionManager copy() {
        return new EasterCollectionManager(collection);
    }

    @Override
    public void save() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getCollectionFilePath());
            gson.toJson(collection, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showWarningMessage("Error", "Can not save the easter egg song collection!");
        }
    }

    @Override
    public Collection<Song> getCollection() {
        return collection;
    }

    @Override
    public ArrayList<Song> getSortedCollection() {
        ArrayList<Song> copy = new ArrayList<Song>(collection);
        copy.sort(new Comparator<Song>() {
            public int compare(Song s1, Song s2) {
                return Collator.getInstance().compare(s1.name(), s2.name());
            }
        });
        return copy;
    }

    @Override
    public ArrayList<Song> getDisplayCollection() {
        return StandardCollectionManager.getInstance().getDisplayCollection();
    }

    @Override
    public ArrayList<Song> getFormalCollection() {
        ArrayList<Song> standardFormalList = StandardCollectionManager.getInstance().getFormalCollection();
        ArrayList<Song> formalList = new ArrayList<Song>();

        for (Song song : standardFormalList) {
            boolean esterEggFound = false;
            if (!song.isActive()) {
                continue;
            }
            searchForEasterEgg:
            for (Song value : collection) {
                if (value.isActive() && value.id() == song.id()) {
                    formalList.add(value);
                    esterEggFound = true;
                }
                break searchForEasterEgg;
            }

            if (!esterEggFound) {
                formalList.add(song);
            }
        }

        return formalList;
    }

    @Override
    public Song addSong(Song s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        if (!new HTMLGenerator().generateSongFile(s)) {
            return null;
        }
        int defaultIndex = StandardCollectionManager.getInstance().getCollectionSongIndex(s);
        if (defaultIndex == -1) {
            throw new IllegalArgumentException();
        }
        collection.add(s);
        save();
        CodeEditor.open(this, s);
        Environment.getInstance().refresh();
        if (Environment.getInstance().getCollectionManager().equals(getInstance())) {
            Environment.navigateWebViewToSong(collection.get(collection.size() - 1));
        }
        SongbookApplication.dialControlPLusRPressed();
        return s;
    }

    @Override
    public void removeSong(Song s) {
        collection.remove(getCollectionSongIndex(s));
        save();
        File songFile = new File(String.format(EASTER_SONG_DATA_FILE_PATH + "/%d.html", s.id()));
        if (songFile.delete()) {
            Environment.showMessage("Success", String.format("Easter Egg Song '%s' id: %d deleted. Ave Caesar!", s.name(), s.id()));
        }
        onSongRemoved(s);
        if (Environment.getInstance().getCollectionManager().equals(getInstance())) {
            Environment.getInstance().refresh();
            SongbookApplication.dialControlPLusRPressed();
        }
    }

    @Override
    public void activateSong(Song s) {
        collection.get(getCollectionSongIndex(s)).setActive(true);
        save();
    }

    @Override
    public void deactivateSong(Song s) {
        collection.get(getCollectionSongIndex(s)).setActive(false);
        save();
    }

    @Override
    public Song updateSongRecord(Song s) {
        int songIdMatches = 0;
        for (Song song : collection) {
            if (song.id() == s.id()) {
                songIdMatches++;
            }
        }

        if (songIdMatches > 1) {
            Environment.showWarningMessage("Warning", "A song with such Id already exists. Please, resolve this conflict manually on the other song before using this Id.");
            return s;
        }

        int index = getCollectionSongIndex(s);
        Song song = null;
        if (index == -1) {

            for (Song item : collection) {
                if (item.id() == s.getFormerId()) {
                    song = item;
                }
            }

            index = getCollectionSongIndex(song);
            collection.remove(index);
            collection.add(s);
            song = s;
        } else {
            collection.get(index).setName(s.name());
            collection.get(index).setUrl(s.getUrl());
            collection.get(index).setActive(s.isActive());
            song = collection.get(index);
        }

        save();
        CollectionEditor.forceRefreshInstance();
        return  song;
    }

    @Override
    public void updateSongRecordTitleFromHTML(Song s) {
        // It is undesired to bind title in easter egg collection
    }

    @Override
    public void updateSongHTMLTitleFromRecord(Song s) {

    }

    @Override
    public void updateSongHTMLFromRecord(Song s) {
        if (!Environment.getInstance().settings.songbook.BIND_SONG_TITLES) {
            return;
        }
        if (s == null || s.id() < 0) {
            return;
        }
        try {
            String songHTML = String.join("\n", Files.readAllLines(Paths.get(getSongFilePath(s.id()))));
            Document document = Jsoup.parse(songHTML);
            Element element = document.select(".song-title").first();
            String songName = element.text();
            System.out.println(songName);

            if (!s.name().equals(songName)) {
                collection.get(getCollectionSongIndex(s)).setName(songName);
            }
            save();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()));
        }
    }

    @Override
    public String getSongFilePath(Song s) {
        return getSongFilePath(s.id());
    }

    @Override
    public String getSongFilePath(int id) {
        return Paths.get(String.format("%s/%d.html", EASTER_SONG_DATA_FILE_PATH, id)).toString();
    }

    @Override
    public int getCollectionSongIndex(Song s) {
        int i = 0;
        for (Song song : collection) {
            if (song.equals(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getCollectionSongIndex(int songId) {
        if (songId< 0) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        for (Song song : collection) {
            if (song.id() == songId) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getSortedCollectionSongIndex(Song s) {
        int i = 0;
        for (Song song : getSortedCollection()) {
            if (song.equals(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getSortedCollectionSongIndex(int id) {
        if (id < 0) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        for (Song song : getSortedCollection()) {
            if (song.id() == id) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getDisplayCollectionSongIndex(Song s) {
        int i = 0;
        for (Song song : getDisplayCollection()) {
            if (song.equals(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getDisplayCollectionSongIndex(int id) {
        if (id < 0) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        for (Song song : getDisplayCollection()) {
            if (song.id() == id) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getFormalCollectionSongIndex(Song s) {
        int i = 0;
        for (Song song : getFormalCollection()) {
            if (song.equals(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int getFormalCollectionSongIndex(int id) {
        if (id < 0) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        for (Song song : getFormalCollection()) {
            if (song.id() == id) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public Song getPlaceholderSong() {
        return new Song("Secret Song", -1);

    }

    @Override
    public void addListener(CollectionListener listener) {
        if (listener == null) {
        throw new IllegalArgumentException();

        }
        listeners.add(listener);
    }

    @Override
    public CollectionSettings getSettings() {
        return new CollectionSettings(Paths.get(new Environment.EnvironmentSettings().DATA_FILE_PATH + "/easter_collection.json").toString(), Paths.get(new Environment.EnvironmentSettings().DATA_FILE_PATH + "/songs/egg/").toString());
    }

    private void onSongRemoved(Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongRemoved(s, this);
        }
    }


    /* Internal helper methods */

    private Song createSongRecordFromLocalFile(int songId) {
        if (songId == -1) {
            throw new IllegalArgumentException();
        }
        String songName = null;
        try {
            String songHTML = String.join("\n", Files.readAllLines(Path.of(getSongFilePath(songId))));
            Document document = Jsoup.parse(songHTML);
            Element element = document.select(".song-title").first();
            songName = element.text();
            element = document.select("meta[name=url]").first();
            String songURL = (element != null) ? element.attr("value") : "";
            element = document.select("meta[name=active]").first();
            Boolean songActiveStatus = (element != null) ? Boolean.parseBoolean(element.attr("value")) : true;

            return new Song(songId, songName, songActiveStatus, songURL);
        } catch (IOException e) {
            logger.info(String.format("Target song: %d %s", songId, songName));
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void repairCollectionDialog() {
        UIManager.put("OptionPane.yesButtonText", "Repair");
        UIManager.put("OptionPane.noButtonText", "Create");

        int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Repair easter egg collection", "No collection file was found but it seems you have saved songs in your easter egg data directory. Would you like to generate a collection file to repair the easter egg collection? Alternatively, you can create a new collection.", JOptionPane.YES_NO_OPTION);

        if (resultCode == JOptionPane.YES_OPTION) {
            repairMissingCollectionFile();
        } else if (resultCode == JOptionPane.NO_OPTION) {
            createNewCollection();
        } else if (resultCode == JOptionPane.CLOSED_OPTION) {

        }

        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
    }

    private void createCollectionDialog() {
        UIManager.put("OptionPane.okButtonText", "Create");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Create an easter egg collection", "Do you want to create an easter egg collection?", JOptionPane.OK_CANCEL_OPTION);

        if (resultCode == JOptionPane.OK_OPTION) {
            createNewCollection();
        } else if (resultCode == JOptionPane.CANCEL_OPTION) {

        } else if (resultCode == JOptionPane.CLOSED_OPTION) {

        }

        UIManager.put("OptionPane.okButtonText", "Yes");
        UIManager.put("OptionPane.cancelButtonText", "No");
    }

    private void repairMissingCollectionFile() {
        logger.info("Repairing easter collection");
        collection = new ArrayList<Song>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(EASTER_SONG_DATA_FILE_PATH))) {
            for (Path path : stream) {

                if (!Files.isDirectory(path) && path.toString().trim().endsWith(".html")) {
                    int songId = -1;
                    String pathString = path.toString();
                    if (pathString.contains(File.separator)) {
                        songId = Integer.parseInt(pathString.substring(pathString.lastIndexOf(File.separator) + 1, pathString.indexOf(".html")));
                    } else {
                        songId = Integer.parseInt(pathString.substring(0, pathString.indexOf(".html")));
                    }
                    collection.add(createSongRecordFromLocalFile(songId));
                }
            }
            save();
            logger.info("Success!");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void createNewCollection() {
        try {
            File songDataFolder = new File(EASTER_SONG_DATA_FILE_PATH);
            songDataFolder.mkdir();
            File collectionJSONFile = new File(Environment.getInstance().settings.collections.get(EasterCollectionManager.getInstance().getCollectionName()).getCollectionFilePath());
            collectionJSONFile.createNewFile();
            collection = new ArrayList<Song>();
            PrintWriter printWriter = new PrintWriter(new FileWriter(collectionJSONFile));
            printWriter.write("[]");
            printWriter.close();
            EnvironmentVerificator.SUPPRESS_WARNINGS = true;

            UIManager.put("OptionPane.okButtonText", "Add");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");

            int option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to add your first easter song?", "Add Easter Song?", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                EnvironmentManager.addSongDialog(getInstance());
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Could not create a new easter song!");
        }
    }


}
