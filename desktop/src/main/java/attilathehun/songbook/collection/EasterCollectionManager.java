package attilathehun.songbook.collection;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.HTMLGenerator;
import attilathehun.songbook.vcs.CacheManager;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO
public final class EasterCollectionManager extends CollectionManager {
    private static final Logger logger = LogManager.getLogger(EasterCollectionManager.class);
    private final String collectionName = "easter";
    private static final List<CollectionListener> listeners = new ArrayList<>();
    private static final EasterCollectionManager INSTANCE = new EasterCollectionManager();


    private ArrayList<Song> collection;

    private EasterCollectionManager() {

    }

    private EasterCollectionManager(ArrayList<Song> collection) {
        this.collection = new ArrayList<Song>(collection);
    }

    public static EasterCollectionManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        if (!Environment.IS_IT_EASTER_ALREADY) {
            return;
        }

        try {
            final File collectionJSONFile = new File(getCollectionFilePath());

            if (!collectionJSONFile.exists()) {
                final File songDataFolder = new File(getSongDataFilePath());
                if (songDataFolder.exists() && songDataFolder.isDirectory()) {
                    repairCollectionDialog();
                } else {
                    createCollectionDialog();
                    return;
                }
            }

            Environment.getInstance().registerCollectionManager(this);

            final String json = String.join("", Files.readAllLines(collectionJSONFile.toPath()));

            final Type targetClassType = new TypeToken<ArrayList<Song>>(){}.getType();
            collection = new Gson().fromJson(json, targetClassType);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Easter Collection Initialisation error").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .setMessage("Can not load the easter song collection, for complete error message view the log file. If the problem persists, try reformatting or deleting the collection file.")
                            .addOkButton().setParent(SongbookApplication.getMainWindow()).build().open();
            return;
        }
        logger.info("initialized");
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
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final FileWriter writer = new FileWriter(getCollectionFilePath());
            gson.toJson(collection, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Can not save the easter song collection, for complete error message view the log file.")
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
    public Song addSong(final Song s) {
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
        onSongAdded(s);
        CodeEditor.open(s, this);
        Environment.getInstance().refresh();
        if (Environment.getInstance().getCollectionManager().equals(getInstance())) {
            Environment.navigateWebViewToSong(collection.get(collection.size() - 1));
        }
        return s;
    }

    @Override
    public void removeSong(Song s) {
        collection.remove(getCollectionSongIndex(s));
        save();
        File songFile = new File(String.format(getSongDataFilePath() + "/%d.html", s.id()));
        if (songFile.delete()) {
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage(String.format("Easter Egg Song '%s' id: %d deleted. Ave Caesar!", s.name(), s.id()))
                    .addOkButton().build().open();
        }
        onSongRemoved(s);
        if (Environment.getInstance().getCollectionManager().equals(getInstance())) {
            Environment.getInstance().refresh();
        }
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
    public Song updateSongRecord(final Song s) {
        int songIdMatches = 0;
        for (Song song : collection) {
            if (song.id() == s.id()) {
                songIdMatches++;
            }
        }

        if (songIdMatches > 1) {
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                    .setMessage("Easter song under this Id already exists. Please resolve this conflict manually by changing the Id of the other song, or by choosing a different Id.")
                    .addOkButton().build().open();
           return null;
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


            File oldFile= new File(getSongFilePath(s.getFormerId()));
            File newFile = new File(getSongFilePath(s.id()));
            if (newFile.exists()) {
                new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                        .setMessage(String.format("A file corresponding to the new Easter song Id already exists. To prevent data loss you should manually rename it to another Id. Alternatively you can delete the file to resolve this issues. File path: %s", newFile.getPath()))
                        .addOkButton().build().open();
                return null;
            }
            if (!oldFile.renameTo(newFile)) {
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage("Something went wrong when renaming the song file. More information may be found in application log file.")
                        .addOkButton().build().open();
                return null;
            }
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
        onSongUpdated(song);
        return song;
    }

    @Override
    public void updateSongRecordTitleFromHTML(final Song s) {
        // It is undesirable to bind title in easter egg collection
    }

    @Override
    public void updateSongHTMLTitleFromRecord(final Song s) {
        // five lines above
    }

    @Override
    // TODO
    public void updateSongHTMLFromRecord(final Song s) {
        if (!CollectionManager.isValidId(s.id())) {
            throw new IllegalArgumentException();
        }
        try {
            String songHTML = String.join("\n", Files.readAllLines(Path.of(getSongFilePath(s))));
            Document document = Jsoup.parse(songHTML);
            Element element = document.select("meta[name=url]").first();
            element.attr("value", s.getUrl());
            element = document.select("meta[name=active]").first();
            element.attr("value", String.valueOf(s.isActive()));

            FileOutputStream out = new FileOutputStream(getSongFilePath(s));
            out.write(document.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();

        } catch (Exception e) {
            logger.error(String.format("Target song: %d", s.id()));
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String getSongFilePath(Song s) {
        return getSongFilePath(s.id());
    }

    @Override
    public String getSongFilePath(int id) {
        return Paths.get(String.format("%s/%d.html", getSongDataFilePath(), id)).toString();
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
        return new Song("Secret Song", CollectionManager.INVALID_SONG_ID);
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
    public String getCollectionFilePath() {
        return Paths.get(SettingsManager.getInstance().getValue("DATA_FILE_PATH"), String.format(CollectionManager.COLLECTION_FILE_NAME, collectionName)).toString();
    }

    @Override
    public String getRelativeFilePath() {
        return Paths.get(String.format(CollectionManager.RELATIVE_DATA_FILE_NAME, collectionName)).toString();
    }

    @Override
    public String getSongDataFilePath() {
        return Paths.get(SettingsManager.getInstance().getValue("SONGS_FILE_PATH"),  collectionName).toString();
    }

    @Override
    public CompletableFuture<Song> addSongDialog() {
       return addSongDialog(getPlaceholderSong());
    }

    private CompletableFuture<Song> addSongDialog(final Song s) {
        CompletableFuture<Song> output = new CompletableFuture<>();
        CheckBox songActiveSwitch = new CheckBox("Active");
        songActiveSwitch.setSelected(s.isActive());
        songActiveSwitch.setTooltip(new Tooltip("When disabled, the song will not be included in the songbook."));
        CompletableFuture<Pair<Integer, ArrayList<Node>>> dialogResult = new AlertDialog.Builder().setTitle("Add an Easter song").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setParent(SongbookApplication.getMainWindow())
                .addTextInput("Name:", s.name(), "Enter song name", "Name of the song. For example 'I Will Always Return'.")
                .addTextInput("Id:", String.valueOf(s.id()), "Enter song id", "Identificator of the song. Do not confuse with collection index (n).")
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
            Song song = new Song(((TextField) result.getValue().get(1)).getText(), Integer.parseInt(((TextField) result.getValue().get(3)).getText()));
            song.setAuthor(((TextField) result.getValue().get(5)).getText());
            song.setUrl(((TextField) result.getValue().get(7)).getText());
            song.setActive(songActiveSwitch.isSelected());
            song = addSong(song);
            output.complete(song);
        });
        return output;
    }

    public CompletableFuture<Song> addSongFromTemplateDialog(final Song s) {
        return addSongDialog(s);
    }

    @Override
    public CompletableFuture<Song> editSongDialog(final Song s) {
        CompletableFuture<Song> output = new CompletableFuture<>();
        CheckBox songActiveSwitch = new CheckBox("Active");
        songActiveSwitch.setSelected(s.isActive());
        songActiveSwitch.setTooltip(new Tooltip("When disabled, the song will not be included in the songbook."));
        CompletableFuture<Pair<Integer, ArrayList<Node>>> dialogResult = new AlertDialog.Builder().setTitle(String.format("Edit eater song id: %s", s.id())).setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setParent(SongbookApplication.getMainWindow())
                .addTextInput("Name:", s.name(), "Enter song name", "Name of the song. For example 'I Will Always Return'.")
                .addTextInput("Id:", String.valueOf(s.id()), "Enter song id", "Identificator of the song. Do not confuse with collection index (n).")
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
            Song song = new Song(((TextField) result.getValue().get(1)).getText(), Integer.parseInt(((TextField) result.getValue().get(3)).getText()));
            song.setFormerId(s.id());
            song.setUrl(((TextField) result.getValue().get(5)).getText());
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

    private void onSongRemoved(final Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongRemoved(s, this);
        }
    }

    private void onSongUpdated(final Song s) {
        for (CollectionListener listener : listeners) {
            listener.onSongUpdated(s, this);
        }
    }


    /* Internal helper methods */

    private Song createSongRecordFromLocalFile(final int songId) {
        if (!CollectionManager.isValidId(songId)) {
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
            boolean songActiveStatus = (element != null) ? Boolean.parseBoolean(element.attr("value")) : true;

            return new Song(songId, songName, songActiveStatus, songURL);
        } catch (Exception e) {
            logger.info(String.format("Target song: %d %s", songId, songName));
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void repairCollectionDialog() {
        CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Repair easter egg collection").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("No collection file was found but it seems there are songs saved in your easter egg data directory. Would you like to generate a collection file to repair the easter egg collection? Alternatively, you can create a new collection.")
                .addOkButton("Repair").addCloseButton("Create").setParent(SongbookApplication.getMainWindow()).setCancelable(false).build().awaitResult();
        result.thenAccept(dialogResult -> {
            if (dialogResult == AlertDialog.RESULT_OK) {
                repairMissingCollectionFile();
            } else if (dialogResult == AlertDialog.RESULT_CLOSE) {
                createNewCollection();
            }
        });
    }

    private void createCollectionDialog() {
        CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Create an easter egg collection").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("Do you want to create an easter egg collection? For more information visit the wiki.")
                .addOkButton("Create").addCloseButton("Cancel").build().awaitResult();
        result.thenAccept(dialogResult -> {
            if (dialogResult == AlertDialog.RESULT_OK) {
                createNewCollection();
            }
        });
    }

    private void repairMissingCollectionFile() {
        logger.info("Repairing easter collection");
        collection = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(getSongDataFilePath()))) {
            for (final Path path : stream) {

                if (!Files.isDirectory(path) && path.toString().trim().endsWith(".html")) {
                    int songId;
                    final String pathString = path.toString();
                    if (pathString.contains(File.separator)) {
                        songId = Integer.parseInt(pathString.substring(pathString.lastIndexOf(File.separator) + 1, pathString.indexOf(".html")));
                    } else {
                        songId = Integer.parseInt(pathString.substring(0, pathString.indexOf(".html")));
                    }
                    collection.add(createSongRecordFromLocalFile(songId));
                }
            }
            // cache it before the collection file is created so its last modified date does not get cached
            CacheManager.getInstance().cacheSongbookVersionTimestamp();

            save();
            // no changes were done by creating the collection file, so we do not want to make the VCS think we changed something
            new File(getCollectionFilePath()).setLastModified(CacheManager.getInstance().getCachedSongbookVersionTimestamp());

            logger.info("Success!");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void createNewCollection() {
        try {
            final File songDataFolder = new File(getSongDataFilePath());
            songDataFolder.mkdirs();
            final File collectionJSONFile = new File(getCollectionFilePath());
            collectionJSONFile.createNewFile();
            collection = new ArrayList<Song>();
            final PrintWriter printWriter = new PrintWriter(new FileWriter(collectionJSONFile));
            printWriter.write("[]");
            printWriter.close();

            final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Add Easter Song?").setMessage("Do you want to create your first easter song?").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .addOkButton("Create").addCloseButton("Cancel").build().awaitResult();
            result.thenAccept(dialogResult -> {
               if (dialogResult == AlertDialog.RESULT_OK) {
                   addSongDialog();
               }
            });

        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setMessage("Could not create a new easter song!").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .addOkButton().build().open();
        }
    }

}
