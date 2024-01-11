package attilathehun.songbook.export;

import java.io.*;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

// further resources https://stackoverflow.com/questions/779793/query-windows-search-from-java
// https://devblogs.microsoft.com/scripting/use-powershell-to-find-installed-software/

class ChromePathResolver extends BrowserPathResolver {
    // Windows
    private static final String USERNAME_PLACEHOLDER = "%UserName%";
    private static final String EXECUTABLE_NAME = "chrome.exe";
    public static final String CHROME_PATH_VARIABLE = "export.browser.chrome.path";
    private static final String[] WHERE_COMMAND = {"where", EXECUTABLE_NAME};
    private static final String[] GET_COMMAND_COMMAND = {"Get-Command", EXECUTABLE_NAME};
    private static final String[] READ_REGISTRY_COMMAND = {"reg", "query", "HKEY_CLASSES_ROOT\\ChromeHTML\\shell\\open\\command"};
    private static final String[] READ_REGISTRY_COMMAND_2 = {"reg", "query", "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\", "/s", "/f", "\\chrome.exe", "|", "findstr", "Default"};
    private static final String DEFAULT_PATH_XP = "C:\\Documents and Settings\\%UserName%\\Local Settings\\Application Data\\Google\\Chrome";
    private static final String DEFAULT_PATH_VISTA = "C:\\Users\\%UserName%\\AppDataLocal\\Google\\Chrome";
    private static final String DEFAULT_PATH_WIN7 = "C:\\Program Files (x86)\\Google\\Application";
    private static final String DEFAULT_PATH_WIN10 = "C:\\Program Files (x86)\\Google\\Chrome\\Application";
    private static final String DEFAULT_PATH_WIN10_2 = "C:\\Program Files\\Google\\Chrome\\Application";
    // Linux

    private static final String DEFAULT_PATH_UBUNTU = "/usr/bin/google-chrome-stable";
    private static final String DEFAULT_PATH_UBUNTU_2 = "/usr/bin/google-chrome";
    private static final String[] WHEREIS_COMMAND = {"whereis", "google-chrome"};
    private static final String[] LOCATE_COMMAND = {"locate", "google-chrome"};

    private final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());

    /**
     * Searches for the path of the Google Chrome executable (its parent). If the executable is not found, returns null;
     * @return path to chrome.exe or null
     */
    public String resolve() throws IOException {

        File file;
        String path;
        String[] possiblePaths = initPaths();
        for (String s : possiblePaths) {
            path = s;
            if (path != null && path.length() != 0) {
                file = new File(Paths.get(path, EXECUTABLE_NAME).toString());
                if (file.exists()) {
                    savePath(path);
                    return path;
                }
            }
        }

        Process process;
        BufferedReader out;
        StringBuilder buffer;
        String s;

        String[][] commands = initCommands();
        for (String[] command : commands) {
            process = Runtime.getRuntime().exec(command);
            for (int i = 0; i < 4; i++) {
                System.out.println("i: " + i);

                out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                buffer = new StringBuilder(4000);

                while ((s = out.readLine()) != null) {
                    buffer.append(s).append("\n");
                }
                String result = out.toString();
                System.out.println(result);

                path = buffer.toString();
                System.out.println(path);
                if (path != null && path.length() != 0) {
                    file = new File(Paths.get(path, EXECUTABLE_NAME).toString());
                    if (file.exists()) {
                        savePath(path);
                        out.close();
                        return path;
                    }
                }

                out.close();
            }
        }


        return null;
    }

    private String[] initPaths() {
        String[] possiblePaths = {
                preferences.get(CHROME_PATH_VARIABLE, null),
                DEFAULT_PATH_WIN10_2,
                DEFAULT_PATH_WIN10,
                DEFAULT_PATH_WIN7,
                DEFAULT_PATH_VISTA.replace(USERNAME_PLACEHOLDER, System.getProperty("user.name")),
                DEFAULT_PATH_XP.replace(USERNAME_PLACEHOLDER, System.getProperty("user.name")),
                DEFAULT_PATH_UBUNTU,
                DEFAULT_PATH_UBUNTU_2
        };

        return possiblePaths;
    }

    private String[][] initCommands() {
        String[][] commands = {
                WHERE_COMMAND,
                GET_COMMAND_COMMAND,
                READ_REGISTRY_COMMAND,
                READ_REGISTRY_COMMAND_2,
                WHEREIS_COMMAND,
                LOCATE_COMMAND
        };

        return commands;
    }

    private void savePath(String path) {
        preferences.put(CHROME_PATH_VARIABLE, path);
    }

}
