package attilathehun.songbook.window;

import attilathehun.songbook.Main;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.Misc;
import attilathehun.songbook.vcs.VCSAgent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;


public final class AdminPanel extends Stage {
    private static final Logger logger = LogManager.getLogger(AdminPanel.class);
    private static final int MODE_PROCESSED = 0;
    private static final int MODE_RAW = 1;
    private static AdminPanel instance = null;
    private final VCSAgent agent;
    @FXML
    public ToggleSwitch rawAccessSwitch;
    @FXML
    public ComboBox<String> endpointSelectionBox;
    @FXML
    public AnchorPane sceneContainer;
    private Scene rawAccessScene;
    private RawAccessViewController rawController;
    private Scene processedAccessScene;
    private ProcessedAccessViewController processedController;
    private int mode = MODE_PROCESSED;
    private String jsonData = null;

    private AdminPanel(final VCSAgent agent) {
        this.setTitle("Admin Panel");
        this.setResizable(false);
        this.agent = agent;
    }

    public static void open() {
        if (instance == null) {
            instance = initialInit();
            instance.show();
        }
        instance.toFront();
    }

    /**
     * Returns the inner instance object. If the windows is not open, this object is null.
     *
     * @return the instance (probably null)
     */
    public static AdminPanel getInstance() {
        return instance;
    }

    private static AdminPanel initialInit() {
        final AdminPanel panelController = new AdminPanel(VCSAgent.getAdminPanelAgent(new Certificate()));
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin-panel.fxml"));
        fxmlLoader.setController(panelController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        panelController.setScene(scene);
        panelController.postInit();
        return panelController;
    }

    /**
     * JavaFX method called when the FXML file is being inflated.
     */
    @FXML
    public void initialize() {
        initSubscenes();
        initUIComponents();
        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (handler) -> {
            instance.close();
            instance = null;
        });
    }

    private void initUIComponents() {

        rawAccessSwitch.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            sceneContainer.getChildren().remove(0);
            if (t1) {
                sceneContainer.getChildren().add(rawAccessScene.getRoot());
                mode = MODE_RAW;
                switch (endpointSelectionBox.getSelectionModel().getSelectedIndex()) {
                    case 0 -> rawController.initUIForBackups();
                    case 1 -> rawController.initUIForTokens();
                    case 2 -> rawController.initUIForActionLog();
                }
            } else {
                sceneContainer.getChildren().add(processedAccessScene.getRoot());
                mode = MODE_PROCESSED;
                switch (endpointSelectionBox.getSelectionModel().getSelectedIndex()) {
                    case 0 -> processedController.initUIForBackups();
                    case 1 -> processedController.initUIForTokens();
                    case 2 -> processedController.initUIForActionLog();
                }
            }
        });

        sceneContainer.getChildren().add(processedAccessScene.getRoot());

        rawAccessSwitch.setSelected(true);
        rawAccessSwitch.setSelected(false);


        endpointSelectionBox.getItems().addAll("backups", "tokens", "action log");
        endpointSelectionBox.setOnAction((handler) -> {

            switch (endpointSelectionBox.getSelectionModel().getSelectedIndex()) {
                case 0 -> {
                    if (((String) SettingsManager.getInstance().getValue("REMOTE_BACKUPS_URL")).length() == 0) {
                        new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(this)
                                .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                        jsonData = null;
                        return;
                    }

                    final String filePath = agent.listRemoteBackups();
                    if (filePath == null) {
                        jsonData = null;
                        return;
                    }
                    final File file = new File((filePath));
                    try (final InputStream stream = new FileInputStream(file)) {
                        jsonData = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                        if (mode == MODE_PROCESSED) {
                            processedController.initUIForBackups();
                        } else {
                            rawController.initUIForBackups();
                        }
                    } catch (final IOException e) {
                        logger.error(e.getMessage(), e);
                        new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(this)
                                .setMessage("Could not parse the response: " + e.getLocalizedMessage()).addOkButton().build().open();
                        jsonData = null;
                    }
                }
                case 1 -> {
                    if (((String) SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL")).length() == 0) {
                        new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(this)
                                .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                        jsonData = null;
                        return;
                    }

                    final String filePath = agent.listTokensOnTheServer();
                    if (filePath == null) {
                        jsonData = null;
                        return;
                    }
                    final File file = new File((filePath));
                    try (final InputStream stream = new FileInputStream(file)) {
                        jsonData = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                        if (mode == MODE_PROCESSED) {
                            processedController.initUIForTokens();
                        } else {
                            rawController.initUIForTokens();
                        }
                    } catch (final IOException e) {
                        logger.error(e.getMessage(), e);
                        new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(this)
                                .setMessage("Could not parse the response: " + e.getLocalizedMessage()).addOkButton().build().open();
                        jsonData = null;
                    }
                }
                case 2 -> {
                    if (((String) SettingsManager.getInstance().getValue("REMOTE_ACTION_LOG_URL")).length() == 0) {
                        new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(this)
                                .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                        jsonData = null;
                        return;
                    }

                    final String filePath = agent.getRemoteActionLog();
                    if (filePath == null) {
                        jsonData = null;
                        return;
                    }
                    final File file = new File((filePath));
                    try (final InputStream stream = new FileInputStream(file)) {
                        jsonData = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                        if (mode == MODE_PROCESSED) {
                            processedController.initUIForActionLog();
                        } else {
                            rawController.initUIForActionLog();
                        }
                    } catch (final IOException e) {
                        logger.error(e.getMessage(), e);
                        new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(this)
                                .setMessage("Could not parse the response: " + e.getLocalizedMessage()).addOkButton().build().open();
                        jsonData = null;
                    }

                }
            }

        });
    }

    private void postInit() {

    }

    private void initSubscenes() {
        initRawAccessScene();
        initProcessedAccessScene();
    }

    private void initRawAccessScene() {
        rawController = new RawAccessViewController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin-panel-raw-access-view.fxml"));
        fxmlLoader.setController(rawController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        rawAccessScene = scene;
        rawController.postInit();
    }

    private void initProcessedAccessScene() {
        processedController = new ProcessedAccessViewController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin-panel-processed-access-view.fxml"));
        fxmlLoader.setController(processedController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        processedAccessScene = scene;
        processedController.postInit();
    }

    private static class RawAccessViewController {
        @FXML
        public TextArea rawAccessViewResponseArea;
        @FXML
        public TextArea rawAccessViewRequestArea;
        @FXML
        public ChoiceBox<String> rawAccessHTTPMethodBox;
        @FXML
        public TextField rawAccessViewURLField;
        @FXML
        public Button rawAccessViewSendButton;
        @FXML
        public Button rawAccessViewHeadersButton;
        private static final String HEADER_DIALOG_HINT_TEXT = "Enter HTTP request headers like following:\nheader1;value\nheader2;value";
        private final TextArea rawAccessHeaderDialogArea = new TextArea();


        @FXML
        public void initialize() {
            initUIElements();
        }

        private void initUIElements() {
            rawAccessHTTPMethodBox.getItems().addAll("GET", "POST", "PUT", "DELETE");
            rawAccessHTTPMethodBox.getSelectionModel().selectFirst();

            rawAccessViewHeadersButton.setOnAction((handler) -> {
                new AlertDialog.Builder().setTitle("Edit headers").addContentNode(rawAccessHeaderDialogArea).addOkButton("Apply").setCancelable(false)
                        .build().open();
            });

            rawAccessViewSendButton.setOnAction((handler) -> {

            });

            rawAccessHeaderDialogArea.setText(HEADER_DIALOG_HINT_TEXT);
            rawAccessHeaderDialogArea.deselect();
        }

        private void postInit() {

        }

        public void initUIForBackups() {
            if (instance.jsonData == null) {
                return;
            }
            rawAccessViewURLField.setText(SettingsManager.getInstance().getValue("REMOTE_BACKUPS_URL"));
        }

        public void initUIForTokens() {
            if (instance.jsonData == null) {
                return;
            }
            rawAccessViewURLField.setText(SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL"));
            rawAccessViewResponseArea.setText(Misc.formatJSON(instance.jsonData));
        }

        public void initUIForActionLog() {
            if (instance.jsonData == null) {
                return;
            }
            rawAccessViewURLField.setText(SettingsManager.getInstance().getValue("REMOTE_ACTION_LOG_URL"));
            rawAccessViewResponseArea.setText(instance.jsonData);
            rawAccessViewResponseArea.selectEnd();
            rawAccessViewResponseArea.deselect();
        }
    }

    private static class ProcessedAccessViewController {
        @FXML
        public BorderPane processedAccessRoot;
        @FXML
        public ListView<String> processedAccessViewDataListView;
        @FXML
        public Button processedAccessViewCreateTokenButton;
        @FXML
        public Button processedAccessViewFreezeTokenButton;
        @FXML
        public Button processedAccessViewCreateBackupButton;
        @FXML
        public Button processedAccessViewRestoreBackupButton;
        public HBox backupsListviewTitlebar;
        public HBox tokensListviewTitlebar;
        public HBox actionLogListviewTitlebar;

        @FXML
        public void initialize() {
            initUIElements();
        }

        private void initUIElements() {
            processedAccessViewCreateBackupButton.setOnAction(handler -> {
                if (((String) SettingsManager.getInstance().getValue("REMOTE_BACKUPS_URL")).length() == 0) {
                    new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(instance)
                            .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                    return;
                }
                instance.agent.createRemoteBackup();
            });

            processedAccessViewRestoreBackupButton.setOnAction(handler -> {
                if (((String) SettingsManager.getInstance().getValue("REMOTE_BACKUPS_URL")).length() == 0) {
                    new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(instance)
                            .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                    return;
                }
                instance.agent.restoreRemoteBackup();
            });

            processedAccessViewCreateTokenButton.setOnAction(handler -> {
                if (((String) SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL")).length() == 0) {
                    new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.CONFIRM).setParent(instance)
                            .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                    return;
                }
                createTokenCreationDialog();
            });

            processedAccessViewFreezeTokenButton.setOnAction(handler -> {
                if (((String) SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL")).length() == 0) {
                    new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(instance)
                            .setMessage("The path to the api endpoint is not specified.").addOkButton().build().open();
                    return;
                }
                if (processedAccessViewDataListView.getSelectionModel().getSelectedIndex() == -1) {
                    new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(instance)
                            .setMessage("You need to select an item first.").addOkButton().build().open();
                    return;
                }
                instance.agent.freezeToken(processedAccessViewDataListView.getSelectionModel().getSelectedIndex());
            });

            initBackupsListviewTitlebar();
            initTokensListviewTitleBar();
            initActionLogListviewTitleBar();
        }

        private void createTokenCreationDialog() {
            final CheckBox readPermissionBox = new CheckBox("Read");
            final CheckBox writePermissionBox = new CheckBox("Write");
            final CheckBox backupPermissionBox = new CheckBox("Backup");
            final CheckBox restorePermissionBox = new CheckBox("Restore");
            final CheckBox manageTokensPermissionBox = new CheckBox("Manage tokens");
            final HBox container = new HBox();
            final Label permissionsTitle = new Label("Permissions");
            container.getChildren().add(permissionsTitle);
            container.getChildren().add(new Separator());
            Pair<Integer, ArrayList<Node>> result;

            try {
                result = new AlertDialog.Builder().setTitle("Create token").addTextInput("Name:", "Enter token name")
                        .addContentNode(readPermissionBox).addContentNode(writePermissionBox).addContentNode(backupPermissionBox)
                        .addContentNode(restorePermissionBox).addContentNode(manageTokensPermissionBox).setCancelable(false)
                        .setParent(instance).addOkButton("Create").addCloseButton("Cancel").build().awaitData().get();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(instance)
                        .setMessage(e.getLocalizedMessage()).addOkButton().build().open();
                return;
            }
            if (result.getKey() != AlertDialog.RESULT_OK) {
                return;
            }
            final String name = ((TextField) result.getValue().get(1)).getText();
            final StringBuilder permissionsBuilder = new StringBuilder();
            if (readPermissionBox.isSelected()) {
                permissionsBuilder.append(1);
            } else {
                permissionsBuilder.append(0);
            }
            if (writePermissionBox.isSelected()) {
                permissionsBuilder.append(1);
            } else {
                permissionsBuilder.append(0);
            }
            if (backupPermissionBox.isSelected()) {
                permissionsBuilder.append(1);
            } else {
                permissionsBuilder.append(0);
            }
            if (restorePermissionBox.isSelected()) {
                permissionsBuilder.append(1);
            } else {
                permissionsBuilder.append(0);
            }
            if (manageTokensPermissionBox.isSelected()) {
                permissionsBuilder.append(1);
            } else {
                permissionsBuilder.append(0);
            }

            instance.agent.createTokenOnTheServer(name, permissionsBuilder.toString());
        }

        private void initBackupsListviewTitlebar() {
            backupsListviewTitlebar = new HBox();
            final Label fileNameLabel = new Label("file name");
            fileNameLabel.setPrefWidth(230);
            fileNameLabel.setPadding(new Insets(0, 0, 0, 8));
            fileNameLabel.setFont(new Font("System Bold", 12.0));
            backupsListviewTitlebar.getChildren().add(fileNameLabel);
            backupsListviewTitlebar = new HBox();
            final Label creationDateLabel = new Label("creation date");
            creationDateLabel.setPrefWidth(230);
            creationDateLabel.setPadding(new Insets(0, 0, 0, 8));
            creationDateLabel.setFont(new Font("System Bold", 12.0));
            backupsListviewTitlebar.getChildren().add(creationDateLabel);
        }

        private void initTokensListviewTitleBar() {
            tokensListviewTitlebar = new HBox();
            // 800 width to distribute
            final Label nameLabel = new Label("name");
            nameLabel.setPrefWidth(230);
            nameLabel.setPadding(new Insets(0, 0, 0, 8));
            nameLabel.setFont(new Font("System Bold", 12.0));
            tokensListviewTitlebar.getChildren().add(nameLabel);
            final Label dateLabel = new Label("creation date");
            dateLabel.setPrefWidth(230);
            dateLabel.setPadding(new Insets(0, 0, 0, 8));
            dateLabel.setFont(new Font("System Bold", 12.0));
            tokensListviewTitlebar.getChildren().add(dateLabel);
            final Label statusLabel = new Label("status");
            statusLabel.setPrefWidth(120);
            statusLabel.setPadding(new Insets(0, 0, 0, 8));
            statusLabel.setFont(new Font("System Bold", 12.0));
            tokensListviewTitlebar.getChildren().add(statusLabel);
            final Label permissionsLabel = new Label("permissions");
            permissionsLabel.setPrefWidth(240);
            permissionsLabel.setPadding(new Insets(0, 0, 0, 8));
            permissionsLabel.setFont(new Font("System Bold", 12.0));
            tokensListviewTitlebar.getChildren().add(permissionsLabel);
        }

        private void initActionLogListviewTitleBar() {
            actionLogListviewTitlebar = new HBox();
            // 800 width to distribute
            final Label dateLabel = new Label("date");
            dateLabel.setPrefWidth(210);
            dateLabel.setPadding(new Insets(0, 0, 0, 8));
            dateLabel.setFont(new Font("System Bold", 12.0));
            actionLogListviewTitlebar.getChildren().add(dateLabel);
            final Label actionLabel = new Label("action");
            actionLabel.setPrefWidth(140);
            actionLabel.setPadding(new Insets(0, 0, 0, 8));
            actionLabel.setFont(new Font("System Bold", 12.0));
            actionLogListviewTitlebar.getChildren().add(actionLabel);
            final Label tokenLabel = new Label("token");
            tokenLabel.setPrefWidth(210);
            tokenLabel.setPadding(new Insets(0, 0, 0, 8));
            tokenLabel.setFont(new Font("System Bold", 12.0));
            actionLogListviewTitlebar.getChildren().add(tokenLabel);
            final Label backupLabel = new Label("backup");
            backupLabel.setPrefWidth(240);
            backupLabel.setPadding(new Insets(0, 0, 0, 8));
            backupLabel.setFont(new Font("System Bold", 12.0));
            actionLogListviewTitlebar.getChildren().add(backupLabel);
        }

        private void initListview() {
            processedAccessViewDataListView.setOrientation(Orientation.VERTICAL);
            processedAccessViewDataListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }

        private void initListviewForBackups() {
            processedAccessViewDataListView.getItems().clear();
            final JsonObject object = new Gson().fromJson(instance.jsonData, JsonObject.class);
            final ReadOnlyListWrapper<String> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
            listViewData.add("complete:");
            listViewData.addAll(object.get("complete").getAsJsonArray().asList().stream().map(JsonElement::toString).toList());
            listViewData.add("inverse:");
            listViewData.addAll(object.get("inverse").getAsJsonArray().asList().stream().map(JsonElement::toString).toList());
            processedAccessViewDataListView.setCellFactory(list -> new TokenCell());
            processedAccessViewDataListView.getItems().addAll(listViewData);
            processedAccessViewDataListView.refresh();
        }

        private void initListviewForTokens() {
            processedAccessViewDataListView.getItems().clear();
            final String data = instance.jsonData.replace("[", "").replace("]", "");
            final ReadOnlyListWrapper<String> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(data.split(",")));
            processedAccessViewDataListView.setCellFactory(list -> new TokenCell());
            processedAccessViewDataListView.getItems().addAll(listViewData);
            processedAccessViewDataListView.refresh();
        }

        private void initListviewForActionLog() {
            processedAccessViewDataListView.getItems().clear();
            final ReadOnlyListWrapper<String> listViewData = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(instance.jsonData.split("\n")));
            processedAccessViewDataListView.setCellFactory(list -> new ActionLogCell());
            processedAccessViewDataListView.getItems().addAll(listViewData);
            processedAccessViewDataListView.refresh();
        }

        private void postInit() {
            initListview();
        }

        public void initUIForBackups() {
            if (instance.jsonData == null) {
                return;
            }
            processedAccessRoot.setTop(backupsListviewTitlebar);
            initListviewForBackups();
            processedAccessViewFreezeTokenButton.setDisable(true);
            processedAccessViewCreateTokenButton.setDisable(true);
            processedAccessViewCreateBackupButton.setDisable(false);
            processedAccessViewRestoreBackupButton.setDisable(false);
        }

        private static class BackupCell extends ListCell<String> {

            public BackupCell() {
            }

            @Override
            public void updateItem(final String s, final boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(new BackupEntry(s));
                }
            }
        }

        private static class BackupEntry extends HBox {
            private BackupEntry(final String s) {
                if (s == null) {
                    throw new IllegalArgumentException();
                }
                String workString = s;
                String fileName;
                String creationDate;
                boolean treatAsTitle = false;
                block:
                try { // parse the line
                    if (workString.equals("complete:") || workString.equals("inverse:")) {
                        fileName = workString;
                        creationDate = "";
                        treatAsTitle = true;
                        break block;
                    }
                    fileName = s.substring(0, s.indexOf(" "));
                    workString = workString.substring(fileName.length() + 1).trim();
                    creationDate = workString;

                } catch (final Exception e) { // parsing failed
                    logger.error(e.getMessage(), e);
                    return;
                }
                // 800 width to distribute
                final Label fileNameLabel = new Label(fileName);
                fileNameLabel.setPrefWidth(230);
                if (treatAsTitle) {
                    fileNameLabel.setFont(new Font("System Bold", 12));
                }
                this.getChildren().add(fileNameLabel);
                final Label creationDateLabel = new Label(creationDate);
                creationDateLabel.setPrefWidth(230);
                this.getChildren().add(creationDateLabel);
                this.setFillHeight(true);
            }

        }

        public void initUIForTokens() {
            if (instance.jsonData == null) {
                return;
            }
            processedAccessRoot.setTop(tokensListviewTitlebar);
            initListviewForTokens();
            processedAccessViewFreezeTokenButton.setDisable(false);
            processedAccessViewCreateTokenButton.setDisable(false);
            processedAccessViewCreateBackupButton.setDisable(true);
            processedAccessViewRestoreBackupButton.setDisable(true);
        }

        private static class TokenCell extends ListCell<String> {

            public TokenCell() {
            }

            @Override
            public void updateItem(final String s, final boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(new TokenEntry(s));
                }
            }
        }

        private static class TokenEntry extends HBox {
            private TokenEntry(final String s) {
                if (s == null) {
                    throw new IllegalArgumentException();
                }
                String workString = s.replace("\"", "");
                String name;
                String creationDate;
                String frozen = "active";
                String permissions;
                try { // parse the line
                    permissions = workString.substring(workString.lastIndexOf(" "));
                    workString = workString.substring(0,workString.length() - permissions.length()).trim();
                    if (workString.substring(workString.lastIndexOf(" ")).trim().equals("frozen")) {
                        frozen = "frozen";
                        workString = workString.substring(0,workString.length() - frozen.length()).trim();
                    }
                    creationDate = workString.substring(workString.lastIndexOf(" "));
                    workString = workString.substring(0,workString.length() - creationDate.length()).trim();
                    name = workString;
                } catch (final Exception e) { // parsing failed
                    logger.error(e.getMessage(), e);
                    return;
                }
                // 800 width to distribute
                final Label nameLabel = new Label(name);
                nameLabel.setPrefWidth(220);
                this.getChildren().add(nameLabel);
                final Label dateLabel = new Label(creationDate);
                dateLabel.setPrefWidth(230);
                this.getChildren().add(dateLabel);
                final Label frozenLabel = new Label(frozen);
                frozenLabel.setPrefWidth(120);
                this.getChildren().add(frozenLabel);
                final Label permissionLabel = new Label(permissions);
                permissionLabel.setPrefWidth(200);
                this.getChildren().add(permissionLabel);
                this.setFillHeight(true);
            }

        }

        public void initUIForActionLog() {
            if (instance.jsonData == null) {
                return;
            }
            processedAccessRoot.setTop(actionLogListviewTitlebar);
            initListviewForActionLog();
            processedAccessViewFreezeTokenButton.setDisable(true);
            processedAccessViewCreateTokenButton.setDisable(true);
            processedAccessViewCreateBackupButton.setDisable(true);
            processedAccessViewRestoreBackupButton.setDisable(true);
        }

        private static class ActionLogCell extends ListCell<String> {

            public ActionLogCell() {
            }

            @Override
            public void updateItem(final String s, final boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(new ActionLogEntry(s));
                }
            }
        }

        private static class ActionLogEntry extends HBox {
            private ActionLogEntry(final String s) {
                if (s == null) {
                    throw new IllegalArgumentException();
                }
                String workString = s;
                String date;
                String action;
                String tokenName;
                String backupFile = "";
                try { // parse the line
                    date = s.substring(0, s.indexOf("]") + 1);
                    workString = workString.substring(date.length()).trim();
                    action = workString.substring(0, workString.indexOf(" "));
                    workString = workString.substring(action.length()).trim();
                    if (workString.endsWith(".zip")) {
                        backupFile = workString.substring(workString.lastIndexOf(" "));
                        workString = workString.substring(0, backupFile.length() - 1).trim();
                    }
                    tokenName = workString;
                } catch (final Exception e) { // parsing failed
                    logger.error(e.getMessage(), e);
                    return;
                }
                // 800 width to distribute
                final Label dateLabel = new Label(date);
                dateLabel.setPrefWidth(210);
                this.getChildren().add(dateLabel);
                final Label actionLabel = new Label(action);
                actionLabel.setPrefWidth(130);
                this.getChildren().add(actionLabel);
                final Label tokenLabel = new Label(tokenName);
                tokenLabel.setPrefWidth(210);
                this.getChildren().add(tokenLabel);
                final Label backupLabel = new Label(backupFile);
                backupLabel.setPrefWidth(230);
                this.getChildren().add(backupLabel);
                this.setFillHeight(true);
            }

        }

    }

    public static class Certificate {
        private Certificate() {
        }
    }
}
