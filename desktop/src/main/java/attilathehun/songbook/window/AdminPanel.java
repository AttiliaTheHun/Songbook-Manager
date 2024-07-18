package attilathehun.songbook.window;

import attilathehun.songbook.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;

public class AdminPanel extends Stage {
    @FXML
    public ToggleSwitch rawAccessSwitch;
    @FXML
    public ComboBox<String> endpointSelectionBox;
    @FXML
    public AnchorPane sceneContainer;

    private Scene rawAccessScene;
    private Scene processedAccessScene;


    private static AdminPanel instance = null;

    private AdminPanel() {
        this.setTitle("Admin Panel");
        this.setResizable(false);
    }

    public static void open() {
        if (instance == null) {
            instance = initialInit();
        }
        instance.show();
        instance.toFront();
    }

    private static AdminPanel initialInit() {
        final AdminPanel panelController = new AdminPanel();
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
            } else {
                sceneContainer.getChildren().add(processedAccessScene.getRoot());
            }
        });

        //sceneContainer.getChildren().add(processedAccessScene.getRoot());

        rawAccessSwitch.setSelected(true);
        rawAccessSwitch.setSelected(false);

        endpointSelectionBox.getItems().addAll("backups", "tokens");
        endpointSelectionBox.setOnAction((handler) -> {

        });
    }

    private void postInit() {

    }

    private void initSubscenes() {
        initRawAccessScene();
        initProcessedAccessScene();
    }

    private void initRawAccessScene() {
        final RawAccessViewController controller = new RawAccessViewController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin-panel-raw-access-view.fxml"));
        fxmlLoader.setController(controller);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        rawAccessScene = scene;
        controller.postInit();
    }

    private void initProcessedAccessScene() {
        final ProcessedAccessViewController controller = new ProcessedAccessViewController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin-panel-processed-access-view.fxml"));
        fxmlLoader.setController(controller);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        processedAccessScene = scene;
        controller.postInit();
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
        public void initialize() {
            initUIElements();
        }

        private void initUIElements() {
            rawAccessHTTPMethodBox.getItems().addAll("GET", "POST", "PUT", "DELETE");
            rawAccessHTTPMethodBox.getSelectionModel().selectFirst();

            rawAccessViewSendButton.setOnAction((handler) -> {

            });
        }

        private void postInit() {

        }
    }

    private static class ProcessedAccessViewController {
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

        @FXML
        public void initialize() {
            initUIElements();
        }

        private void initUIElements() {
            processedAccessViewCreateBackupButton.setOnAction(handler -> {

            });

            processedAccessViewRestoreBackupButton.setOnAction(handler -> {

            });

            processedAccessViewCreateTokenButton.setOnAction(handler -> {

            });

            processedAccessViewFreezeTokenButton.setOnAction(handler -> {

            });


        }

        private void postInit() {

        }
    }



}
