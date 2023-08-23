package attilathehun.songbook;

import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.EnvironmentVerificator;
import attilathehun.songbook.environment.Installer;
import attilathehun.songbook.ui.CodeEditor;
import attilathehun.songbook.util.KeyEventListener;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import  com.github.kwhat.jnativehook.GlobalScreen;
import javafx.stage.WindowEvent;


public class SongbookApplication extends Application {

    private boolean CONTROL_PRESSED = false;
    private static final List<KeyEventListener> listeners = new ArrayList<KeyEventListener>();
    public static void main(String[] args) {
        Installer.runDiagnostics();
        Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
        EnvironmentVerificator.automated();
        launch(args);
        if (Arrays.asList(Environment.getInstance().perform(args)).contains(Environment.Result.FAILURE)) {
           Environment.showWarningMessage("Warning", "Could not resolve the command line arguments. See log file");
        };
    }

    @Override
    public void start(Stage stage) throws IOException {

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                CodeEditor.setApplicationClosed();
                if (CodeEditor.instanceCount() == 0) {
                    Platform.exit();
                    System.exit(0);
                }

            }
        });

        FXMLLoader fxmlLoader = new FXMLLoader(SongbookApplication.class.getResource("songbook-view.fxml"));
        AnchorPane root = fxmlLoader.load();
        stage.setMaximized(true);
        Scene scene = new Scene(root, 800, 600);

                try {
                    GlobalScreen.registerNativeHook();
                    GlobalScreen.addNativeKeyListener(new NativeKeyListener() {


                        @Override
                        public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                            if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
                                CONTROL_PRESSED = false;
                            }
                        }

                        @Override
                        public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                            if (!stage.isFocused()) {
                                return;
                            }
                            switch (nativeEvent.getKeyCode()) {
                                case NativeKeyEvent.VC_CONTROL -> CONTROL_PRESSED = true;
                                case NativeKeyEvent.VC_LEFT -> dialLeftArrowPressed();
                                case NativeKeyEvent.VC_RIGHT -> dialRightArrowPressed();
                                case NativeKeyEvent.VC_R -> { //refresh
                                    if (CONTROL_PRESSED) {
                                        Environment.getInstance().getCollectionManager().init();
                                        Environment.getInstance().refresh();
                                        new EnvironmentVerificator(true);
                                        dialControlPLusRPressed();
                                    }
                                }
                                case NativeKeyEvent.VC_S -> { //save
                                    if (CONTROL_PRESSED) {
                                        Environment.showMessage("Keyboard shortcut successful", "CTRL+S");
                                        new EnvironmentManager().saveData();
                                    }
                                }
                                case NativeKeyEvent.VC_L -> { //load
                                    if (CONTROL_PRESSED) {
                                        Environment.showMessage("Keyboard shortcut successful", "CTRL+L");
                                        new EnvironmentManager().loadData();
                                    }
                                }
                                case NativeKeyEvent.VC_O -> { //open song link
                                    if (CONTROL_PRESSED) {
                                        Environment.showMessage("Keyboard shortcut successful", "CTRL+O");
                                        //Open dialog to select which of the two songs and then open youtube
                                    }
                                }
                            }
                        }
                    });
                } catch (NativeHookException e) {
                    e.printStackTrace();
                }
           // }
       // });
        stage.setTitle("Songbook Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void addListener(KeyEventListener listener) {
        listeners.add(listener);
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

    public static void dialControlPLusRPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onControlPlusRPressed();
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

}
