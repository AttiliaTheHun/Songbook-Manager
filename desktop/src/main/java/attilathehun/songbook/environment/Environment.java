package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionListener;
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

public final class Environment implements CollectionListener {

    private static final Logger logger = LogManager.getLogger(Environment.class);
    private static final List<EnvironmentStateListener> listeners = new ArrayList<EnvironmentStateListener>();
    private static final Environment INSTANCE = new Environment();
    public static boolean FLAG_IGNORE_SEGMENTS = false;
    private EnvironmentSettings settings = getDefaultSettings();
    private final Map<String, CollectionManager> collectionManagers = new HashMap<>();

    private CollectionManager selectedCollectionManager;

    private String tokenInMemory = null;

    private Environment() {
    }

    public static Environment getInstance() {
        return INSTANCE;
    }


    /**
     * Scrolls the songbook so that the requested song is visible in the web view as if the user navigated on it himself, which means that
     * the left/right position of the song will be preserved.
     *
     * @param s the song to be displayed
     */
    public static void navigateWebViewToSong(final Song s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        if (s.getManager() != null && !s.getManager().equals(getInstance().getCollectionManager())) {
            getInstance().setCollectionManager(s.getManager());
        }
        int index = getInstance().getCollectionManager().getFormalCollectionSongIndex(s);
        if (index % 2 == 0) {
            notifyOnSongOneSet(getInstance().getCollectionManager().getFormalCollection().get(index));
            if (index + 1 >= getInstance().getCollectionManager().getFormalCollection().size()) {
                notifyOnSongTwoSet(CollectionManager.getShadowSong());
            } else {
                notifyOnSongTwoSet(getInstance().getCollectionManager().getFormalCollection().get(index + 1));
            }
        } else {
            notifyOnSongTwoSet(getInstance().getCollectionManager().getFormalCollection().get(index));
            if (index - 1 < 0) {
                notifyOnSongOneSet(CollectionManager.getShadowSong());
            } else {
                notifyOnSongOneSet(getInstance().getCollectionManager().getFormalCollection().get(index - 1));
            }
        }
    }

    /**
     * Registers an {@link EnvironmentStateListener}, allowing it to receive Environment-related events.
     *
     * @param listener the listener
     */
    public static void addListener(final EnvironmentStateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        listeners.add(listener);
    }

    public static void removeListener(final EnvironmentStateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        listeners.remove(listener);
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

    public static void notifyOnSongOneSet(final Song s) {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongOneSet(s);
        }
    }

    public static void notifyOnSongTwoSet(final Song s) {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongTwoSet(s);
        }
    }

    private static void notifyOnCollectionManagerChanged(final CollectionManager m) {
        for (EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onCollectionManagerChanged(m);
        }
    }

    public String acquireToken() {
        if (tokenInMemory != null) {
            return tokenInMemory;
        }

        // TODO

       return null;
    }

    public void refresh() {
        try {
            for (File f : new File((String) settings.get("TEMP_FILE_PATH")).listFiles()) {
                if (FLAG_IGNORE_SEGMENTS && f.getName().startsWith("segment")) {
                    continue;
                }
                if (!f.delete() && !f.getName().endsWith(PDFGenerator.EXTENSION_PDF)) {
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
     * Returns the default {@link CollectionManager}.
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
     * Sets the default {@link CollectionManager}.
     *
     * @param collectionManager the manager
     */
    public void setCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager is null");
        }
        if (selectedCollectionManager != null) {
            selectedCollectionManager.removeListener(this);
        }

        this.selectedCollectionManager = collectionManager;
        selectedCollectionManager.addListener(this);
        refresh();
        notifyOnCollectionManagerChanged(selectedCollectionManager);
    }

    public void registerCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager is null");
        }
        collectionManagers.put(collectionManager.getCollectionName(), collectionManager);
        SettingsManager.getInstance().save();
    }

    public void unregisterCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager is null");
        }
        collectionManagers.remove(collectionManager.getCollectionName());
        SettingsManager.getInstance().save();
    }

    public Map<String, CollectionManager> getRegisteredManagers() {
        return collectionManagers;
    }

    public void loadTokenToMemory(final String token) {
        if (token == null || token.length() == 0) {
            return;
        }
        tokenInMemory = token;
    }

    public void exit() {
        SettingsManager.getInstance().save();
        Platform.exit();
    }

    public EnvironmentSettings getDefaultSettings() {
        EnvironmentSettings settings = new EnvironmentSettings();
        settings.put("DATA_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/data/").toString());
        settings.put("SONGS_FILE_PATH", Paths.get(settings.get("DATA_FILE_PATH") + "/songs/").toString());
        settings.put("RESOURCES_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/resources/").toString());
        settings.put("CSS_RESOURCES_FILE_PATH", Paths.get(settings.get("RESOURCES_FILE_PATH") + "/css/").toString());
        settings.put("TEMPLATE_RESOURCES_FILE_PATH", Paths.get(settings.get("RESOURCES_FILE_PATH") + "/templates/").toString());
        settings.put("DATA_ZIP_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/data.zip").toString());
        settings.put("TEMP_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/temp/").toString());
        settings.put("TEMP_TIMESTAMP_FILE_PATH", Paths.get(settings.get("TEMP_FILE_PATH") + "/session_timestamp.txt").toString());
        settings.put("ASSETS_RESOURCES_FILE_PATH", Paths.get(settings.get("RESOURCES_FILE_PATH") + "/assets/").toString());
        settings.put("OUTPUT_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/pdf/").toString());
        settings.put("LOG_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/log.txt").toString());
        settings.put("SCRIPTS_FILE_PATH", Paths.get(System.getProperty("user.dir") + "/scripts/").toString());
        return settings;
    }

    public EnvironmentSettings getSettings() {
        return settings;
    }

    public void setSettings(final EnvironmentSettings s) {
        if (s == null) {
            throw new IllegalArgumentException("settings are null");
        }
        settings = s;
    }

    @Override
    public void onSongRemoved(final Song s, final CollectionManager m) {
        refresh();
    }

    @Override
    public void onSongUpdated(final Song s, final CollectionManager m) {
        refresh();
    }

    @Override
    public void onSongAdded(final Song s, final CollectionManager m) {
        refresh();
    }

    public static final class EnvironmentSettings extends HashMap<String, Object> implements Serializable {
        private static final Logger logger = LogManager.getLogger(attilathehun.songbook.environment.Environment.EnvironmentSettings.class);
        public static final String SETTINGS_FILE_PATH = "settings.json";
        private static final String EASTER_EXE_FILE_PATH = "easter.exe";
        public static final boolean IS_IT_EASTER_ALREADY = new File(EASTER_EXE_FILE_PATH).exists() && new File(EASTER_EXE_FILE_PATH).length() == 0;

    }


}
