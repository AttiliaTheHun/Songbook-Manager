package attilathehun.songbook.window;

import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Implementation of a {@link Preloader} for the application. The preloader shows a progress indicator while the application
 * starts up, to tell the user that something is happening.
 */
public class ApplicationPreloader extends Preloader {
    ProgressBar progress;
    Label message;
    Stage stage;

    @Override
    public void start(final Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Application startup");
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        stage.setWidth(300);
        stage.setHeight(80);
        stage.setScene(createPreloaderScene());
        stage.show();
    }

    private Scene createPreloaderScene() {
        progress = new ProgressBar(0);
        progress.setMinWidth(260);
        final VBox container = new VBox();
        container.setMinWidth(300);
        container.setPadding(new Insets(8, 8, 8, 8));
        container.getChildren().add(progress);
        message = new Label("Initializing application components...");
        message.setPadding(new Insets(0, 8, 0, 0));
        container.getChildren().add(message);
        final BorderPane wrapper = new BorderPane(container);
        return new Scene(wrapper, 300, -1);
    }

    @Override
    public void handleStateChangeNotification(final StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.close();
        }
    }

    @Override
    public void handleApplicationNotification(final PreloaderNotification preloaderNotification) {
        if (preloaderNotification.getClass().equals(ProgressNotification.class)) {
            progress.setProgress(((ProgressNotification) preloaderNotification).getProgress());
        } else if (preloaderNotification.getClass().equals(ProgressMessageNotification.class)) {
            message.setText(((ProgressMessageNotification) preloaderNotification).message());
        }
    }

    @Override
    public boolean handleErrorNotification(final ErrorNotification en) {
        final Label l = new Label(en.getDetails());
        stage.getScene().setRoot(l);
        // Return true to prevent default error handler to take care of this error
        return true;
    }

    public record ProgressMessageNotification(String message) implements PreloaderNotification {}
}
