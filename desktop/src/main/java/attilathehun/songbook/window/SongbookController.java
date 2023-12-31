package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import attilathehun.songbook.Main;
import attilathehun.songbook.collection.*;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.EnvironmentStateListener;
import attilathehun.songbook.plugin.Export;
import attilathehun.songbook.util.HTMLGenerator;
import attilathehun.songbook.util.KeyEventListener;
import attilathehun.songbook.util.PDFGenerator;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebView;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

//TODO: when songs get disabled, there is a problem with the latest page/song being out of bonds, fixing it?
@TODO()
public class SongbookController implements CollectionListener, EnvironmentStateListener {

    private static final Logger logger = LogManager.getLogger(SongbookController.class);

    private static Song SONG_ONE;
    private static int SONG_ONE_INDEX;
    private static Song SONG_TWO;
    private static int SONG_TWO_INDEX;
    @FXML
    private WebView webview;
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

    private final HTMLGenerator generator = new HTMLGenerator();

    @FXML
    private void initialize() throws MalformedURLException {
        Environment.addListener(this);

        initWebView();

        initCollectionEditor();

        initUIComponents();
    }

    /**
     * Initializes the web view to the default position. Default position is start of the songbook, more practically speaking the beginning of the default collection manager's collection
     * substituted, if necessary, by shadow songs.
     * @throws MalformedURLException
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

        URL url = new File(generator.generatePageFile(SONG_ONE, SONG_TWO)).toURI().toURL();
        webview.getEngine().load(url.toExternalForm());
    }

    /**
     * Initializes and creates the CollectionEditor window, which is an instance of {@link CollectionEditor}. The window is hidden in background and can be summoned using
     * {@link CollectionEditor#open()}.
     */
    private void initCollectionEditor() {
        CollectionEditor editorController = CollectionEditor.getInstance();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("collection-editor.fxml"));
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

    private void initUIComponents() {

        songOneIdField.setText(SONG_ONE.getDisplayId());
        songTwoIdField.setText(SONG_TWO.getDisplayId());



        editCollectionButton.setOnAction(event -> {
            CollectionEditor.open();
        });

        loadDataButton.setOnAction(event -> {
            new EnvironmentManager().load();
        });

        saveDataButton.setOnAction(event -> {
            new EnvironmentManager().save();
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
            String text = songOneIdField.getText().trim().toLowerCase();
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

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (id == -1) {
                int index = 0;
                for (Song song : Environment.getInstance().getCollectionManager().getFormalCollection()) {
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
            String text = songTwoIdField.getText().trim().toLowerCase();
            if (text.length() == 0) {
                return;
            }
            int id = -1;
            lol:
            try {
                id = Integer.parseInt(text);
                if (id == -1)
                    break lol;
                int index = Environment.getInstance().getCollectionManager().getFormalCollectionSongIndex(id);
                if (index != -1) {
                    SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(index);
                    SONG_TWO_INDEX = index;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (id == -1) {
                int index = 0;
                for (Song song : Environment.getInstance().getCollectionManager().getFormalCollection()) {
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
            //WARNING only the active manager is refreshed
            Environment.getInstance().getCollectionManager().init();
            Environment.getInstance().refresh();
            refreshWebView();
        });

        editSongOneHTML.setOnAction(event -> {
            if (SONG_ONE.id() < 0) {
                Environment.showMessage("Message", "This page is generated automatically. You can find the templates under " + Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH);
                return;
            }
            CodeEditor.open(SONG_ONE, Environment.getInstance().getCollectionManager());
        });

        editSongTwoHTML.setOnAction(event -> {
            if (SONG_TWO.id() < 0) {
                Environment.showMessage("Message", "This page is generated automatically. You can find the templates under " + Environment.getInstance().settings.environment.TEMPLATE_RESOURCES_FILE_PATH);
                return;
            }
            CodeEditor.open(SONG_TWO, Environment.getInstance().getCollectionManager());
        });

        exportButton.setOnAction(event -> {
            System.out.println("exportButton.onAction");
            if (!Environment.getInstance().settings.plugins.getEnabled(Export.getInstance().getName())) {
                Environment.showMessage("Exporting disabled", "It seems like exporting has been disabled in the settings. You can modify the settings and restart or read more in the documentation.");
                return;
            }

        });

        exportButton.addEventHandler(new ActionEvent().getEventType(), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("exportButton.x.handle");
            }
        });


        if (!Environment.getInstance().settings.plugins.getEnabled(Export.getInstance().getName())) {
            singlepageSelection.setVisible(false);
            defaultSelection.setVisible(false);
            printableSelection.setVisible(false);
        }

        singlepageSelection.setOnAction(event -> {
            try {
                new PDFGenerator().generateSinglePage();
            } catch (Exception e) {
                Environment.showErrorMessage("Error", e.getMessage());
            }

        });

        defaultSelection.setOnAction(event -> {
            try {
                new PDFGenerator().generateDefault();
            } catch (Exception e) {
                Environment.showErrorMessage("Error", e.getMessage());
            }
        });

        printableSelection.setOnAction(event -> {
            try {
                new PDFGenerator().generatePrintable();
            } catch (Exception e) {
                Environment.showErrorMessage("Error", e.getMessage());
            }
        });

        if (Environment.getInstance().settings.environment.IS_IT_EASTER_ALREADY) {
            easterSwitch.setManaged(true);
            easterSwitch.setVisible(true);
        } else {
            easterSwitch.setManaged(false);
            easterSwitch.setVisible(false);
        }

        easterSwitch.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (easterSwitch.isSelected()) {
                    Environment.getInstance().setCollectionManager(EasterCollectionManager.getInstance());
                } else {
                    Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
                }
                Environment.getInstance().refresh();
                refreshWebView();
            }
        });

        previewButton.setOnAction(event -> {

            if (!Environment.getInstance().settings.plugins.getEnabled(Export.getInstance().getName())) {
                Environment.showMessage("Action aborted", "This feature is a part of the Export plugin. Enable it in settings first!");
                return;
            }

            try {
                Desktop.getDesktop().open(new File(new PDFGenerator().generatePreview(SONG_ONE, SONG_TWO).replace(".html", ".pdf")));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                Environment.showErrorMessage("Error", ex.getMessage());
            }
        });

        initAddSongButton();

    }

    private void initAddSongButton() {
        addSongButton.setOnAction(event -> {
            EnvironmentManager.addSongDialog(Environment.getInstance().getCollectionManager());
        });
    }

    private void switchPage(boolean toTheRight) {
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
            URL url = new File(new HTMLGenerator().generatePageFile(SONG_ONE, SONG_TWO)).toURI().toURL();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webview.getEngine().load(url.toExternalForm());
                }
            });
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            Environment.showErrorMessage("WebView configuration error", "Error when refreshing webview!");
        }
        songOneIdField.setText(SONG_ONE.getDisplayId());
        songTwoIdField.setText(SONG_TWO.getDisplayId());
    }

    public static Song getSongOne() {
        return SONG_ONE;
    }

    public static Song getSongTwo() {
        return SONG_TWO;
    }

    @Override
    public void onSongRemoved(Song s, CollectionManager m) {
        if (s.id() == SONG_ONE.id() || s.id() == SONG_TWO.id()) {
            refreshWebView();
        }
    }

    @Override
    public void onSongUpdated(Song s, CollectionManager m) {
        if (s.id() == SONG_ONE.id() || s.id() == SONG_TWO.id()) {
            refreshWebView();
        }
    }

    @Override
    public void onSongAdded(Song s, CollectionManager m) {
        refreshWebView();
    }

    @Override
    public void onRefresh() {
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
    public void onSongOneSet(Song s) {
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
    public void onSongTwoSet(Song s) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}