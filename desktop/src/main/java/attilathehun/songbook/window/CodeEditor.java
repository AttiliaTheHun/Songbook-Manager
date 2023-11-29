package attilathehun.songbook.window;

import attilathehun.songbook.collection.CollectionListener;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.Misc;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.embed.swing.SwingNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CodeEditor extends Stage implements CollectionListener, DocumentListener {
    private static final Logger logger = LogManager.getLogger(CodeEditor.class);
    private static boolean SHIFT_PRESSED = false;

    private String filePath;
    private Song song;

    private final RSyntaxTextArea textArea;
    private final CollectionManager manager;

    private CodeEditor() {
        throw new RuntimeException("Constructor not allowed");
    }

    public CodeEditor(CollectionManager manager, Song s) {
        setTitle("R2G2");
        if (s == null) {
            throw new IllegalArgumentException();
        }
        if (manager == null) {
            if (s.getManager() == null) {
                this.manager = Environment.getInstance().getCollectionManager();
            } else {
                this.manager = s.getManager();
            }

        } else {
            this.manager = manager;
        }

        this.setResizable(true);

        AnchorPane root = new AnchorPane();
        SwingNode node = new SwingNode();

        JPanel cp = new JPanel(new BorderLayout());
        textArea = new RSyntaxTextArea(100, 120);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        textArea.setCodeFoldingEnabled(true);
        textArea.getDocument().addDocumentListener(this);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        node.setContent(cp);
        root.getChildren().add(node);
        setScene(new Scene(root));

        this.setSong(s);

        this.show();
    }

    private void registerKeyboardShortcuts() {
        focusedProperty().addListener((ov, onHidden, onShown) -> {
            if (onHidden) {
                SHIFT_PRESSED = false;
            }
        });

        this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case S -> {
                        if (keyEvent.isControlDown()) {
                            saveChanges();
                            if (getTitle().startsWith("*")) {
                                setTitle(getTitle().substring(1));
                            }
                        }
                    }
                    case SHIFT-> {
                        if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
                            SHIFT_PRESSED = true;
                        } else if(keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
                            SHIFT_PRESSED = false;
                        }
                    }
                }
            }
        });
    }

    private void registerWindowListener() {
       setOnCloseRequest( event -> {
           if (getTitle().startsWith("*")) {
               boolean confirmed = false;
               if (SHIFT_PRESSED) {
                   confirmed = true;
               } else {
                   String message = "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?";
                   confirmed = Environment.showConfirmMessage("Songbook Manager HTML Editor", "Do you want to save the changes?", null);
               }
               if (!confirmed) {
                   return;
               }
           }
           close();
       });
    }

    private void setSong(Song s) {
        this.filePath = manager.getSongFilePath(s);
        this.song = s;

        if (Misc.fileExists(filePath)) {
            try {
                ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(filePath)));
                textArea.setRows(lines.size());
                textArea.setText(String.join("\n", lines));
                if (this.manager.equals(EasterCollectionManager.getInstance())) {
                    setTitle(String.format("[E] HTML editor - %s (id: %d)", s.name(), s.id()));
                } else {
                    setTitle(String.format("HTML editor - %s (id: %d)", s.name(), s.id()));
                }

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                Environment.showErrorMessage("Songbook Manager HTML Editor", "Error", "Can not open song data file");
            }

        } else {
            Environment.showMessage("Songbook Manager HTML Editor", "Error", "This should never have happened!");
        }
    }

    private void saveChanges() {
        try {
            FileWriter writer = new FileWriter(filePath, false);
            writer.write(textArea.getText());
            writer.close();
            manager.updateSongRecordTitleFromHTML(song);
            Environment.getInstance().refresh();
            SongbookApplication.dialControlPLusRPressed();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("Songbook Manager HTML Editor", "Error", "Can not save the changes! You can save them manually to the path " + filePath + "from your clipboard");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), new StringSelection(textArea.getText()));
        }
    }


    @Override //TODO
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

    @Override
    public void onSongUpdated(Song s, CollectionManager m) {

    }

    @Override
    public void onSongAdded(Song s, CollectionManager m) {

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!getTitle().startsWith("*")) {
            setTitle("*" + getTitle());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (!getTitle().startsWith("*")) {
            setTitle("*" + getTitle());
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (!getTitle().startsWith("*")) {
            setTitle("*" + getTitle());
        }
    }
}
