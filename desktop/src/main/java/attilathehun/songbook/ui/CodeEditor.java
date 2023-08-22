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

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.environment.Environment;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

public class CodeEditor extends  JFrame {

    private String filePath;

    private Song song;

    private final RSyntaxTextArea textArea;

    public CodeEditor()  {

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

              //  JFrame frame = new JFrame();

                /*final JOptionPane optionPane = new JOptionPane(
                        "The only way to close this dialog is by\n"
                                + "pressing one of the following buttons.\n"
                                + "Do you understand?",
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.OK_CANCEL_OPTION);*/
                int resultCode = JOptionPane.showConfirmDialog(new JDialog(), "Do you want to save the changes?", "Save changes?", JOptionPane.OK_CANCEL_OPTION);

                if (resultCode == JOptionPane.OK_OPTION) {
                    saveChanges();
                    setVisible(false);
                } else if (resultCode == JOptionPane.CANCEL_OPTION) {

                } else if (resultCode == JOptionPane.CLOSED_OPTION) {
                    setVisible(false);
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
            Environment.getInstance().getCollectionManager().updateSongRecord(song);
        } catch (IOException exp) {
            exp.printStackTrace();
            Environment.getInstance().logTimestamp();
            exp.printStackTrace(Environment.getInstance().getLogPrintStream());
            Environment.showWarningMessage("Warning", "Can not save the changes! You can save them manually to the path " + filePath + "from your clipboard");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), new StringSelection(textArea.getText()));
        }
    }
}
