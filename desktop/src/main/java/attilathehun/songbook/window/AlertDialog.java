package attilathehun.songbook.window;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Utility class that allows the creation of customizable pop-up dialogs with ease and only minor headache.
 */
public class AlertDialog extends Stage {
    public static final int RESULT_OK = 0;
    public static final int RESULT_CLOSE = 1;
    public static final int RESULT_EXTRA = 2;
    public static final int RESULT_CANCEL = -1;

    private Label messageField;
    private ArrayList<Node> nodes = new ArrayList<Node>();
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
    private AlertDialog(final String title) {
        super(StageStyle.DECORATED);
        setTitle(title);
        setResizable(false);
    }

    /**
     * Set {@link AlertDialog#FLAG_AUTO_CANCEL} value. When true, the cancel (X) button will close the dialog, before returning the result.
     *
     * @param b new value
     */
    private void setAutoCancel(final boolean b) {
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
        final CompletableFuture<Integer> result = new CompletableFuture<>();
        if (okButton != null) {
            okButton.setOnAction((event) -> {
                if (okButtonAction == null) {
                    result.complete(RESULT_OK);
                    close();
                } else {
                    if (okButtonAction.perform(result)) {
                        close();
                    }
                }
            });
        }
        if (closeButton != null) {
            closeButton.setOnAction((event) -> {
                if (closeButtonAction == null) {
                    result.complete(RESULT_CLOSE);
                    close();
                } else {
                    if (closeButtonAction.perform(result)) {
                        close();
                    }
                }
            });
        }
        if (extraButton != null) {
            extraButton.setOnAction((event) -> {
                if (extraButtonAction == null) {
                    result.complete(RESULT_EXTRA);
                    close();
                } else {
                    if (extraButtonAction.perform(result)) {
                        close();
                    }
                }
            });
        }
        setOnCloseRequest((event) -> {
            if (FLAG_AUTO_CANCEL) {
                result.complete(RESULT_CANCEL);
            } else {
                event.consume();
            }
        });
        showAndWait();
        toFront();
        return result;
    }

    /**
     * The means of getting complete dialog data through {@link CompletableFuture#thenAccept(Consumer)}. Upon completion returns a compound containing
     * the result value (see {@link AlertDialog#awaitResult()}) and the complete list of content nodes. Content nodes can be added via {@link AlertDialog.Builder#addContentNode(Node)}
     * or {@link Builder#addTextInput()}. If you already have references to the content nodes of interest, you may as well use {@link AlertDialog#awaitResult()}.
     * Content nodes are stored (and returned) in the order they were added.
     *
     * @return handle to the result
     */
    public CompletableFuture<Pair<Integer, ArrayList<Node>>> awaitData() {
        final CompletableFuture<Pair<Integer, ArrayList<Node>>> result = new CompletableFuture<>();
        if (okButton != null) {
            okButton.setOnAction((event) -> {
                if (okButtonAction == null) {
                    result.complete(new Pair<>(RESULT_OK, nodes));
                    close();
                } else {
                    if (okButtonAction.perform(result)) {
                        close();
                    }
                }
            });
        }
        if (closeButton != null) {
            closeButton.setOnAction((event) -> {
                if (closeButtonAction == null) {
                    result.complete(new Pair<>(RESULT_CLOSE, nodes));
                    close();
                } else {
                    if (closeButtonAction.perform(result)) {
                        close();
                    }
                }
            });
        }
        if (extraButton != null) {
            extraButton.setOnAction((event) -> {
                if (extraButtonAction == null) {
                    result.complete(new Pair<>(RESULT_EXTRA, nodes));
                    close();
                } else {
                    if (extraButtonAction.perform(result)) {
                        close();
                    }
                }
            });
        }
        setOnCloseRequest((event) -> {
            if (FLAG_AUTO_CANCEL) {
                result.complete(new Pair<>(RESULT_CANCEL, nodes));
            } else {
                event.consume();
            }
        });
        showAndWait();
        toFront();
        return result;
    }

    /**
     * Shows the dialog without returning any result, which is useful for information notice messages. By default, the Ok, Close and Cancel buttons close the dialog.
     */
    public void open() {
        if (okButton != null) {
            okButton.setOnAction((event) -> {
                if (okButtonAction == null) {
                    close();
                } else {
                    if (okButtonAction.perform(null)) {
                        close();
                    }
                }
            });
        }
        if (closeButton != null) {
            closeButton.setOnAction((event) -> {
                if (closeButtonAction == null) {
                    close();
                } else {
                    if (closeButtonAction.perform(null)) {
                        close();
                    }
                }
            });
        }
        if (extraButton != null) {
            extraButton.setOnAction((event) -> {
                if (extraButtonAction != null) {
                    if (extraButtonAction.perform(null)) {
                        close();
                    }
                } else {
                    close();
                }
            });
        }
        setOnCloseRequest((event) -> {
            if (!FLAG_AUTO_CANCEL) {
                event.consume();
            }
        });
        showAndWait();
        toFront();
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
                             final Builder.Action okButtonAction, final Builder.Action closeButtonAction, final Builder.Action extraButtonAction,
                             final ArrayList<Node> nodes) {
        this.okButtonAction = okButtonAction;
        this.closeButtonAction = closeButtonAction;
        this.extraButtonAction = extraButtonAction;
        this.nodes = nodes;
        final BorderPane root = new BorderPane();
        // We create the layout for the custom nodes
        GridPane customContentContainer = null;
        if (nodes.size() != 0) {
            customContentContainer = new GridPane();
            GridPane.setHgrow(customContentContainer, Priority.ALWAYS);
            customContentContainer.setVgap(10d);
            customContentContainer.setPadding(new Insets(8, 8, 8, 8)); //top right bottom left

            // make the first column of fixed size for vertical alignment
            final ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(30d);
            columnConstraints.setHalignment(HPos.LEFT); // center the content, just a visual improvement
            customContentContainer.getColumnConstraints().add(columnConstraints);
            // fill rest of the width with the second column
            final ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints2.setPercentWidth(70d);
            columnConstraints2.setHalignment(HPos.LEFT); // center the content, just a visual improvement
            customContentContainer.getColumnConstraints().add(columnConstraints2);

            int rowNumber = 0;


            for (int i = 0; i < nodes.size(); i++) {
                // every TextField added through addTextInput() has a preceding label, we want to put this pair in the same row
                // and ensure that all text inputs are visually (vertically) aligned
                if (nodes.get(i) instanceof Label) {
                    HBox.setHgrow(nodes.get(i), Priority.ALWAYS);
                    if (i + 1 < nodes.size() && nodes.get(i + 1) instanceof TextField) {
                        customContentContainer.add(nodes.get(i), 0, rowNumber);
                        HBox.setHgrow(nodes.get(i + 1), Priority.ALWAYS);
                        customContentContainer.add(nodes.get(i + 1), 1, rowNumber);
                        rowNumber++;
                        i++; // I know it is a bad practice, I am quite enjoying doing so tbh
                        continue;
                    }
                }
                // custom nodes are supposed to have custom layout, and thus we let them fill the line
                customContentContainer.add(nodes.get(i), 0, rowNumber, 2, 1); // columnIndex rowIndex columnSpan rowSpan
                rowNumber++;
            }
        }


        if (message != null && message.length() != 0) {
            messageField = new Label(message);
            messageField.setWrapText(true);
            messageField.setPadding(new Insets(8, 8, 8, 8));
            // In case we want to add icon, we need to modify the layout accordingly by adding another node and wrapping
            // both of these in an HBox
            if (icon != null && icon.length() != 0) {
                final ImageView image = new ImageView(icon);
                final HBox rootBorderPaneCenterContainer = new HBox();
                HBox.setHgrow(rootBorderPaneCenterContainer, Priority.ALWAYS);
                rootBorderPaneCenterContainer.setAlignment(Pos.CENTER_LEFT);
                rootBorderPaneCenterContainer.setPadding(new Insets(8, 8, 8, 8)); // top, right, bottom, left

                rootBorderPaneCenterContainer.getChildren().add(image);

                if (customContentContainer == null) {
                    rootBorderPaneCenterContainer.getChildren().add(messageField);
                } else {
                    final VBox box = new VBox(messageField, customContentContainer);
                    HBox.setHgrow(box, Priority.ALWAYS);
                    rootBorderPaneCenterContainer.getChildren().add(box);
                }

                root.setCenter(rootBorderPaneCenterContainer);
            } else {
                if (customContentContainer == null) {
                    root.setCenter(messageField);
                } else {
                    final VBox box = new VBox(messageField, customContentContainer);
                    root.setCenter(box);
                }
            }
        } else {
            root.setCenter(customContentContainer);
        }



        // Now we add the buttons

        final GridPane buttonBar = new GridPane();
        buttonBar.setPadding(new Insets(8, 8, 8, 8));

        // we want to add only the buttons that are desired (their text is not empty)
        final ArrayList<Button> buttons = new ArrayList<>();
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
        final VBox bottomWrapper = new VBox();
        bottomWrapper.getChildren().add(new Separator());
        bottomWrapper.getChildren().add(buttonBar);
        if (buttonBar.getChildren().size() != 0) {
            ((Button) buttonBar.getChildren().get(0)).setDefaultButton(true);
            root.setBottom(bottomWrapper);
        }

        // finally create and set the scene
        final Scene scene = new Scene(root, 400, -1); // width, height

        setScene(scene);
    }


    /**
     * Convenience class to build {@link AlertDialog} instances
     */
    public static class Builder {
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
        private final ArrayList<Node> nodes = new ArrayList<>();

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
         * Sets the message of the AlertDialog. The message will be displayed in the dialog's body, eventually right of the icon. The message, if set,
         * will appear before any other custom content and will not be included in {@link AlertDialog#awaitData()} result. The message must not be null.
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
         * Sets the AlertDialog icon. The icon is displayed left of the AlertDialog message and any other custom content.
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
         * Adds an empty {@link Label} and an empty {@link TextField} to the dialog content. These two nodes can be retrieved
         * via {@link AlertDialog#awaitData()}.
         *
         * @return this
         */
        public Builder addTextInput() {
            return addTextInput("", "", null, null);
        }

        /**
         * Adds a {@link Label} with custom text and an empty {@link TextField} to the dialog content. These two nodes can be retrieved
         * via {@link AlertDialog#awaitData()}.
         *
         * @param label text on the {@link Label}
         * @return this
         */
        public Builder addTextInput(final String label) {
           return addTextInput(label, "", null, null);
        }

        /**
         * Adds a {@link Label} with custom text and an empty {@link TextField} with custom hint text (see {@link TextField#setPromptText(String)})
         * to the dialog content. These two nodes can be retrieved via {@link AlertDialog#awaitData()}.
         *
         * @param label text on the {@link Label}
         * @param hintText hint text of the {@link TextField}
         * @return this
         */
        public Builder addTextInput(final String label, final String hintText) {
            return addTextInput(label, "", hintText, null);
        }

        /**
         * Adds a {@link Label} with custom text and a {@link TextField} with custom text and custom hint text (see {@link TextField#setPromptText(String)})
         * to the dialog content. These two nodes can be retrieved via {@link AlertDialog#awaitData()}.
         *
         * @param label text on the {@link Label}
         * @param defaultText text in the {@link TextField}
         * @param hintText hint text of the {@link TextField}
         * @return this
         */
        public Builder addTextInput(final String label, final String defaultText, final String hintText) {
            return addTextInput(label, defaultText, hintText, null);
        }

        /**
         * Adds a {@link Label} with custom text and a {@link TextField} with custom text and custom hint text (see {@link TextField#setPromptText(String)})
         * and a custom {@link Tooltip} to the dialog content. These two nodes can be retrieved via {@link AlertDialog#awaitData()}.
         *
         * @param label text on the {@link Label}
         * @param defaultText text in the {@link TextField}
         * @param hintText hint text of the {@link TextField}
         * @param tooltip {@link TextField} tooltip text
         * @return this
         */
        public Builder addTextInput(String label, String defaultText, final String hintText, final String tooltip) {
            if (label == null) {
                label = "";
            }
            final Label l = new Label(label);
            l.setWrapText(true);
            if (defaultText == null) {
                defaultText = "";
            }
            final TextField inputField = new TextField(defaultText);
            if (hintText != null && hintText.length() != 0) {
                inputField.setPromptText(hintText);
            }
            if (tooltip != null) {
                inputField.setTooltip(new Tooltip(tooltip));
            }
            nodes.add(l);
            nodes.add(inputField);
            return this;
        }

        /**
         * Adds a custom {@link Node} to the dialog content. This node will be injected to the scene layout without any modification, so any kind of
         * setup must be performed manually. The node can be retrieved via {@link AlertDialog#awaitData()}.
         *
         * @param n the node to be added
         * @return this
         */
        public Builder addContentNode(final Node n) {
            if (n != null) {
                nodes.add(n);
            }
            return this;
        }

        /**
         * Finishes the build and creates an {@link AlertDialog} object with the corresponding parameters. The AlertDialog is hidden by default.
         *
         * @return the output AlertDialog
         */
        public AlertDialog build() {
            final AlertDialog target = new AlertDialog(title);

            target.createScene(message, okButtonText, closeButtonText, extraButtonText, icon, okButtonAction, closeButtonAction, extraButtonAction, nodes);
            if (parent != null) {
                target.initOwner(parent);
                target.initModality(Modality.WINDOW_MODAL);
            }
            target.setAutoCancel(cancelable);

            return target;
        }

        /**
         * This class represent the available options for an AlertDialog icon. The AlertDialog class uses the original icons from JavaFX.
         * Also, if you change the default stylesheet from caspian.css, this shit may break.
         */
        public enum Icon {
            // if you ask me where I got these paths, I asked ChatGPT lol
            INFO("/com/sun/javafx/scene/control/skin/caspian/dialog-information.png"),
            WARNING("/com/sun/javafx/scene/control/skin/caspian/dialog-warning.png"),
            ERROR("/com/sun/javafx/scene/control/skin/caspian/dialog-error.png"),
            CONFIRM("/com/sun/javafx/scene/control/skin/caspian/dialog-confirm.png");

            private final String file;

            Icon(final String s) {
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

        /**
         * This interface represents a custom action that may be added to AlertDialog buttons.
         */
        public interface Action {

            /**
             * This method contains the {@link Action}'s executable code. If the method returns true, the {@link AlertDialog} will close after performing the action. Note that
             * if the parameter @result is not null, it must be completed in order for the dialog to get closed.
             *
             * @param result the result object of the AlertDialog the method was passed into. Its type and value depends on the way the AlertDialog was opened. In case of
             * {@link AlertDialog#awaitResult()} the type is CompletableFuture&#060;Integer&#062;. If {@link AlertDialog#awaitData()} was used, then result is of type
             * CompletableFuture&#060;Pair&#060;Integer, ArrayList&#060;Node&#062;&#062;&#062; For {@link AlertDialog#open()} result is null;
             */
            boolean perform(CompletableFuture result);
        }
    }
}
