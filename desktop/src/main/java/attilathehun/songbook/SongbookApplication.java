package attilathehun.songbook;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.EnvironmentVerificator;
import attilathehun.songbook.environment.Installer;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.window.CodeEditor;
import attilathehun.songbook.util.KeyEventListener;
import attilathehun.songbook.window.CollectionEditor;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.kwhat.jnativehook.GlobalScreen;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;


public class SongbookApplication extends Application {

    private static final Logger logger = LogManager.getLogger(SongbookApplication.class);

    private static boolean CONTROL_PRESSED = false;
    private static boolean SHIFT_PRESSED = false;

    private static final List<KeyEventListener> listeners = new ArrayList<KeyEventListener>();

    public static void main(String[] args) {

        System.setProperty("log4j.configurationFile", "C:\\Users\\Jaroslav\\Programs\\Git\\Songbook-Manager\\desktop\\src\\main\\resources\\log4j2.yaml");
        PluginManager.loadPlugins();

        Installer.runDiagnostics();
        new EnvironmentManager().autoLoad();
        Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
        EnvironmentVerificator.automated();
        launch(args);
        if (Arrays.asList(Environment.getInstance().perform(args)).contains(Environment.Result.FAILURE)) {
            Environment.showWarningMessage("Warning", "Could not resolve the command line arguments. See log file");
        }
        logger.debug("Application started successfully");
    }

    @Override
    public void start(Stage stage) throws IOException {

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                CodeEditor.setApplicationClosed();
                if (CodeEditor.instanceCount() == 0) {
                    Platform.exit();
                    Environment.getInstance().exit();
                }

            }
        });

        FXMLLoader fxmlLoader = new FXMLLoader(SongbookApplication.class.getResource("songbook-view.fxml"));
        AnchorPane root = fxmlLoader.load();
        stage.setMaximized(true);
        Scene scene = new Scene(root, 1000, 800);

        registerNativeHook(stage);

        stage.setTitle("Songbook Manager");
        stage.setScene(scene);
        stage.show();
    }

    private static void registerNativeHook(Stage stage) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {


                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                    if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
                        CONTROL_PRESSED = false;
                        //dialControlReleased();
                    } else if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                        SHIFT_PRESSED = false;
                        //dialShiftReleased();
                    }
                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                    if (!stage.isFocused() && !CollectionEditor.focused()) {
                        return;
                    }
                    //TODO fill this up (perhaps in separate function)
                    Integer[] notWritableKeys = new Integer[]{NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_LEFT,};
                    if (!Arrays.stream(notWritableKeys).anyMatch(x -> x == nativeEvent.getKeyCode())) {
                        dialKeyPressed();
                    }
                    switch (nativeEvent.getKeyCode()) {
                        case NativeKeyEvent.VC_CONTROL -> {
                            CONTROL_PRESSED = true;
                            //dialControlPress();
                        }
                        case NativeKeyEvent.VC_SHIFT -> {
                            SHIFT_PRESSED = true;
                            //dialShiftPressed();
                        }
                        case NativeKeyEvent.VC_LEFT, NativeKeyEvent.VC_PAGE_DOWN -> {
                            if (!listeners.get(0).onImaginaryIsTextFieldFocusedKeyPressed()) {
                                dialLeftArrowPressed();
                            }
                        }
                        case NativeKeyEvent.VC_RIGHT, NativeKeyEvent.VC_PAGE_UP -> {
                            if (!listeners.get(0).onImaginaryIsTextFieldFocusedKeyPressed()) {
                                dialRightArrowPressed();
                            }
                        }
                        case NativeKeyEvent.VC_R -> { //refresh
                            if (CONTROL_PRESSED) {
                                Environment.getInstance().getCollectionManager().init();
                                Environment.getInstance().refresh();
                                EnvironmentVerificator.automated();
                                dialControlPLusRPressed();
                            }
                        }
                        case NativeKeyEvent.VC_S -> { //save
                            if (CONTROL_PRESSED) {
                                new EnvironmentManager().save();
                            }
                        }
                        case NativeKeyEvent.VC_L -> { //load
                            if (CONTROL_PRESSED) {
                                new EnvironmentManager().load();
                            }
                        }
                        case NativeKeyEvent.VC_H -> { //load
                            if (CONTROL_PRESSED) {
                                try {
                                    Desktop.getDesktop().browse(new URL("https://github.com/AttiliaTheHun/Songbook-Manager/wiki/English").toURI());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        case NativeKeyEvent.VC_O -> { //open song link
                            if (CONTROL_PRESSED) {
                                if (SongbookController.getSongOne().getUrl().equals("") && SongbookController.getSongTwo().getUrl().equals("")) {
                                    Environment.showMessage("Message", "None of the displayed songs has an associated URL. You can managed URLs in the Collection Editor Window.");
                                } else if (!SongbookController.getSongOne().getUrl().equals("") && SongbookController.getSongTwo().getUrl().equals("")) {

                                    int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to open the associated URL in a web browser?", String.format("%s - %s", SongbookController.getSongOne().name(), SongbookController.getSongOne().getUrl()), JOptionPane.YES_NO_OPTION);

                                    if (resultCode == JOptionPane.YES_OPTION) {
                                        try {
                                            Desktop.getDesktop().browse(new URL(SongbookController.getSongOne().getUrl()).toURI());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Environment.showWarningMessage("Warning", "The associated URL is malformed. Please check for typos :)");
                                        }

                                    }
                                } else if (SongbookController.getSongOne().getUrl().equals("") && !SongbookController.getSongTwo().getUrl().equals("")) {

                                    int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Do you want to open the associated URL in a web browser?", SongbookController.getSongTwo().getUrl(), JOptionPane.YES_NO_OPTION);

                                    if (resultCode == JOptionPane.YES_OPTION) {
                                        try {
                                            Desktop.getDesktop().browse(new URL(SongbookController.getSongTwo().getUrl()).toURI());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Environment.showWarningMessage("Warning", "The associated URL is malformed. Please check for typos :)");
                                        }


                                    }
                                } else /* both have a URL */ {
                                    UIManager.put("OptionPane.yesButtonText", SongbookController.getSongOne().name());
                                    UIManager.put("OptionPane.noButtonText", SongbookController.getSongTwo().name());
                                    int resultCode = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), "Which song's associated URL do you want to open in the browser?", String.format("%s or %s?", SongbookController.getSongOne().name(), SongbookController.getSongTwo().name()), JOptionPane.YES_NO_OPTION);
                                    if (resultCode == JOptionPane.YES_OPTION) {
                                        try {
                                            Desktop.getDesktop().browse(new URL(SongbookController.getSongOne().getUrl()).toURI());
                                        } catch (Exception e) {
                                        }

                                        Environment.showWarningMessage("Warning", "The associated URL is malformed. PLease check for typos :)");
                                    } else if (resultCode == JOptionPane.NO_OPTION) {
                                        try {
                                            Desktop.getDesktop().browse(new URL(SongbookController.getSongTwo().getUrl()).toURI());
                                        } catch (Exception e) {
                                            Environment.showWarningMessage("Warning", "The associated URL is malformed. Please check for typos :)");
                                        }

                                    }
                                    UIManager.put("OptionPane.yesButtonText", "Yes");
                                    UIManager.put("OptionPane.noButtonText", "No");
                                }

                            }
                        }
                        case NativeKeyEvent.VC_DELETE -> {
                            dialDeletePressed();
                        }
                    }
                }
            });
        } catch (NativeHookException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Native Hook registered");
    }

    public static void addListener(KeyEventListener listener) {
        listeners.add(listener);
        logger.debug("registered listener: " + listener.getClass().getName());
    }

    private static void dialLeftArrowPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onLeftArrowPressed();
        }
    }

    private static void dialRightArrowPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onRightArrowPressed();
        }
    }

    private static void dialDeletePressed() {
        for (KeyEventListener listener : listeners) {
            listener.onDeletePressed();
        }
    }

    public static void dialControlPLusRPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onControlPlusRPressed();
        }
    }

    public static void dialControlPLusSPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onControlPlusSPressed();
        }
    }

    public static void dialImaginarySongOneKeyPressed(Song s) {
        for (KeyEventListener listener : listeners) {
            listener.onImaginarySongOneKeyPressed(s);
        }
    }

    public static void dialImaginarySongTwoKeyPressed(Song s) {
        for (KeyEventListener listener : listeners) {
            listener.onImaginarySongTwoKeyPressed(s);
        }
    }

    public static void dialKeyPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onKeyPressed();
        }
    }

    public static boolean isControlPressed() {
        return CONTROL_PRESSED;
    }

    public static boolean isShiftPressed() {
        return SHIFT_PRESSED;
    }

}
