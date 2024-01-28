package attilathehun.songbook.window;

import attilathehun.songbook.Main;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.*;
import attilathehun.songbook.plugin.PluginManager;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.kwhat.jnativehook.GlobalScreen;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;


public class SongbookApplication extends Application {

    private static final Logger logger = LogManager.getLogger(SongbookApplication.class);

    private static boolean CONTROL_PRESSED = false;
    private static boolean SHIFT_PRESSED = false;


    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", Main.class.getResource("log4j2.yaml").toString());
        PluginManager.loadPlugins();
        Installer.runDiagnostics();
        new EnvironmentManager().autoLoad();
        Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
        EnvironmentVerificator.automated();
        launch(args);
        logger.debug("Application started successfully");
    }

    @Override
    public void start(Stage stage) throws IOException {


        /*stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                CodeEditorV1.setApplicationClosed();
                if (CodeEditorV1.instanceCount() == 0) {
                    Platform.exit();
                    Environment.getInstance().exit();
                }

            }
        });
*/

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("songbook-view.fxml"));
        AnchorPane root = fxmlLoader.load();
        stage.setMaximized(true);
        Scene scene = new Scene(root, 1000, 800);

        registerNativeHook(stage);

        stage.setTitle("Songbook Manager");
        stage.setScene(scene);
        initKeyboardShortcuts(stage);
        stage.show();

        AlertDialog alert = new AlertDialog.Builder().setIcon(AlertDialog.Builder.Icon.INFO).setTitle("Warning").setMessage("The changes you have made in this session will be discarded. Do you want to proceed?hjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj").addOkButton("Discard").addCloseButton("Cancel").build();
        CompletableFuture<Integer> result = alert.awaitResult();
        result.thenAccept(dialogResult -> {
            System.out.println(dialogResult);
        });


    }

    private static void registerNativeHook(Stage stage) {
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

    private void initKeyboardShortcuts(Stage stage) {
        stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case LEFT, PAGE_DOWN -> {
                        Environment.notifyOnPageTurnedBack();

                    }
                    case RIGHT, PAGE_UP -> {
                        Environment.notifyOnPageTurnedForward();
                    }
                    case R -> { //refresh
                        if (keyEvent.isControlDown()) {
                            Environment.getInstance().getCollectionManager().init();
                            Environment.getInstance().refresh();
                            EnvironmentVerificator.automated();
                        }
                    }
                    case S -> { //save
                        if (keyEvent.isControlDown()) {
                            new EnvironmentManager().save();
                        }
                    }
                    case L -> { //load
                        if (keyEvent.isControlDown()) {
                            new EnvironmentManager().load();
                        }
                    }
                    case H -> { //help
                        if (keyEvent.isControlDown()) {
                            try {
                                Desktop.getDesktop().browse(new URL("https://github.com/AttiliaTheHun/Songbook-Manager/wiki/English").toURI());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    case O -> { //open song link
                        if (keyEvent.isControlDown()) {
                           openAssociatedLink();
                        }
                    }
                }
            }
        });
    }

    private void openAssociatedLink() {
        final boolean songOneHasURL = !SongbookController.getSongOne().getUrl().equals("");
        final boolean songTwoHasURL = !SongbookController.getSongTwo().getUrl().equals("");
        if (! (songOneHasURL && songTwoHasURL)) {
            Environment.showMessage("Message", "None of the displayed songs has an associated URL. You can managed URLs in the Collection Editor Window.");
        }
        int resultCode = -1;
        boolean pass = false;
        Song song = null;
        if (songOneHasURL && !songTwoHasURL) {
            resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to open the associated URL in a web browser?", SongbookController.getSongOne().getUrl(), JOptionPane.YES_NO_OPTION);
            song = SongbookController.getSongOne();
        } else if(!songOneHasURL && songTwoHasURL) {
            resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to open the associated URL in a web browser?", SongbookController.getSongTwo().getUrl(), JOptionPane.YES_NO_OPTION);
            song = SongbookController.getSongTwo();
        } else if(songOneHasURL && songTwoHasURL) {
            UIManager.put("OptionPane.yesButtonText", SongbookController.getSongOne().name());
            UIManager.put("OptionPane.noButtonText", SongbookController.getSongTwo().name());
            resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Which song's associated URL do you want to open in the browser?", String.format("%s or %s?", SongbookController.getSongOne().name(), SongbookController.getSongTwo().name()), JOptionPane.YES_NO_OPTION);
            UIManager.put("OptionPane.yesButtonText", "Yes");
            UIManager.put("OptionPane.noButtonText", "No");
            if (resultCode == JOptionPane.NO_OPTION) {
                song = SongbookController.getSongOne();
                pass = true;
            } else {
                song = SongbookController.getSongTwo();
            }
        }
        if (resultCode == JOptionPane.YES_OPTION || pass) {
            try {
                Desktop.getDesktop().browse(new URL(song.getUrl()).toURI());
            } catch (Exception e) {
                e.printStackTrace();
                Environment.showWarningMessage("Warning", "The associated URL is malformed. Please check for typos :)");
            }
        }
    }

    public static boolean isControlPressed() {
        return CONTROL_PRESSED;
    }

    public static boolean isShiftPressed() {
        return SHIFT_PRESSED;
    }

}
