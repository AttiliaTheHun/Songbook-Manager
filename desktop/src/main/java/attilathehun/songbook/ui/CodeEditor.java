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
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

public class CodeEditor extends JFrame {
    private static int instances = 0;

    private static boolean isApplicationClosed = false;

    private String filePath;

    private Song song;

    private final RSyntaxTextArea textArea;

    public static int instanceCount() {
        return instances;
    }

    private static void removeInstance() {
        instances--;
        if (instances == 0 && isApplicationClosed) {
            System.exit(0);
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

            private boolean CONTROL_PRESSED = false;

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    CONTROL_PRESSED = true;
                } else if (CONTROL_PRESSED && e.getKeyCode() == KeyEvent.VK_S) {
                    saveChanges();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    CONTROL_PRESSED = false;
                }
            }
        });
        toFront();
        instances++;
    }

    /**
     * @source https://stackoverflow.com/questions/309023/how-to-bring-a-window-to-the-front
     * @author Lawrence Dol
     */
    public @Override void toFront() {
        int sta = super.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;

        super.setExtendedState(sta);
        super.setAlwaysOnTop(true);
        super.toFront();
        super.requestFocus();
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
                e.printStackTrace();
                Environment.getInstance().logTimestamp();
                e.printStackTrace(Environment.getInstance().getLogPrintStream());
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
        } catch (IOException exp) {
            exp.printStackTrace();
            Environment.getInstance().logTimestamp();
            exp.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Can not save the changes! You can save them manually to the path " + filePath + "from your clipboard");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), new StringSelection(textArea.getText()));
        }
    }
}
