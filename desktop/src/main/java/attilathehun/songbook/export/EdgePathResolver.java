package attilathehun.songbook.export;

import attilathehun.annotation.TODO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.prefs.Preferences;

@TODO(description = "Replace hardcoded paths with win path variables like %ProgramFile(x86)%")
public class EdgePathResolver extends BrowserPathResolver {
    private static final Logger logger = LogManager.getLogger(EdgePathResolver.class);

    //Windows
    private static final String EXECUTABLE_NAME_WINDOWS = "msedge.exe";
    public static final String EDGE_PATH_VARIABLE = "export.browser.edge.path";
    private static final String[] WHERE_COMMAND = {"where", EXECUTABLE_NAME_WINDOWS};
    private static final String[] READ_REGISTRY_COMMAND = {"reg", "query", "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\", "/s", "/f", "\\msedge.exe", "|", "findstr", "Default"};
    private static final String DEFAULT_PATH_WIN10 = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application";
    private static final String DEFAULT_PATH_WIN10_2 = "C:\\Program Files\\Microsoft\\Edge\\Application";

    // Linux

    private static final String EXECUTABLE_NAME_LINUX = "microsoft-edge";
    private static final String DEFAULT_PATH_UBUNTU = "/usr/bin/microsoft-edge-stable";
    private static final String DEFAULT_PATH_UBUNTU_2 = "/usr/bin/microsoft-edge";
    private static final String[] WHEREIS_COMMAND = {"whereis", EXECUTABLE_NAME_LINUX};
    private static final String[] LOCATE_COMMAND = {"locate", EXECUTABLE_NAME_LINUX};

    private final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());


    @Override
    public String resolve() throws IOException {
        return null;
    }

    private String[] initPaths() {
        if (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) {
            return new String[]{
                    preferences.get(EDGE_PATH_VARIABLE, null),
                    DEFAULT_PATH_WIN10_2,
                    DEFAULT_PATH_WIN10
            };
        }

        return new String[]{
                preferences.get(EDGE_PATH_VARIABLE, null),
                DEFAULT_PATH_UBUNTU,
                DEFAULT_PATH_UBUNTU_2
        };
    }

    private String[][] initCommands() {
        if (BrowserWrapper.getOS().equals((BrowserWrapper.OS_WINDOWS))) {
            return new String[][]{
                    WHERE_COMMAND,
                    READ_REGISTRY_COMMAND,
            };
        }

        return new String[][]{
                WHEREIS_COMMAND,
                LOCATE_COMMAND
        };
    }

    private void savePath(String path) {
        preferences.put(EDGE_PATH_VARIABLE, path);
    }

}
