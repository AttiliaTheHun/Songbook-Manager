package attilathehun.songbook.collection;

import attilathehun.songbook.window.SongbookApplication;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.plugin.DynamicSonglist;
import attilathehun.songbook.plugin.Frontpage;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.window.CodeEditorV1;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class StandardCollectionManager extends CollectionManager {

    private static final Logger logger = LogManager.getLogger(StandardCollectionManager.class);

    private static final List<CollectionListener> listeners = new ArrayList<>();
    private static final StandardCollectionManager instance;

    private final String collectionName = "standard";

    static {
        instance = new StandardCollectionManager();
        instance.init();
    }

    private ArrayList<Song> collection;

    private StandardCollectionManager() {

    }

    private StandardCollectionManager(ArrayList<Song> collection) {
        this.collection = new ArrayList<Song>(collection);
    }

    public static StandardCollectionManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        Environment.getInstance().registerCollectionManager(this);
        try {
            File collectionJSONFile = new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getCollectionFilePath());
            if (!collectionJSONFile.exists()) {
                File songDataFolder = new File(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath());
                if (songDataFolder.exists() && songDataFolder.isDirectory()) {
                    repairSongbookDialog();
                } else {
                    createSongbookDialog();
                }
                return;
            }

            String json = String.join("", Files.readAllLines(collectionJSONFile.toPath()));

            Type targetClassType = new TypeToken<ArrayList<Song>>() {
            }.getType();
            collection = new Gson().fromJson(json, targetClassType);
            for (Song song : collection) {
                song.setManager(this);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Collection Initialisation error", "Can not load the song collection!", true);
        }
        logger.info("StandardCollectionManager initialized");
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public CollectionManager copy() {
        return new StandardCollectionManager(collection);
    }

    @Override
    public void save() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getCollectionFilePath());
            gson.toJson(collection, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", "Can not save the song collection!");
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
        ArrayList<Song> displayList = new ArrayList<Song>(new ArrayList<Song>(getSortedCollection().stream().filter(Song::isActive).collect(Collectors.toList())));
        return displayList;
    }

    @Override
    public ArrayList<Song> getFormalCollection() {
        ArrayList<Song> formalList = new ArrayList<Song>();
        if (PluginManager.getInstance().getSettings().getEnabled(Frontpage.class.getSimpleName())) {
            formalList.add(CollectionManager.getFrontpageSong());
        }
        if (PluginManager.getInstance().getSettings().getEnabled(DynamicSonglist.class.getSimpleName())) {
            for (int i = 0; i < PluginManager.getInstance().getPlugin(DynamicSonglist.class.getSimpleName()).execute(); i++) {
                formalList.add(CollectionManager.getSonglistSong(i));
            }

        }

        formalList.addAll(new ArrayList<Song>(getSortedCollection().stream().filter(Song::isActive).collect(Collectors.toList())));
        return formalList;
    }

    @Override
    public Song addSong(Song s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        Song song = s;
        song.setId(getNextId());

        if (!new HTMLGenerator().generateSongFile(song)) {
            return null;
        }
        song.setManager(this);
        collection.add(song);
        save();
        onSongAdded(s);
        CodeEditorV1.open(this, s);
        Environment.getInstance().refresh();
        Environment.navigateWebViewToSong(collection.get(collection.size() - 1));
        SongbookApplication.dialControlPLusRPressed();
        return song;
    }

    @Override
    public void removeSong(Song s) {
        if (s == null || s.id() < 0) {
            throw new IllegalArgumentException();
        }
        collection.remove(getCollectionSongIndex(s));
        save();
        File songFile = new File(String.format(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath() + "/%d.html", s.id()));
        if (songFile.delete()) {
            Environment.showMessage("Success", String.format("Song '%s' id: %d deleted. Have a nice day!", s.name(), s.id()));
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
        int index = getCollectionSongIndex(s.id());

        collection.get(index).setName(s.name());
        collection.get(index).setUrl(s.getUrl());
        collection.get(index).setActive(s.isActive());

        save();
        onSongUpdated(s);
        Environment.getInstance().refresh();
        SongbookApplication.dialControlPLusRPressed();
        return s;
    }

    @Override
    public void updateSongRecordTitleFromHTML(Song s) {
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
    public void updateSongHTMLTitleFromRecord(Song s) {
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
            element.text(s.name());

            FileOutputStream out = new FileOutputStream(getSongFilePath(s));
            out.write(document.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Error", String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()));
        }
    }

    @Override
    public void updateSongHTMLFromRecord(Song s) {
        if (!Environment.getInstance().settings.songbook.BIND_SONG_TITLES) {
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
        return Paths.get(String.format("%s/%d.html", Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath(), id)).toString();
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
        int min = 0;
        int max = 41;
        int random = (int) (Math.random() * max + min);
        Song song;
        switch (random) {
            case 1 -> {
                song = new Song("New Song", -1);
                song.setUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                return song;
            }
            case 2 -> {
                song = new Song("Večer křupavých srdíček", -1);
                song.setAuthor("Vlasta Redl");
                song.setUrl("https://www.youtube.com/watch?v=txLfhpEroYI");
                return song;
            }
            case 3 -> {
                song = new Song("Je reviendrai vers toi", -1);
                song.setAuthor("Bryan Adams");
                song.setUrl("https://www.youtube.com/watch?v=29TPk17Z4AA");
                return song;
            }
        }

        return new Song("New Song", -1);

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
        return new CollectionSettings(Paths.get(new Environment.EnvironmentSettings().DATA_FILE_PATH + "/collection.json").toString(), Paths.get(new Environment.EnvironmentSettings().DATA_FILE_PATH + "/songs/html/").toString());
    }


    private void onSongAdded(Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongAdded(s, this);
        }
    }

    private void onSongUpdated(Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongUpdated(s, this);
        }
    }

    private void onSongRemoved(Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongRemoved(s, this);
        }
    }


    /* Internal helper methods */
    private int getNextId() {
        int id = -1;
        for (Song song : collection) {
            if (song.id() > id) {
                id = song.id();
            }
        }
        return id + 1;
    }

    private Song createSongRecordFromLocalFile(int songId) {
        if (songId == -1) {
            throw new IllegalArgumentException();
        }
        String songName = null;
        try {
            String songHTML = String.join("\n", Files.readAllLines(Paths.get(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath() + String.format("/%d.html", songId))));
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

    private void repairSongbookDialog() {
        UIManager.put("OptionPane.yesButtonText", "Repair");
        UIManager.put("OptionPane.noButtonText", "Create");
        UIManager.put("OptionPane.cancelButtonText", "Load");

        int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "No collection file was found but it seems you have saved songs in your songbook. Would you like to generate a collection file to repair the songbook? Alternatively, you can create a new songbook or load an existing one from a local or remote zip file.", "Repair songbook", JOptionPane.YES_NO_CANCEL_OPTION);

        if (resultCode == JOptionPane.YES_OPTION) {
            repairMissingCollectionFile();
        } else if (resultCode == JOptionPane.NO_OPTION) {
            new EnvironmentManager().createNewSongbook();
        } else if (resultCode == JOptionPane.CANCEL_OPTION) {
            new EnvironmentManager().loadSongbook();
        } else if (resultCode == JOptionPane.CLOSED_OPTION) {

        }

        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
    }

    private void createSongbookDialog() {
        UIManager.put("OptionPane.yesButtonText", "Create");
        UIManager.put("OptionPane.noButtonText", "Load");

        int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Create a songbook", "Do you want to create a new songbook? Alternatively, you can load an existing one from a local or remote zip file.", JOptionPane.YES_NO_CANCEL_OPTION);

        if (resultCode == JOptionPane.YES_OPTION) {
            collection = new ArrayList<Song>();
            new EnvironmentManager().createNewSongbook();
        } else if (resultCode == JOptionPane.NO_OPTION) {
            new EnvironmentManager().loadSongbook();
        } else if (resultCode == JOptionPane.CANCEL_OPTION) {

        } else if (resultCode == JOptionPane.CLOSED_OPTION) {

        }

        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
    }

    private void repairMissingCollectionFile() {
        logger.info("Repairing standard collection");
        collection = new ArrayList<Song>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Environment.getInstance().settings.collections.get(StandardCollectionManager.getInstance().getCollectionName()).getSongDataFilePath()))) {
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

}
