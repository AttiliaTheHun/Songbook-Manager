package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.export.PDFGenerator;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.vcs.VCSAdmin;
import attilathehun.songbook.window.AlertDialog;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static javax.swing.JOptionPane.showMessageDialog;

public final class Environment {

    private static final Logger logger = LogManager.getLogger(Environment.class);
    private static final List<EnvironmentStateListener> listeners = new ArrayList<EnvironmentStateListener>();
    private static final Environment instance = new Environment();
    public static boolean FLAG_IGNORE_SEGMENTS = false;
    public final Settings settings = Settings.getSettings();
    private final Map<String, CollectionManager> collectionManagers = new HashMap<>();

    private CollectionManager selectedCollectionManager;

    private String tokenInMemory = null;

    private Environment() {
        refresh();
        logger.debug("Environment instantiated");
    }

    public static Environment getInstance() {
        return instance;
    }

    @Deprecated
    public static void showErrorMessage(String title, String message) {
        showErrorMessage(title, message, false);
    }

    @Deprecated
    public static void showErrorMessage(String title, String message, boolean fatal) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.ERROR_MESSAGE);
        if (fatal) {
            getInstance().exit();
        }
    }

    @Deprecated
    public static void showMessage(String title, String message) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.INFORMATION_MESSAGE);

    }

    @Deprecated
    public static void showMessage(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    @Deprecated
    public static void showErrorMessage(String title, String header, String message, boolean fatal) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
        if (fatal) {
            Environment.getInstance().exit();
        }
    }

    @Deprecated
    public static void showErrorMessage(String title, String header, String message) {
        showErrorMessage(title, header, message, false);
    }

    @Deprecated
    public static void showWarningMessage(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    @Deprecated
    public static boolean showConfirmMessage(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(header);
        alert.setTitle(title);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();

        return ((result.isPresent()) && (result.get() == ButtonType.OK));
    }

    @Deprecated
    public static void showWarningMessage(String title, String message) {
        showMessageDialog(getAlwaysOnTopJDialog(), message, title,
                JOptionPane.WARNING_MESSAGE);
    }

    @Deprecated
    public static JDialog getAlwaysOnTopJDialog() {
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        dialog.toFront();
        dialog.requestFocusInWindow();
        return dialog;
    }

    /**
     * Scrolls the songbook so that the requested song is visible in the web view as if the user navigated on it himself, which means that
     * the left/right position of the song will be preserved.
     *
     * @param s the song to be displayed
     */
    public static void navigateWebViewToSong(Song s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        if (s.getManager() != null && !s.getManager().equals(Environment.getInstance().getCollectionManager())) {
            Environment.getInstance().setCollectionManager(s.getManager());
        }
        int index = getInstance().getCollectionManager().getFormalCollectionSongIndex(s);
        if (index % 2 == 0) {
            notifyOnSongOneSet(Environment.getInstance().getCollectionManager().getFormalCollection().get(index));
            if (index + 1 >= Environment.getInstance().getCollectionManager().getFormalCollection().size()) {
                notifyOnSongTwoSet(CollectionManager.getShadowSong());
            } else {
                notifyOnSongTwoSet(Environment.getInstance().getCollectionManager().getFormalCollection().get(index + 1));
            }
        } else {
            notifyOnSongTwoSet(Environment.getInstance().getCollectionManager().getFormalCollection().get(index));
            if (index - 1 < 0) {
                notifyOnSongOneSet(CollectionManager.getShadowSong());
            } else {
                notifyOnSongOneSet(Environment.getInstance().getCollectionManager().getFormalCollection().get(index - 1));
            }
        }
    }

    /**
     * Registers an {@link EnvironmentStateListener}, allowing it to receive Environment-related events.
     *
     * @param listener the listener
     */
    public static void addListener(EnvironmentStateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        listeners.add(listener);
    }

    public static List<EnvironmentStateListener> getListeners() {
        return listeners;
    }

    private static void notifyOnRefresh() {
        for (EnvironmentStateListener listener : listeners) {
            listener.onRefresh();
        }
    }

    public static void notifyOnPageTurnedBack() {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onPageTurnedBack();
        }
    }

    public static void notifyOnPageTurnedForward() {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onPageTurnedForward();
        }
    }

    public static void notifyOnSongOneSet(Song s) {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongOneSet(s);
        }
    }

    public static void notifyOnSongTwoSet(Song s) {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongTwoSet(s);
        }
    }

    public String acquireToken(VCSAdmin.Certificate certificate) {
        if (tokenInMemory != null) {
            return tokenInMemory;
        }

        if (Misc.fileExists(settings.user.getAuthFilePath(new Certificate()))) {
            try {
                return String.join("", Files.readAllLines(Path.of(settings.user.getAuthFilePath(new Certificate()))));
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
                showWarningMessage("Error", "Error reading auth file, continuing with default token.");
            }

        }

        return settings.user.getDefaultReadToken(new Certificate());
    }

    public void refresh() {
        try {
            for (File f : new File(settings.environment.TEMP_FILE_PATH).listFiles()) {
                if (FLAG_IGNORE_SEGMENTS && f.getName().startsWith("segment")) {
                    continue;
                }
               /* if (f.getPath().equals(String.format(PDFGenerator.PREVIEW_SEGMENT_PATH, PDFGenerator.EXTENSION_PDF))) {
                    continue;
                }*/
                if (!f.delete()) {
                    new AlertDialog.Builder().setTitle("Refreshing error").setIcon(AlertDialog.Builder.Icon.ERROR)
                                    .setMessage("Cannot clean temp folder!").addOkButton().build().open();
                    exit();
                }
            }
            logger.debug("Environment refresh()");
        } catch (NullPointerException npe) {
            logger.error(npe.getMessage(), npe);
        }
        notifyOnRefresh();
    }

    /**
     * Returns the default CollectionManager.
     *
     * @return default Collection Manager ({@link StandardCollectionManager} if null)
     */
    public CollectionManager getCollectionManager() {
        if (selectedCollectionManager == null) {
            return StandardCollectionManager.getInstance();
        }
        return selectedCollectionManager;
    }

    /**
     * Sets the default CollectionManager.
     *
     * @param collectionManager the manager
     */
    public void setCollectionManager(final CollectionManager collectionManager) {
        this.selectedCollectionManager = collectionManager;
    }

    public void registerCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager is null");
        }
        this.settings.collections.putIfAbsent(collectionManager.getCollectionName(), collectionManager.getSettings());
        Settings.save(this.settings);
        collectionManagers.put(collectionManager.getCollectionName(), collectionManager);
    }

    public void unregisterCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager is null");
        }
        this.settings.collections.remove(collectionManager.getCollectionName());
        Settings.save(this.settings);
        collectionManagers.remove(collectionManager.getCollectionName());
    }

    public Map<String, CollectionManager> getRegisteredManagers() {
        return collectionManagers;
    }

    public void loadTokenToMemory(String token, VCSAdmin.Certificate certificate) {
        tokenInMemory = token;
    }

    public void exit() {
        Settings.save(settings);
        Platform.exit();
    }

    public static class EnvironmentSettings implements Serializable {
        public static final String SETTINGS_FILE_PATH = "settings.json";
        public static final String EASTER_EXE_FILE_PATH = "easter.exe";
        private static final Logger logger = LogManager.getLogger(attilathehun.songbook.environment.Environment.EnvironmentSettings.class);
        public final transient boolean IS_IT_EASTER_ALREADY = new File(EASTER_EXE_FILE_PATH).exists() && new File(EASTER_EXE_FILE_PATH).length() == 0;
        public final String RESOURCE_FILE_PATH;
        public final String CSS_RESOURCES_FILE_PATH;
        public final String TEMPLATE_RESOURCES_FILE_PATH;
        public final String DATA_ZIP_FILE_PATH;
        public final String TEMP_FILE_PATH;
        public final String ASSETS_RESOURCES_FILE_PATH;
        public final String OUTPUT_FILE_PATH;
        public final String DATA_FILE_PATH;
        public final String TEMP_TIMESTAMP_FILE_PATH;
        public final String LOG_FILE_PATH;
        public final String SCRIPTS_FILE_PATH;


        public EnvironmentSettings() {
            DATA_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/data/").toString();
            RESOURCE_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/resources/").toString();
            CSS_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/css/").toString();
            TEMPLATE_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/templates/").toString();
            DATA_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/data.zip").toString();
            TEMP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/temp/").toString();
            TEMP_TIMESTAMP_FILE_PATH = Paths.get(TEMP_FILE_PATH + "/session_timestamp.txt").toString();
            ASSETS_RESOURCES_FILE_PATH = Paths.get(RESOURCE_FILE_PATH + "/assets/").toString();
            OUTPUT_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/pdf/").toString();
            LOG_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/log.txt").toString();
            SCRIPTS_FILE_PATH = Paths.get(System.getProperty("user.dir") + "/scripts/").toString();
        }


    }

    public static final class Certificate {
        private Certificate() {

        }
    }

}
