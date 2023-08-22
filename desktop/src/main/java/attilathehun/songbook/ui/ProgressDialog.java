package attilathehun.songbook.ui;
/** (MODIFIED)
 * @source https://github.com/pteraforce/fx-libs/blob/master/src/com/pteraforce/fxdiags/ProgressDialog.java
 */
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Displays a dialog window with a progress bar. The most common usage of this
 * dialog window is as a loading dialog.
 *
 * @author Tyler Smith
 * @author AttiliaTheHun
 * @version 1.0.0
 */
public class ProgressDialog extends Dialog<Boolean>{
    private final HBox hbox;
    private final Label label;
    private final ProgressBar progressBar;

    private volatile boolean isFinished = false;

    /**
     * Creates a new progress dialog at 0% progress.
     */
    public ProgressDialog() {
        this(0.0d);
    }

    /**
     * Creates a new progress dialog with the given progress.
     *
     * @param progress The progress to apply to the progress bar. Must be
     * between 0 and 1.
     */

    public ProgressDialog(double progress) {
        final DialogPane dialogPane = getDialogPane();

        // layout manager
        hbox = new HBox();
        hbox.setFillHeight(true);
        hbox.setMaxWidth(Double.MAX_VALUE);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPrefWidth(400d);

        // progress bar
        progressBar = new ProgressBar(progress);
        progressBar.prefWidthProperty().bind(hbox.widthProperty().subtract(10));
       // progressBar.progressProperty().bind(task.progressProperty());

        // label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle("Working...");
        dialogPane.setHeaderText("Doing cool stuff in the background ;)");
       // dialogPane.headerTextProperty().bind(task.messageProperty());
        updateGrid();

        setResult(isFinished);
    }

    private void updateGrid() {
        hbox.getChildren().clear();

        hbox.getChildren().add(progressBar);
        hbox.getChildren().add(label);
        getDialogPane().setContent(hbox);

        Platform.runLater(progressBar::requestFocus);
    }

    /**
     * Performs the function of <code>DialogPane.createContentLabel</code>. The
     * built-in JavaFX dialogs call that method, but this class cannot access
     * it. This method is used by this class instead.
     *
     * @param text The text to be applied to the label.
     * @return A new label for the dialog.
     */
    private static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    /**
     * Set the current state of the progress bar. This method accepts a double
     * from 0-1 and fills in the progress bar accordingly.
     *
     * @param progress The progress to apply to the progress bar. Must be
     * between 0 and 1.
     */
    public void setProgress(double progress) {
        progressBar.setProgress(progress);
        if (progress == 1d) {
            isFinished = true;
            hide();
        }
    }

    public double getProgress() {
        return progressBar.getProgress();
    }

    public void setText(String text) {
        getDialogPane().setHeaderText(text);
    }

    public void bind(Task task) {
        progressBar.progressProperty().bind(task.progressProperty());
        label.textProperty().bind(task.messageProperty());
    }

}