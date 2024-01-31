package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import attilathehun.songbook.collection.*;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.export.PDFGenerator;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.plugin.Export;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@TODO(priority = true)
public class CollectionEditor extends Stage implements CollectionListener {
    private static final Logger logger = LogManager.getLogger(CollectionEditor.class);
    private static final CollectionEditor instance = new CollectionEditor();
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
    private Song selectedSong = null;
    private CollectionManager selectedManager = null;

    private CollectionEditor() {
        this.setTitle("Collection Editor");
        this.setResizable(false);
    }

    public static void open() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        if (instance.isShowing()) {
            instance.toFront();
        } else {
            instance.show();
        }
    }

    public static CollectionEditor getInstance() {
        return instance;
    }

    public static void shut() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        instance.hide();
    }

    @FXML
    private void initialize() {
        initTabbedPane();
        initBottomToolbar();

        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, window -> hide());

    }

    public void postInit() {
        initKeyboardShortcuts();
    }

    private void initTabbedPane() {
        if (tabbedPane == null) {
            throw new IllegalStateException();
        }
        tabbedPane.getTabs().add(new CollectionPanel(StandardCollectionManager.getInstance()));
        if (Environment.EnvironmentSettings.IS_IT_EASTER_ALREADY && EasterCollectionManager.getInstance().getCollection() != null) {
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
                new AlertDialog.Builder().setTitle("SongbookManager Collection Editor").setMessage("Select a song first!").setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
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
                new AlertDialog.Builder().setTitle("SongbookManager Collection Editor").setMessage("Select a song first!").setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
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
                new AlertDialog.Builder().setTitle("SongbookManager Collection Editor").setMessage("Select a song first!").setParent(this)
                        .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                return;
            }
            Environment.navigateWebViewToSong(selectedSong);
        });
        previewPDFButton.setOnAction(actionEvent -> {

            if (!Export.getInstance().getSettings().getEnabled()) {
                new AlertDialog.Builder().setTitle("SongbookManager Collection Editor")
                        .setMessage("This feature is part of the Export plugin. You can enable the export plugin in settings.")
                        .setParent(this).setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton().build().open();
                return;
            }

            refreshStoredSelection();

            if (selectedSong == null) {
                new AlertDialog.Builder().setTitle("SongbookManager Collection Editor").setMessage("Select a song first!").setParent(this)
                        .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                return;
            }

            Platform.runLater(() -> {
                try {
                    Desktop.getDesktop().open(new File(new PDFGenerator(selectedManager).generatePreview(selectedSong)));
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    new AlertDialog.Builder().setTitle("Error").setMessage(ex.getLocalizedMessage()).setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                }
            });

        });
        deleteSongButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                new AlertDialog.Builder().setTitle("SongbookManager Collection Editor").setMessage("Select a song first!").setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                return;
            }

            if (SongbookApplication.isShiftPressed()) {
                selectedManager.removeSong(selectedSong);
            } else {
                CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle(String.format("Delete song '%s' id:%s?", selectedSong.name(), selectedSong.id()))
                        .setMessage("Deleting a song is not reversible. Alternatively, you can hide it by deactivating it. Are you sure you want to proceed?")
                        .setParent(this).setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton("Delete").addCloseButton("Cancel").build().awaitResult();
                result.thenAccept(dialogResult -> {
                    if (dialogResult == AlertDialog.RESULT_OK) {
                        selectedManager.removeSong(selectedSong);
                    }
                });
            }
        });
    }

    public void initKeyboardShortcuts() {

        this.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case DELETE -> {
                    deleteSongButton.fire();
                }
                case R -> {
                    if (keyEvent.isControlDown()) {
                        refreshStoredSelection();
                        refreshCurrentList();
                    }
                }
                case ESCAPE -> {
                    shut();
                }
            }
        });
    }

    private void refreshStoredSelection() {
        selectedSong = ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getList().getSelectionModel().getSelectedItem();
        selectedManager = ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getManager();
    }

    private void refreshCurrentList() {
        ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).refresh();
    }

    @Override
    public void onSongRemoved(Song s, CollectionManager m) {
        for (Tab panel : tabbedPane.getTabs()) {
            if (((CollectionPanel) panel).getManager().equals(m)) {
                ((CollectionPanel) panel).refresh();
                break;
            }
        }
    }

    @Override
    public void onSongUpdated(Song s, CollectionManager m) {
        for (Tab panel : tabbedPane.getTabs()) {
            if (((CollectionPanel) panel).getManager().equals(m)) {
                ((CollectionPanel) panel).refresh();
                break;
            }
        }
    }

    @Override
    public void onSongAdded(Song s, CollectionManager m) {
        for (Tab panel : tabbedPane.getTabs()) {
            if (((CollectionPanel) panel).getManager().equals(m)) {
                ((CollectionPanel) panel).refresh();
                break;
            }
        }
    }

    public static class CollectionPanel extends Tab {
        private static final Logger logger = LogManager.getLogger(CollectionPanel.class);

        private CollectionManager manager;
        private ListView<Song> list;

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
                    throw new IllegalArgumentException();
                }
                int index = s.getManager().getSortedCollectionSongIndex(s);
                Label nLabel = new Label(String.valueOf(index));
                nLabel.setPrefWidth(30);
                this.getChildren().add(nLabel);
                Label nameLabel = new Label(s.name());
                nameLabel.setPrefWidth(260);
                this.getChildren().add(nameLabel);
                Label IDLabel = new Label(String.valueOf(s.id()));
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
