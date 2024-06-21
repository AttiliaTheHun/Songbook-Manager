package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import attilathehun.songbook.Main;
import attilathehun.songbook.collection.*;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.EnvironmentStateListener;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.PDFGenerator;
import attilathehun.songbook.util.HTMLGenerator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

//TODO: when songs get disabled, there is a problem with the latest page/song being out of bonds, fixing it?
public class SongbookController implements CollectionListener, EnvironmentStateListener {

    private static final Logger logger = LogManager.getLogger(SongbookController.class);

    private static Song SONG_ONE;
    private static int SONG_ONE_INDEX;
    private static Song SONG_TWO;
    private static int SONG_TWO_INDEX;
    private HTMLGenerator generator = new HTMLGenerator();
    @FXML
    private WebView webview;
    @FXML
    private ImageView openSettingsButton;
    @FXML
    private TextField songOneIdField;
    @FXML
    private TextField songTwoIdField;
    @FXML
    private Button applySongOneId;
    @FXML
    private Button applySongTwoId;
    @FXML
    private Button editCollectionButton;
    @FXML
    private Button loadDataButton;
    @FXML
    private Button saveDataButton;
    @FXML
    private Button editSongOneHTML;
    @FXML
    private Button editSongTwoHTML;
    @FXML
    private Button refreshButton;
    @FXML
    private Button previewButton;
    @FXML
    private Button addSongButton;
    @FXML
    private MenuButton exportButton;
    @FXML
    private ToggleSwitch easterSwitch;
    @FXML
    private MenuItem singlepageSelection;
    @FXML
    private MenuItem defaultSelection;
    @FXML
    private MenuItem printableSelection;

    /**
     * Returns the song in display on the left. It is possible the song will be a shadow song.
     *
     * @return the song on the left
     */
    public static Song getSongOne() {
        return SONG_ONE;
    }

    /**
     * Returns the song in display on the right. It is possible the song will be a shadow song.
     *
     * @return the song on the right
     */
    public static Song getSongTwo() {
        return SONG_TWO;
    }

    /**
     * JavaFX method that gets called when an FXML file gets inflated.
     *
     * @throws MalformedURLException
     */
    @FXML
    private void initialize() throws MalformedURLException {
        Environment.addListener(this);

        initWebView();

        initCollectionEditor();

        initSettingsEditor();

        initUIComponents();
    }

    /**
     * Initializes the web view to the default position. Default position is start of the songbook, more practically speaking the beginning of the default collection manager's
     * collection substituted, if necessary, by shadow songs.
     *
     * @throws MalformedURLException shouldn't happen
     */
    private void initWebView() throws MalformedURLException {
        SONG_ONE_INDEX = 0;
        SONG_TWO_INDEX = 1;

        if (Environment.getInstance().getCollectionManager().getFormalCollection().size() == 0) {
            SONG_ONE = CollectionManager.getShadowSong();
            SONG_TWO = CollectionManager.getShadowSong();
        } else if (Environment.getInstance().getCollectionManager().getFormalCollection().size() == 1) {
            SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(0);
            SONG_TWO = CollectionManager.getShadowSong();
        } else {
            SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(0);
            SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(1);
        }

        final URL url = new File(generator.generatePageFile(SONG_ONE, SONG_TWO)).toURI().toURL();
        webview.getEngine().load(url.toExternalForm());
    }

    /**
     * Initializes and creates the CollectionEditor window, which is an instance of {@link CollectionEditor}. The window is hidden in background and can be summoned using
     * {@link CollectionEditor#open()}.
     */
    private void initCollectionEditor() {
        final CollectionEditor editorController = CollectionEditor.getInstance();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("collection-editor.fxml"));
        fxmlLoader.setController(editorController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        editorController.setScene(scene);
        editorController.postInit();
    }

    /**
     * Initializes and creates the SettingsEditor window, which is an instance of {@link SettingsEditor}. The window is hidden in background and can be summoned using
     * {@link SettingsEditor#open()}.
     */
    private void initSettingsEditor() {
        final SettingsEditor editorController = SettingsEditor.getInstance();
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("settings-editor.fxml"));
        fxmlLoader.setController(editorController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        editorController.setScene(scene);
        editorController.postInit();
    }

    /**
     * Initializes the main window UI components' actions and behavior
     */
    private void initUIComponents() {

        songOneIdField.setText(SONG_ONE.getDisplayId());
        songTwoIdField.setText(SONG_TWO.getDisplayId());

        openSettingsButton.setImage(new Image(Main.class.getResourceAsStream("/assets/gear.png")));


        // doesn't work, fix?
        openSettingsButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            openSettingsButton.setStyle("-fx-background-color:#dae7f3;");
        });

        openSettingsButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            openSettingsButton.setStyle("-fx-background-color:transparent;");
        });

        openSettingsButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (SettingsEditor.getInstance() != null && SettingsEditor.getInstance().isShowing()) {
                SettingsEditor.shut();
            } else {
                SettingsEditor.open();
                event.consume();
            }
        });

        editCollectionButton.setOnAction(event -> {
            CollectionEditor.open();
        });

        loadDataButton.setOnAction(event -> {
            EnvironmentManager.getInstance().load();
        });

        saveDataButton.setOnAction(event -> {
            EnvironmentManager.getInstance().save();
        });

        songOneIdField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                applySongOneId.fire();
            }
        });


        songTwoIdField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                applySongTwoId.fire();
            }
        });

        applySongOneId.setOnAction(event -> {
            final String text = songOneIdField.getText().trim().toLowerCase();
            if (text.length() == 0) {
                return;
            }
            int id = CollectionManager.INVALID_SONG_ID;
            lol:
            try {
                id = Integer.parseInt(text);
                if (id < 0)
                    break lol;
                int index = Environment.getInstance().getCollectionManager().getFormalCollectionSongIndex(id);
                if (index > 0) {
                    SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(index);
                    SONG_ONE_INDEX = index;
                }

            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            if (id == -1) {
                int index = 0;
                for (final Song song : Environment.getInstance().getCollectionManager().getFormalCollection()) {
                    if (song.name().equals(text)) {
                        SONG_ONE = song;
                        SONG_ONE_INDEX = index;
                        break;
                    }
                    index++;
                }
            }
            refreshWebView();
        });

        //TODO: when inputing "songlist0" this throws @NumberFormatException, fix it!
        applySongTwoId.setOnAction(event -> {
            final String text = songTwoIdField.getText().trim().toLowerCase();
            if (text.length() == 0) {
                return;
            }
            int id = -1;
            lol:
            try {
                id = Integer.parseInt(text);
                if (id == -1)
                    break lol;
                final int index = Environment.getInstance().getCollectionManager().getFormalCollectionSongIndex(id);
                if (index != -1) {
                    SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(index);
                    SONG_TWO_INDEX = index;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (id == -1) {
                int index = 0;
                for (final Song song : Environment.getInstance().getCollectionManager().getFormalCollection()) {
                    if (song.name().equals(text)) {
                        SONG_TWO = song;
                        SONG_TWO_INDEX = index;
                        break;
                    }
                    index++;
                }
            }
            refreshWebView();
        });

        refreshButton.setOnAction(event -> {
            Environment.getInstance().hardRefresh();
        });

        editSongOneHTML.setOnAction(event -> {
            if (SONG_ONE.id() < 0) {
                new AlertDialog.Builder().setTitle("Message").setIcon(AlertDialog.Builder.Icon.INFO)
                        .setMessage(String.format("This page is generated automatically from a template. You can edit the template at %s.", SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH")))
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
            CodeEditor.open(SONG_ONE, Environment.getInstance().getCollectionManager());
        });

        // TODO when SML is enabled, change text to "Edit SML" and allow for "Edit HTML" with Shift-Click
        editSongTwoHTML.setOnAction(event -> {
            if (SONG_TWO.id() < 0) {
                new AlertDialog.Builder().setTitle("Message").setIcon(AlertDialog.Builder.Icon.INFO)
                        .setMessage(String.format("This page is generated automatically from a template. You can edit the template at %s.", SettingsManager.getInstance().getValue("TEMPLATE_RESOURCES_FILE_PATH")))
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
            CodeEditor.open(SONG_TWO, Environment.getInstance().getCollectionManager());
        });

        exportButton.showingProperty().addListener((event) -> {
            if (!(Boolean) SettingsManager.getInstance().getValue("EXPORT_ENABLED")) {
                new AlertDialog.Builder().setTitle("Exporting disabled").setIcon(AlertDialog.Builder.Icon.WARNING)
                        .setMessage("It seems like the Export plugin is disabled. You can enabled it in settings.")
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
        });

        if (!(Boolean) SettingsManager.getInstance().getValue("EXPORT_ENABLED")) {
            singlepageSelection.setVisible(false);
            defaultSelection.setVisible(false);
            printableSelection.setVisible(false);
        }

        singlepageSelection.setOnAction(event -> {
            try {
                new PDFGenerator().generateSinglepage();
            } catch (final Exception e) {
                if ("ignore".equals(e.getMessage())) {
                    return;
                }
                logger.error(e.getLocalizedMessage(), e);
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage(e.getMessage())
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            }

        });

        defaultSelection.setOnAction(event -> {
            try {
                new PDFGenerator().generateDefault();
            } catch (final Exception e) {
                if ("ignore".equals(e.getMessage())) {
                    return;
                }
                logger.error(e.getLocalizedMessage(), e);
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage(e.getMessage())
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            }
        });

        printableSelection.setOnAction(event -> {
            try {
                new PDFGenerator().generatePrintable();
            } catch (final Exception e) {
                if ("ignore".equals(e.getMessage())) {
                    return;
                }
                logger.error(e.getLocalizedMessage(), e);
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage(e.getMessage())
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            }
        });

        if (Environment.IS_IT_EASTER_ALREADY) {
            easterSwitch.setManaged(true);
            easterSwitch.setVisible(true);
        } else {
            easterSwitch.setManaged(false);
            easterSwitch.setVisible(false);
        }

        easterSwitch.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                Environment.getInstance().setCollectionManager(EasterCollectionManager.getInstance());
            } else {
                Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
            }

            refreshWebView();
        });

        previewButton.setOnAction(event -> {

            if (!(Boolean) SettingsManager.getInstance().getValue("EXPORT_ENABLED")) {
                new AlertDialog.Builder().setTitle("Action Aborted").setIcon(AlertDialog.Builder.Icon.WARNING)
                        .setMessage("Exporting has been disabled in the settings.")
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }

            try {
                Desktop.getDesktop().open(new File(new PDFGenerator().generatePreview(SONG_ONE, SONG_TWO)));
            } catch (final Exception ex) {
                if (ex.getMessage().equals("ignore")) {
                    return;
                }
                logger.error(ex.getMessage(), ex);
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage(ex.getLocalizedMessage()).setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            }
        });

        initAddSongButton();
    }

    private void initAddSongButton() {
        addSongButton.setOnAction(event -> {
            Environment.getInstance().getCollectionManager().addSongDialog();
        });
    }

    /**
     * Turns the page in the songbook.
     *
     * @param toTheRight if true, the page will be turned to the right; it gets turned to the left otehrwise
     */
    private void switchPage(final boolean toTheRight) {
        if (toTheRight) {
            SONG_ONE_INDEX += 2;
            SONG_TWO_INDEX += 2;
        } else {
            SONG_ONE_INDEX -= 2;
            SONG_TWO_INDEX -= 2;
        }
        SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_ONE_INDEX);
        if (SONG_TWO_INDEX >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
            SONG_TWO = CollectionManager.getShadowSong();
        } else {
            SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_TWO_INDEX);
        }

        refreshWebView();
    }

    /**
     * Refreshes the webview, expecting the internal variables have changed.
     */
    private void refreshWebView() {
        if (SONG_ONE.id() == CollectionManager.SHADOW_SONG_ID) {
            SONG_ONE = CollectionManager.getShadowSong();
        } else if (SONG_ONE_INDEX < 0 || SONG_ONE_INDEX >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
            SONG_ONE_INDEX = 0;
            SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_ONE_INDEX);
        }
        if (SONG_TWO.id() == CollectionManager.SHADOW_SONG_ID) {
            SONG_TWO = CollectionManager.getShadowSong();
        } else if (SONG_TWO_INDEX < 0 || (SONG_TWO_INDEX > Environment.getInstance().getCollectionManager().getFormalCollection().size() && SONG_TWO.id() != CollectionManager.SHADOW_SONG_ID)) {
            SONG_TWO_INDEX = 1;
            SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_TWO_INDEX);
        }
        try {
            final URL url = new File(generator.generatePageFile(SONG_ONE, SONG_TWO)).toURI().toURL();

            Platform.runLater(() -> webview.getEngine().load(url.toExternalForm()));
        } catch (final MalformedURLException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("WebView Configuration Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Error refreshing webview.").setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        }
        songOneIdField.setText(SONG_ONE.getDisplayId());
        songTwoIdField.setText(SONG_TWO.getDisplayId());
    }

    @Override
    public void onSongRemoved(final Song s, final CollectionManager m) {
        if (s.id() == SONG_ONE.id() || s.id() == SONG_TWO.id()) {
            refreshWebView();
        }
    }

    @Override
    public void onSongUpdated(final Song s, final CollectionManager m) {
        if (s.id() == SONG_ONE.id() || s.id() == SONG_TWO.id()) {
            refreshWebView();
        }
    }

    @Override
    public void onSongAdded(final Song s, final CollectionManager m) {
        refreshWebView();
    }

    @Override
    public void onRefresh() {
        generator = new HTMLGenerator();
        webview.getEngine().reload();
        refreshWebView();
    }

    @Override
    public void onPageTurnedBack() {
        if ((SONG_ONE.name().equals("frontpage") || SONG_ONE.name().equals("songlist0") || SONG_ONE.equals(Environment.getInstance().getCollectionManager().getFormalCollection().get(0))) || SONG_TWO_INDEX - SONG_ONE_INDEX != 1) {
            return;
        } else {
            switchPage(false);
        }
    }

    @Override
    public void onPageTurnedForward() {
        if (SONG_TWO.name().equals(Environment.getInstance().getCollectionManager().getFormalCollection().get(Environment.getInstance().getCollectionManager().getFormalCollection().size() - 1).name()) || SONG_TWO_INDEX - SONG_ONE_INDEX != 1 || SONG_TWO.id() == CollectionManager.SHADOW_SONG_ID) {
            return;
        } else {
            switchPage(true);
        }
    }

    @Override
    public void onSongOneSet(final Song s) {
        try {
            if (s.id() == CollectionManager.INVALID_SONG_ID) {
                return;
            }
            if (s.id() == CollectionManager.SHADOW_SONG_ID) {
                SONG_ONE_INDEX = Environment.getInstance().getCollectionManager().getFormalCollection().size();
            } else {
                SONG_ONE_INDEX = Environment.getInstance().getCollectionManager().getFormalCollectionSongIndex(s);
            }

            SONG_ONE = s;

            refreshWebView();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onSongTwoSet(final Song s) {
        try {
            if (s.id() == CollectionManager.INVALID_SONG_ID) {
                return;
            }
            if (s.id() == CollectionManager.SHADOW_SONG_ID) {
                SONG_TWO_INDEX = Environment.getInstance().getCollectionManager().getFormalCollection().size();
            } else {
                SONG_TWO_INDEX = Environment.getInstance().getCollectionManager().getFormalCollectionSongIndex(s);
            }

            SONG_TWO = s;

            refreshWebView();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onCollectionManagerChanged(final CollectionManager m) {

    }
}