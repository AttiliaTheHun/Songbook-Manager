package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.plugin.Export;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.util.PDFGenerator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

@TODO(priority = true)
public class CollectionEditor extends Stage {
    private static final Logger logger = LogManager.getLogger(CollectionEditor.class);

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

    private static final CollectionEditor instance = new CollectionEditor();

    private Song selectedSong = null;
    private CollectionManager selectedManager = null;

    @FXML
    private void initialize() {
        initTabbedPane();
        initBottomToolbar();

        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, window -> hide());

    }

    public void postInit() {
        initKeyboardShortcuts();
    }

    private CollectionEditor(){
        this.setTitle("Collection Editor");
        this.setResizable(false);
    }

    public static void open() {
        if (instance == null) {
            throw  new IllegalStateException();
        }
        instance.show();
    }

    public static CollectionEditor getInstance() {
        return instance;
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
            refreshStoredSelection();
            Song song;
            if (SongbookApplication.isControlPressed()) {
                song = EnvironmentManager.addEasterSongFromTemplateDialog(selectedSong, selectedManager);
            } else {
                song = Environment.getInstance().getCollectionManager().addSongDialog();
            }
            if (song == null) {
                return;
            }
            //TODO refresh list
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
            CodeEditor.open(selectedSong, selectedManager);
        });
        editSongRecordButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Songbook Manager Collection Editor", "Select a song first!", null);
                return;
            }

            Song song = Environment.getInstance().getCollectionManager().editSongDialog(selectedSong);

            if (song == null) {
                return;
            }

            //TODO refresh list
            int index = selectedManager.getDisplayCollectionSongIndex(song);
            if (index != -1) {
                //TODO list.setSelectedValue(index, true);
            }
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

            if (!Environment.getInstance().settings.plugins.getEnabled(Export.getInstance().getName())) {
                Environment.showMessage("Action aborted", "This feature is a part of the Export plugin. Enable it in settings first!");
                return;
            }

            refreshStoredSelection();

            if (selectedSong == null) {
                Environment.showMessage("Songbook Manager Collection Editor", "Select a song first!", null);
                return;
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        //TODO port this to the new PDFGenerator when complete
                        Desktop.getDesktop().open(new File(new PDFGenerator(selectedManager).generatePreview(selectedSong).replace(".html", ".pdf")));
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        Environment.showErrorMessage("Error", ex.getMessage(), null);
                    }
                }
            });

        });
        deleteSongButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                Environment.showMessage("Songbook Manager Collection Editor", "Select a song first!", null);
                return;
            }

            boolean confirmed = false;
            if (SongbookApplication.isShiftPressed()) {
                confirmed = true;
            } else {
                String message = "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?";
                confirmed = Environment.showConfirmMessage("Songbook Manager Collection Editor", String.format("Delete song '%s' id:%s?", selectedSong.name(), selectedSong.id()), message);
            }
            if (confirmed) {
                selectedManager.removeSong(selectedSong);
                //TODO refresh list
                selectedSong = null;
            }

        });
    }

    //TODO
    public void initKeyboardShortcuts() {

        this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case DELETE -> {
                        refreshStoredSelection();
                        boolean confirmed = false;
                        if (keyEvent.isShiftDown()) {
                            confirmed = true;
                        } else {
                            String message = "Deleting a song is permanent - irreversible. If you only want to hide it from the songbook, try deactivating it. Are you sure you want to proceed?";
                            confirmed = Environment.showConfirmMessage("Delete song", String.format("Delete song '%s' id:%s ?", selectedSong.name(), selectedSong.id()), message);
                        }
                        if (confirmed) {
                            selectedManager.removeSong(selectedSong);
                            //TODO refresh list
                            selectedSong = null;
                        }
                    }
                    case R -> {
                        if (keyEvent.isControlDown()) {
                            refreshStoredSelection();
                            //TODO refresh list
                        }
                    }
                }


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
                nameLabel.setPrefWidth(260);
                this.getChildren().add(nameLabel);
                Label IDLabel = new Label (String.valueOf(s.id()));
                IDLabel.setPrefWidth(30);
                this.getChildren().add(IDLabel);
                Label URLLabel = new Label(s.getUrl());
                URLLabel.setPrefWidth(450);
                this.getChildren().add(URLLabel);
                CheckBox activityCheckBox = new CheckBox(null);
                activityCheckBox.setSelected(s.isActive());
                activityCheckBox.setPrefWidth(40);
                activityCheckBox.setOnAction(actionEvent -> {
                    if (activityCheckBox.isSelected()) {
                        manager.activateSong(s);
                        logger.debug("Song activated: " + s.id());
                    } else {
                        manager.deactivateSong(s);
                        logger.debug("Song deactivated: " + s.id());
                    }
                });
                this.getChildren().add(activityCheckBox);
                this.setFillHeight(true);
            }

        }

    }

}
