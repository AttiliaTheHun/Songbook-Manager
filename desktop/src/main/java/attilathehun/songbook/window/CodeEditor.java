package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import attilathehun.songbook.collection.CollectionListener;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.Misc;
import eu.mihosoft.monacofx.MonacoFX;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CodeEditor extends Stage implements CollectionListener {
    private static final Logger logger = LogManager.getLogger(CodeEditor.class);
    private static final HashMap<Song, CodeEditor> instances = new HashMap<>();
    private Song song;
    private final CollectionManager manager;
    private String filePath;
    private MonacoFX monaco;

    private CodeEditor(final Song s, final CollectionManager manager) {
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


    public static void open(final Song s, final CollectionManager m) {
        System.out.println(instances.size());
        CodeEditor instance;
        if (instances.get(s) == null) {
            instance = new CodeEditor(s, m);
            instances.put(instance.song, instance);
        } else {
            instance = instances.get(s);
        }
        instance.show();
        instance.toFront();
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
        manager.addListener(this);
    }


    private void destroy() {
        manager.removeListener(this);
        instances.remove(song);
        close();
        if (!(hasInstanceOpen() || SongbookApplication.getMainWindow().isShowing())) {
            Environment.getInstance().exit();
        }
    }

    public static boolean hasInstanceOpen() {
        return instances.size() > 0;
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
    public void onSongRemoved(final Song s, final CollectionManager m) {
        if (!(song.equals(s) && manager.equals(m))) {
            return;
        }
        CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle(String.format("Song deleted: %s (%d)", s.name(), s.id())).setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("The song you are editing was deleted. If you want, you can recreate it. Otherwise, this editor will close.")
                .setCancelable(false).setParent(this).addOkButton("Recreate").addCloseButton("Close").build().awaitResult();
        result.thenAccept((dialogResult) -> {
            if (dialogResult == AlertDialog.RESULT_OK) {
                song = m.addSong(s);
                filePath = manager.getSongFilePath(song);
                saveChanges();
            } else if (dialogResult == AlertDialog.RESULT_CLOSE) {
                destroy();
            }
        });
    }

    @TODO(description = "if BIND_SONG_TITLES is active, notify about change (if change happened)")
    @Override
    public void onSongUpdated(final Song s, final CollectionManager m) {
        if (s.id() != song.id()) {
            return;
        }
        if (m.equals(EasterCollectionManager.getInstance()) && s.getFormerId() != -1) {

        }

    }

    @Override
    public void onSongAdded(final Song s, final CollectionManager m) {

    }
}
