package attilathehun.songbook.window;

import attilathehun.songbook.Main;
import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.*;
import attilathehun.songbook.util.BrowserFactory;
import attilathehun.songbook.util.TokenProvider;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.awt.Desktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;


public class SongbookApplication extends Application {
    private static final Logger logger = LogManager.getLogger(SongbookApplication.class);

    static {
        System.setProperty("log4j2.configurationFile", Main.class.getResource("log4j2.yaml").toString());
    }

    private static boolean CONTROL_PRESSED = false;
    private static boolean SHIFT_PRESSED = false;

    private static Stage mainWindow;


    public static void main(final String[] args) {
        Application.launch(SongbookApplication.class, args);
    }

    private static void registerNativeHook() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                    if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
                        CONTROL_PRESSED = false;
                    } else if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        SHIFT_PRESSED = false;
                    }
                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                    if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
                        CONTROL_PRESSED = true;
                    } else if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        SHIFT_PRESSED = true;
                    }
                }

            });
        } catch (NativeHookException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Native Hook registered");
    }

    public static boolean isControlPressed() {
        return CONTROL_PRESSED;
    }

    public static boolean isShiftPressed() {
        return SHIFT_PRESSED;
    }

    public static Stage getMainWindow() {
        return mainWindow;
    }

    /**
     * JavaFX main method. UI thread entry point. Initializes the environment and loads the main window.
     *
     * @param stage the main stage
     * @throws IOException
     */
    @Override
    public void start(final Stage stage) throws IOException {
        SettingsManager.init();
        Installer.runDiagnostics();
        Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
        StandardCollectionManager.getInstance().init();
        EasterCollectionManager.getInstance().init();
        BrowserFactory.init();
        EnvironmentVerificator.automated();

        stage.setOnCloseRequest(t -> {
            if (!CodeEditor.hasInstanceOpen()) {
                mainWindow = null;
                Environment.getInstance().exit();
            }
        });


        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("songbook-view.fxml"));
        final HBox root = fxmlLoader.load();
        stage.setMaximized(true);
        final Scene scene = new Scene(root, 1000, 800);

        registerNativeHook();

        stage.setTitle("SongbookManager");
        stage.setScene(scene);
        initKeyboardShortcuts(stage);
        stage.show();
        // beware of memory leaks
        mainWindow = stage;
        logger.debug("Application started successfully");
    }

    /**
     * Initializes the keyboard shortcuts on the main stage.
     *
     * @param stage the main stage
     */
    private void initKeyboardShortcuts(final Stage stage) {
        stage.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case LEFT, PAGE_DOWN -> {
                    Environment.notifyOnPageTurnedBack();
                }
                case RIGHT, PAGE_UP -> {
                    Environment.notifyOnPageTurnedForward();
                }
                case R -> { // refresh
                    if (keyEvent.isControlDown()) {
                        Environment.getInstance().hardRefresh();
                    }
                }
                case S -> { // save
                    if (keyEvent.isControlDown()) {
                        EnvironmentManager.getInstance().save();
                    }
                }
                case L -> { // load
                    if (keyEvent.isControlDown()) {
                        EnvironmentManager.getInstance().load();
                    }
                }
                case H -> { // help
                    if (keyEvent.isControlDown()) {
                        try {
                            Desktop.getDesktop().browse(new URL("https://github.com/AttiliaTheHun/Songbook-Manager/wiki/English").toURI());
                        } catch (final Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                case O -> { // open song link
                    if (keyEvent.isControlDown()) {
                        openAssociatedLink();
                    }
                }
                case N -> {
                    if (keyEvent.isControlDown()) {
                        Environment.getInstance().getCollectionManager().addSongDialog();
                    }
                }
                case A -> { // open server admin panel
                    if (keyEvent.isControlDown() && keyEvent.isAltDown()) {
                        AdminPanel.open();
                    }
                }
            }
        });
    }

    /**
     * Lets the user open links associated with the song on the currently open page of the songbook.
     */
    private void openAssociatedLink() {
        final boolean songOneHasURL = !SongbookController.getSongOne().getUrl().equals("");
        final boolean songTwoHasURL = !SongbookController.getSongTwo().getUrl().equals("");
        // both song have a link
        if (songOneHasURL && songTwoHasURL) {
            final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Open associated link?").setMessage("Both of the songs have an associated link, which one do you want to open? You can manage the links in the Collection Editor.")
                    .addOkButton(SongbookController.getSongOne().name()).addCloseButton(SongbookController.getSongTwo().name())
                    .build().awaitResult();
            result.thenAccept(dialogResult -> {
                try {
                    if (dialogResult == AlertDialog.RESULT_OK) {
                        Desktop.getDesktop().browse(new URL(SongbookController.getSongOne().getUrl()).toURI());
                    } else if (dialogResult == AlertDialog.RESULT_CLOSE) {
                        Desktop.getDesktop().browse(new URL(SongbookController.getSongTwo().getUrl()).toURI());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    new AlertDialog.Builder().setTitle("Error").setMessage("Could not open the associated link, maybe there is a typo.")
                            .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
                }
            });
            return;
        }
        // only song one has a link
        if (songOneHasURL) {
            final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Open associated link?")
                    .setMessage(String.format("Do you want to open the link associated to song '%s' (id: %d)?. You can manage the links in the Collection Editor.", SongbookController.getSongOne().name(), SongbookController.getSongOne().id()))
                    .addOkButton("Open link").addCloseButton("Cancel")
                    .build().awaitResult();
            result.thenAccept(dialogResult -> {
                if (dialogResult == AlertDialog.RESULT_OK) {
                    try {
                        Desktop.getDesktop().browse(new URL(SongbookController.getSongOne().getUrl()).toURI());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        new AlertDialog.Builder().setTitle("Error").setMessage("Could not open the associated link, maybe there is a typo.")
                                .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
                    }
                }
            });
            return;
        }

        // only song two has a link
        if (songTwoHasURL) {
            final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Open associated link?")
                    .setMessage(String.format("Do you want to open the link associated to song '%s' (id: %d)?. You can manage the links in the Collection Editor.", SongbookController.getSongTwo().name(), SongbookController.getSongTwo().id()))
                    .addOkButton("Open link").addCloseButton("Cancel")
                    .build().awaitResult();
            result.thenAccept(dialogResult -> {
                if (dialogResult == AlertDialog.RESULT_OK) {
                    try {
                        Desktop.getDesktop().browse(new URL(SongbookController.getSongTwo().getUrl()).toURI());
                    } catch (final Exception e) {
                        logger.error(e.getMessage(), e);
                        new AlertDialog.Builder().setTitle("Error").setMessage("Could not open the associated link, maybe there is a typo.")
                                .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
                    }
                }
            });
            return;
        }
        // none of the songs has a link
        new AlertDialog.Builder().setTitle("SongbookManager").setMessage("Neither of the songs has an associated link. You can manage the links in the Collection Editor.")
                .setIcon(AlertDialog.Builder.Icon.INFO).addOkButton("Close").addCloseButton("Open Collection Editor", (result) -> {
                    CollectionEditor.open();
                    return true;
                }).build().open();
    }

}
