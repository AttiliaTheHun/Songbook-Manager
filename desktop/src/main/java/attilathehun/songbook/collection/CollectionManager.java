package attilathehun.songbook.collection;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CollectionManager {

    private static CollectionManager instance;
    private Collection<Song> collection;

    public abstract Collection<Song> getCollection();

    public abstract ArrayList<Song> getSortedCollection();

    public abstract ArrayList<Song> getFormalCollection();

    public abstract void init();

    public abstract String getSongFilePath(Song s);

    public abstract String getSongFilePath(int id);

    public abstract void updateSongRecord(Song s);
    public abstract void updateSongRecordFromHTML(Song s);

    public abstract void save();

    public abstract Song addSong(Song s);

    public abstract void removeSong(Song s);

    public abstract void deactivateSong(Song s);

    public abstract void activateSong(Song s);
}
