package attilathehun.songbook.window;

import attilathehun.annotation.TODO;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AlertDialog extends Stage {
    public static final int RESULT_OK = 0;
    public static final int RESULT_CLOSE = 1;
    public static final int RESULT_EXTRA = 2;
    public static final int RESULT_CANCEL = -1;

    private Label messageField;
    private Button okButton;
    private Button closeButton;
    private Button extraButton;
    private boolean FLAG_AUTO_CANCEL = true;
    private Builder.Action okButtonAction = null;
    private Builder.Action closeButtonAction = null;
    private Builder.Action extraButtonAction = null;

    /**
     * Creates an unresizable decorated stage with the given title.
     *
     * @param title the title
     */
    private AlertDialog(String title) {
        super(StageStyle.DECORATED);
        setTitle(title);
        setResizable(false);
    }

    /**
     * Set {@link AlertDialog#FLAG_AUTO_CANCEL} value. When true, the cancel (X) button will close the dialog, before returning the result.
     *
     * @param b new value
     */
    public void setAutoCancel(boolean b) {
        FLAG_AUTO_CANCEL = b;
    }

    /**
     * The means of getting the dialog result through {@link CompletableFuture#thenAccept(Consumer)}. Upon measuring returns either {@link AlertDialog#RESULT_OK},
     * {@link AlertDialog#RESULT_CLOSE} or {@link AlertDialog#RESULT_EXTRA} respectively, dependent upon which of the buttons was clicked or {@link AlertDialog#RESULT_CANCEL}
     * if the dialog was closed with the cancel (X) button.
     *
     * @return handle to the result
     */
    public CompletableFuture<Integer> awaitResult() {
        show();
        CompletableFuture<Integer> result = new CompletableFuture<>();
        if (okButton != null) {
            okButton.setOnAction((event) -> {
                if (okButtonAction == null) {
                    result.complete(RESULT_OK);
                    close();
                } else {
                    okButtonAction.perform();
                }
            });
        }
        if (closeButton != null) {
            closeButton.setOnAction((event) -> {
                if (closeButtonAction == null) {
                    result.complete(RESULT_CLOSE);
                    close();
                } else {
                    closeButtonAction.perform();
                }
            });
        }
        if (extraButton != null) {
            extraButton.setOnAction((event) -> {
                if (extraButtonAction == null) {
                    result.complete(RESULT_EXTRA);
                } else {
                    extraButtonAction.perform();
                }
            });
        }
        setOnCloseRequest((event) -> {
            result.complete(RESULT_CANCEL);
            if (!FLAG_AUTO_CANCEL) {
                event.consume();
            }
        });
        return result;
    }

    /**
     * Shows the dialog without returning any result. By default, the Ok, Close and Cancel buttons close the dialog.
     */
    public void open() {
        show();
        if (okButton != null) {
            okButton.setOnAction((event) -> {
                if (okButtonAction == null) {
                    close();
                } else {
                    okButtonAction.perform();
                }
            });
        }
        if (closeButton != null) {
            closeButton.setOnAction((event) -> {
                if (closeButtonAction == null) {
                    close();
                } else {
                    closeButtonAction.perform();
                }
            });
        }
        if (extraButton != null) {
            extraButton.setOnAction((event) -> {
                if (extraButtonAction != null) {
                    extraButtonAction.perform();
                }
            });
        }
        setOnCloseRequest((event) -> {
            if (!FLAG_AUTO_CANCEL) {
                event.consume();
            }
        });
    }

    /**
     * Creates and sets the scene of the AlertDialog from the arguments.
     *
     * @param message         the message
     * @param okButtonText    text on the leftmost button
     * @param closeButtonText text on the middle button
     * @param extraButtonText text on the rightmost button
     * @param icon            dialog icon (see {@link Builder.Icon})
     */
    private void createScene(final String message, final String okButtonText, final String closeButtonText, final String extraButtonText, final String icon,
                             final Builder.Action okButtonAction, final Builder.Action closeButtonAction, final Builder.Action extraButtonAction) {
        this.okButtonAction = okButtonAction;
        this.closeButtonAction = closeButtonAction;
        this.extraButtonAction = extraButtonAction;
        BorderPane root = new BorderPane();
        messageField = new Label(message);
        messageField.setWrapText(true);
        messageField.setPadding(new Insets(8, 8, 8, 8));
        // In case we want to add icon, we need to modify the layout accordingly by adding another node and wrapping
        // both of these in an HBox
        if (icon != null && icon.length() != 0) {
            ImageView image = new ImageView(icon);
            HBox centerContainer = new HBox();
            centerContainer.setAlignment(Pos.CENTER_LEFT);
            centerContainer.setPadding(new Insets(0, 0, 0, 8)); // top, right, bottom, left

            centerContainer.getChildren().add(image);

            centerContainer.getChildren().add(messageField);
            root.setCenter(centerContainer);
        } else {
            root.setCenter(messageField);
        }

        GridPane buttonBar = new GridPane();
        buttonBar.setPadding(new Insets(8, 8, 8, 8));

        // we want to add only the buttons that are desired (their text is not empty)
        ArrayList<Button> buttons = new ArrayList<>();
        if (okButtonText != null && okButtonText.length() != 0) {
            okButton = new Button(okButtonText);
            buttons.add(okButton);
        }
        if (closeButtonText != null && closeButtonText.length() != 0) {
            closeButton = new Button(closeButtonText);
            buttons.add(closeButton);
        }
        if (extraButtonText != null && extraButtonText.length() != 0) {
            extraButton = new Button(extraButtonText);
            buttons.add(extraButton);
        }

        // make the width distribution even for each column
        for (int i = 0; i < buttons.size(); i++) {
            GridPane.setConstraints(buttons.get(i), i, 0); // column i, row 0
            final ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth((double) 100 / buttons.size());
            columnConstraints.setHalignment(HPos.CENTER); // center the content, just a visual improvement
            buttonBar.getColumnConstraints().add(columnConstraints);
        }

        buttonBar.getChildren().addAll(buttons);
        // create a VBox, so we can include the separator to the BorderPane's bottom rather than to the center
        VBox bottomWrapper = new VBox();
        bottomWrapper.getChildren().add(new Separator());
        bottomWrapper.getChildren().add(buttonBar);
        if (buttonBar.getChildren().size() != 0) {
            ((Button) buttonBar.getChildren().get(0)).setDefaultButton(true);
            root.setBottom(bottomWrapper);
        }

        // finally create and set the scene
        Scene scene = new Scene(root, 400, -1); // width, height

        setScene(scene);
    }


    /**
     * Convenience class to build {@link AlertDialog} instances
     */
    public static class Builder {
        @TODO(description = "implement stage reusing (recreating the scenes using createScene()) to improve performance")
        private static final ArrayList<Stage> dialogs = new ArrayList<>();
        private String title = "";
        private String message = "";
        private String okButtonText = "";
        private Action okButtonAction = null;
        private String closeButtonText = "";
        private Action closeButtonAction = null;
        private String extraButtonText = "";
        private Action extraButtonAction = null;
        private String icon = "";
        private Window parent;
        private boolean cancelable = true;

        /**
         * Sets the title of the AlertDialog. It is the title of the window. The title must not be null.
         *
         * @param s the title
         * @return this
         */
        public Builder setTitle(final String s) {
            if (s == null) {
                throw new IllegalArgumentException("Title can not be null");
            }
            title = s;
            return this;
        }

        /**
         * Sets the message of the AlertDialog. The message will be displayed in the dialog's body, eventually right of the icon. The message must not be null.
         *
         * @param s the message
         * @return this
         */
        public Builder setMessage(final String s) {
            if (s == null) {
                throw new IllegalArgumentException("Message can not be null");
            }
            message = s;
            return this;
        }

        /**
         * Adds another button to the dialog with default button text. This button will then as a result return {@link AlertDialog#RESULT_OK}.
         * Calling this method multiple times will not create any additional buttons.
         *
         * @return this
         */
        public Builder addOkButton() {
            okButtonText = "OK";
            return this;
        }

        /**
         * Adds another button to the dialog with a custom button text. This button will then as a result return {@link AlertDialog#RESULT_OK}.
         * Calling this method multiple times will override the button.
         *
         * @param buttonText text to appear on the button
         * @return this
         */
        public Builder addOkButton(final String buttonText) {
            if (buttonText == null) {
                return addOkButton();
            }
            okButtonText = buttonText;
            return this;
        }

        /**
         * Adds another button to the dialog with a custom button text and a custom action to be performed when the button is clicked. This button will then
         * as a result return {@link AlertDialog#RESULT_OK}. Calling this method multiple times will override the button.
         *
         * @param buttonText text to appear on the button
         * @param a          the callback action
         * @return this
         */
        public Builder addOkButton(final String buttonText, final Action a) {
            okButtonAction = a;
            if (buttonText == null) {
                return addOkButton();
            }
            okButtonText = buttonText;
            return this;
        }

        /**
         * Adds another button to the dialog with default button text. This button will then as a result return {@link AlertDialog#RESULT_CLOSE}.
         * Calling this method multiple times will not create any additional buttons.
         *
         * @return this
         */
        public Builder addCloseButton() {
            closeButtonText = "Close";
            return this;
        }

        /**
         * Adds another button to the dialog with a custom button text. This button will then as a result return {@link AlertDialog#RESULT_CLOSE}.
         * Calling this method multiple times will override the button.
         *
         * @param buttonText text to appear on the button
         * @return this
         */
        public Builder addCloseButton(final String buttonText) {
            if (buttonText == null) {
                return addCloseButton();
            }
            closeButtonText = buttonText;
            return this;
        }

        /**
         * Adds another button to the dialog with a custom button text and a custom action to be performed when the button is clicked. This button will then
         * as a result return {@link AlertDialog#RESULT_CLOSE}. Calling this method multiple times will override the button.
         *
         * @param buttonText text to appear on the button
         * @param a          the callback action
         * @return this
         */
        public Builder addCloseButton(final String buttonText, final Action a) {
            closeButtonAction = a;
            if (buttonText == null) {
                return addCloseButton();
            }
            closeButtonText = buttonText;
            return this;
        }

        /**
         * Adds another button to the dialog with default button text. This button will then as a result return {@link AlertDialog#RESULT_EXTRA}.
         * Calling this method multiple times will not create any additional buttons.
         *
         * @return this
         */
        public Builder addExtraButton() {
            extraButtonText = "Extra";
            return this;
        }

        /**
         * Adds another button to the dialog with a custom button text. This button will then as a result return {@link AlertDialog#RESULT_EXTRA}.
         * Calling this method multiple times will override the button.
         *
         * @param buttonText text to appear on the button
         * @return this
         */
        public Builder addExtraButton(final String buttonText) {
            if (buttonText == null) {
                return addExtraButton();
            }
            extraButtonText = buttonText;
            return this;
        }

        /**
         * Adds another button to the dialog with a custom button text and a custom action to be performed when the button is clicked. This button will then
         * as a result return {@link AlertDialog#RESULT_EXTRA}. Calling this method multiple times will override the button.
         *
         * @param buttonText text to appear on the button
         * @param a          the callback action
         * @return this
         */
        public Builder addExtraButton(final String buttonText, final Action a) {
            extraButtonAction = a;
            if (buttonText == null) {
                return addExtraButton();
            }
            extraButtonText = buttonText;
            return this;
        }

        /**
         * Sets the parent of the {@link AlertDialog}. The parent then acts as an owner, see {@link Stage#initOwner(Window)}.
         *
         * @param w the parent
         * @return this
         */
        public Builder setParent(final Window w) {
            parent = w;
            return this;
        }

        /**
         * Sets the AlertDialog icon. The icon is displayed left of the AlertDialog message.
         *
         * @param i desired icon
         * @return this
         */
        public Builder setIcon(final Icon i) {
            if (i != null) {
                icon = i.toFile();
            }
            return this;
        }

        /**
         * When true, the AlertDialog will close upon clicking the cancel (X) button.
         *
         * @param b the value
         * @return this
         */
        public Builder setCancelable(final boolean b) {
            cancelable = b;
            return this;
        }

        /**
         * Finishes the build and creates and {@link AlertDialog} object with the corresponding parameters. The AlertDialog by default is hidden.
         *
         * @return the output AlertDialog
         */
        public AlertDialog build() {
            AlertDialog target = new AlertDialog(title);
            target.createScene(message, okButtonText, closeButtonText, extraButtonText, icon, okButtonAction, closeButtonAction, extraButtonAction);
            if (parent != null) {
                target.initOwner(parent);
                target.initModality(Modality.WINDOW_MODAL);
            }
            target.setAutoCancel(cancelable);
            return target;
        }


        /**
         * This class represent the only available options for an AlertDialog icon. The AlertDialog class uses the original icons from JavaFX.
         * Also, if you change the default stylesheet from caspian.css, this shit may break.
         */
        public enum Icon {
            // if you ask me where I got these paths, I asked ChatGPT lol
            INFO("/com/sun/javafx/scene/control/skin/caspian/dialog-information.png"),
            WARNING("/com/sun/javafx/scene/control/skin/caspian/dialog-warning.png"),
            ERROR("/com/sun/javafx/scene/control/skin/caspian/dialog-error.png"),
            CONFIRM("/com/sun/javafx/scene/control/skin/caspian/dialog-confirm.png");

            private final String file;

            Icon(String s) {
                file = s;
            }

            /**
             * Returns the underlying file path.
             *
             * @return icon file path
             */
            public String toFile() {
                return file;
            }
        }

        public interface Action {
            void perform();
        }
    }
}
