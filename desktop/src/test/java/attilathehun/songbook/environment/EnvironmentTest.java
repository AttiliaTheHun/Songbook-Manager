package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.collection.Song;
import attilathehun.songbook.collection.StandardCollectionManager;
import attilathehun.songbook.collection.TestCollectionManager;
import org.junit.BeforeClass;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnvironmentTest implements EnvironmentStateListener {
    private boolean FLAG_REFRESH_EVENT_RECEIVED = false;
    private boolean FLAG_PAGE_BACK_EVENT_RECEIVED = false;
    private boolean FLAG_PAGE_FORWARD_EVENT_RECEIVED = false;
    private boolean FLAG_MANAGER_CHANGED_EVENT_RECEIVED = false;
    private boolean FLAG_SONG_ONE_EVENT_RECEIVED = false;
    private boolean FLAG_SONG_TWO_EVENT_RECEIVED = false;

    private SettingsManager settingsManager;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public static void followUp() {
        assertEquals(0, Environment.getListeners().size());
    }

    @Test
    public void testGetInstanceIsNotNull() {
        assertNotNull(Environment.getInstance());
    }

    @Test
    public void testAddListener_whenListenerNull() {
        assertThrows(IllegalArgumentException.class, () -> Environment.addListener(null));
    }


    @Test
    public void testRemoveListener_whenListenerNull() {
        assertThrows(IllegalArgumentException.class, () -> Environment.removeListener(null));
    }

    @Test
    public void testGetListeners() {
        Environment.addListener(this);
        assertEquals(1, Environment.getListeners().size());
        Environment.removeListener(this);
    }

    @Test
    public void testNotifyOnPageTurnedBack() {
        FLAG_PAGE_BACK_EVENT_RECEIVED = false;
        Environment.addListener(this);
        Environment.notifyOnPageTurnedBack();
        assertTrue(FLAG_PAGE_BACK_EVENT_RECEIVED);
        Environment.removeListener(this);
    }

    @Test
    public void testNotifyOnPageTurnedForward() {
        FLAG_PAGE_FORWARD_EVENT_RECEIVED = false;
        Environment.addListener(this);
        Environment.notifyOnPageTurnedForward();
        assertTrue(FLAG_PAGE_FORWARD_EVENT_RECEIVED);
        Environment.removeListener(this);
    }

    @Test
    public void testNotifyOnSongOneSet() {
        FLAG_SONG_ONE_EVENT_RECEIVED = false;
        Environment.addListener(this);
        Environment.notifyOnSongOneSet(null);
        assertTrue(FLAG_SONG_ONE_EVENT_RECEIVED);
        Environment.removeListener(this);
    }

    @Test
    public void testNotifyOnSongTwoSet() {
        FLAG_SONG_TWO_EVENT_RECEIVED = false;
        Environment.addListener(this);
        Environment.notifyOnSongTwoSet(null);
        assertTrue(FLAG_SONG_TWO_EVENT_RECEIVED);
        Environment.removeListener(this);
    }

    @Test
    public void testGetCollectionManager_thatIsNotNull() throws NoSuchFieldException, IllegalAccessException {
        Field field = Environment.getInstance().getClass().getDeclaredField("selectedCollectionManager");
        field.setAccessible(true);
        field.set(Environment.getInstance(), null);
        field.setAccessible(false);

        StandardCollectionManager standardMock = mock(StandardCollectionManager.class);

        try (MockedStatic<StandardCollectionManager> standardManagerMock = mockStatic(StandardCollectionManager.class)) {
            standardManagerMock.when(StandardCollectionManager::getInstance).thenReturn(standardMock);

            assertNotNull(Environment.getInstance().getCollectionManager());
        }
    }

    @Test
    public void testGetCollectionManager_returnValue() {
        TestCollectionManager manager1 = new TestCollectionManager();
        TestCollectionManager manager2 = new TestCollectionManager();

        Environment.getInstance().setCollectionManager(manager1);
        assertEquals(manager1, Environment.getInstance().getCollectionManager());
        Environment.getInstance().setCollectionManager(manager2);
        assertEquals(manager2, Environment.getInstance().getCollectionManager());
    }

    @Test
    public void testGetCollectionManager_thatDoesNotLookForStandardCollectionManagerWhenCollectionManagerWasSet() {
        StandardCollectionManager standardMock = mock(StandardCollectionManager.class);

        TestCollectionManager manager = new TestCollectionManager();

        try (MockedStatic<StandardCollectionManager> standardManagerMock = mockStatic(StandardCollectionManager.class)) {
            standardManagerMock.when(StandardCollectionManager::getInstance).thenReturn(standardMock);

            Environment.getInstance().setCollectionManager(manager);
            Environment.getInstance().getCollectionManager();

            standardManagerMock.verify(StandardCollectionManager::getInstance, times(0));
        }
    }

    @Test
    public void testSetCollectionManager_whenManagerIsNull() {
        assertThrows(IllegalArgumentException.class, () -> Environment.getInstance().setCollectionManager(null));
    }

    @Test
    public void testSetCollectionManager_whenManagerIsNotNull() {
        assertDoesNotThrow(() -> Environment.getInstance().setCollectionManager(new TestCollectionManager()));
    }

    @Test
    public void testSetCollectionManager_thatOnCollectionManagerChangedIsEmitted() {
        FLAG_MANAGER_CHANGED_EVENT_RECEIVED = false;
        CollectionManager manager = new TestCollectionManager();
        Environment.addListener(this);
        Environment.getInstance().setCollectionManager(manager);
        assertTrue(FLAG_MANAGER_CHANGED_EVENT_RECEIVED);
        Environment.removeListener(this);
    }

    @Test
    public void testSetCollectionManager_thatPreviousManagerListenerUnregistered() {
        FLAG_MANAGER_CHANGED_EVENT_RECEIVED = false;
        TestCollectionManager manager1 = new TestCollectionManager();
        TestCollectionManager manager2 = new TestCollectionManager();

        Environment.getInstance().setCollectionManager(manager1);
        assertEquals(1, manager1.getListeners().size());

        Environment.getInstance().setCollectionManager(manager2);
        assertEquals(0, manager1.getListeners().size());
    }

    @Test
    public void testRegisterCollectionManager_whenManagerIsNull() {
        assertThrows(IllegalArgumentException.class, () -> Environment.getInstance().registerCollectionManager(null));
    }

    @Test
    public void testRegisterCollectionManager_whenManagerIsNotNull() {
        settingsManager = mock(SettingsManager.class);

        try (MockedStatic<SettingsManager> settingsManagerMock = mockStatic(SettingsManager.class)) {
            settingsManagerMock.when(SettingsManager::getInstance).thenReturn(settingsManager);
        
            TestCollectionManager manager = new TestCollectionManager();
            assertDoesNotThrow(() -> Environment.getInstance().registerCollectionManager(manager));
            Environment.getInstance().unregisterCollectionManager(manager);
        }
    }

    @Test
    public void testRegisterCollectionManager_thatManagerIsAddedToTheList() {
        settingsManager = mock(SettingsManager.class);

        try (MockedStatic<SettingsManager> settingsManagerMock = mockStatic(SettingsManager.class)) {
            settingsManagerMock.when(SettingsManager::getInstance).thenReturn(settingsManager);

            TestCollectionManager manager = new TestCollectionManager();
            Environment.getInstance().registerCollectionManager(manager);
            assertEquals(1, Environment.getInstance().getRegisteredManagers().size());
            Environment.getInstance().unregisterCollectionManager(manager);
        }
    }

    @Test
    public void testRegisterCollectionManager_thatSettingsAreSaved() {
        settingsManager = mock(SettingsManager.class);

        try (MockedStatic<SettingsManager> settingsManagerMock = mockStatic(SettingsManager.class)) {
            settingsManagerMock.when(SettingsManager::getInstance).thenReturn(settingsManager);

            TestCollectionManager manager = new TestCollectionManager();
            Environment.getInstance().registerCollectionManager(manager);
            verify(settingsManager, times(1)).save();
            Environment.getInstance().unregisterCollectionManager(manager);
        }
    }

    @Test
    public void testUnregisterCollectionManager_whenManagerIsNull() {
        assertThrows(IllegalArgumentException.class, () -> Environment.getInstance().unregisterCollectionManager(null));
    }

    @Test
    public void testUnregisterCollectionManager_whenManagerIsNotNull() {
        settingsManager = mock(SettingsManager.class);

        try (MockedStatic<SettingsManager> settingsManagerMock = mockStatic(SettingsManager.class)) {
            settingsManagerMock.when(SettingsManager::getInstance).thenReturn(settingsManager);

            TestCollectionManager manager = new TestCollectionManager();
            Environment.getInstance().registerCollectionManager(manager);
            assertDoesNotThrow(() -> Environment.getInstance().unregisterCollectionManager(manager));
        }

    }

    @Test
    public void testUnregisterCollectionManager_thatManagerIsRemovedFromTheList() {
        settingsManager = mock(SettingsManager.class);

        try (MockedStatic<SettingsManager> settingsManagerMock = mockStatic(SettingsManager.class)) {
            settingsManagerMock.when(SettingsManager::getInstance).thenReturn(settingsManager);

            TestCollectionManager manager = new TestCollectionManager();
            Environment.getInstance().registerCollectionManager(manager);
            assertEquals(1, Environment.getInstance().getRegisteredManagers().size());
            Environment.getInstance().unregisterCollectionManager(manager);
            assertEquals(0, Environment.getInstance().getRegisteredManagers().size());
        }
    }

    @Test
    public void testUnregisterCollectionManager_thatSettingsAreSaved() {
        settingsManager = mock(SettingsManager.class);

        try (MockedStatic<SettingsManager> settingsManagerMock = mockStatic(SettingsManager.class)) {
            settingsManagerMock.when(SettingsManager::getInstance).thenReturn(settingsManager);

            TestCollectionManager manager = new TestCollectionManager();
            Environment.getInstance().registerCollectionManager(manager);
            verify(settingsManager, times(1)).save();
            Environment.getInstance().unregisterCollectionManager(manager);
            verify(settingsManager, times(2)).save();
        }

    }

    @Test
    public void testGetRegisteredManagers() {
        assertNotNull(Environment.getInstance().getRegisteredManagers());
        TestCollectionManager manager = new TestCollectionManager();
        Environment.getInstance().registerCollectionManager(manager);
        assertEquals(1, Environment.getInstance().getRegisteredManagers().size());
        Environment.getInstance().unregisterCollectionManager(manager);
        assertEquals(0, Environment.getInstance().getRegisteredManagers().size());
    }

    @Test
    public void testGetDefaultSettings_thatIsNotNull() {
        assertNotNull(Environment.getInstance().getDefaultSettings());
    }

    @Test
    public void testGetSettings_thatIsNotNull() {
        assertNotNull(Environment.getInstance().getSettings());
    }

    @Test
    public void testSetSettings_whenSettingsAreNull() {
        assertThrows(IllegalArgumentException.class, () -> Environment.getInstance().setSettings(null));
    }

    @Test
    public void testSetSettings_whenSettingsAreNotNull() {
        assertDoesNotThrow(() -> Environment.getInstance().setSettings(new Environment.EnvironmentSettings()));
    }

    @Test
    public void testSetSettings_resultValue() {
        Environment.EnvironmentSettings settings = new Environment.EnvironmentSettings();
        Environment.getInstance().setSettings(settings);
        assertEquals(settings, Environment.getInstance().getSettings());
    }

    @Nested
    public static class EnvironmentSettingsTest {

        @Test
        public void testGet_whenSettingNotDefined() {
            Environment.EnvironmentSettings settings = new Environment.EnvironmentSettings();
            assertNull(settings.get("UNDEFINED_SETTING"));
        }

        @Test
        public void testGet_whenSettingDefined() {
            Environment.EnvironmentSettings settings = new Environment.EnvironmentSettings();
            String setting = "DEFINED_SETTING";
            String value = "null but in string";
            settings.put(setting, value);
            assertEquals(value, settings.get(setting));
        }

        @Test
        public void testGet_whenSettingRedefined() {
            Environment.EnvironmentSettings settings = new Environment.EnvironmentSettings();
            String setting = "DEFINED_SETTING";
            String value1 = "null but in string";
            String value2 = "undefined but in string";
            String value3 = "0 but in string";
            settings.put(setting, value1);
            assertEquals(value1, settings.get(setting));
            settings.put(setting, value2);
            assertEquals(value2, settings.get(setting));
            settings.put(setting, value3);
            assertEquals(value3, settings.get(setting));
        }


    }


    @Override
    public void onRefresh() {
        FLAG_REFRESH_EVENT_RECEIVED = true;
    }

    @Override
    public void onPageTurnedBack() {
        FLAG_PAGE_BACK_EVENT_RECEIVED = true;
    }

    @Override
    public void onPageTurnedForward() {
        FLAG_PAGE_FORWARD_EVENT_RECEIVED = true;
    }

    @Override
    public void onSongOneSet(final Song s) {
        FLAG_SONG_ONE_EVENT_RECEIVED = true;
    }

    @Override
    public void onSongTwoSet(final Song s) {
        FLAG_SONG_TWO_EVENT_RECEIVED = true;
    }

    @Override
    public void onCollectionManagerChanged(final CollectionManager m) {
        FLAG_MANAGER_CHANGED_EVENT_RECEIVED = true;
    }
}