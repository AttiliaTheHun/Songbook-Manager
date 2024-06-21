package attilathehun.songbook.window;

import attilathehun.songbook.Main;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.environment.Setting;
import attilathehun.songbook.environment.SettingsManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

// TODO settings may be altered through other components os the window should get refreshed somehow
public final class SettingsEditor extends Stage {
    private static final Logger logger = LogManager.getLogger(SettingsEditor.class);
    private static final SettingsEditor INSTANCE = new SettingsEditor();

    @FXML
    public ListView<String> settingCategoryList;
    @FXML
    public AnchorPane sceneContainer;


    private Scene generalSettingsScene;
    private Scene environmentSettingsScene;
    private Scene VCSSettingsScene;

    private SettingsEditor() {
        this.setTitle("Settings");
        this.setResizable(false);
    }

    public static SettingsEditor getInstance() {
        return INSTANCE;
    }

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

    public static void shut() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        }
        INSTANCE.hide();
    }

    public static void refresh() {
        getInstance().initSubscenes();
        getInstance().settingCategoryList.getSelectionModel().select(getInstance().settingCategoryList.getSelectionModel().getSelectedIndex());
    }

    @FXML
    public void initialize() {
        initSubscenes();
        initSettingCategoryListView();
        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, window -> hide());
    }

    public void postInit() {
        this.getScene().addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.R) {
                if (keyEvent.isControlDown()) {
                    refresh();
                }
            }
        });
    }

    private void initSettingCategoryListView() {
        final ArrayList<String> categories = new ArrayList<>();
        categories.add("General");
        categories.add("Environment");
        categories.add("Version Control System");
        settingCategoryList.setItems(FXCollections.observableArrayList(categories));
        settingCategoryList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        settingCategoryList.getSelectionModel().selectedItemProperty().addListener((observableValue, s, newValue) -> {
            sceneContainer.getChildren().remove(0);
            if (newValue.equals(categories.get(0))) {
                sceneContainer.getChildren().add(generalSettingsScene.getRoot());
            } else if (newValue.equals(categories.get(1))) {
                sceneContainer.getChildren().add(environmentSettingsScene.getRoot());
            } else if (newValue.equals(categories.get(2))) {
                sceneContainer.getChildren().add(VCSSettingsScene.getRoot());
            }
            settingCategoryList.refresh();
        });

        settingCategoryList.refresh();

        sceneContainer.getChildren().add(generalSettingsScene.getRoot());

        settingCategoryList.getSelectionModel().select(1);
        settingCategoryList.getSelectionModel().select(2);
        settingCategoryList.getSelectionModel().selectFirst();

    }

    private void initSubscenes() {
        initGeneralSettingsScene();
        initEnvironmentSettingsScene();
        initVCSSettingsScene();
    }

    private void initGeneralSettingsScene() {
        final GeneralSettingsController controller = new GeneralSettingsController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("settings-editor-general-settings.fxml"));
        fxmlLoader.setController(controller);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        generalSettingsScene = scene;
        controller.postInit();
    }

    private void initEnvironmentSettingsScene() {
        final EnvironmentSettingsController controller = new EnvironmentSettingsController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("settings-editor-environment-settings.fxml"));
        fxmlLoader.setController(controller);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        environmentSettingsScene = scene;
        controller.postInit();
    }

    private void initVCSSettingsScene() {
        final VCSSettingsController controller = new VCSSettingsController();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("settings-editor-vcs-settings.fxml"));
        fxmlLoader.setController(controller);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        VCSSettingsScene = scene;
        controller.postInit();
    }

    private static class GeneralSettingsController {
        @FXML
        public TextField songbookLanguageField;
        @FXML
        public CheckBox bindSongTitlesSwitch;
        @FXML
        public CheckBox enableFrontpageSwitch;
        @FXML
        public CheckBox enableDynamicSonglistSwitch;
        @FXML
        public TextField dynamicSonglistSongsPerColumnField;
        @FXML
        public CheckBox autoLoadDataSwitch;
        @FXML
        public TextField defaultReadTokenField;
        @FXML
        public TextField authFilePathField;
        @FXML
        public Button browseFilesForAuthFileButton;
        @FXML
        public CheckBox exportEnabledSwitch;
        @FXML
        public CheckBox keepBrowserInstanceAliveSwitch;
        @FXML
        public TextField defaultExportFileNameField;
        @FXML
        public TextField printableExportFileNameField;
        @FXML
        public TextField singlepageExportFileNameField;
        @FXML
        public TextField browserExecutablePathField;
        @FXML
        public Button browseBrowserExecutablePathButton;


        @FXML
        public void initialize() {
            initUIElements();
        }

        private void initUIElements() {
            final Setting<String> songbookLanguageSetting = (Setting<String>) SettingsManager.getInstance().get("SONGBOOK_LANGUAGE");
            songbookLanguageField.setText(songbookLanguageSetting.getValue());
            songbookLanguageField.setPromptText(songbookLanguageSetting.getInputFormatDescription());
            final Tooltip songbookLanguageFieldTooltip = new Tooltip();
            songbookLanguageFieldTooltip.setText(songbookLanguageSetting.getDescription());
            songbookLanguageField.setTooltip(songbookLanguageFieldTooltip);
            songbookLanguageField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // means focus lost
                    try {
                        if (songbookLanguageField.getText().trim().length() == 0) {
                            throw new Exception("a language set is necessary");
                        }
                        Locale.forLanguageTag(songbookLanguageField.getText().trim());
                        SettingsManager.getInstance().set("SONGBOOK_LANGUAGE", songbookLanguageField.getText().trim());
                    } catch (final Exception e) {
                        songbookLanguageField.setText(songbookLanguageSetting.getValue());
                    }
                }
            });

            final Setting<Boolean> bindTitlesLanguageSetting = (Setting<Boolean>) SettingsManager.getInstance().get("BIND_SONG_TITLES");
            bindSongTitlesSwitch.setSelected(bindTitlesLanguageSetting.getValue());
            final Tooltip bindSongTitlesSwitchTooltip = new Tooltip();
            bindSongTitlesSwitchTooltip.setText(bindTitlesLanguageSetting.getDescription());
            bindSongTitlesSwitch.setTooltip(bindSongTitlesSwitchTooltip);
            bindSongTitlesSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("BIND_SONG_TITLES", newValue);
            });

            final Setting<Boolean> enableFrontpageSetting = (Setting<Boolean>) SettingsManager.getInstance().get("ENABLE_FRONTPAGE");
            enableFrontpageSwitch.setSelected(enableFrontpageSetting.getValue());
            final Tooltip enableFrontpageSwitchTooltip = new Tooltip();
            enableFrontpageSwitchTooltip.setText(enableFrontpageSetting.getDescription());
            enableFrontpageSwitch.setTooltip(enableFrontpageSwitchTooltip);
            enableFrontpageSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("ENABLE_FRONTPAGE", newValue);
            });

            final Setting<Boolean> enableDynamicSonglistSetting = (Setting<Boolean>) SettingsManager.getInstance().get("ENABLE_DYNAMIC_SONGLIST");
            final Tooltip enableDynamicSonglistSwitchTooltip = new Tooltip();
            enableDynamicSonglistSwitchTooltip.setText(enableDynamicSonglistSetting.getDescription());
            enableDynamicSonglistSwitch.setTooltip(enableDynamicSonglistSwitchTooltip);
            enableDynamicSonglistSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("ENABLE_DYNAMIC_SONGLIST", newValue);
                dynamicSonglistSongsPerColumnField.setDisable(oldValue);
            });
            enableDynamicSonglistSwitch.setSelected(enableDynamicSonglistSetting.getValue());

            final Setting<Integer> songsPerColumnSetting = (Setting<Integer>) SettingsManager.getInstance().get("DYNAMIC_SONGLIST_SONGS_PER_COLUMN");
            dynamicSonglistSongsPerColumnField.setText(String.valueOf(songsPerColumnSetting.getValue()));
            dynamicSonglistSongsPerColumnField.setPromptText(songsPerColumnSetting.getInputFormatDescription());
            final Tooltip dynamicSonglistSongsPerColumnFieldTooltip = new Tooltip();
            dynamicSonglistSongsPerColumnFieldTooltip.setText(songsPerColumnSetting.getDescription());
            dynamicSonglistSongsPerColumnField.setTooltip(dynamicSonglistSongsPerColumnFieldTooltip);
            dynamicSonglistSongsPerColumnField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (songsPerColumnSetting.verifyValue(dynamicSonglistSongsPerColumnField.getText().trim())) {
                        SettingsManager.getInstance().set("DYNAMIC_SONGLIST_SONGS_PER_COLUMN", dynamicSonglistSongsPerColumnField.getText().trim());
                    } else {
                        dynamicSonglistSongsPerColumnField.setText(songsPerColumnSetting.getValue().toString());
                    }
                }
            });


            final Setting<Boolean> autoLoadDataSetting = (Setting<Boolean>) SettingsManager.getInstance().get("AUTO_LOAD_DATA");
            autoLoadDataSwitch.setSelected(enableDynamicSonglistSetting.getValue());
            final Tooltip autoLoadDataSwitchTooltip = new Tooltip();
            autoLoadDataSwitchTooltip.setText(autoLoadDataSetting.getDescription());
            autoLoadDataSwitch.setTooltip(autoLoadDataSwitchTooltip);
            autoLoadDataSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("AUTO_LOAD_DATA", newValue);
            });

            final Setting<String> defaultReadTokenSetting = (Setting<String>) SettingsManager.getInstance().get("DEFAULT_READ_TOKEN");
            defaultReadTokenField.setText(defaultReadTokenSetting.getValue());
            defaultReadTokenField.setPromptText(defaultReadTokenSetting.getInputFormatDescription());
            final Tooltip defaultReadTokenFieldTooltip = new Tooltip();
            defaultReadTokenFieldTooltip.setText(defaultReadTokenSetting.getDescription());
            defaultReadTokenField.setTooltip(defaultReadTokenFieldTooltip);
            defaultReadTokenField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (defaultReadTokenSetting.verifyValue(defaultReadTokenField.getText().trim())) {
                        SettingsManager.getInstance().set("DEFAULT_READ_TOKEN", defaultReadTokenField.getText().trim());
                    } else {
                        defaultReadTokenField.setText(defaultReadTokenSetting.getValue());
                    }
                }
            });

            final Setting<String> authFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("AUTH_FILE_PATH");
            authFilePathField.setText(authFilePathSetting.getValue());
            authFilePathField.setPromptText(authFilePathSetting.getInputFormatDescription());
            final Tooltip authFilePathFieldTooltip = new Tooltip();
            authFilePathFieldTooltip.setText(defaultReadTokenSetting.getDescription());
            authFilePathField.setTooltip(authFilePathFieldTooltip);
            authFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (authFilePathSetting.verifyValue(authFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("AUTH_FILE_PATH", authFilePathField.getText().trim());
                    } else {
                        authFilePathField.setText(authFilePathSetting.getValue());
                    }
                }
            });

            browseFilesForAuthFileButton.setOnAction(event -> {
                final FileChooser fileChoose = new FileChooser();
                fileChoose.getExtensionFilters().add(new FileChooser.ExtensionFilter("Weird files",  "*.auth"));
                final File file = fileChoose.showOpenDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("AUTH_FILE_PATH", file.toString());
                    authFilePathField.setText(file.toString());
                }
            });

            final Setting<Boolean> enableExportSetting = (Setting<Boolean>) SettingsManager.getInstance().get("EXPORT_ENABLED");
            final Tooltip enableExportSwitchTooltip = new Tooltip();
            enableExportSwitchTooltip.setText(enableExportSetting.getDescription());
            exportEnabledSwitch.setTooltip(enableExportSwitchTooltip);
            exportEnabledSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("EXPORT_ENABLED", newValue);
                keepBrowserInstanceAliveSwitch.setDisable(oldValue);
                defaultReadTokenField.setDisable(oldValue);
                printableExportFileNameField.setDisable(oldValue);
                singlepageExportFileNameField.setDisable(oldValue);
            });
            exportEnabledSwitch.setSelected(enableDynamicSonglistSetting.getValue());

            final Setting<Boolean> keepBworserInstanceAliveSetting = (Setting<Boolean>) SettingsManager.getInstance().get("EXPORT_KEEP_BROWSER_INSTANCE");
            final Tooltip keepBrowserInstanceAliveSwitchTooltip = new Tooltip();
            keepBrowserInstanceAliveSwitchTooltip.setText(keepBworserInstanceAliveSetting.getDescription());
            keepBrowserInstanceAliveSwitch.setSelected(keepBworserInstanceAliveSetting.getValue());
            keepBrowserInstanceAliveSwitch.setTooltip(keepBrowserInstanceAliveSwitchTooltip);
            keepBrowserInstanceAliveSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("EXPORT_KEEP_BROWSER_INSTANCE", newValue);
            });
            
            final Setting<String> defaultFileNameSetting = (Setting<String>) SettingsManager.getInstance().get("EXPORT_DEFAULT_FILE_NAME");
            defaultExportFileNameField.setText(defaultFileNameSetting.getValue());
            defaultExportFileNameField.setPromptText(defaultFileNameSetting.getInputFormatDescription());
            final Tooltip defaultExportFileNameFieldFieldTooltip = new Tooltip();
            defaultExportFileNameFieldFieldTooltip.setText(defaultFileNameSetting.getDescription());
            defaultExportFileNameField.setTooltip(defaultExportFileNameFieldFieldTooltip);
            defaultExportFileNameField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (defaultFileNameSetting.verifyValue(defaultExportFileNameField.getText().trim())) {
                        SettingsManager.getInstance().set("EXPORT_DEFAULT_FILE_NAME", defaultExportFileNameField.getText().trim());
                    } else {
                        defaultExportFileNameField.setText(defaultFileNameSetting.getValue().toString());
                    }
                }
            });

            final Setting<String> printableFileNameSetting = (Setting<String>) SettingsManager.getInstance().get("EXPORT_PRINTABLE_FILE_NAME");
            printableExportFileNameField.setText(printableFileNameSetting.getValue());
            printableExportFileNameField.setPromptText(printableFileNameSetting.getInputFormatDescription());
            final Tooltip printableExportFileNameFieldFieldTooltip = new Tooltip();
            printableExportFileNameFieldFieldTooltip.setText(printableFileNameSetting.getDescription());
            printableExportFileNameField.setTooltip(printableExportFileNameFieldFieldTooltip);
            printableExportFileNameField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (printableFileNameSetting.verifyValue(printableExportFileNameField.getText().trim())) {
                        SettingsManager.getInstance().set("EXPORT_PRINTABLE_FILE_NAME", printableExportFileNameField.getText().trim());
                    } else {
                        printableExportFileNameField.setText(printableFileNameSetting.getValue().toString());
                    }
                }
            });

            final Setting<String> singlepageFileNameSetting = (Setting<String>) SettingsManager.getInstance().get("EXPORT_SINGLEPAGE_FILE_NAME");
            singlepageExportFileNameField.setText(singlepageFileNameSetting.getValue());
            singlepageExportFileNameField.setPromptText(singlepageFileNameSetting.getInputFormatDescription());
            final Tooltip singlepageExportFileNameFieldFieldTooltip = new Tooltip();
            singlepageExportFileNameFieldFieldTooltip.setText(singlepageFileNameSetting.getDescription());
            singlepageExportFileNameField.setTooltip(singlepageExportFileNameFieldFieldTooltip);
            singlepageExportFileNameField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (singlepageFileNameSetting.verifyValue(singlepageExportFileNameField.getText().trim())) {
                        SettingsManager.getInstance().set("EXPORT_SINGLEPAGE_FILE_NAME", singlepageExportFileNameField.getText().trim());
                    } else {
                        singlepageExportFileNameField.setText(singlepageFileNameSetting.getValue().toString());
                    }
                }
            });

            final Setting<String> browserExecutablePathSetting = (Setting<String>) SettingsManager.getInstance().get("EXPORT_BROWSER_EXECUTABLE_PATH");
            browserExecutablePathField.setText(browserExecutablePathSetting.getValue());
            browserExecutablePathField.setPromptText(browserExecutablePathSetting.getInputFormatDescription());
            final Tooltip browserExecutablePathFieldTooltip = new Tooltip();
            browserExecutablePathFieldTooltip.setText(browserExecutablePathSetting.getDescription());
            browserExecutablePathField.setTooltip(browserExecutablePathFieldTooltip);
            browserExecutablePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (browserExecutablePathSetting.verifyValue(browserExecutablePathField.getText().trim())) {
                        SettingsManager.getInstance().set("EXPORT_BROWSER_EXECUTABLE_PATH", browserExecutablePathField.getText().trim());
                    } else {
                        browserExecutablePathField.setText(browserExecutablePathSetting.getValue());
                    }
                }
            });

            browseBrowserExecutablePathButton.setOnAction(event -> {
                final FileChooser fileChoose = new FileChooser();
                fileChoose.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable files",  "*.exe"));
                final File file = fileChoose.showOpenDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("EXPORT_BROWSER_EXECUTABLE_PATH", file.toString());
                    browserExecutablePathField.setText(file.toString());
                }
            });


        }

        public void postInit() {

        }

    }

    private static class EnvironmentSettingsController {
        @FXML
        public TextField dataFilePathField;
        @FXML
        public Button browseDataFilePathButton;
        @FXML
        public TextField songsFilePathField;
        @FXML
        public Button browseSongsFilePathButton;
        @FXML
        public TextField resourcesFilePathField;
        @FXML
        public Button browseResourcesFilePathButton;
        @FXML
        public TextField CSSResourcesFilePathField;
        @FXML
        public Button browseCSSResourcesFilePathButton;
        @FXML
        public TextField templateResourcesFilePathField;
        @FXML
        public Button browseTemplateResourcesFilePathButton;
        @FXML
        public TextField assetResourcesFilePathField;
        @FXML
        public Button browseAssetResourcesFilePathButton;
        @FXML
        public TextField dataZipFilePathField;
        @FXML
        public Button browseDataZipFilePathButton;
        @FXML
        public TextField tempFilePathField;
        @FXML
        public Button browseTempFilePathButton;
        @FXML
        public TextField exportFilePathField;
        @FXML
        public Button browseExportFilePathButton;
        @FXML
        public TextField logFilePathField;
        @FXML
        public Button browseLogFilePathButton;
        @FXML
        public TextField scriptsFilePathField;
        @FXML
        public Button browseScriptsFilePathButton;


        @FXML
        public void initialize() {
            initUIElements();
        }

        private void initUIElements() {
            final DirectoryChooser directoryChooser = new DirectoryChooser();

            final Setting<String> dataFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("DATA_FILE_PATH");
            dataFilePathField.setText(dataFilePathSetting.getValue());
            dataFilePathField.setPromptText(dataFilePathSetting.getInputFormatDescription());
            final Tooltip dataFilePathFieldTooltip = new Tooltip();
            dataFilePathFieldTooltip.setText(dataFilePathSetting.getDescription());
            dataFilePathField.setTooltip(dataFilePathFieldTooltip);
            dataFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (dataFilePathSetting.verifyValue(dataFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("DATA_FILE_PATH", dataFilePathField.getText().trim());
                    } else {
                        dataFilePathField.setText(dataFilePathSetting.getValue());
                    }
                }
            });

            browseDataFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("DATA_FILE_PATH", file.toString());
                    dataFilePathField.setText(file.toString());
                }
            });

            final Setting<String> songsFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("SONGS_FILE_PATH");
            songsFilePathField.setText(songsFilePathSetting.getValue());
            songsFilePathField.setPromptText(songsFilePathSetting.getInputFormatDescription());
            final Tooltip songsFilePathFieldTooltip = new Tooltip();
            songsFilePathFieldTooltip.setText(songsFilePathSetting.getDescription());
            songsFilePathField.setTooltip(songsFilePathFieldTooltip);
            songsFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (songsFilePathSetting.verifyValue(songsFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("SONGS_FILE_PATH", songsFilePathField.getText().trim());
                    } else {
                        songsFilePathField.setText(songsFilePathSetting.getValue());
                    }
                }
            });

            browseSongsFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("SONGS_FILE_PATH", file.toString());
                    songsFilePathField.setText(file.toString());
                }
            });

            final Setting<String> resourcesFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("RESOURCES_FILE_PATH");
            resourcesFilePathField.setText(resourcesFilePathSetting.getValue());
            resourcesFilePathField.setPromptText(resourcesFilePathSetting.getInputFormatDescription());
            final Tooltip resourcesFilePathFieldTooltip = new Tooltip();
            resourcesFilePathFieldTooltip.setText(resourcesFilePathSetting.getDescription());
            resourcesFilePathField.setTooltip(resourcesFilePathFieldTooltip);
            resourcesFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (resourcesFilePathSetting.verifyValue(resourcesFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("RESOURCES_FILE_PATH", resourcesFilePathField.getText().trim());
                    } else {
                        resourcesFilePathField.setText(resourcesFilePathSetting.getValue());
                    }
                }
            });

            browseResourcesFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("RESOURCES_FILE_PATH", file.toString());
                    resourcesFilePathField.setText(file.toString());
                }
            });

            final Setting<String> CSSFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("CSS_RESOURCES_FILE_PATH");
            CSSResourcesFilePathField.setText(CSSFilePathSetting.getValue());
            CSSResourcesFilePathField.setPromptText(CSSFilePathSetting.getInputFormatDescription());
            final Tooltip CSSResourcesFilePathFieldTooltip = new Tooltip();
            CSSResourcesFilePathFieldTooltip.setText(CSSFilePathSetting.getDescription());
            CSSResourcesFilePathField.setTooltip(CSSResourcesFilePathFieldTooltip);
            CSSResourcesFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (CSSFilePathSetting.verifyValue(CSSResourcesFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("CSS_RESOURCES_FILE_PATH", CSSResourcesFilePathField.getText().trim());
                    } else {
                        CSSResourcesFilePathField.setText(CSSFilePathSetting.getValue());
                    }
                }
            });

            browseCSSResourcesFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("CSS_RESOURCES_FILE_PATH", file.toString());
                    CSSResourcesFilePathField.setText(file.toString());
                }
            });

            final Setting<String> templatesFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("TEMPLATE_RESOURCES_FILE_PATH");
            templateResourcesFilePathField.setText(templatesFilePathSetting.getValue());
            templateResourcesFilePathField.setPromptText(templatesFilePathSetting.getInputFormatDescription());
            final Tooltip templateResourcesFilePathFieldTooltip = new Tooltip();
            templateResourcesFilePathFieldTooltip.setText(templatesFilePathSetting.getDescription());
            templateResourcesFilePathField.setTooltip(templateResourcesFilePathFieldTooltip);
            templateResourcesFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (templatesFilePathSetting.verifyValue(templateResourcesFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("TEMPLATE_RESOURCES_FILE_PATH", templateResourcesFilePathField.getText().trim());
                    } else {
                        templateResourcesFilePathField.setText(templatesFilePathSetting.getValue());
                    }
                }
            });

            browseTemplateResourcesFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("TEMPLATE_RESOURCES_FILE_PATH", file.toString());
                    templateResourcesFilePathField.setText(file.toString());
                }
            });

            final Setting<String> assetsFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("ASSET_RESOURCES_FILE_PATH");
            assetResourcesFilePathField.setText(assetsFilePathSetting.getValue());
            assetResourcesFilePathField.setPromptText(assetsFilePathSetting.getInputFormatDescription());
            final Tooltip assetResourcesFilePathFieldTooltip = new Tooltip();
            assetResourcesFilePathFieldTooltip.setText(assetsFilePathSetting.getDescription());
            assetResourcesFilePathField.setTooltip(assetResourcesFilePathFieldTooltip);
            assetResourcesFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (assetsFilePathSetting.verifyValue(assetResourcesFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("ASSET_RESOURCES_FILE_PATH", assetResourcesFilePathField.getText().trim());
                    } else {
                        assetResourcesFilePathField.setText(assetsFilePathSetting.getValue());
                    }
                }
            });

            browseAssetResourcesFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("ASSET_RESOURCES_FILE_PATH", file.toString());
                    assetResourcesFilePathField.setText(file.toString());
                }
            });

            final Setting<String> dataZipFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("DATA_ZIP_FILE_PATH");
            dataZipFilePathField.setText(dataZipFilePathSetting.getValue());
            dataZipFilePathField.setPromptText(dataZipFilePathSetting.getInputFormatDescription());
            final Tooltip dataZipFilePathFieldTooltip = new Tooltip();
            dataZipFilePathFieldTooltip.setText(dataZipFilePathSetting.getDescription());
            dataZipFilePathField.setTooltip(dataZipFilePathFieldTooltip);
            dataZipFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (dataZipFilePathSetting.verifyValue(dataZipFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("DATA_ZIP_FILE_PATH", dataZipFilePathField.getText().trim());
                    } else {
                        dataZipFilePathField.setText(dataZipFilePathSetting.getValue());
                    }
                }
            });

            browseDataZipFilePathButton.setOnAction(event -> {
                final FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archive files", "*.zip"));
                final File file = fileChooser.showOpenDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("DATA_ZIP_FILE_PATH", file.toString());
                    dataZipFilePathField.setText(file.toString());
                }
            });

            final Setting<String> tempFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("TEMP_FILE_PATH");
            tempFilePathField.setText(tempFilePathSetting.getValue());
            tempFilePathField.setPromptText(tempFilePathSetting.getInputFormatDescription());
            final Tooltip tempFilePathFieldTooltip = new Tooltip();
            tempFilePathFieldTooltip.setText(tempFilePathSetting.getDescription());
            tempFilePathField.setTooltip(tempFilePathFieldTooltip);
            tempFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (tempFilePathSetting.verifyValue(tempFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("TEMP_FILE_PATH", tempFilePathField.getText().trim());
                    } else {
                        tempFilePathField.setText(tempFilePathSetting.getValue());
                    }
                }
            });

            browseTempFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("TEMP_FILE_PATH", file.toString());
                    tempFilePathField.setText(file.toString());
                }
            });

            final Setting<String> exportFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("EXPORT_FILE_PATH");
            exportFilePathField.setText(exportFilePathSetting.getValue());
            exportFilePathField.setPromptText(exportFilePathSetting.getInputFormatDescription());
            final Tooltip exportFilePathFieldTooltip = new Tooltip();
            exportFilePathFieldTooltip.setText(exportFilePathSetting.getDescription());
            exportFilePathField.setTooltip(exportFilePathFieldTooltip);
            exportFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (exportFilePathSetting.verifyValue(exportFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("EXPORT_FILE_PATH", exportFilePathField.getText().trim());
                    } else {
                        exportFilePathField.setText(exportFilePathSetting.getValue());
                    }
                }
            });

            browseExportFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("EXPORT_FILE_PATH", file.toString());
                    exportFilePathField.setText(file.toString());
                }
            });

            final Setting<String> logFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("LOG_FILE_PATH");
            logFilePathField.setText(logFilePathSetting.getValue());
            logFilePathField.setPromptText(logFilePathSetting.getInputFormatDescription());
            final Tooltip logFilePathFieldTooltip = new Tooltip();
            logFilePathFieldTooltip.setText(logFilePathSetting.getDescription());
            logFilePathField.setTooltip(logFilePathFieldTooltip);
            logFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (logFilePathSetting.verifyValue(logFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("LOG_FILE_PATH", logFilePathField.getText().trim());
                    } else {
                        logFilePathField.setText(logFilePathSetting.getValue());
                    }
                }
            });

            browseLogFilePathButton.setOnAction(event -> {
                final FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
                final File file = fileChooser.showOpenDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("LOG_FILE_PATH", file.toString());
                    logFilePathField.setText(file.toString());
                }
            });

            final Setting<String> scriptsFilePathSetting = (Setting<String>) SettingsManager.getInstance().get("SCRIPTS_FILE_PATH");
            scriptsFilePathField.setText(scriptsFilePathSetting.getValue());
            scriptsFilePathField.setPromptText(scriptsFilePathSetting.getInputFormatDescription());
            final Tooltip scriptsFilePathFieldTooltip = new Tooltip();
            scriptsFilePathFieldTooltip.setText(scriptsFilePathSetting.getDescription());
            scriptsFilePathField.setTooltip(scriptsFilePathFieldTooltip);
            scriptsFilePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (scriptsFilePathSetting.verifyValue(scriptsFilePathField.getText().trim())) {
                        SettingsManager.getInstance().set("SCRIPTS_FILE_PATH", scriptsFilePathField.getText().trim());
                    } else {
                        scriptsFilePathField.setText(scriptsFilePathSetting.getValue());
                    }
                }
            });

            browseScriptsFilePathButton.setOnAction(event -> {
                final File file = directoryChooser.showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("EXPORT_FILE_PATH", file.toString());
                    exportFilePathField.setText(file.toString());
                }
            });
        }

        public void postInit() {

        }

    }

    private static class VCSSettingsController {
        @FXML
        public CheckBox remoteSaveLoadSwitch;
        @FXML
        public TextField remoteDataDownloadURLField;
        @FXML
        public TextField remoteDataUploadURLField;
        @FXML
        public TextField remoteDataIndexURLField;
        @FXML
        public TextField remoteDataVersionTimestampField;
        @FXML
        public TextField VCSCachePathField;
        @FXML
        public Button browseVCSCachePathButton;
        @FXML
        public TextField VCSThreadCountField;


        @FXML
        public void initialize() {
            initUIComponents();
        }


        private void initUIComponents() {
            final Setting<Boolean> enableVCSSetting = (Setting<Boolean>) SettingsManager.getInstance().get("REMOTE_SAVE_LOAD_ENABLED");
            final Tooltip remoteSaveLoadDataSwitchTooltip = new Tooltip();
            remoteSaveLoadDataSwitchTooltip.setText(enableVCSSetting.getDescription());
            remoteSaveLoadSwitch.setTooltip(remoteSaveLoadDataSwitchTooltip);
            remoteSaveLoadSwitch.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                SettingsManager.getInstance().set("REMOTE_SAVE_LOAD_ENABLED", newValue);
                remoteDataDownloadURLField.setDisable(oldValue);
                remoteDataUploadURLField.setDisable(oldValue);
                remoteDataIndexURLField.setDisable(oldValue);
                remoteDataVersionTimestampField.setDisable(oldValue);
                VCSCachePathField.setDisable(oldValue);
                browseVCSCachePathButton.setDisable(oldValue);
                VCSThreadCountField.setDisable(oldValue);
            });
            remoteSaveLoadSwitch.setSelected(enableVCSSetting.getValue());

            final Setting<String> remoteDownloadSetting = (Setting<String>) SettingsManager.getInstance().get("REMOTE_DATA_DOWNLOAD_URL");
            remoteDataDownloadURLField.setText(remoteDownloadSetting.getValue());
            remoteDataDownloadURLField.setPromptText(remoteDownloadSetting.getInputFormatDescription());
            final Tooltip remoteDataDownloadFieldTooltip = new Tooltip();
            remoteDataDownloadFieldTooltip.setText(remoteDownloadSetting.getDescription());
            remoteDataDownloadURLField.setTooltip(remoteDataDownloadFieldTooltip);
            remoteDataDownloadURLField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (remoteDownloadSetting.verifyValue(remoteDataDownloadURLField.getText().trim())) {
                        SettingsManager.getInstance().set("REMOTE_DATA_DOWNLOAD_URL", remoteDataDownloadURLField.getText().trim());
                    } else {
                        remoteDataDownloadURLField.setText(remoteDownloadSetting.getValue());
                    }
                }
            });

            final Setting<String> remoteUploadSetting = (Setting<String>) SettingsManager.getInstance().get("REMOTE_DATA_UPLOAD_URL");
            remoteDataUploadURLField.setText(remoteUploadSetting.getValue());
            remoteDataUploadURLField.setPromptText(remoteUploadSetting.getInputFormatDescription());
            final Tooltip remoteDataUploadFieldTooltip = new Tooltip();
            remoteDataUploadFieldTooltip.setText(remoteUploadSetting.getDescription());
            remoteDataUploadURLField.setTooltip(remoteDataUploadFieldTooltip);
            remoteDataUploadFieldTooltip.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (remoteUploadSetting.verifyValue(remoteDataUploadFieldTooltip.getText().trim())) {
                        SettingsManager.getInstance().set("REMOTE_DATA_UPLOAD_URL", remoteDataUploadFieldTooltip.getText().trim());
                    } else {
                        remoteDataUploadFieldTooltip.setText(remoteUploadSetting.getValue());
                    }
                }
            });

            final Setting<String> remoteIndexSetting = (Setting<String>) SettingsManager.getInstance().get("REMOTE_DATA_INDEX_URL");
            remoteDataIndexURLField.setText(remoteIndexSetting.getValue());
            remoteDataIndexURLField.setPromptText(remoteIndexSetting.getInputFormatDescription());
            final Tooltip remoteDataIndexFieldTooltip = new Tooltip();
            remoteDataIndexFieldTooltip.setText(remoteIndexSetting.getDescription());
            remoteDataIndexURLField.setTooltip(remoteDataIndexFieldTooltip);
            remoteDataIndexFieldTooltip.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (remoteIndexSetting.verifyValue(remoteDataIndexURLField.getText().trim())) {
                        SettingsManager.getInstance().set("REMOTE_DATA_INDEX_URL", remoteDataIndexURLField.getText().trim());
                    } else {
                        remoteDataIndexURLField.setText(remoteIndexSetting.getValue());
                    }
                }
            });

            final Setting<String> remoteVersionTimestampSetting = (Setting<String>) SettingsManager.getInstance().get("REMOTE_DATA_VERSION_TIMESTAMP_URL");
            remoteDataVersionTimestampField.setText(remoteVersionTimestampSetting.getValue());
            remoteDataVersionTimestampField.setPromptText(remoteVersionTimestampSetting.getInputFormatDescription());
            final Tooltip remoteDataVersionTimestampFieldTooltip = new Tooltip();
            remoteDataVersionTimestampFieldTooltip.setText(remoteVersionTimestampSetting.getDescription());
            remoteDataVersionTimestampField.setTooltip(remoteDataVersionTimestampFieldTooltip);
            remoteDataVersionTimestampField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (remoteDownloadSetting.verifyValue(remoteDataVersionTimestampField.getText().trim())) {
                        SettingsManager.getInstance().set("REMOTE_DATA_VERSION_TIMESTAMP_URL", remoteDataVersionTimestampField.getText().trim());
                    } else {
                        remoteDataVersionTimestampField.setText(remoteVersionTimestampSetting.getValue());
                    }
                }
            });

            final Setting<String> VCSCachePathSetting = (Setting<String>) SettingsManager.getInstance().get("VCS_CACHE_PATH");
            VCSCachePathField.setText(VCSCachePathSetting.getValue());
            VCSCachePathField.setPromptText(VCSCachePathSetting.getInputFormatDescription());
            final Tooltip VCSCachePathFieldTooltip = new Tooltip();
            VCSCachePathFieldTooltip.setText(VCSCachePathSetting.getDescription());
            VCSCachePathField.setTooltip(VCSCachePathFieldTooltip);
            VCSCachePathField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (VCSCachePathSetting.verifyValue(VCSCachePathField.getText().trim())) {
                        SettingsManager.getInstance().set("VCS_CACHE_PATH", VCSCachePathField.getText().trim());
                    } else {
                        VCSThreadCountField.setText(VCSCachePathSetting.getValue());
                    }
                }
            });

            browseVCSCachePathButton.setOnAction(event -> {
                final File file = new DirectoryChooser().showDialog(new Stage());
                if (file != null) {
                    SettingsManager.getInstance().set("VCS_CACHE_PATH", file.toString());
                    VCSCachePathField.setText(file.toString());
                }
            });
            

            final Setting<Integer> VCSThreadCountSetting = (Setting<Integer>) SettingsManager.getInstance().get("VCS_THREAD_COUNT");
            VCSThreadCountField.setText(VCSThreadCountSetting.getValue().toString());
            VCSThreadCountField.setPromptText(VCSThreadCountSetting.getInputFormatDescription());
            final Tooltip VCSThreadCountFieldTooltip = new Tooltip();
            VCSThreadCountFieldTooltip.setText(VCSThreadCountSetting.getDescription());
            VCSThreadCountField.setTooltip(VCSThreadCountFieldTooltip);
            VCSThreadCountField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue) { // focus lost
                    if (VCSThreadCountSetting.verifyValue(VCSThreadCountField.getText().trim())) {
                        SettingsManager.getInstance().set("VCS_THREAD_COUNT", VCSThreadCountField.getText().trim());
                    } else {
                        VCSCachePathField.setText(VCSThreadCountSetting.getValue().toString());
                    }
                }
            });

            
        }

        public void postInit() {

        }
    }

}
