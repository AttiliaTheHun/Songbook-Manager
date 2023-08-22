package attilathehun.songbook.collection;

import attilathehun.songbook.environment.Environment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

//TODO
//KEEP IN MIND that you want the special songs to keep their index within the collection so the special print of the songbook is
//compliant to the default one (when sorting)
public class EasterCollectionManager extends CollectionManager{
    private static final EasterCollectionManager instance = new EasterCollectionManager();


    private Collection<Song> collection;

    private EasterCollectionManager() { }

    public static EasterCollectionManager getInstance() {
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
    public String getSongFilePath(Song s) {
        return getSongFilePath(s.id());
    };

    @Override
    public String getSongFilePath(int id) {
        return null;
    }

    @Override
    public void updateSongRecord(Song s) {

    }

    @Override
    public void save() {

    }

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

    public boolean isEasterSong(Song s) {
        return false;
    }
}
