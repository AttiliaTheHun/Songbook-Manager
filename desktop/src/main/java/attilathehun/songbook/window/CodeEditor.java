package attilathehun.songbook.window;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.collection.CollectionListener;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.KeyEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

public class CodeEditor extends JFrame implements KeyEventListener, DocumentListener, CollectionListener {

    private static final Logger logger = LogManager.getLogger(CodeEditor.class);

    private static final HashMap<Song, CodeEditor> instanceMap = new HashMap<>();

    private static int instances = 0;

    private static boolean isApplicationClosed = false;
    private static int focusedInstances = 0;

    private String filePath;

    private Song song;

    private boolean IS_FOCUSED = false;

    private final RSyntaxTextArea textArea;

    private final CollectionManager manager;

    public static int instanceCount() {
        return instances;
    }


    public static boolean hasFocusedInstance() {
        return focusedInstances != 0;
    }

    private void addFocusedInstance() {
        focusedInstances += 1;
    }

    private void removeFocusedInstance() {
        focusedInstances -= 1;
    }

    public static void setApplicationClosed() {
        CodeEditor.isApplicationClosed = true;
    }

    private CodeEditor() {
        throw new RuntimeException("Constructor not allowed");
    }

    private CodeEditor(CollectionManager manager, Song s) {
        if (manager == null) {
            this.manager = Environment.getInstance().getCollectionManager();
        } else {
            this.manager = manager;
        }

        JPanel cp = new JPanel(new BorderLayout());

        textArea = new RSyntaxTextArea(100, 120);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        textArea.setCodeFoldingEnabled(true);
        textArea.getDocument().addDocumentListener(this);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        registerWindowListener();
        registerWindowFocusListener();
        SongbookApplication.addListener(this);
        manager.addListener(this);
        setSong(s);


        instances++;
        setVisible(true);
        toFront();
        textArea.requestFocus();
    }

    /**
     * Opens a CodeEditor window for the specific song. If the song is already open in a CodeEditor, pushes the target
     * instance to front instead.
     * @param manager the collection manager to use
     * @param s target Song object
     */
    public static void open(CollectionManager manager, Song s) {
        if (manager == null && s == null) {
            throw  new IllegalArgumentException();
        }
        if (instanceMap.get(s) == null) {
            CodeEditor instance = new CodeEditor(manager, s);
            instanceMap.put(s,instance);
            instance.toFront();
            return;
        }
        instanceMap.get(s).toFront();
    }

    /**
     * Opens a CodeEditor window for the specific song. If the song is already open in a CodeEditor, pushes the target
     * instance to front instead. Assumes the song belongs to the StandardCollectionManager.
     * @param s target Song object
     */
    public static void open(Song s) {
        open(Environment.getInstance().getCollectionManager(), s);
    }

    private static void close(CodeEditor instance) {
        instances--;
        instanceMap.remove(instance.song);
        if (instances == 0 && isApplicationClosed) {
            Environment.getInstance().exit();
        }
        instance.setVisible(false);
    }


    private void registerWindowListener() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        CodeEditor reference = this;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                if (SongbookApplication.isShiftPressed() || !getTitle().trim().startsWith("*")) {
                    close(reference);
                    return;
                }

                int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to save the changes?", "Save changes?", JOptionPane.YES_NO_OPTION);

                if (resultCode == JOptionPane.YES_OPTION) {
                    saveChanges();
                    close(reference);
                } else if (resultCode == JOptionPane.NO_OPTION) {
                    close(reference);
                } else if (resultCode == JOptionPane.CLOSED_OPTION) {

                }
            }
        });
    }

    private void registerWindowFocusListener() {
        addWindowFocusListener(new WindowAdapter() {

            //To check window gained focus
            public void windowGainedFocus(WindowEvent e) {
                addFocusedInstance();
                IS_FOCUSED = true;
            }

            //To check window lost focus
            public void windowLostFocus(WindowEvent e) {
                removeFocusedInstance();
                IS_FOCUSED = false;
            }
        });
    }


    /**
     * @source <a href="https://stackoverflow.com/questions/309023/how-to-bring-a-window-to-the-front">stackoverflow</a>
     * @author Lawrence Dol
     */
    public @Override void toFront() {
        int sta = super.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;

        super.setExtendedState(sta);
        super.setAlwaysOnTop(true);
        super.toFront();
        super.requestFocusInWindow();
        super.setAlwaysOnTop(false);
    }

    private void setSong(Song s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        this.filePath = manager.getSongFilePath(s);
        this.song = s;

        if (Environment.fileExists(filePath)) {
            try {
                ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(filePath)));
                textArea.setRows(lines.size());
                textArea.setText(String.join("\n", lines));
                setTitle(String.format("HTML editor - %s (id: %d)", s.name(), s.id()));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                Environment.showErrorMessage("Error", "Can not open song data file");
            }

        } else {
            Environment.showMessage("No way", "This should never have happened");
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
            Environment.showErrorMessage("Error", "Can not save the changes! You can save them manually to the path " + filePath + "from your clipboard");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), new StringSelection(textArea.getText()));
        }
    }

    @Override
    public void setTitle(String s) {
        String title = s;

        if (s.startsWith("*")) {
            if (getTitle().startsWith("*")) {
                return;
            }
            super.setTitle(title);
            return;
        }
        if (manager.equals(EasterCollectionManager.getInstance()) && !getTitle().startsWith("[E]")) {
            title = "[E] " + s;
        }
        super.setTitle(title);
    }

    @Override
    public void onLeftArrowPressed() {

    }

    @Override
    public void onRightArrowPressed() {

    }

    @Override
    public void onControlPlusRPressed() {

    }

    @Override
    public void onDeletePressed() {

    }

    @Override
    public void onControlPlusSPressed() {
        if (IS_FOCUSED) {
            saveChanges();
            if (getTitle().startsWith("*")) {
                CodeEditor.super.setTitle(getTitle().substring(1));
            }
        }
    }

    @Override
    public void onImaginarySongOneKeyPressed(Song s) {

    }

    @Override
    public void onImaginarySongTwoKeyPressed(Song s) {

    }

    @Override
    public boolean onImaginaryIsTextFieldFocusedKeyPressed() {
        return false;
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

    @Override
    public void onSongRemoved(Song s, CollectionManager m) {
        if (s.equals(song) && manager.equals(m)) {
            UIManager.put("OptionPane.okButtonText", "Recreate");
            UIManager.put("OptionPane.cancelButtonText", "Close");

            int option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "The song you are editing was deleted. If you want, you can recreate it. Otherwise, this editor will close.", String.format("Song deleted: %s (%d)", s.name(), s.id()), JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                m.addSong(s);
                saveChanges();
                return;
            }
            close(this);
            UIManager.put("OptionPane.okButtonText", "Ok");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");
        }
    }

    @Override
    public void onSongUpdated(Song s, CollectionManager m) {

    }

    @Override
    public void onSongAdded(Song s, CollectionManager m) {

    }

}
