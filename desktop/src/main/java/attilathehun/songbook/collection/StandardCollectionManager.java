package attilathehun.songbook.collection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.plugin.DynamicSonglist;
import attilathehun.songbook.plugin.Frontpage;
import attilathehun.songbook.plugin.Plugin;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.ui.CodeEditor;
import attilathehun.songbook.util.HTMLGenerator;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;


public class StandardCollectionManager extends CollectionManager {

    private static final Logger logger = LogManager.getLogger(StandardCollectionManager.class);
    private static final StandardCollectionManager instance;

    static {
        instance = new StandardCollectionManager();
        instance.init();
    }

    private ArrayList<Song> collection;

    private StandardCollectionManager() {

    }

    public static StandardCollectionManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        try {
            File collectionJSONFile = new File(Environment.getInstance().settings.COLLECTION_FILE_PATH);
            if (!collectionJSONFile.exists()) {
                File songDataFolder = new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH);
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
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Collection Initialisation error", "Can not load the song collection!");
            //TODO: Wiki Troubleshooting: should be fixed by deleting collection.json and then Repairing (add guide with screenshots)
        }
        logger.debug("StandardCollectionManager initialised");
    }

    private void repairSongbookDialog() {
        UIManager.put("OptionPane.yesButtonText", "Repair");
        UIManager.put("OptionPane.noButtonText", "Create");
        UIManager.put("OptionPane.cancelButtonText", "Load");

        int resultCode = JOptionPane.showConfirmDialog(new JDialog(), "No collection file was found but it seems you have saved songs in your songbook. Would you like to generate a collection file to repair the songbook? Alternatively, you can create a new songbook or load an existing one from a local or remote zip file.", "Repair songbook", JOptionPane.YES_NO_CANCEL_OPTION);

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

    private void repairMissingCollectionFile() {
        //TODO
    }

    private void createSongbookDialog() {
        UIManager.put("OptionPane.yesButtonText", "Create");
        UIManager.put("OptionPane.noButtonText", "Load");

        int resultCode = JOptionPane.showConfirmDialog(new JDialog(), "Create a songbook", "Do you want to create a new songbook? Alternatively, you can load an existing one from a local or remote zip file.", JOptionPane.YES_NO_CANCEL_OPTION);

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

    @Override
    public String getSongFilePath(int id) {
        return Paths.get(String.format("%s/%d.html", Environment.getInstance().settings.SONG_DATA_FILE_PATH, id)).toString();
    }

    @Override
    public String getSongFilePath(Song s) {
        return getSongFilePath(s.id());
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
    public ArrayList<Song> getDisplayCollection() {
        ArrayList<Song> displayList = new ArrayList<Song>(new ArrayList<Song>(getSortedCollection().stream().filter(Song::isActive).collect(Collectors.toList())));
        return displayList;
    }

    @Override
    public Song updateSongRecord(Song s) {
        int index = getSongIndex(s);

        collection.get(index).setName(s.name());
        collection.get(index).setUrl(s.getUrl());
        collection.get(index).setActive(s.isActive());

        save();
        Environment.getInstance().refresh();
        SongbookApplication.dialControlPLusRPressed();
        return  s;
    }

    //TODO: <strike>Do we want this at all? And if so, </strike>editing the record from CollectionEditor should update the HTML
    @Override
    public void updateSongRecordFromHTML(Song s) {
        if (!Environment.getInstance().settings.BIND_SONG_TITLES) {
            return;
        }
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            Environment.showWarningMessage("Error", "Can not save the song collection!");
        }
    }

    @Override
    public Song addSong(Song s) {
        Song song = s;
        song.setId(getNextId());

        if (!new HTMLGenerator().generateSongFile(song)) {
            return null;
        }
        collection.add(song);
        save();
        CodeEditor editor = new CodeEditor();
        editor.setTitle(String.format("HTML editor - %s (id: %d)", song.name(), song.id()));
        editor.setSong(s);
        editor.setVisible(true);
        Environment.getInstance().refresh();
        /*int index = 0;
        boolean isPageOne = false;
        ArrayList<Song> formalCollection = getFormalCollection();
        for (int i = 0; i < formalCollection.size(); i++) {
            if (formalCollection.get(i).equals(s)) {
                isPageOne = i % 2 == 0 ? false : true;
                index = i;
                break;
            }
        }
        if (isPageOne) {
            SongbookApplication.dialImaginarySongOneKeyPressed(song);
            SongbookApplication.dialImaginarySongTwoKeyPressed(formalCollection.get(index + 1));
        } else {
            SongbookApplication.dialImaginarySongOneKeyPressed(formalCollection.get(index - 1));
            SongbookApplication.dialImaginarySongTwoKeyPressed(song);
        }*/
        //TODO: navigate webview to the new song if active
        SongbookApplication.dialControlPLusRPressed();
        return song;
    }

    @Override
    public void removeSong(Song s) {
        collection.remove(getSongIndex(s));
        save();
        File songFile = new File(String.format(Environment.getInstance().settings.SONG_DATA_FILE_PATH + "/%d.html", s.id()));
        if (songFile.delete()) {
            Environment.showMessage("Success", String.format("Song '%s' id: %d deleted. Have a nice day!", s.name(), s.id()));
        }
        Environment.getInstance().refresh();
        SongbookApplication.dialControlPLusRPressed();
    }

    @Override
    public void deactivateSong(Song s) {
        collection.get(getSongIndex(s)).setActive(false);
        save();
    }

    @Override
    public void activateSong(Song s) {
        collection.get(getSongIndex(s)).setActive(true);
        save();
    }

    private int getNextId() {
        int id = -1;
        for (Song song : collection) {
            if (song.id() > id) {
                id = song.id();
            }
        }
        System.out.println("Highest id found: " + id);
        return id + 1;
    }

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

}