package attilathehun.songbook.collection;

public interface CollectionListener {

    public void onSongRemoved(Song s, CollectionManager m);
    public void onSongUpdated(Song s, CollectionManager m);
    public void onSongAdded(Song s, CollectionManager m);

}
