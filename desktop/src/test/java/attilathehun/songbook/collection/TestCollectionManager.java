package attilathehun.songbook.collection;

import attilathehun.songbook.environment.Environment;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestCollectionManager extends CollectionManager {

    private final List<CollectionListener> listeners = new ArrayList<>();

    private static int instanceNumber = 0;
    private final String collectionName = String.format("test%d", instanceNumber);

    public TestCollectionManager() {
        instanceNumber++;
    }

    @Override
    public void load() {

    }

    @Override
    public void init() {

    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public CollectionManager copy() {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public Collection<Song> getCollection() {
        return null;
    }

    @Override
    public ArrayList<Song> getSortedCollection() {
        return null;
    }

    @Override
    public ArrayList<Song> getDisplayCollection() {
        return null;
    }

    @Override
    public ArrayList<Song> getFormalCollection() {
        return null;
    }

    @Override
    public Song addSong(Song s) {
        return null;
    }

    @Override
    public void removeSong(Song s) {

    }

    @Override
    public void activateSong(Song s) {

    }

    @Override
    public void deactivateSong(Song s) {

    }

    @Override
    public Song updateSongRecord(Song s) {
        return null;
    }

    @Override
    public void updateSongRecordTitleFromHTML(Song s) {

    }

    @Override
    public void updateSongHTMLTitleFromRecord(Song s) {

    }

    @Override
    public void updateSongHTMLFromRecord(Song s) {

    }

    @Override
    public String getSongFilePath(Song s) {
        return null;
    }

    @Override
    public String getSongFilePath(int id) {
        return null;
    }

    @Override
    public int getCollectionSongIndex(Song s) {
        return 0;
    }

    @Override
    public int getCollectionSongIndex(int songId) {
        return 0;
    }

    @Override
    public int getSortedCollectionSongIndex(Song s) {
        return 0;
    }

    @Override
    public int getSortedCollectionSongIndex(int songId) {
        return 0;
    }

    @Override
    public int getDisplayCollectionSongIndex(Song s) {
        return 0;
    }

    @Override
    public int getDisplayCollectionSongIndex(int songId) {
        return 0;
    }

    @Override
    public int getFormalCollectionSongIndex(Song s) {
        return 0;
    }

    @Override
    public int getFormalCollectionSongIndex(int songId) {
        return 0;
    }

    @Override
    public Song getPlaceholderSong() {
        return null;
    }

    @Override
    public void addListener(CollectionListener listener) {
        listeners.add(listener);
    }

    public List<CollectionListener> getListeners() {
        return listeners;
    }

    @Override
    public void removeListener(CollectionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public CompletableFuture<Song> addSongDialog() {
        return null;
    }

    @Override
    public CompletableFuture<Song> editSongDialog(Song s) {
        return null;
    }

    @Override
    public String getCollectionFilePath() {
        return "path1";
    }

    @Override
    public String getRelativeFilePath() {
        return "path2";
    }

    @Override
    public String getSongDataFilePath() {
        return "path3";
    }

}
