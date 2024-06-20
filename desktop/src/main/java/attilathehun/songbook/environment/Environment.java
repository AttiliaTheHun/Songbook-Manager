package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionListener;
import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.util.BrowserFactory;
import attilathehun.songbook.util.PDFGenerator;
import attilathehun.songbook.vcs.CacheManager;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.CollectionEditor;
import attilathehun.songbook.window.SettingsEditor;
import attilathehun.songbook.window.SongbookApplication;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

import static javax.swing.JOptionPane.showMessageDialog;

public final class Environment implements CollectionListener {
    private static final Logger logger = LogManager.getLogger(Environment.class);
    public static final String SETTINGS_FILE_PATH = "settings.json";
    private static final String EASTER_EXE_FILE_PATH = "easter.exe";
    public static final boolean IS_IT_EASTER_ALREADY = new File(EASTER_EXE_FILE_PATH).exists() && new File(EASTER_EXE_FILE_PATH).length() == 0;
    private static final List<EnvironmentStateListener> listeners = new ArrayList<EnvironmentStateListener>();
    private static final Environment INSTANCE = new Environment();
    public static boolean FLAG_IGNORE_SEGMENTS = false;
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
        final int index = getInstance().getCollectionManager().getFormalCollectionSongIndex(s);
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
        for (final EnvironmentStateListener listener : listeners) {
            listener.onRefresh();
        }
    }

    public static void notifyOnPageTurnedBack() {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onPageTurnedBack();
        }
    }

    public static void notifyOnPageTurnedForward() {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onPageTurnedForward();
        }
    }

    public static void notifyOnSongOneSet(final Song s) {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongOneSet(s);
        }
    }

    public static void notifyOnSongTwoSet(final Song s) {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongTwoSet(s);
        }
    }

    private static void notifyOnCollectionManagerChanged(final CollectionManager m) {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
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

    /**
     * A soft refresh to ensure the environment is working with the latest sure-to-change data. This operation is relatively lightweight as it updates
     * only the components whose working data changes often during runtime, such as the webview source data. For a more thorough refresh use {@link #hardRefresh()}. Emits {@link EnvironmentStateListener#onRefresh()}.
     */
    public void refresh() {
        try {
            for (final File f : new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).listFiles()) {
                if (FLAG_IGNORE_SEGMENTS && f.getName().startsWith("segment")) {
                    continue;
                }
                if (!f.delete() && !f.getName().endsWith(PDFGenerator.EXTENSION_PDF)) {
                    new AlertDialog.Builder().setTitle("Refreshing error").setIcon(AlertDialog.Builder.Icon.ERROR)
                                    .setMessage("Cannot clean temp folder!").addOkButton().build().open();
                    exit();
                }
            }

            logger.debug("environment soft refreshed");
        } catch (final NullPointerException npe) {
            logger.error(npe.getMessage(), npe);
        }
        notifyOnRefresh();
    }

    /**
     * Performs a complete refresh of the environment without restarting it. This operation is quite heavy and may result in crash of the program. It is designed
     * to be used only when absolute necessary, for example when loading a new songbook at runtime. If you only need to mae sure some changes in the configuration
     * or in the collections are applied, use {@link #refresh()} which is much lighter and much less likely to crash the application. Emits {@link EnvironmentStateListener#onRefresh()}.
     */
    public void hardRefresh() {
        SettingsManager.getInstance().load();
        try {
            for (final File f : new File((String) SettingsManager.getInstance().getValue("TEMP_FILE_PATH")).listFiles()) {
                if (!f.delete()) {
                    new AlertDialog.Builder().setTitle("Refreshing error").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .setMessage("Cannot clean temp folder!").addOkButton().build().open();
                    logger.error("failed to clean temp folder on full refresh");
                    exit();
                }
            }
        } catch (final NullPointerException e) {
            logger.error(e.getMessage(), e);
        }

        for (final CollectionManager manager : collectionManagers.values()) {
            manager.init();
        }

        CollectionEditor.refresh();
        CacheManager.getInstance().clearCache();
        BrowserFactory.init();
        EnvironmentVerificator.automated();

        notifyOnRefresh();

        logger.info("environment hard refreshed");
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
    }

    public void unregisterCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager is null");
        }
        collectionManagers.remove(collectionManager.getCollectionName());
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
        for (final CollectionManager m : getRegisteredManagers().values()) {
            m.save();
        }
        BrowserFactory.close();
        CollectionEditor.getInstance().close();
        SettingsEditor.getInstance().close();
        if (SongbookApplication.getMainWindow() != null) {
            SongbookApplication.getMainWindow().close();
        }
        logger.info("environment destroyed");
        Platform.exit();
        System.exit(0);
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

}
