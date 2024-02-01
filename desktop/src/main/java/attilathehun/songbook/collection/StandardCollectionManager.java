package attilathehun.songbook.collection;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.plugin.DynamicSonglist;
import attilathehun.songbook.plugin.Frontpage;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.util.HTMLGenerator;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.CodeEditor;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class StandardCollectionManager extends CollectionManager {

    private static final Logger logger = LogManager.getLogger(StandardCollectionManager.class);

    private static final List<CollectionListener> listeners = new ArrayList<>();
    private static final StandardCollectionManager INSTANCE = new StandardCollectionManager();
    
    private CollectionSettings settings = getDefaultSettings();

    private final String collectionName = "standard";
    private ArrayList<Song> collection;

    private StandardCollectionManager() {
    }

    private StandardCollectionManager(ArrayList<Song> collection) {
        this.collection = new ArrayList<Song>(collection);
    }

    public static StandardCollectionManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        Environment.getInstance().registerCollectionManager(this);
        try {
            File collectionJSONFile = new File(settings.getCollectionFilePath());
            if (!collectionJSONFile.exists()) {
                File songDataFolder = new File(settings.getSongDataFilePath());
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
            new AlertDialog.Builder().setTitle("Collection Initialisation error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Can not load the song collection, for complete error message view the log file. If the problem persists, try reformatting or deleting the collection file.")
                    .addOkButton().build().open();
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
            FileWriter writer = new FileWriter(settings.getCollectionFilePath());
            gson.toJson(collection, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Can not save the song collection, for complete error message view the log file.")
                    .addOkButton().setParent(SongbookApplication.getMainWindow()).build().open();
        }
    }

    @Override
    public Collection<Song> getCollection() {
        return collection;
    }

    @Override
    public ArrayList<Song> getSortedCollection() {
        ArrayList<Song> copy = new ArrayList<Song>(collection);
        copy.sort((s1, s2) -> Collator.getInstance().compare(s1.name(), s2.name()));
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
        if (Frontpage.getInstance().getSettings().getEnabled()) {
            formalList.add(CollectionManager.getFrontpageSong());
        }
        if (DynamicSonglist.getInstance().getSettings().getEnabled()) {
            for (int i = 0; i < (Integer) PluginManager.getInstance().getPlugin(DynamicSonglist.getInstance().getName()).execute(); i++) {
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
        CodeEditor.open(s, this);
        //Environment.getInstance().refresh();
        Environment.navigateWebViewToSong(collection.get(collection.size() - 1));
        return song;
    }

    @Override
    public void removeSong(Song s) {
        if (s == null || s.id() < 0) {
            throw new IllegalArgumentException();
        }
        collection.remove(getCollectionSongIndex(s));
        save();
        File songFile = new File(String.format("%s/%d.html",settings.getSongDataFilePath(), s.id()));


        if (songFile.delete()) {
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage(String.format("Song '%s' id: %d deleted. Have a nice day!", s.name(), s.id()))
                    .addOkButton().build().open();
        }
        onSongRemoved(s);
        /*if (Environment.getInstance().getCollectionManager().equals(getInstance())) {
            Environment.getInstance().refresh();
        }*/
    }

    @Override
    public void activateSong(Song s) {
        collection.get(getCollectionSongIndex(s)).setActive(true);
        save();
        onSongUpdated(s);
    }

    @Override
    public void deactivateSong(Song s) {
        collection.get(getCollectionSongIndex(s)).setActive(false);
        save();
        onSongUpdated(s);
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
        return s;
    }

    @Override
    public void updateSongRecordTitleFromHTML(Song s) {
        if (!(Boolean) EnvironmentManager.getInstance().getSongbookSettings().get("BIND_SONG_TITLES")) {
            return;
        } // TODO do not forget to emit the songUpdated event
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
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()))
                    .addOkButton().build().open();
        }
    }

    @Override
    public void updateSongHTMLTitleFromRecord(Song s) {
        if (!(Boolean) EnvironmentManager.getInstance().getSongbookSettings().get("BIND_SONG_TITLES")) {
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
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()))
                    .addOkButton().build().open();
        }
    }

    @Override
    public void updateSongHTMLFromRecord(Song s) {
        if (!(Boolean) EnvironmentManager.getInstance().getSongbookSettings().get("BIND_SONG_TITLES")) {
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
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()))
                    .addOkButton().build().open();
        }
    }

    @Override
    public String getSongFilePath(Song s) {
        return getSongFilePath(s.id());
    }

    @Override
    public String getSongFilePath(int id) {
        return Paths.get(String.format("%s/%d.html", settings.getSongDataFilePath(), id)).toString();
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
        if (songId < 0) {
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
    public CollectionSettings getDefaultSettings() {
        CollectionSettings settings = new CollectionSettings();
        settings.put("COLLECTION_FILE_PATH", Paths.get(Environment.getInstance().getSettings().get("DATA_FILE_PATH") + "/collection.json").toString());
        settings.put("SONG_DATA_FILE_PATH", Paths.get(Environment.getInstance().getSettings().get("DATA_FILE_PATH") + "/songs/html/").toString());
        return settings;
    }

    @Override
    public CollectionSettings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(final CollectionSettings c) {
        if (c == null) {
            return;
        }
        settings = c;
    }

    @Override
    public CompletableFuture<Song> addSongDialog() {
        Song s = getPlaceholderSong();
        CompletableFuture<Song> output = new CompletableFuture<>();
        CheckBox songActiveSwitch = new CheckBox("Active");
        songActiveSwitch.setSelected(s.isActive());
        songActiveSwitch.setTooltip(new Tooltip("When disabled, the song will not be included in the songbook."));
        CompletableFuture<Pair<Integer, ArrayList<Node>>> dialogResult = new AlertDialog.Builder().setTitle("Add a song").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                        .setParent(SongbookApplication.getMainWindow())
                        .addTextInput("Name:", s.name(), "Enter song name", "Name of the song. For example 'I Will Always Return'.")
                        .addTextInput("Author:", s.getAuthor(), "Enter song author", "Author or interpret of the song. For example 'Leonard Cohen'.")
                        .addTextInput("URL:", s.getUrl(), "Link to a performance of the song.")
                        .addContentNode(songActiveSwitch)
                        .addOkButton("Add")
                        .addCloseButton("Cancel")
                        .build().awaitData();

        dialogResult.thenAccept((result) -> {
            if (result.getKey() != AlertDialog.RESULT_OK) {
                output.complete(null);
                return;
            }
            Song song = new Song(((TextField) result.getValue().get(1)).getText(), -1);
            song.setUrl(((TextField) result.getValue().get(5)).getText());
            song.setAuthor(((TextField) result.getValue().get(3)).getText());
            song.setActive(songActiveSwitch.isSelected());
            song = addSong(song);
            output.complete(song);
        });
        return output;
    }

    @Override
    public CompletableFuture<Song> editSongDialog(final Song s) {
        CompletableFuture<Song> output = new CompletableFuture<>();
        CheckBox songActiveSwitch = new CheckBox("Active");
        songActiveSwitch.setSelected(s.isActive());
        songActiveSwitch.setTooltip(new Tooltip("When disabled, the song will not be included in the songbook."));
        CompletableFuture<Pair<Integer, ArrayList<Node>>> dialogResult = new AlertDialog.Builder().setTitle(String.format("Edit song id: %s", s.id())).setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setParent(SongbookApplication.getMainWindow())
                .addTextInput("Name:", s.name(), "Enter song name", "Name of the song. For example 'I Will Always Return'.")
                .addTextInput("URL:", s.getUrl(), "Link to a performance of the song.")
                .addContentNode(songActiveSwitch)
                .addOkButton("Add")
                .addCloseButton("Cancel")
                .build().awaitData();

        dialogResult.thenAccept((result) -> {
            if (result.getKey() != AlertDialog.RESULT_OK) {
                output.complete(null);
                return;
            }
            Song song = new Song(((TextField) result.getValue().get(1)).getText(), s.id());
            song.setUrl(((TextField) result.getValue().get(3)).getText());
            song.setActive(songActiveSwitch.isSelected());
            song = updateSongRecord(song);
            output.complete(song);
        });
        return output;
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
            String songHTML = String.join("\n", Files.readAllLines(Paths.get(settings.getSongDataFilePath() + String.format("/%d.html", songId))));
            Document document = Jsoup.parse(songHTML);
            Element element = document.select(".song-title").first();
            songName = element.text();
            element = document.select("meta[name=url]").first();
            String songURL = (element != null) ? element.attr("value") : "";
            element = document.select("meta[name=active]").first();
            boolean songActiveStatus = (element != null) ? Boolean.parseBoolean(element.attr("value")) : true;

            return new Song(songId, songName, songActiveStatus, songURL);
        } catch (Exception e) {
            logger.info(String.format("Target song: %d %s", songId, songName));
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void repairSongbookDialog() {
        CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Repair songbook").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("No collection file was found but it seems there are songs saved in your songbook. Would you like to generate a collection file to repair the songbook? Alternatively, you can create a new songbook or load an existing one from a local zip file.")
                .addOkButton("Repair").addCloseButton("Create New").addExtraButton("Load").setCancelable(false).build().awaitResult();
        result.thenAccept(dialogResult -> {
            if (dialogResult == AlertDialog.RESULT_OK) {
                repairMissingCollectionFile();
            } else if (dialogResult == AlertDialog.RESULT_CLOSE) {
                EnvironmentManager.getInstance().createNewSongbook();
            } else if (dialogResult == AlertDialog.RESULT_EXTRA) {
                EnvironmentManager.getInstance().loadSongbook();
            }
        });
    }

    private void createSongbookDialog() {
        CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Create a songbook").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("Do you want to create a new songbook? Alternatively, you can load an existing one from a local zip file.")
                .addOkButton("Create").addCloseButton("Load").setCancelable(false).build().awaitResult();
        result.thenAccept(dialogResult -> {
            if (dialogResult == AlertDialog.RESULT_OK) {
                EnvironmentManager.getInstance().createNewSongbook();
            } else if (dialogResult == AlertDialog.RESULT_CLOSE) {
                EnvironmentManager.getInstance().loadSongbook();
            }
        });
    }

    private void repairMissingCollectionFile() {
        logger.info("Repairing standard collection");
        collection = new ArrayList<Song>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(settings.getSongDataFilePath()))) {
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
