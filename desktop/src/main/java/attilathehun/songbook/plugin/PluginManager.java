package attilathehun.songbook.plugin;

import attilathehun.songbook.misc.Misc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes(value = "attilathehun.songbook.plugin.AutoRegister")
public final class PluginManager extends AbstractProcessor {

    private static final Logger logger = LogManager.getLogger(PluginManager.class);

    private static final PluginManager instance = new PluginManager();

    private final Map<String, Plugin> plugins = new HashMap<>();


    private PluginManager() {

    }


    @Deprecated
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        System.out.println("processing annotations");
        System.out.println(Arrays.toString(annotations.toArray()));
        for (final TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            System.out.println(Arrays.toString(annotatedElements.toArray()));
            if (annotation.getQualifiedName().contentEquals(AutoRegister.class.getName())) {
                for (final Element element : annotatedElements) {
                    if (element.getEnclosedElements().get(0) instanceof Plugin) {
                        ((Plugin) element.getEnclosedElements().get(0)).register();
                    }
                }
            }

        }

        return true;
    }

    public void init() {
        try {
            autoRegisterPlugins();
        } catch (Exception e) {
            logger.error("automatic plugin registration failed", e);
            e.printStackTrace();
        }

    }

    /**
     * Automatically registers {@link Plugin}s that are marked for registration. This action is meant to be performed only
     * once, at startup.
     */
    private void autoRegisterPlugins() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
       /* Iterable<Class<?>> autoRegisterPlugins = ClassIndex.getAnnotated(AutoRegister.class);

        for (final Class c : autoRegisterPlugins) {
            System.out.println(c.getSimpleName());
            if (c.getSuperclass() != null && c.getSuperclass().equals(Plugin.class)) {
                ((Plugin)c.getMethod("getInstance").invoke(null)).register();
            }
        }*/
    }

    @Deprecated(forRemoval = true)
    public static void loadPlugins() {
        DynamicSonglist.getInstance();
        Frontpage.getInstance();
        Export.getInstance();
        SML.getInstance();
    }

    public static PluginManager getInstance() {
        return instance;
    }

    /**
     * Adds a plugin to the plugin collection. Unless registered, a plugin will not be recognized my the most part of the
     * program as plugins are supposed to be accessed through {@link #getPlugin(String)}. A plugin can not be unregistered,
     * but can be set as disabled which the rest of the program should abide by not using this plugin.
     *
     * @param plugin the wannabe registered plugin
     */
    public void registerPlugin(final Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
        logger.info("Plugin registered: " + plugin.getName());
    }

    /**
     * Returns a plugin, if such a plugin is registered.
     *
     * @param name target plugin name
     * @return the plugin or null
     */
    public Plugin getPlugin(final String name) {
        return plugins.get(name);
    }

    /**
     * Collects settings of all the registered plugins to a name-settings mapping. This mapping can then be serialised and
     * saved.
     *
     * @return plugin settings collection
     */
    public HashMap<String, Plugin.PluginSettings> getSettingsMap() {
        HashMap<String, Plugin.PluginSettings> map = new HashMap<>();
        for (Plugin p : plugins.values()) {
            map.put(p.getName(), p.getSettings());
        }
        return map;
    }

    /**
     * Injects settings to the registered plugins. Settings are provided as a pluginName-pluginSettings mapping.
     *
     * @param settingsMap the new plugin settings
     */
    public void loadPluginSettings(final HashMap<String, Plugin.PluginSettings> settingsMap) {
        if (settingsMap == null) {
            throw new IllegalArgumentException("plugin settings map must never be null");
        }
        for (Plugin p : plugins.values()) {
            if (settingsMap.get(p.getName()) != null) {
                p.setSettings(settingsMap.get(p.getName()));
            }

        }
    }

}
