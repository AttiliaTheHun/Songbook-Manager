package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import attilathehun.songbook.collection.CollectionListener;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.misc.Misc;
import eu.mihosoft.monacofx.MonacoFX;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CodeEditor extends Stage implements CollectionListener {
    private static final Logger logger = LogManager.getLogger(CodeEditor.class);
    private static final HashMap<Song, CodeEditor> instances = new HashMap<>();
    private final Song song;
    private final CollectionManager manager;
    private String filePath;
    private MonacoFX monaco;

    private CodeEditor(Song s, CollectionManager manager) {
        if (s == null) {
            throw new IllegalArgumentException("Song must not be null!");
        }
        this.song = s;
        if (manager == null) {
            if (s.getManager() == null) {
                this.manager = Environment.getInstance().getCollectionManager();
            } else {
                this.manager = s.getManager();
            }
        } else {
            this.manager = manager;
        }
        init();
    }


    public static void open(Song s, CollectionManager m) {
        CodeEditor instance = new CodeEditor(s, m);
        instances.put(instance.song, instance);
        instance.show();
    }

    private void init() {
        monaco = new MonacoFX();
        StackPane root = new StackPane(monaco);

        filePath = manager.getSongFilePath(song);

        if (!Misc.fileExists(filePath)) {
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage("Song file not found. This should have never happened.").addOkButton().build().open();
            return;
        }

        try {
            ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(filePath)));
            monaco.getEditor().getDocument().setText(String.join("\n", lines));

            if (this.manager.equals(EasterCollectionManager.getInstance())) {
                setTitle(String.format("[E] HTML editor - %s (id: %d)", song.name(), song.id()));
            } else {
                setTitle(String.format("HTML editor - %s (id: %d)", song.name(), song.id()));
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Editor").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Error: Cannot open song data file.").addOkButton().build().open();
        }

        monaco.getEditor().setCurrentLanguage("html");
        monaco.getEditor().setCurrentTheme("vs-light");

        Scene scene = new Scene(root, 800, 600);
        setScene(scene);
        show();

        registerKeyboardShortcuts();
        registerWindowListener();
        registerTextChangedListener();
    }


    private void destroy() {
        instances.remove(song);
        close();
    }


    private void registerKeyboardShortcuts() {
        this.getScene().setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.S) {
                if (keyEvent.isControlDown()) {
                    saveChanges();
                    if (getTitle().startsWith("*")) {
                        setTitle(getTitle().substring(1));
                    }
                }
            }
        });
    }


    @TODO(description = "move from JOptionPane")
    private void registerWindowListener() {
        setOnCloseRequest(event -> {
            if (getTitle().startsWith("*")) {
                if (SongbookApplication.isShiftPressed()) {
                    destroy();
                    return;
                }
                event.consume();
                CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("HTML Editor").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage("Saves changes?").addOkButton("Save").addCloseButton("Discard").build().awaitResult();
                result.thenAccept(dialogResult -> {
                    if (dialogResult == AlertDialog.RESULT_OK) {
                        saveChanges();
                    }
                    if (dialogResult != AlertDialog.RESULT_CANCEL) {
                        destroy();
                    }
                });
            }
        });
    }

    private void registerTextChangedListener() {
        monaco.getEditor().getDocument().textProperty().addListener((event) -> {
            if (!getTitle().startsWith("*")) {
                setTitle("*" + getTitle());
            }
        });
    }

    private void saveChanges() {
        try {
            FileWriter writer = new FileWriter(filePath, false);
            writer.write(monaco.getEditor().getDocument().getText());
            writer.close();
            manager.updateSongRecordTitleFromHTML(song);
            Environment.getInstance().refresh();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("HTML Editor").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .setMessage(String.format("Error: cannot save the changes. You can save them manually to the path %s from your clipboard.", filePath)).addOkButton().build().open();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(monaco.getEditor().getDocument().getText()), new StringSelection(monaco.getEditor().getDocument().getText()));
        }
    }


    @TODO
    @Override
    public void onSongRemoved(Song s, CollectionManager m) {
        /*if (s.equals(song) && manager.equals(m)) {
            UIManager.put("OptionPane.okButtonText", "Recreate");
            UIManager.put("OptionPane.cancelButtonText", "Close");

            int option = JOptionPane.showConfirmDialog(this, "The song you are editing was deleted. If you want, you can recreate it. Otherwise, this editor will close.", String.format("Song deleted: %s (%d)", s.name(), s.id()), JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                m.addSong(s);
                saveChanges();
                return;
            }
            close(this);
            UIManager.put("OptionPane.okButtonText", "Ok");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");
        }*/
    }

    @TODO(description = "if BIND_SONG_TITLES is active, notify about change (if change happened)")
    @Override
    public void onSongUpdated(Song s, CollectionManager m) {

    }

    @Override
    public void onSongAdded(Song s, CollectionManager m) {

    }
}
