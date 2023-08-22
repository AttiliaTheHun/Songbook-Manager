package attilathehun.songbook.collection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.*;

import attilathehun.songbook.environment.Environment;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * This class manages the collection of the songs and any regarding operations.
 */
public class StandardCollectionManager extends CollectionManager {
    private static final StandardCollectionManager instance = new StandardCollectionManager();

    private ArrayList<Song> collection;

    private StandardCollectionManager() {
        init();
    }

    public static StandardCollectionManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        try {
            File file = new File(Environment.getInstance().settings.COLLECTION_FILE_PATH);
            if (!file.exists()) {
                //collection does not exist yet
            }

            String json = String.join("", Files.readAllLines(file.toPath()));

            Type targetClassType = new TypeToken<ArrayList<Song>>() { }.getType();
            collection = new Gson().fromJson(json, targetClassType);
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showErrorMessage("Refreshing error", "Can not reload the song collection!");
        }
    }

    @Override
    public String getSongFilePath(int id) {
        return Paths.get(String.format("%s/%d.html", Environment.getInstance().settings.SONG_DATA_FILE_PATH, id)).toString();
    }

    @Override
    public String getSongFilePath(Song s) {
        return getSongFilePath(s.id());
    };

    @Override
    public Collection<Song> getCollection() {
        return collection;
    }

    @Override
    public ArrayList<Song> getSortedCollection() {
        ArrayList<Song> copy = new ArrayList<Song>(collection);
        Collections.sort(copy, new Comparator<Song>() {
            public int compare(Song s1, Song s2) {
                return Collator.getInstance().compare(s1.name(), s2.name());
            }
        });
        return copy;
    }

    /**
     * Adds frontpage and songlists as songs to the beginning of the list.
     * @return ArrayList of songs
     */
    @Override
    public ArrayList<Song> getFormalCollection() {
        ArrayList<Song> formalList = new ArrayList<Song>();
        formalList.add(new Song("frontpage", -1));
        formalList.add(new Song("songlist1", -1));
        formalList.add(new Song("songlist2", -1));
        formalList.addAll(getSortedCollection());
        return formalList;
    }

    @Override
    public void updateSongRecord(Song s) {
        try {
            String songHTML = String.join("\n", Files.readAllLines(Paths.get(getSongFilePath(s.id()))));
            Document document = Jsoup.parse(songHTML);
            Element element = document.select(".song-title").first();
            String songName = element.text();
            System.out.println(songName);

            if (!s.name().equals(songName)) {
                collection.get(getSongIndex(s)).setName(songName);
            }
            save();
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", String.format("A song record could not be updated! Song: %s id: %d", s.name(), s.id()));
        }

    }

    private int getSongIndex(Song s) {
        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i).id() == s.id()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void save() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Environment.getInstance().settings.COLLECTION_FILE_PATH);
            gson.toJson(collection, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Environment.getInstance().logTimestamp();
            e.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Error", "Can not save the song collection!");
        }
    }
}
