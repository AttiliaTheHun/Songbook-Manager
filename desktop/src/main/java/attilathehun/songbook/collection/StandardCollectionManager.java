package attilathehun.songbook.collection;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.Misc;
import attilathehun.songbook.util.DynamicSonglist;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.stream.Collectors;


public final class StandardCollectionManager extends CollectionManager {
    private static final Logger logger = LogManager.getLogger(StandardCollectionManager.class);
    private static final List<CollectionListener> listeners = new ArrayList<>();
    private static final StandardCollectionManager INSTANCE = new StandardCollectionManager();
    private final String collectionName = "standard";

    private ArrayList<Song> collection = new ArrayList<>();

    private StandardCollectionManager() {
    }

    private StandardCollectionManager(final ArrayList<Song> collection) {
        this.collection = new ArrayList<>(collection);
    }

    public static StandardCollectionManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void load() {
        Environment.getInstance().registerCollectionManager(this);
        final File collectionJSONFile = new File(getCollectionFilePath());
        collection = Misc.loadObjectFromFileInJSON(new TypeToken<ArrayList<Song>>(){}, collectionJSONFile);

        if (collection == null) {
            Environment.getInstance().exit();
        }

        for (final Song song : collection) {
            song.setManager(this);
        }

        logger.debug("StandardCollectionManager loaded");
    }

    @Override
    public void init() {
        final File songDataFolder = new File(getSongDataFilePath());
        if (songDataFolder.exists() && songDataFolder.isDirectory()) {
            repairSongbookDialog();
        } else {
            createSongbookDialog();
        }

        //load();
        logger.debug("StandardCollectionManager initialized");
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
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final FileWriter writer = new FileWriter(getCollectionFilePath());
            gson.toJson(collection, writer);
            writer.close();
        } catch (final IOException e) {
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
        final ArrayList<Song> displayList = new ArrayList<Song>(new ArrayList<Song>(getSortedCollection().stream().filter(Song::isActive).collect(Collectors.toList())));
        return displayList;
    }

    @Override
    public ArrayList<Song> getFormalCollection() {
        final ArrayList<Song> formalList = new ArrayList<Song>();
        if (SettingsManager.getInstance().getValue("ENABLE_FRONTPAGE")) {
            formalList.add(CollectionManager.getFrontpageSong());
        }
        if (SettingsManager.getInstance().getValue("ENABLE_DYNAMIC_SONGLIST")) {
            for (int i = 0; i < DynamicSonglist.getInstance().getListPages(); i++) {
                formalList.add(CollectionManager.getSonglistSong(i));
            }

        }

        formalList.addAll(new ArrayList<Song>(getSortedCollection().stream().filter(Song::isActive).collect(Collectors.toList())));

        return formalList;
    }

    @Override
    public Song addSong(final Song s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        final Song song = s;
        song.setId(getNextId());

        song.setManager(this);

        if (!new HTMLGenerator().generateSongFile(song)) {
            return null;
        }

        collection.add(song);
        save();
        onSongAdded(s);
        CodeEditor.open(s, this);
        if (Environment.getInstance().getCollectionManager().equals(this)) {
            Environment.navigateWebViewToSong(collection.getLast());
        }
        return song;
    }

    @Override
    public void removeSong(final Song s) {
        if (s == null || s.id() < 0) {
            throw new IllegalArgumentException();
        }
        collection.remove(getCollectionSongIndex(s));
        save();
        final File songFile = new File(String.format("%s/%d.html", getSongDataFilePath(), s.id()));


        if (songFile.delete()) {
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage(String.format("Song '%s' id: %d deleted. Have a nice day!", s.name(), s.id()))
                    .addOkButton().build().open();
        }
        onSongRemoved(s);
    }

    @Override
    public void activateSong(final Song s) {
        if (s == null || (s.getManager() != null && s.getManager() != this)) {
            throw new IllegalArgumentException();
        }
        collection.get(getCollectionSongIndex(s)).setActive(true);
        save();
        onSongUpdated(s);
    }

    @Override
    public void deactivateSong(final Song s) {
        if (s == null || (s.getManager() != null && s.getManager() != this)) {
            throw new IllegalArgumentException();
        }
        collection.get(getCollectionSongIndex(s)).setActive(false);
        save();
        onSongUpdated(s);
    }

    @Override
    public Song updateSongRecord(final Song s) {
        if (s == null || (s.getManager() != null && s.getManager() != this)) {
            throw new IllegalArgumentException();
        }
        int index = getCollectionSongIndex(s.id());

        collection.get(index).setName(s.name());
        collection.get(index).setUrl(s.getUrl());
        collection.get(index).setActive(s.isActive());

        save();
        updateSongHTMLTitleFromRecord(s);
        updateSongHTMLFromRecord(s);
        onSongUpdated(s);
        Environment.getInstance().refresh();
        return s;
    }

    @Override
    public void updateSongRecordTitleFromHTML(final Song s) {
        if (!(Boolean) SettingsManager.getInstance().getValue("BIND_SONG_TITLES")) {
            return;
        }
        if (s == null || !CollectionManager.isValidId(s.id())) {
            return;
        }
        try {
            final String songHTML = String.join("\n", Files.readAllLines(Paths.get(getSongFilePath(s.id()))));
            final Document document = Jsoup.parse(songHTML);
            final Element element = document.select(".song-title").first();
            final String songName = element.text();

            if (!s.name().equals(songName)) {
                collection.get(getCollectionSongIndex(s)).setName(songName);
                save();
                onSongUpdated(collection.get(getCollectionSongIndex(s)));
            }

        } catch (final IOException e) {
            logger.error("song id: {}", s.id());
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()))
                    .addOkButton().build().open();
        }
    }

    @Override
    public void updateSongHTMLTitleFromRecord(final Song s) {
        if (!(Boolean) SettingsManager.getInstance().getValue("BIND_SONG_TITLES")) {
            return;
        }
        if (s == null || !CollectionManager.isValidId(s.id())) {
            return;
        }

        try {
            final String songHTML = String.join("\n", Files.readAllLines(Paths.get(getSongFilePath(s.id()))));
            final Document document = Jsoup.parse(songHTML);
            document.outputSettings().indentAmount(0).prettyPrint(false);
            final Element element = document.select(".song-title").first();
            element.text(s.name());
            FileOutputStream out = new FileOutputStream(getSongFilePath(s));
            out.write(document.select(".song").first().toString().replace("<pre class=\"song-text-formatting\"><span class=\"verse\"", "<pre class=\"song-text-formatting\">\n\n<span class=\"verse\"")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage(String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()))
                    .addOkButton().build().open();
        }
    }

    @Override
    public void updateSongHTMLFromRecord(final Song s) {
        if (!CollectionManager.isValidId(s.id())) {
            throw new IllegalArgumentException();
        }
        try {
            final String songHTML = String.join("\n", Files.readAllLines(Path.of(getSongFilePath(s))));
            final Document document = Jsoup.parse(songHTML);
            document.outputSettings().indentAmount(0).prettyPrint(false);
            Element element = document.select("meta[name=url]").first();
            element.attr("value", s.getUrl());
            element = document.select("meta[name=active]").first();
            element.attr("value", String.valueOf(s.isActive()));

            final FileOutputStream out = new FileOutputStream(getSongFilePath(s));
            out.write(document.select(".song").first().toString().replace("<pre class=\"song-text-formatting\"><span class=\"verse\"", "<pre class=\"song-text-formatting\">\n\n<span class=\"verse\"")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();

        } catch (final Exception e) {
            logger.error("Target song: {}", s.id());
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String getSongFilePath(final Song s) {
        return getSongFilePath(s.id());
    }

    @Override
    public String getSongFilePath(final int id) {
        return Paths.get(String.format("%s/%d.html", getSongDataFilePath(), id)).toString();
    }

    @Override
    public int getCollectionSongIndex(final Song s) {
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
    public int getCollectionSongIndex(final int songId) {
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
    public int getSortedCollectionSongIndex(final Song s) {
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
    public int getSortedCollectionSongIndex(final int id) {
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
    public int getDisplayCollectionSongIndex(final Song s) {
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
    public int getDisplayCollectionSongIndex(final int id) {
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
    public int getFormalCollectionSongIndex(final Song s) {
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
    public int getFormalCollectionSongIndex(final int id) {
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
    public void addListener(final CollectionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(final CollectionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        listeners.remove(listener);
    }

    @Override
    public CompletableFuture<Song> addSongDialog() {
        final Song s = getPlaceholderSong();
        final CompletableFuture<Song> output = new CompletableFuture<>();
        final CheckBox songActiveSwitch = new CheckBox("Active");
        songActiveSwitch.setSelected(s.isActive());
        songActiveSwitch.setTooltip(new Tooltip("When disabled, the song will not be included in the songbook."));
        final CompletableFuture<Pair<Integer, ArrayList<Node>>> dialogResult = new AlertDialog.Builder().setTitle("Add a song").setIcon(AlertDialog.Builder.Icon.CONFIRM)
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
                .addOkButton("Edit")
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


    private void onSongAdded(final Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongAdded(s, this);
        }
    }

    private void onSongUpdated(final Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongUpdated(s, this);
        }
    }

    private void onSongRemoved(final Song s) {
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

    private Song createSongRecordFromLocalFile(final int songId) {
        if (songId == -1) {
            throw new IllegalArgumentException();
        }
        String songName = null;
        try {
            String songHTML = String.join("\n", Files.readAllLines(Paths.get(getSongDataFilePath() + String.format("/%d.html", songId))));
            Document document = Jsoup.parse(songHTML);
            Element element = document.select(".song-title").first();
            songName = element.text();
            element = document.select("meta[name=url]").first();
            String songURL = (element != null) ? element.attr("value") : "";
            element = document.select("meta[name=active]").first();
            boolean songActiveStatus = element == null || Boolean.parseBoolean(element.attr("value"));

            return new Song(songId, songName, songActiveStatus, songURL);
        } catch (final Exception e) {
            logger.error("Target song: {} {}", songId, songName);
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void repairSongbookDialog() {
        final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Repair songbook").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("No collection file was found but it seems there are songs saved in your songbook. Would you like to generate a collection file to repair the songbook? Alternatively, you can create a new songbook or load an existing one from a local zip file.")
                .addOkButton("Repair").addCloseButton("Create New").addExtraButton("Load").setCancelable(false).build().awaitResult();
        result.thenAccept(dialogResult -> {
            if (dialogResult == AlertDialog.RESULT_OK) {
                repairMissingCollectionFile();
                load();
            } else if (dialogResult == AlertDialog.RESULT_CLOSE) {
                EnvironmentManager.getInstance().createNewSongbook();
            } else if (dialogResult == AlertDialog.RESULT_EXTRA) {
                EnvironmentManager.getInstance().loadSongbook();
            }
        });
    }

    private void createSongbookDialog() {
        final String messagePartTwo = (SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) ? " Alternatively, you can download an existing one from the VCS server." : " Alternatively, you can load an existing one from a local zip file.";
        CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Create a songbook").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("Do you want to create a new songbook?" + messagePartTwo)
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
        logger.info("repairing standard collection");
        collection = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(getSongDataFilePath()))) {
            for (final Path path : stream) {

                if (!Files.isDirectory(path) && path.toString().trim().endsWith(".html")) {
                    int songId = -1;
                    final String pathString = path.toString();
                    if (pathString.contains(File.separator)) {
                        songId = Integer.parseInt(pathString.substring(pathString.lastIndexOf(File.separator) + 1, pathString.indexOf(".html")));
                    } else {
                        songId = Integer.parseInt(pathString.substring(0, pathString.indexOf(".html")));
                    }
                    collection.add(createSongRecordFromLocalFile(songId));
                }
            }

            save();

            logger.info("success!");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
