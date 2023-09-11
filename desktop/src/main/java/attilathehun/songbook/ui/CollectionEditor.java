package attilathehun.songbook.ui;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.util.PDFGenerator;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CollectionEditor extends JFrame {

    private static final Logger logger = LogManager.getLogger(CollectionEditor.class);

    private static boolean CONTROL_PRESSED = false;

    private static boolean SHIFT_PRESSED = false;

    private static CollectionEditor instance = null;

    private static final int ACTION_EDIT = 0;
    private static final int ACTION_ADD = 1;

    private JTabbedPane tabPane;

    private Song selectedSong = null;

    private CollectionManager selectedManager = null;

    private JList list = null;

    private CollectionEditor() {
        instance = this;
        setContentPane(new JPanel(new BorderLayout()));
        setTitle("Collection Editor");
        setSize(800, 600);
        setResizable(false);
        addTableHeader();
        addTabbedPane();
        addBottomToolbar();
        registerKeyboardShortcuts();
        registerWindowListener();
        registerKeybinds();
        setVisible(true);
    }

    public static CollectionEditor openCollectionEditor() {
        if (instance != null) {
            instance.setVisible(true);
            instance.forceRefreshAll();
            instance.toFront();
            return null;
        }
        return new CollectionEditor();
    }

    public static CollectionEditor getInstance() {
        return instance;
    }

    private void registerKeybinds() {
        registerInputs();
        registerActions();
    }

    private void registerInputs() {

        JPanel panel = (JPanel)instance.getContentPane();

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0), "ctrlPressedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), "ctrlReleasedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0), "shiftPressedAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "shiftReleasedAction");

    }

    private void registerActions() {

        Action deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("X");
                refreshStoredSelection();
                if (selectedSong == null) {
                    return;
                }
                int result = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?", "Delete song '" + selectedSong.name() + "' id: " + selectedSong.id(), JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    selectedManager.removeSong(selectedSong);
                    forceRefreshList();
                    selectedSong = null;
                }
            }
        };
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

        JPanel panel = (JPanel)instance.getContentPane();

        panel.getActionMap().put("deleteAction", deleteAction);
        panel.getActionMap().put("ctrlPressedAction", ctrlPressedAction);
        panel.getActionMap().put("ctrlReleasedAction", ctrlReleasedAction);
        panel.getActionMap().put("shiftPressedAction", shiftPressedAction);
        panel.getActionMap().put("shiftReleasedAction", shiftReleasedAction);

    }

    public static void forceRefreshInstance() {
        if (instance == null) {
            return;
        }
        instance.forceRefreshAll();
    }

    /**
     * @source <a href="https://stackoverflow.com/questions/309023/how-to-bring-a-window-to-the-front">...</a>
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

    private void addTableHeader() {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setOpaque(true);
        JLabel collectionIdLabel = new JLabel(" n");
        JLabel songNameLabel = new JLabel("Name");
        JLabel songIdLabel = new JLabel("ID");
        JLabel songURLLabel = new JLabel("URL");//new JLabel(((Song) value).getUrl());
        JLabel isSongActiveBox = new JLabel("Active");

        collectionIdLabel.setPreferredSize(new Dimension(30, 20));
        songNameLabel.setPreferredSize(new Dimension(250, 20));
        songIdLabel.setPreferredSize(new Dimension(30, 20));
        songURLLabel.setPreferredSize(new Dimension(400, 20));
        isSongActiveBox.setPreferredSize(new Dimension(60, 20));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.05d;
        constraints.weighty = 1;
        panel.add(collectionIdLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.4d;
        panel.add(songNameLabel, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0.05d;
        panel.add(songIdLabel, constraints);

        constraints.gridx = 3;
        constraints.weightx = 5d;
        panel.add(songURLLabel, constraints);

        constraints.gridx = 4;
        constraints.weightx = 0.0d;
        panel.add(isSongActiveBox, constraints);

        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        getContentPane().add(panel, BorderLayout.PAGE_START);
    }

    private void addTabbedPane() {
        tabPane = new JTabbedPane();
        tabPane.setTabPlacement(JTabbedPane.BOTTOM);
        JComponent standardCollectionPanel = new CollectionPanel(StandardCollectionManager.getInstance());
        tabPane.addTab("Standard Collection", null, standardCollectionPanel,
                "Default song collection");

        if (Environment.getInstance().settings.environment.IS_IT_EASTER_ALREADY && EasterCollectionManager.getInstance().getCollection() != null) {
            JComponent easterCollectionPanel = new CollectionPanel(EasterCollectionManager.getInstance());
            tabPane.addTab("Easter Collection", null, easterCollectionPanel,
                    "Collection of easter eggs");
        }

        tabPane.addChangeListener(e -> {
            logger.debug("Tab switched: " + tabPane.getSelectedIndex());
            refreshStoredSelection();
        });

        getContentPane().add(tabPane, BorderLayout.CENTER);
    }

    private void addBottomToolbar() {
        JPanel bottomToolbar = new JPanel(new GridLayout(1, 6));

        JButton editSongRecordButton = new JButton("Edit Song Record");
        editSongRecordButton.addActionListener(e -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }

            Song song = EnvironmentManager.editSongDialog(selectedSong, selectedManager);

            if (song == null) {
                return;
            }

            forceRefreshList();
            int index = selectedManager.getDisplayCollectionSongIndex(song);
            if (index != -1) {
                list.setSelectedValue(index, true);
            }
        });

        JButton editSongHTMLButton = new JButton("Edit Song HTML");
        editSongHTMLButton.addActionListener(e -> {
            refreshStoredSelection();

            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            CodeEditor editor = new CodeEditor(selectedManager);
            editor.setTitle(String.format("HTML editor - %s (id: %d)", selectedSong.name(), selectedSong.id()));
            editor.setSong(selectedSong);
            editor.setVisible(true);
        });

        JButton viewSongInBrowserButton = new JButton("View Song");
        viewSongInBrowserButton.addActionListener(e -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            int index = Environment.getInstance().getCollectionManager().getFormalCollectionSongIndex(selectedSong);
            if (index == -1) {
                Environment.showMessage("Message", "Something went wrong.");
                return;
            }
            if (index % 2 == 0) {
                SongbookApplication.dialImaginarySongOneKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index));
                if (index + 1 >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
                    SongbookApplication.dialImaginarySongTwoKeyPressed(CollectionManager.getShadowSong());
                } else {
                    SongbookApplication.dialImaginarySongTwoKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index + 1));
                }
            } else {
                SongbookApplication.dialImaginarySongTwoKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index));
                if (index - 1 < 0) {
                    SongbookApplication.dialImaginarySongOneKeyPressed(CollectionManager.getShadowSong());
                } else {
                    SongbookApplication.dialImaginarySongOneKeyPressed(Environment.getInstance().getCollectionManager().getFormalCollection().get(index - 1));
                }
            }
        });
        JButton previewSongPDFButton = new JButton("Preview PDF");
        previewSongPDFButton.addActionListener(e -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Desktop.getDesktop().open(new File(new PDFGenerator(selectedManager).generatePreview(selectedSong).replace(".html", ".pdf")));
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                            Environment.showErrorMessage("Error", ex.getMessage());
                        }
                    }
                });

        });
        JButton deleteSongButton = new JButton("Delete Song");
        deleteSongButton.addActionListener(e -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            int result = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?", "Delete song '" + selectedSong.name() + "' id: " + selectedSong.id(), JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                selectedManager.removeSong(selectedSong);
                forceRefreshList();
                selectedSong = null;
            }
        });
        JButton addNewSongButton = new JButton("Add New Song");
        addNewSongButton.addActionListener(e -> {
            refreshStoredSelection();
            Song song;
            if (CONTROL_PRESSED) {
                song = EnvironmentManager.addEasterSongFromTemplateDialog(selectedSong, selectedManager);
            } else {
                song = EnvironmentManager.addSongDialog(selectedManager);
            }
            if (song == null) {
                return;
            }
            forceRefreshList();
            list.setSelectedValue(song, true);
        });

        bottomToolbar.add(editSongRecordButton);
        bottomToolbar.add(editSongHTMLButton);
        bottomToolbar.add(viewSongInBrowserButton);
        bottomToolbar.add(previewSongPDFButton);
        bottomToolbar.add(deleteSongButton);
        bottomToolbar.add(addNewSongButton);

        bottomToolbar.setBorder(new EmptyBorder(0, -1, 0, -1));

        getContentPane().add(bottomToolbar, BorderLayout.PAGE_END);
    }

    private void refreshStoredSelection() {
        CollectionPanel tabPanel = ((CollectionPanel) tabPane.getComponentAt(tabPane.getSelectedIndex()));
        selectedSong = tabPanel.getSelectedSong();
        selectedManager = tabPanel.getCollectionManager();
        list = tabPanel.getList();
        logger.debug("Selected tab CollectionManager: " + selectedManager.getClass().getName());
    }

    private void registerKeyboardShortcuts() {
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    CONTROL_PRESSED = true;
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    list.ensureIndexIsVisible(list.getSelectedIndex());
                } else if (CONTROL_PRESSED && e.getKeyCode() == KeyEvent.VK_R) {
                    forceRefreshList();
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

    private void registerWindowListener() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                dispose();
                instance = null;
            }
        });
    }

    private void forceRefreshList() {
        DefaultListModel listModel = new DefaultListModel();
        refreshStoredSelection();
        for (Song song : selectedManager.getSortedCollection()) {
            listModel.addElement(song);
        }
        list.setModel(listModel);
    }

    private void forceRefreshAll() {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            DefaultListModel listModel = new DefaultListModel();
            CollectionPanel panel = (CollectionPanel) tabPane.getComponentAt(i);
            CollectionManager targetManager = panel.getCollectionManager();
            for (Song song : targetManager.getSortedCollection()) {
                listModel.addElement(song);
            }
            panel.getList().setModel(listModel);
        }
        logger.debug("Forcefully refreshed all lists");
    }

    private static class CollectionPanel extends JPanel {

        private static final Logger logger = LogManager.getLogger(CollectionPanel.class);

        private CollectionManager manager;
        private JList list;

        private Song selectedSong;


        private CollectionPanel() {
            throw new RuntimeException("Constructor not allowed!");
        }

        public CollectionPanel(CollectionManager manager) {
            super(new BorderLayout());
            setDoubleBuffered(false);
            this.manager = manager;
            DefaultListModel listModel = new DefaultListModel();
            for (Song song : manager.getSortedCollection()) {
                listModel.addElement(song);
            }
            list = new JList(listModel);
            list.setLayoutOrientation(JList.VERTICAL);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            CollectionPanelListRenderer listCellRenderer = new CollectionPanelListRenderer();
            list.setCellRenderer(listCellRenderer);

            list.addListSelectionListener(e -> {
                if (list.getSelectedIndex() == -1) {
                    return;
                }
                selectedSong = manager.getSortedCollection().get(list.getSelectedIndex());
            });


            add(new JScrollPane(list), BorderLayout.CENTER);
        }

        public JList getList() {
            return list;
        }

        public CollectionManager getCollectionManager() {
            return manager;
        }

        public Song getSelectedSong() {
            return selectedSong;
        }
    }

    private static class CollectionPanelListRenderer extends JPanel implements ListCellRenderer {

        private JLabel collectionIdLabel;
        private JLabel songNameLabel;
        private JLabel songIdLabel;
        private JLabel songURLLabel;
        private JCheckBox isSongActiveBox;

        public CollectionPanelListRenderer() {
            super(new GridBagLayout());
        }


        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            setOpaque(true);
            collectionIdLabel = new JLabel(" " + index);
            songNameLabel = new JLabel(((Song) value).name());
            songIdLabel = new JLabel(String.valueOf(((Song) value).id()));
            songURLLabel = new JLabel(((Song) value).getUrl());
            isSongActiveBox = new JCheckBox("", ((Song) value).isActive());

            collectionIdLabel.setPreferredSize(new Dimension(30, 20));
            songNameLabel.setPreferredSize(new Dimension(250, 20));
            songIdLabel.setPreferredSize(new Dimension(30, 20));
            songURLLabel.setPreferredSize(new Dimension(400, 20));
            isSongActiveBox.setPreferredSize(new Dimension(40, 20));

            removeAll();
            revalidate();

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.05d;
            constraints.weighty = 1;
            add(collectionIdLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0.4d;
            add(songNameLabel, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0.05d;
            add(songIdLabel, constraints);

            constraints.gridx = 3;
            constraints.weightx = 5d;
            add(songURLLabel, constraints);

            constraints.gridx = 4;
            constraints.weightx = 0.0d;
            add(isSongActiveBox, constraints);

            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else if (!((Song) value).isActive()) {
                setBackground(new Color(165, 164, 168));

            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

}
