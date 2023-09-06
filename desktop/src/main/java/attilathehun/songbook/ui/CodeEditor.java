package attilathehun.songbook.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

public class CodeEditor extends JFrame {

    private static final Logger logger = LogManager.getLogger(CodeEditor.class);

    private static int instances = 0;

    private static boolean isApplicationClosed = false;

    private String filePath;

    private Song song;

    private static boolean SHIFT_PRESSED = false;
    private static boolean CONTROL_PRESSED = false;

    private final RSyntaxTextArea textArea;

    public static int instanceCount() {
        return instances;
    }

    private static void removeInstance() {
        instances--;
        if (instances == 0 && isApplicationClosed) {
            Environment.getInstance().exit();
        }

    }

    public static void setApplicationClosed() {
        CodeEditor.isApplicationClosed = true;
    }

    public CodeEditor() {

        JPanel cp = new JPanel(new BorderLayout());

        textArea = new RSyntaxTextArea(100, 120);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);
        setTitle("Code Editor");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (SHIFT_PRESSED || getTitle().startsWith("*")) {
                    removeInstance();
                    SHIFT_PRESSED = false;
                    setVisible(false);
                    return;
                }

                int resultCode = JOptionPane.showConfirmDialog(new JDialog(), "Do you want to save the changes?", "Save changes?", JOptionPane.YES_NO_OPTION);

                if (resultCode == JOptionPane.YES_OPTION) {
                    saveChanges();
                    removeInstance();
                    setVisible(false);
                } else if (resultCode == JOptionPane.NO_OPTION) {
                    removeInstance();
                    setVisible(false);
                } else if (resultCode == JOptionPane.CLOSED_OPTION) {

                }

            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        textArea.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    SHIFT_PRESSED = true;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    CONTROL_PRESSED = true;
                } else if (CONTROL_PRESSED && e.getKeyCode() == KeyEvent.VK_S) {
                    saveChanges();
                    if (getTitle().startsWith("*")) {
                        CodeEditor.super.setTitle(getTitle().substring(1));
                    }
                } else if (!getTitle().startsWith("*")) {
                    CodeEditor.super.setTitle("*" + getTitle());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    SHIFT_PRESSED = false;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    CONTROL_PRESSED = false;
                }
            }
        });
        toFront();
        instances++;
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

    public void setSong(Song s) {
        this.filePath = Environment.getInstance().getCollectionManager().getSongFilePath(s);
        this.song = s;

        if (Environment.fileExists(filePath)) {
            try {
                ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(filePath)));
                textArea.setRows(lines.size());
                textArea.setText(String.join("\n", lines));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                Environment.showWarningMessage("Warning", "Can not open song data file");
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
            Environment.getInstance().getCollectionManager().updateSongRecordFromHTML(song);
            Environment.getInstance().refresh();
            SongbookApplication.dialControlPLusRPressed();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Environment.showWarningMessage("Warning", "Can not save the changes! You can save them manually to the path " + filePath + "from your clipboard");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), new StringSelection(textArea.getText()));
        }
    }

    @Override
    public void setTitle(String s) {
        String title = s;
        if (Environment.getInstance().getCollectionManager().equals(EasterCollectionManager.getInstance())) {
            title = "[E] " + s;
        }
        super.setTitle(title);
    }
}
