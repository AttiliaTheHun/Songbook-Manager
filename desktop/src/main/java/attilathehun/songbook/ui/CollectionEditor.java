package attilathehun.songbook.ui;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import com.google.gson.Gson;
import javafx.scene.control.Tooltip;
import javafx.util.Pair;
import org.controlsfx.control.ToggleSwitch;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

public class CollectionEditor extends JFrame {

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
            JComponent easterCollection = makeTextPanel("Panel 2");
            tabPane.addTab("Easter Collection", null, easterCollection,
                    "Collection of easter eggs");

        }

        /*tabPane.addChangeListener(e -> {
            refreshStoredSelection();
        });*/

        add(tabPane, BorderLayout.CENTER);
    }

    private void addBottomToolbar() {
        JPanel bottomToolbar = new JPanel(new GridLayout(1,2));

        JButton editSongRecordButton = new JButton("Edit Song Record");
        editSongRecordButton.addActionListener(e -> {
            actionDialog(ACTION_EDIT);
        });
        JButton addNewSongButton = new JButton("Add New Song");
        addNewSongButton.addActionListener(e -> {
            actionDialog(ACTION_ADD);
        });
        bottomToolbar.add(editSongRecordButton);
        bottomToolbar.add(addNewSongButton);

        add(bottomToolbar, BorderLayout.PAGE_END);
    }

    private void refreshStoredSelection() {
        CollectionPanel tabPanel = ((CollectionPanel) tabPane.getComponentAt(tabPane.getSelectedIndex()));
        selectedSong = tabPanel.getSelectedSong();
        selectedManager = tabPanel.getCollectionManager();
        list = tabPanel.getList();
        System.out.print(selectedManager.getClass().getName() + ": ");
        System.out.println(new Gson().toJson(selectedSong));
    }

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
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



        Object[] message;
        int option;

        switch (action) {
            case ACTION_EDIT:
                UIManager.put("OptionPane.yesButtonText", "Save Changes");
                UIManager.put("OptionPane.noButtonText", "Delete Song");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                songNameField.setText(selectedSong.name());
                songURLField.setText(selectedSong.getUrl());
                songActiveSwitch.setSelected(selectedSong.isActive());

                message = new Object[]{
                        "Name:", songNameField,
                        "URL:", songURLField,
                        songActiveSwitch
                };

                option = JOptionPane.showConfirmDialog(null, message, "Edit Song id: " + selectedSong.id(), JOptionPane.YES_NO_CANCEL_OPTION);

                UIManager.put("OptionPane.okButtonText", "Yes");
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                if (option == JOptionPane.YES_OPTION) {
                    Song song = new Song(songNameField.getText(), selectedSong.id());
                    song.setUrl(songURLField.getText());
                    song.setActive(songActiveSwitch.isSelected());
                    selectedManager.updateSongRecord(song);
                    forceRefreshList();
                    list.setSelectedValue(song, true);



                } else if (option == JOptionPane.NO_OPTION) {
                    int result = JOptionPane.showConfirmDialog(null, "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?", "Delete song '" + selectedSong.name() + "' id: " + selectedSong.id(), JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        //int listIndex = list.getSelect;
                        //list.remove(listIndex);
                        selectedManager.removeSong(selectedSong);
                        forceRefreshList();


                        selectedSong = null;
                    }

                }


                break;

            case ACTION_ADD:
                UIManager.put("OptionPane.okButtonText", "Add");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                JTextField songAuthorField = new JTextField();
                Song placeholder = getPlaceholderSong();

                songNameField.setText(placeholder.name());
                songAuthorField.setText(placeholder.getAuthor());
                songURLField.setText(placeholder.getUrl());
                songActiveSwitch.setSelected(true);


                songAuthorField.setToolTipText("Author or interpret of the song. For example 'Leonard Cohen'.");

                message = new Object[]{
                        "Name:", songNameField,
                        "Author:", songAuthorField,
                        "URL:", songURLField,
                        songActiveSwitch
                };

                option = JOptionPane.showConfirmDialog(null, message, "Add a Song", JOptionPane.OK_CANCEL_OPTION);

                UIManager.put("OptionPane.okButtonText", "Ok");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");

                if (option == JOptionPane.YES_OPTION) {
                    Song song = new Song(songNameField.getText(), -1);
                    song.setUrl(songURLField.getText());
                    song.setAuthor(songAuthorField.getText());
                    song.setActive(songActiveSwitch.isSelected());
                    song = selectedManager.addSong(song);
                    forceRefreshList();
                    list.setSelectedValue(song, true);
                }


                break;

        }
    }

    private void forceRefreshList() {
        DefaultListModel listModel = new DefaultListModel();
        for (Song song : selectedManager.getSortedCollection()) {
            listModel.addElement(song);
        }
        list.setModel(listModel);

        //list.validate();
        //list.updateUI();
        //list.getModel();
    }

    private Song getPlaceholderSong() {
        int min = 1;
        int max = 21;
        int random = (int)Math.random() * (max - min + 1) + min;
        Song song;
        switch (random) {
            case 1 -> {
                song = new Song("New Song", -1);
                song.setUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                return song;
            }
            case 2 -> {
                song = new Song("Večer křupavých srdíček", -1);
                song.setAuthor("Vlasta Redl");
                song.setUrl("https://www.youtube.com/watch?v=txLfhpEroYI");
                return song;
            }
            case 3 -> {
                song = new Song("Je reviendrai vers toi", -1);
                song.setAuthor("Bryan Adams");
                song.setUrl("https://www.youtube.com/watch?v=29TPk17Z4AA");
                return song;
            }
        }

        return new Song("New Song", -1);

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
                System.out.println(String.format("List selection changed selectedIndex: %d songId: %d", list.getSelectedIndex(), selectedSong.id()));
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
