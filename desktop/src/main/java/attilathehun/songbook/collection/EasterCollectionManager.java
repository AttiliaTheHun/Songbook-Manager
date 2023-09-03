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

public class EasterCollectionManager extends CollectionManager {

    private static final Logger logger = LogManager.getLogger(EasterCollectionManager.class);

    public static final String EASTER_SONG_DATA_FILE_PATH = Paths.get(Environment.getInstance().settings.DATA_FILE_PATH + "/songs/egg/").toString();
    private static final EasterCollectionManager instance = new EasterCollectionManager();

    private ArrayList<Song> collection;

    private EasterCollectionManager() {
        init();
    }

    public static EasterCollectionManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        try {
            File collectionJSONFile = new File(Environment.getInstance().settings.EASTER_COLLECTION_FILE_PATH);
            if (!collectionJSONFile.exists()) {
                File songDataFolder = new File(Environment.getInstance().settings.SONG_DATA_FILE_PATH);
                if (songDataFolder.exists() && songDataFolder.isDirectory()) {
                    repairCollectionDialog();
                } else {
                    createCollectionDialog();
                }
                return;
            }

            String json = String.join("", Files.readAllLines(collectionJSONFile.toPath()));

            Type targetClassType = new TypeToken<ArrayList<Song>>() {
            }.getType();
            collection = new Gson().fromJson(json, targetClassType);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Easter Collection Initialisation error", "Can not load the easter egg song collection!");
            //TODO: Wiki Troubleshooting: should be fixed by deleting collection.json and then Repairing (add guide with screenshots)
        }
    }

    private void repairCollectionDialog() {
        UIManager.put("OptionPane.yesButtonText", "Repair");
        UIManager.put("OptionPane.noButtonText", "Create");

        int resultCode = JOptionPane.showConfirmDialog(new JDialog(), "Repair easter egg collection", "No collection file was found but it seems you have saved songs in your easter egg data directory. Would you like to generate a collection file to repair the easter egg collection? Alternatively, you can create a new one.", JOptionPane.YES_NO_OPTION);

        if (resultCode == JOptionPane.YES_OPTION) {
            repairMissingCollectionFile();
        } else if (resultCode == JOptionPane.NO_OPTION) {
            createNewCollection();
        } else if (resultCode == JOptionPane.CLOSED_OPTION) {

        }

        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
    }

    private void repairMissingCollectionFile() {
        //TODO
    }

    private void createNewCollection() {
        //TODO
    }

    private void createCollectionDialog() {
        UIManager.put("OptionPane.okButtonText", "Create");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        int resultCode = JOptionPane.showConfirmDialog(new JDialog(), "Create an easter egg collection", "Do you want to create an easter egg collection?", JOptionPane.OK_CANCEL_OPTION);

        if (resultCode == JOptionPane.OK_OPTION) {
            createNewCollection();
        } else if (resultCode == JOptionPane.CANCEL_OPTION) {

        } else if (resultCode == JOptionPane.CLOSED_OPTION) {

        }

        UIManager.put("OptionPane.okButtonText", "Yes");
        UIManager.put("OptionPane.cancelButtonText", "No");
    }

    @Override
    public String getSongFilePath(int id) {
        return Paths.get(String.format("%s/%d.html", EASTER_SONG_DATA_FILE_PATH, id)).toString();
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
    public ArrayList<Song> getDisplayCollection() {
       /* ArrayList<Song> standardDisplayList = StandardCollectionManager.getInstance().getDisplayCollection();
        ArrayList<Song> displayList = new ArrayList<Song>();

        for (Song song : standardDisplayList) {
            boolean esterEggFound = false;
            if (!song.isActive()) {
                continue;
            }
            searchForEasterEgg:
            for (Song value : collection) {
                if (song.id() == value.id()) {
                    if (value.isActive()) {
                        displayList.add(value);
                        esterEggFound = true;
                    }
                    break searchForEasterEgg;
                }
            }

            if (!esterEggFound) {
                displayList.add(song);
            }*/

        //}

        return StandardCollectionManager.getInstance().getDisplayCollection();
       //return displayList;
    }

    @Override
    public Song updateSongRecord(Song s) {
        int songIdMatches = 0;
        for (Song song : collection) {
            if (song.id() == s.id()) {
                songIdMatches++;
            }
        }

        if (songIdMatches > 1) {
            Environment.showWarningMessage("Warning", "A song with such Id already exists. Please, resolve this conflict manually on the other song before using this Id.");
            return s;
        }

        int index = getSongIndex(s);
        Song song = null;
        if (index == -1) {

            for (Song item : collection) {
                if (item.id() == s.getFormerId()) {
                    song = item;
                }
            }

            index = getSongIndex(song);
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
        return  song;

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
            Environment.showWarningMessage("Warning", String.format("An easter egg song record could not be updated! Song: %s id: %d", s.name(), s.id()));
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
            FileWriter writer = new FileWriter(Environment.getInstance().settings.EASTER_COLLECTION_FILE_PATH);
            gson.toJson(collection, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showWarningMessage("Error", "Can not save the easter egg song collection!");
        }
    }

    // HTMLGenerator uses Environment#getCollectionManager() which may as well point to the standard one!!!
    @Override
    public Song addSong(Song s) {
        Song song = s;
        if (s.id() == -1) {
            throw new IllegalArgumentException();
        }
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
       /* int index = 0;
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
        File songFile = new File(String.format(EASTER_SONG_DATA_FILE_PATH + "/%d.html", s.id()));
        if (songFile.delete()) {
            Environment.showMessage("Success", String.format("Easter Egg Song '%s' id: %d deleted. Ave Caesar!", s.name(), s.id()));
        }
        if (Environment.getInstance().getCollectionManager().equals(getInstance())) {
            Environment.getInstance().refresh();
            SongbookApplication.dialControlPLusRPressed();
        }
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

    public Song getPlaceholderSong() {

        //TODO
        /*int min = 0;
        int max = 21;
        int random = (int)(Math.random() * max + min);
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
        }*/

        // return new Song("New Song", -1);

        return new Song("Secret Song", -1);
    }


}
