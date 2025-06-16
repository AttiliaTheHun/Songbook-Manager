package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.util.BrowserFactory;
import attilathehun.songbook.util.HTMLGenerator;
import attilathehun.songbook.util.PDFGenerator;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.CollectionEditor;
import attilathehun.songbook.window.SettingsEditor;
import attilathehun.songbook.window.SongbookApplication;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * This class represents the state of the application. It states the current {@link CollectionManager} in use and emits environment-related events.
 */
public final class Environment {
    private static final Logger logger = LogManager.getLogger(Environment.class);
    public static final String SETTINGS_FILE_PATH = "settings.json";
    private static final String EASTER_EXE_FILE_PATH = "easter.exe";
    public static final boolean IS_IT_EASTER_ALREADY = new File(EASTER_EXE_FILE_PATH).exists() && new File(EASTER_EXE_FILE_PATH).length() == 0;
    private static final List<EnvironmentStateListener> listeners = new ArrayList<EnvironmentStateListener>();
    private static final Environment INSTANCE = new Environment();
    public static boolean FLAG_IGNORE_SEGMENTS = false;
    private final Map<String, CollectionManager> collectionManagers = new HashMap<>();

    private CollectionManager selectedCollectionManager;

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

    /**
     * Removes a {@link EnvironmentStateListener}, so it no longer receives Environment-related events.
     *
     * @param listener the listener
     */
    public static void removeListener(final EnvironmentStateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        listeners.remove(listener);
    }

    /**
     * Returns the complete list of registered {@link EnvironmentStateListener}s upon which custom events may be emitted.
     *
     * @return the list of registered listeners
     */
    public static List<EnvironmentStateListener> getListeners() {
        return listeners;
    }

    /**
     * Sends out the {@link EnvironmentStateListener#onRefresh()} event to the registered listeners.
     */
    private static void notifyOnRefresh() {
        for (final EnvironmentStateListener listener : listeners) {
            listener.onRefresh();
        }
    }

    /**
     * Sends out the {@link EnvironmentStateListener#onPageTurnedBack()} event to the registered listeners.
     */
    public static void notifyOnPageTurnedBack() {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onPageTurnedBack();
        }
    }

    /**
     * Sends out the {@link EnvironmentStateListener#onPageTurnedForward()} event to the registered listeners.
     */
    public static void notifyOnPageTurnedForward() {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onPageTurnedForward();
        }
    }

    /**
     * Sends out the {@link EnvironmentStateListener#onSongOneSet(Song)} event to the registered listeners.
     */
    public static void notifyOnSongOneSet(final Song s) {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongOneSet(s);
        }
    }

    /**
     * Sends out the {@link EnvironmentStateListener#onSongTwoSet(Song)} event to the registered listeners.
     */
    public static void notifyOnSongTwoSet(final Song s) {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onSongTwoSet(s);
        }
    }

    /**
     * Sends out the {@link EnvironmentStateListener#onCollectionManagerChanged(CollectionManager, CollectionManager)} event to the registered listeners.
     */
    private static void notifyOnCollectionManagerChanged(final CollectionManager m, final CollectionManager old) {
        for (final EnvironmentStateListener listener : Environment.getListeners()) {
            listener.onCollectionManagerChanged(m, old);
        }
    }

    /**
     * A soft refresh to ensure the environment is working with the latest sure-to-change data. This operation is relatively lightweight as it updates
     * only the components whose working data changes often during runtime, such as the webview source data. For a more thorough refresh use {@link #hardRefresh()}.
     * Emits {@link EnvironmentStateListener#onRefresh()}.
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

                }
            }
        } catch (final NullPointerException npe) {
            logger.error(npe.getMessage(), npe);
        }
        HTMLGenerator.init();
        notifyOnRefresh();
        logger.debug("environment soft refreshed");
    }

    /**
     * Performs a complete refresh of the environment without restarting it. This operation is quite heavy and may result in crashing of the program. It is designed
     * to be used only when absolutely necessary, for example when loading a new songbook at runtime. If you only need to make sure some changes in the configuration
     * or in the collections are applied, use {@link #refresh()} which is much lighter and much less likely to crash the application.
     * Emits {@link EnvironmentStateListener#onRefresh()}.
     */
    public void hardRefresh() {
        SettingsManager.getInstance().load();

        for (final CollectionManager manager : collectionManagers.values()) {
            if (manager.canLoad()) {
                manager.load();
            } else {
                manager.init();
            }
        }

        SettingsEditor.refresh();
        CollectionEditor.refresh();
        BrowserFactory.init();
        EnvironmentVerificator.automated();
        refresh();

        logger.debug("environment hard refreshed");
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
        final CollectionManager oldManager = selectedCollectionManager;
        this.selectedCollectionManager = collectionManager;
        notifyOnCollectionManagerChanged(selectedCollectionManager, oldManager);
    }

    /**
     * Registers a {@link CollectionManager} to the environment so that other components such as the Version Control System or the {@link CollectionEditor} recognize it.
     * The manager must not be null.
     *
     * @param collectionManager the manager
     */
    public void registerCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager cannot be null");
        }
        collectionManagers.put(collectionManager.getCollectionName(), collectionManager);
    }

    /**
     * Unregisters a {@link CollectionManager} from the environment so that other components will no longer utilise it. The manager must not be null.
     *
     * @param collectionManager the manager
     */
    public void unregisterCollectionManager(final CollectionManager collectionManager) {
        if (collectionManager == null) {
            throw new IllegalArgumentException("manager cannot null");
        }
        collectionManagers.remove(collectionManager.getCollectionName());
    }

    /**
     * Returns the list of all registered {@link CollectionManager}s.
     *
     * @return the list of the managers
     */
    public Map<String, CollectionManager> getRegisteredManagers() {
        return collectionManagers;
    }

    /**
     * Saves the settings and collections, frees up any resources held and closes the application. This is preferred over {@link System#exit(int)} and {@link Platform#exit()}
     * as it eventually calls both of these methods.
     */
    public void exit() {
        SettingsManager.getInstance().save();
        for (final CollectionManager m : getRegisteredManagers().values()) {
            m.save();
        }
        BrowserFactory.finish();
        CollectionEditor.getInstance().close();
        SettingsEditor.getInstance().close();
        if (SongbookApplication.getMainWindow() != null) {
            SongbookApplication.getMainWindow().close();
        }
        logger.info("environment destroyed");
        Platform.exit();
        System.exit(0);
    }

}
