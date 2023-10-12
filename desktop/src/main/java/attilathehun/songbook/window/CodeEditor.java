package attilathehun.songbook.window;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.collection.CollectionManager;
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

    private final CollectionManager manager;

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

    public CodeEditor(CollectionManager manager) {
        if (manager == null) {
            this.manager = Environment.getInstance().getCollectionManager();
        } else {
            this.manager = manager;
        }

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

                if (SHIFT_PRESSED || !getTitle().trim().startsWith("*")) {
                    removeInstance();
                    SHIFT_PRESSED = false;
                    setVisible(false);
                    return;
                }

                int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to save the changes?", "Save changes?", JOptionPane.YES_NO_OPTION);

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

        registerInputs();
        registerActions();

        toFront();
        instances++;
    }

    public CodeEditor() {
        this(Environment.getInstance().getCollectionManager());
    }

    private void registerInputs() {

        JPanel panel = (JPanel)getContentPane();

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0), "ctrlPressedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), "ctrlReleasedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0), "shiftPressedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "shiftReleasedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "sAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "vAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "cAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.KEY_TYPED, 0), "keyTypedAction");

    }

    private void registerActions() {

        Action ctrlPressedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("A");
                CONTROL_PRESSED = true;
            }
        };
        Action ctrlReleasedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("B");
                CONTROL_PRESSED = false;
            }
        };
        Action shiftPressedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("C");
                SHIFT_PRESSED = true;
            }
        };
        Action shiftReleasedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("D");
                SHIFT_PRESSED = false;
            }
        };
        Action sAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("E");
                if (SHIFT_PRESSED) {
                    saveChanges();
                    if (getTitle().startsWith("*")) {
                        CodeEditor.super.setTitle(getTitle().substring(1));
                    }
                }

            }
        };
        Action vAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("F");
                if (SHIFT_PRESSED) {

                }
            }
        };
        Action cAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("G");
                if (SHIFT_PRESSED) {

                }
            }
        };
        Action keyTypedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("H");
                if (!getTitle().startsWith("*")) {
                    CodeEditor.super.setTitle("*" + getTitle());
                }
            }
        };


        JPanel panel = (JPanel)getContentPane();

        panel.getActionMap().put("ctrlPressedAction", ctrlPressedAction);
        panel.getActionMap().put("ctrlReleasedAction", ctrlReleasedAction);
        panel.getActionMap().put("shiftPressedAction", shiftPressedAction);
        panel.getActionMap().put("shiftReleasedAction", shiftReleasedAction);
        panel.getActionMap().put("sAction", sAction);
        panel.getActionMap().put("vAction", vAction);
        panel.getActionMap().put("cAction", cAction);
        panel.getActionMap().put("keyTypedAction", keyTypedAction);
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
        this.filePath = manager.getSongFilePath(s);
        this.song = s;

        if (Environment.fileExists(filePath)) {
            try {
                ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(filePath)));
                textArea.setRows(lines.size());
                textArea.setText(String.join("\n", lines));
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
        if (manager.equals(EasterCollectionManager.getInstance())) {
            title = "[E] " + s;
        }
        super.setTitle(title);
    }
}
