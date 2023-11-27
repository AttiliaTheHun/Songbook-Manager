package attilathehun.songbook.window;

import attilathehun.songbook.SongbookApplication;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.util.Misc;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class CollectionEditor2 extends Stage {
    private static final Logger logger = LogManager.getLogger(CollectionEditor2.class);

    @FXML
    public Button editSongHTMLButton;
    @FXML
    public Button editSongRecordButton;
    @FXML
    public Button previewPDFButton;
    @FXML
    public Button viewSongButton;
    @FXML
    public Button deleteSongButton;
    @FXML
    public Button addSongButton;
    @FXML
    public TabPane tabbedPane;

    private static CollectionEditor2 instance = null;

    private Song selectedSong = null;
    private CollectionManager selectedManager = null;

    public void initialize() {
        initTabbedPane();
        initBottomToolbar();
    }

    public CollectionEditor2(){
        this.setTitle("Collection Editor");
        this.setResizable(false);
    }

    public static void open() {
        if (instance == null) {
            throw  new IllegalStateException();
        }
        instance.show();
    }

    public static void shut() {
        if (instance == null) {
            throw  new IllegalStateException();
        }
        instance.hide();
    }

    private void initTabbedPane() {
        if (tabbedPane == null) {
            throw new IllegalStateException();
        }
        tabbedPane.getTabs().add(new CollectionPanel(StandardCollectionManager.getInstance()));
        if (Environment.getInstance().settings.environment.IS_IT_EASTER_ALREADY && EasterCollectionManager.getInstance().getCollection() != null) {
            tabbedPane.getTabs().add(new CollectionPanel(EasterCollectionManager.getInstance()));
        }

    }

    //TODO
    private void initBottomToolbar() {
        addSongButton.setOnAction(actionEvent -> {

        });
        editSongHTMLButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Songbook Manager Collection Editor", "Select a song first!", null);
                return;
            }
            if (selectedManager == null) {
                selectedManager = selectedSong.getManager(); //can still be null, theoretically :)
            }
            CodeEditor.open(selectedManager, selectedSong);
        });
        editSongRecordButton.setOnAction(actionEvent -> {

        });
        viewSongButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Songbook Manager Collection Editor", "Select a song first!", null);
                return;
            }
            Environment.navigateWebViewToSong(selectedSong);
        });
        previewPDFButton.setOnAction(actionEvent -> {

        });
        deleteSongButton.setOnAction(actionEvent -> {

        });
    }

    //TODO
    public void initKeyboardShortcuts() {
        this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                System.out.println(keyEvent.getCode());
            }
        });
    }

    private void refreshStoredSelection() {
        selectedSong = ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getList().getSelectionModel().getSelectedItem();
        selectedManager = ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getManager();
    }

    public class CollectionPanel extends Tab {
        private static final Logger logger = LogManager.getLogger(CollectionPanel.class);

        private CollectionManager manager;
        private ListView<Song> list;

        private CollectionPanel() {
            throw new RuntimeException("Constructor not allowed!");
        }

        private CollectionPanel(CollectionManager manager) {
            if (manager == null) {
                throw new IllegalArgumentException();
            }
            this.manager = manager;
            this.setText(Misc.toTitleCase(manager.getCollectionName()));
            this.list = new ListView<Song>();
            list.setOrientation(Orientation.VERTICAL);
            list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            ReadOnlyListWrapper<Song> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList((ArrayList<Song>) manager.getSortedCollection()));
            list.setCellFactory(list -> new SongCell());
            list.getItems().addAll(listViewData);
            list.refresh();
            this.setContent(list);
        }

        public CollectionManager getManager() {
            return manager;
        }

        public ListView<Song> getList() {
            return list;
        }

        public void refresh() {
            list.getItems().clear();
            ReadOnlyListWrapper<Song> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList((ArrayList<Song>) manager.getSortedCollection()));
            list.getItems().addAll(listViewData);
            list.refresh();
        }

        private class SongCell extends ListCell<Song> {

            public SongCell() {
            }

            @Override
            public void updateItem(Song s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(new SongEntry(s));
                }
            }
        }

        private class SongEntry extends HBox {
            private SongEntry(Song s) {
                if (s == null || s.getManager() == null) {
                    throw  new IllegalArgumentException();
                }
                int index = s.getManager().getSortedCollectionSongIndex(s);
                Label nLabel = new Label(String.valueOf(index));
                nLabel.setPrefWidth(30);
                this.getChildren().add(nLabel);
                Label nameLabel = new Label(s.name());
                nameLabel.setPrefWidth(250);
                this.getChildren().add(nameLabel);
                Label IDLabel = new Label (String.valueOf(s.id()));
                IDLabel.setPrefWidth(30);
                this.getChildren().add(IDLabel);
                Label URLLabel = new Label(s.getUrl());
                URLLabel.setPrefWidth(430);
                this.getChildren().add(URLLabel);
                CheckBox activityCheckBox = new CheckBox(null);
                activityCheckBox.setSelected(s.isActive());
                activityCheckBox.setPrefWidth(40);
                activityCheckBox.setOnAction(actionEvent -> {
                    if (activityCheckBox.isSelected()) {
                        manager.activateSong(s);
                        System.out.println("Song activated: " + s.id());
                    } else {
                        manager.deactivateSong(s);
                        System.out.println("Song deactivated: " + s.id());
                    }

                });
                this.getChildren().add(activityCheckBox);
            }

        }

    }

}
