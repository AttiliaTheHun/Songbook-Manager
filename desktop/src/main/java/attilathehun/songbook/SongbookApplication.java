package attilathehun.songbook;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.EnvironmentManager;
import attilathehun.songbook.environment.EnvironmentVerificator;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import  com.github.kwhat.jnativehook.GlobalScreen;


public class SongbookApplication extends Application {

    private boolean CONTROL_PRESSED = false;
    private static final List<KeyEventListener> listeners = new ArrayList<KeyEventListener>();
    public static void main(String[] args) {
       EnvironmentVerificator.verify();
       Environment.getInstance().setCollectionManager(StandardCollectionManager.getInstance());
       launch(args);
       if (Arrays.asList(Environment.getInstance().perform(args)).contains(Environment.Result.FAILURE)) {
           Environment.showWarningMessage("Warning", "Could not resolve the command line arguments. See log file");
        };
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SongbookApplication.class.getResource("songbook-view.fxml"));
        AnchorPane root = fxmlLoader.load();
        stage.setMaximized(true);
        Scene scene = new Scene(root, 320, 240);

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
                                        EnvironmentVerificator.verify();
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

    private static void dialControlPLusRPressed() {
        for (KeyEventListener listener : listeners) {
            listener.onControlPlusRPressed();
        }
    }

}
