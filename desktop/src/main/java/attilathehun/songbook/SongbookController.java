package attilathehun.songbook;

import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.ui.CodeEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.ToggleSwitch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

//import org.xhtmlrenderer.*;

//import com.fifesoft.rsyntaxtextarea;

public class SongbookController implements KeyEventListener {

    private static boolean CONTROL_PRESSED = false;
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

    public void initialize() throws MalformedURLException {
        SongbookApplication.addListener(this);

        URL url = new File(generator.generatePageFile(Environment.getInstance().getCollectionManager().getFormalCollection().get(0), Environment.getInstance().getCollectionManager().getFormalCollection().get(1))).toURI().toURL();
        SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(0);
        SONG_ONE_INDEX = 0;
        SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(1);
        SONG_TWO_INDEX = 1;
        webview.getEngine().load(url.toExternalForm());
        songOneIdField.setText(SONG_ONE.getDisplayId());
        songTwoIdField.setText(SONG_TWO.getDisplayId());


        editCollectionButton.setOnAction(event -> {

        });

        loadDataButton.setOnAction(event -> {
            new EnvironmentManager().loadData();
        });

        saveDataButton.setOnAction(event -> {
            new EnvironmentManager().saveData();
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
            int id = -1;
            lol:
            try {
                id = Integer.parseInt(text);
                if (id == -1)
                    break lol;
                int index = 0;
                for (Song song : Environment.getInstance().getCollectionManager().getFormalCollection()) {
                    if (song.id() == id) {
                        SONG_ONE = song;
                        SONG_ONE_INDEX = index;
                        break;
                    }
                    index++;
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
                int index = 0;
                for (Song song : Environment.getInstance().getCollectionManager().getFormalCollection()) {
                    if (song.id() == id) {
                        SONG_TWO = song;
                        SONG_TWO_INDEX = index;
                        break;
                    }
                    index++;
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
            if (SONG_ONE.id() == -1) {
                Environment.showMessage("Message", "This page is generated automatically. You can find the templates under " + Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH);
                return;
            }
            CodeEditor editor = new CodeEditor();
            editor.setTitle(String.format("HTML editor - %s (id: %d)", SONG_ONE.name(), SONG_ONE.id()));
            editor.setSong(SONG_ONE);
            editor.setVisible(true);
        });

        editSongTwoHTML.setOnAction(event -> {
            if (SONG_TWO.id() == -1) {
                Environment.showMessage("Message", "This page is generated automatically. You can find the templates under " + Environment.getInstance().settings.TEMPLATE_RESOURCES_FILE_PATH);
                return;
            }
            CodeEditor editor = new CodeEditor();
            editor.setTitle(String.format("HTML editor - %s (id: %d)", SONG_TWO.name(), SONG_TWO.id()));
            editor.setSong(SONG_TWO);
            editor.setVisible(true);
        });

        singlepageSelection.setOnAction((EventHandler<javafx.event.ActionEvent>) event -> {
            if (easterSwitch.isSelected()) {
                new PDFGenerator(true).generateSinglePage();
            } else {
                new PDFGenerator().generateSinglePage();
            }

        });

        defaultSelection.setOnAction((EventHandler<javafx.event.ActionEvent>) event -> {
            if (easterSwitch.isSelected()) {
                new PDFGenerator(true).generateDefault();
            } else {
                new PDFGenerator().generateDefault();
            }

        });

        printableSelection.setOnAction((EventHandler<javafx.event.ActionEvent>) event -> {
            if (easterSwitch.isSelected()) {
                new PDFGenerator(true).generatePrintable();
            } else {
                new PDFGenerator().generatePrintable();
            }

        });

        if (!Environment.getInstance().settings.IS_IT_EASTER_ALREADY) {
            easterSwitch.managedProperty().bind(easterSwitch.visibleProperty());
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

    }


    @Override
    public void onLeftArrowPressed() {

        if (SONG_ONE.name().equals(HTMLGenerator.FRONTPAGE) || SONG_TWO_INDEX - SONG_ONE_INDEX != 1) {
            return;
        } else {
            switchPage(false);
        }
    }

    @Override
    public void onRightArrowPressed() {

        if (SONG_TWO.name().equals(Environment.getInstance().getCollectionManager().getFormalCollection().get(Environment.getInstance().getCollectionManager().getFormalCollection().size() - 1).name())  || SONG_TWO_INDEX - SONG_ONE_INDEX != 1) {
            return;
        } else {
            switchPage(true);
        }
    }

    @Override
    public void onControlPlusRPressed() {
        refreshWebView();
    }

    private void switchPage(boolean toTheRight){
            if (toTheRight) {
                SONG_ONE_INDEX += 2;
                SONG_TWO_INDEX += 2;
            } else {
                SONG_ONE_INDEX -= 2;
                SONG_TWO_INDEX -= 2;
            }
            SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_ONE_INDEX);
            SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_TWO_INDEX);
            refreshWebView();
            songOneIdField.setText(SONG_ONE.getDisplayId());
            songTwoIdField.setText(SONG_TWO.getDisplayId());

    }

    private void refreshWebView() {
        if (SONG_ONE_INDEX < 0 || SONG_ONE_INDEX >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
            SONG_ONE_INDEX = 0;
            SONG_ONE = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_ONE_INDEX);
        }
        if (SONG_TWO_INDEX < 0  || SONG_TWO_INDEX >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
            SONG_TWO_INDEX = 1;
            SONG_TWO = Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_TWO_INDEX);
        }
        try {
            URL url = new File(generator.generatePageFile(Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_ONE_INDEX), Environment.getInstance().getCollectionManager().getFormalCollection().get(SONG_TWO_INDEX))).toURI().toURL();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webview.getEngine().load(url.toExternalForm());
                }
            });
        } catch (MalformedURLException e) {
            Environment.showErrorMessage("WebView configuration error", "Error when refreshing webview!");
        }
    }
}