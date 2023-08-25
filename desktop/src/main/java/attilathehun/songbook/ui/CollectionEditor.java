package attilathehun.songbook.ui;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CollectionEditor extends JFrame {

    private static boolean CONTROL_PRESSED = false;

    private static CollectionEditor instance = null;

    private static final int ACTION_EDIT = 0;
    private static final int ACTION_ADD = 1;

    private JTabbedPane tabPane;

    private Song selectedSong = null;

    private CollectionManager selectedManager = null;

    private JList list = null;

    private CollectionEditor() {
        instance = this;
        setTitle("Collection Editor");
        setSize(800, 600);
        setResizable(false);
        addTableHeader();
        addTabbedPane();
        addBottomToolbar();
        registerKeyboardShortcuts();
        setVisible(true);
    }

    public static CollectionEditor openCollectionEditor() {
        if (instance != null) {
            instance.setVisible(true);
            instance.toFront();
            return null;
        }
        return new CollectionEditor();
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

        add(panel, BorderLayout.PAGE_START);
    }

    private void addTabbedPane() {
        tabPane = new JTabbedPane();
        tabPane.setTabPlacement(JTabbedPane.BOTTOM);
        JComponent standardCollectionPanel = new CollectionPanel(StandardCollectionManager.getInstance());
        tabPane.addTab("Standard Collection", null, standardCollectionPanel,
                "Default song collection");

        if (Environment.getInstance().settings.IS_IT_EASTER_ALREADY) {
            JComponent easterCollectionPanel = new CollectionPanel(EasterCollectionManager.getInstance());
            tabPane.addTab("Easter Collection", null, easterCollectionPanel,
                    "Collection of easter eggs");
        }

        tabPane.addChangeListener(e -> {
            System.out.println("Tab switched: " + tabPane.getSelectedIndex());
            //refreshStoredSelection();
        });

        add(tabPane, BorderLayout.CENTER);
    }

    private void addBottomToolbar() {
        JPanel bottomToolbar = new JPanel(new GridLayout(1, 6));

        JButton editSongRecordButton = new JButton("Edit Song Record");
        editSongRecordButton.addActionListener(e -> {
            actionDialog(ACTION_EDIT);
        });

        JButton editSongHTMLButton = new JButton("Edit Song HTML");
        editSongHTMLButton.addActionListener(e -> {
            refreshStoredSelection();

            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            CodeEditor editor = new CodeEditor();
            editor.setTitle(String.format("HTML editor - %s (id: %d)", selectedSong.name(), selectedSong.id()));
            editor.setSong(selectedSong);
            editor.setVisible(true);
        });

        JButton viewSongInBrowserButton = new JButton("View in Browser");
        viewSongInBrowserButton.addActionListener(e -> {
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            //TODO
        });
        JButton previewSongPDFButton = new JButton("Preview PDF");
        previewSongPDFButton.addActionListener(e -> {
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            //TODO
        });
        JButton deleteSongButton = new JButton("Delete Song");
        deleteSongButton.addActionListener(e -> {
            if (selectedSong == null) {
                Environment.showMessage("Message", "Select a song first.");
                return;
            }
            int result = JOptionPane.showConfirmDialog(null, "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?", "Delete song '" + selectedSong.name() + "' id: " + selectedSong.id(), JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                selectedManager.removeSong(selectedSong);
                forceRefreshList();
                selectedSong = null;
            }
        });
        JButton addNewSongButton = new JButton("Add New Song");
        addNewSongButton.addActionListener(e -> {
            actionDialog(ACTION_ADD);
        });
        bottomToolbar.add(editSongRecordButton);
        bottomToolbar.add(editSongHTMLButton);
        bottomToolbar.add(viewSongInBrowserButton);
        bottomToolbar.add(previewSongPDFButton);
        bottomToolbar.add(deleteSongButton);
        bottomToolbar.add(addNewSongButton);

        bottomToolbar.setBorder(new EmptyBorder(0, -1, 0, -1));

        add(bottomToolbar, BorderLayout.PAGE_END);
    }

    private void refreshStoredSelection() {
        CollectionPanel tabPanel = ((CollectionPanel) tabPane.getComponentAt(tabPane.getSelectedIndex()));
        selectedSong = tabPanel.getSelectedSong();
        selectedManager = tabPanel.getCollectionManager();
        list = tabPanel.getList();
        System.out.println("Selected tab CollectionManager: " + selectedManager.getClass().getName());
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

    public void actionDialog(int action) {

        if (action < ACTION_EDIT || action > ACTION_ADD) {
            throw new IllegalArgumentException();
        }

        refreshStoredSelection();

        if (action == ACTION_EDIT && selectedSong == null) {
            Environment.showMessage("Message", "Select a song first.");
            return;
        }

        JTextField songNameField = new JTextField();
        songNameField.setToolTipText("Name of the song. For example 'I Will Always Return'.");
        JTextField songURLField = new JTextField();
        songURLField.setToolTipText("Link to a video performance of the song.");
        JCheckBox songActiveSwitch = new JCheckBox("Active");
        songActiveSwitch.setToolTipText("When disabled, the song will not be included in the songbook.");

        NumberFormat longFormat = NumberFormat.getIntegerInstance();

        NumberFormatter numberFormatter = new NumberFormatter(longFormat);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setMinimum(0l);

        JFormattedTextField songIdField = new JFormattedTextField(numberFormatter);
        songIdField.setToolTipText("Identificator of the song. Do not confuse with collection index (n).");

        Object[] message;
        int option;

        switch (action) {
            case ACTION_EDIT:
                UIManager.put("OptionPane.okButtonText", "Save Changes");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                songNameField.setText(selectedSong.name());
                songURLField.setText(selectedSong.getUrl());
                songActiveSwitch.setSelected(selectedSong.isActive());
                if (selectedManager.equals(EasterCollectionManager.getInstance())) {
                    songIdField.setText(String.valueOf(selectedSong.id()));
                    message = new Object[]{
                            "Name:", songNameField,
                            "Id: ", songIdField,
                            "URL:", songURLField,
                            songActiveSwitch
                    };
                } else {
                    message = new Object[]{
                            "Name:", songNameField,
                            "URL:", songURLField,
                            songActiveSwitch
                    };
                }


                option = JOptionPane.showConfirmDialog(null, message, "Edit Song id: " + selectedSong.id(), JOptionPane.OK_CANCEL_OPTION);

                UIManager.put("OptionPane.okButtonText", "Yes");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                if (option == JOptionPane.OK_OPTION) {
                    Song song = null;
                    if (selectedManager.equals(EasterCollectionManager.getInstance())) {
                        if (songIdField.getText().equals("")) {
                            Environment.showWarningMessage("Warning", "Invalid Id value!");
                            return;
                        }
                        song = new Song(songNameField.getText(), Integer.parseInt(songIdField.getText()));
                        if (selectedSong.id() != song.id()) {
                            song.setFormerId(selectedSong.id());
                        }
                    } else {
                        song = new Song(songNameField.getText(), selectedSong.id());
                    }

                    song.setUrl(songURLField.getText());
                    song.setActive(songActiveSwitch.isSelected());
                    selectedManager.updateSongRecord(song);
                    forceRefreshList();
                    int index = getSongIndexInSortedCollection(song);
                    if (index != -1) {
                        list.setSelectedValue(index, true);
                    }


                }

                break;

            case ACTION_ADD:
                UIManager.put("OptionPane.okButtonText", "Add");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                JTextField songAuthorField = new JTextField();
                Song placeholder = selectedManager.getPlaceholderSong();

                songNameField.setText(placeholder.name());
                songAuthorField.setText(placeholder.getAuthor());
                songURLField.setText(placeholder.getUrl());
                songActiveSwitch.setSelected(true);


                songAuthorField.setToolTipText("Author or interpret of the song. For example 'Leonard Cohen'.");

                if (selectedManager.equals(EasterCollectionManager.getInstance()) || (selectedManager.equals(StandardCollectionManager.getInstance()) && CONTROL_PRESSED)) {
                    songIdField.setText(String.valueOf(placeholder.id()));
                    message = new Object[]{
                            "Name:", songNameField,
                            "Author:", songAuthorField,
                            "Id: ", songIdField,
                            "URL:", songURLField,
                            songActiveSwitch
                    };
                } else {
                    message = new Object[]{
                            "Name:", songNameField,
                            "Author:", songAuthorField,
                            "URL:", songURLField,
                            songActiveSwitch
                    };
                }

                option = JOptionPane.showConfirmDialog(null, message, "Add a Song", JOptionPane.OK_CANCEL_OPTION);

                UIManager.put("OptionPane.okButtonText", "Ok");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");
                //TODO: when browsing standard collection, holding CTRL and clicking 'Add New Song' creates an easter copy of the selected song
                //TODO: Ctrl+R should forceRefreshList
                if (option == JOptionPane.YES_OPTION) {
                    Song song = null;
                    if (selectedManager.equals(EasterCollectionManager.getInstance()) || (selectedManager.equals(StandardCollectionManager.getInstance()) && CONTROL_PRESSED)) {
                        if (songIdField.getText().equals("")) {
                            Environment.showWarningMessage("Warning", "Invalid Id value!");
                            return;
                        }
                        song = new Song(songNameField.getText(), Integer.parseInt(songIdField.getText()));
                    } else {
                        song = new Song(songNameField.getText(), -1);
                    }
                    song.setUrl(songURLField.getText());
                    song.setAuthor(songAuthorField.getText());
                    song.setActive(songActiveSwitch.isSelected());
                    song = selectedManager.addSong(song);
                    if (song == null) {
                        return;
                    }
                    forceRefreshList();
                    list.setSelectedValue(song, true);
                }


                break;

        }
    }

    private int getSongIndexInSortedCollection(Song s) {
        ArrayList<Song> sortedCollection = selectedManager.getSortedCollection();
        for (int i = 0; i < sortedCollection.size(); i++) {
            if (sortedCollection.get(i).id() == s.id()) {
                return i;
            }
        }
        return -1;
    }

    private void forceRefreshList() {
        DefaultListModel listModel = new DefaultListModel();
        for (Song song : selectedManager.getSortedCollection()) {
            listModel.addElement(song);
        }
        list.setModel(listModel);
    }

    private static class CollectionPanel extends JPanel {
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

            //System.out.println(new Gson().toJson(value));
            //setPreferredSize(new Dimension(getParent().getWidth()));
            setOpaque(true);
            collectionIdLabel = new JLabel(" " + String.valueOf(index));
            songNameLabel = new JLabel(((Song) value).name());
            songIdLabel = new JLabel(String.valueOf(((Song) value).id()));
            songURLLabel = new JLabel("");//new JLabel(((Song) value).getUrl());
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
