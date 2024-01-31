package attilathehun.songbook.environment;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.plugin.Plugin;
import attilathehun.songbook.plugin.PluginManager;
import attilathehun.songbook.vcs.VCSAdmin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;

public final class Settings implements Serializable {

    private static final Logger logger = LogManager.getLogger(Settings.class);

    public Environment.EnvironmentSettings environment;
    public HashMap<String, CollectionManager.CollectionSettings> collections;
    public UserSettings user;
    public SongbookSettings songbook;
    public HashMap<String, Plugin.PluginSettings> plugins;
    public VCSAdmin.VCSSettings vcs;

}
