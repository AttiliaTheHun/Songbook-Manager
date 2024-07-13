package attilathehun.songbook.environment;

public interface SettingsListener {

    /**
     * Emitted when value of a setting has been updated via {@link SettingsManager#set(String, Object)}.
     *
     * @param name name of the affected setting
     * @param old state of the setting before update
     * @param _new state of the setting after update
     */
    public void onSettingChanged(String name, Setting old, Setting _new);
}
