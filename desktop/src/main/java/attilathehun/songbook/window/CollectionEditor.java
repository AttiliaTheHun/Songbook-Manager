package attilathehun.songbook.window;

import attilathehun.songbook.collection.*;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.PDFGenerator;
import attilathehun.songbook.util.Misc;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;
// TODO on right click upon a list item, a menu with "delete", "create easter song from template" and "open link" could open ;)
public final class CollectionEditor extends Stage {
    private static final Logger logger = LogManager.getLogger(CollectionEditor.class);
    private static final CollectionEditor INSTANCE = new CollectionEditor();
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

    /**
     * Opens the CollectionEditor window. If it already is open, it pushes it to the front.
     */
    public static void open() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        }
        if (INSTANCE.isShowing()) {
            INSTANCE.toFront();
        } else {
            INSTANCE.show();
        }
    }

    public static CollectionEditor getInstance() {
        return INSTANCE;
    }

    /**
     * Hides the CollectionEditor window, but does not close it. The window still lives hidden in the background.
     */
    public static void shut() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        }
        INSTANCE.hide();
    }

    /**
     * JavaFX method called when the FXML file is being inflated.
     */
    @FXML
    private void initialize() {
        initTabbedPane();
        initBottomToolbar();
        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, window -> hide());
    }

    /**
     * Code to be executed when all the UI pieces are inflated and available.
     */
    public void postInit() {
        initKeyboardShortcuts();
    }

    /**
     * Initializes the {@link TabPane} by creating a tab for each registered {@link CollectionManager}.
     */
    private void initTabbedPane() {
        if (tabbedPane == null) {
            throw new IllegalStateException();
        }
        for (final CollectionManager manager : Environment.getInstance().getRegisteredManagers().values()) {
            if (manager.getCollectionName().equals(EasterCollectionManager.getInstance().getCollectionName()) && !Environment.IS_IT_EASTER_ALREADY) {
                continue;
            }
            tabbedPane.getTabs().add(new CollectionPanel(manager));
        }

    }

    /**
     * Recreates the UI that works with data that is subject to runtime changes.
     */
    public static void refresh() {
        INSTANCE.tabbedPane.getTabs().clear();
        INSTANCE.initTabbedPane();
    }

    /**
     * Initializes the buttons on the bottom of the window and their respective actions.
     */
    private void initBottomToolbar() {
        addSongButton.setFocusTraversable(true);
        addSongButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            selectedManager.addSongDialog();
            // TODO if ctrl is held, allow to create an easter song from template of the current song?
        });
        editSongHTMLButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                new AlertDialog.Builder().setTitle("Collection Editor").setMessage("Select a song first!").setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
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
                new AlertDialog.Builder().setTitle("Collection Editor").setMessage("Select a song first!").setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                return;
            }

            CompletableFuture<Song> result = selectedManager.editSongDialog(selectedSong);
            result.thenAccept((song -> {
                if (song == null) {
                    return;
                }
                int index = selectedManager.getDisplayCollectionSongIndex(song);
                if (index != -1) {
                    ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getList().getSelectionModel().select(index);
                }
            }));

        });
        viewSongButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                new AlertDialog.Builder().setTitle("Collection Editor").setMessage("Select a song first!").setParent(this)
                        .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                return;
            }
            Environment.navigateWebViewToSong(selectedSong);
        });
        previewPDFButton.setOnAction(actionEvent -> {

            if (!(Boolean) SettingsManager.getInstance().getValue("EXPORT_ENABLED")) {
                new AlertDialog.Builder().setTitle("PDF preview")
                        .setMessage("This feature is disabled. You can enable exporting in the settings.")
                        .setParent(this).setIcon(AlertDialog.Builder.Icon.WARNING).addOkButton().build().open();
                return;
            }

            refreshStoredSelection();

            if (selectedSong == null) {
                new AlertDialog.Builder().setTitle("Collection Editor").setMessage("Select a song first!").setParent(this)
                        .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                return;
            }

            Platform.runLater(() -> {
                try {
                    Desktop.getDesktop().open(new File(new PDFGenerator(selectedManager).generatePreview(selectedSong)));
                } catch (final Exception ex) {
                    if (ex.getMessage().equals("ignore")) {
                        return;
                    }
                    logger.error(ex.getMessage(), ex);
                    new AlertDialog.Builder().setTitle("Error").setMessage(ex.getLocalizedMessage()).setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
                }
            });

        });
        deleteSongButton.setOnAction(actionEvent -> {
            refreshStoredSelection();
            if (selectedSong == null) {
                new AlertDialog.Builder().setTitle("Collection Editor").setMessage("Select a song first!").setParent(this).setIcon(AlertDialog.Builder.Icon.INFO).addOkButton().build().open();
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

    /**
     * Initializes the keyboard shortcuts available in the window.
     */
    private void initKeyboardShortcuts() {
        this.getScene().addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
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
                case N -> {
                    refreshStoredSelection();
                    if (keyEvent.isControlDown()) {
                        if (keyEvent.isShiftDown()) {
                            if (!selectedManager.equals(EasterCollectionManager.getInstance()) && selectedSong != null) {
                                EasterCollectionManager.getInstance().addSongFromTemplateDialog(selectedSong);
                                return;
                            }
                        }
                        selectedManager.addSongDialog();
                    }
                }
            }
        });
    }

    /**
     * Reassigns the local variables to the selected values.
     */
    private void refreshStoredSelection() {
        selectedSong = ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getList().getSelectionModel().getSelectedItem();
        selectedManager = ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).getManager();
    }

    /**
     * Refreshes the {@link ListView} in the currently selected tab.
     */
    private void refreshCurrentList() {
        ((CollectionPanel) tabbedPane.getSelectionModel().getSelectedItem()).refresh();
    }

    /**
     * A special Tab implementation that serves as a container for data of one {@link CollectionManager}. The tab automatically refreshes when the manager's collection
     * is updated.
     */
    public static class CollectionPanel extends Tab implements CollectionListener {
        private static final Logger logger = LogManager.getLogger(CollectionPanel.class);

        private final CollectionManager manager;
        private final ListView<Song> list;

        private CollectionPanel(final CollectionManager manager) {
            if (manager == null) {
                throw new IllegalArgumentException();
            }
            this.manager = manager;
            this.setText(Misc.toTitleCase(manager.getCollectionName()));
            this.list = new ListView<Song>();
            list.setOrientation(Orientation.VERTICAL);
            list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            ReadOnlyListWrapper<Song> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(manager.getSortedCollection()));
            list.setCellFactory(list -> new SongCell());
            list.getItems().addAll(listViewData);
            list.refresh();
            this.setContent(list);
            manager.addListener(this);
        }

        /**
         * Returns the associated {@link CollectionManager}.
         *
         * @return the manager
         */
        public CollectionManager getManager() {
            return manager;
        }

        /**
         * Returns the internal {@link ListView}.
         *
         * @return the listview
         */
        public ListView<Song> getList() {
            return list;
        }

        /**
         * Refreshes the internal {@link ListView}.
         */
        public void refresh() {
            list.getItems().clear();
            final ReadOnlyListWrapper<Song> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(manager.getSortedCollection()));
            list.getItems().addAll(listViewData);
            list.refresh();
        }

        @Override
        public void onSongRemoved(final Song s, final CollectionManager m) {
            refresh();
        }

        @Override
        public void onSongUpdated(final Song s, final CollectionManager m) {
            refresh();
        }

        @Override
        public void onSongAdded(final Song s, final CollectionManager m) {
            refresh();
        }

        /**
         * Custom {@link ListView} item implementation.
         */
        private class SongCell extends ListCell<Song> {

            public SongCell() {
            }

            @Override
            public void updateItem(final Song s, final boolean empty) {
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

        /**
         * The underlying layout that actually makes up the item (the one line in the list view).
         */
        private class SongEntry extends HBox {
            private SongEntry(final Song s) {
                if (s == null || s.getManager() == null) {
                    throw new IllegalArgumentException();
                }
                int index = s.getManager().getSortedCollectionSongIndex(s);
                final Label nLabel = new Label(String.valueOf(index));
                nLabel.setPrefWidth(30);
                this.getChildren().add(nLabel);
                final Label nameLabel = new Label(s.name());
                nameLabel.setPrefWidth(260);
                this.getChildren().add(nameLabel);
                final Label IDLabel = new Label(String.valueOf(s.id()));
                IDLabel.setPrefWidth(30);
                this.getChildren().add(IDLabel);
                final Label URLLabel = new Label(s.getUrl());
                URLLabel.setPrefWidth(440);
                this.getChildren().add(URLLabel);
                final CheckBox activityCheckBox = new CheckBox(null);
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
