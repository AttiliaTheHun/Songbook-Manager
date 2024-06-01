package attilathehun.songbook.export;

import attilathehun.annotation.TODO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.prefs.Preferences;

// further resources https://stackoverflow.com/questions/779793/query-windows-search-from-java
// https://devblogs.microsoft.com/scripting/use-powershell-to-find-installed-software/

@Deprecated
@TODO(description = "Replace hardcoded paths with win path variables like %ProgramFile(x86)%")
public class ChromePathResolver extends BrowserPathResolver {
    public static final String CHROME_PATH_VARIABLE = "export.browser.chrome.path";
    private static final String EXECUTABLE_NAME_WINDOWS = "chrome.exe";
    // Linux
    private static final String EXECUTABLE_NAME_LINUX = "google-chrome";
    private static final Logger logger = LogManager.getLogger(ChromePathResolver.class);
    // Windows
    private static final String USERNAME_PLACEHOLDER = "%UserName%";
    private static final String[] WHERE_COMMAND = {"where", EXECUTABLE_NAME_WINDOWS};
    // private static final String[] GET_COMMAND_COMMAND = {"Get-Command", EXECUTABLE_NAME_WINDOWS}; POWERSHELL ONLY
    private static final String[] READ_REGISTRY_COMMAND = {"reg", "query", "HKEY_CLASSES_ROOT\\ChromeHTML\\shell\\open\\command"};
    private static final String[] READ_REGISTRY_COMMAND_2 = {"reg", "query", "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\", "/s", "/f", "\\chrome.exe", "|", "findstr", "Default"};
    private static final String DEFAULT_PATH_XP = "C:\\Documents and Settings\\%UserName%\\Local Settings\\Application Data\\Google\\Chrome";
    private static final String DEFAULT_PATH_VISTA = "C:\\Users\\%UserName%\\AppDataLocal\\Google\\Chrome";
    private static final String DEFAULT_PATH_WIN7 = "C:\\Program Files (x86)\\Google\\Application";
    private static final String DEFAULT_PATH_WIN10 = "C:\\Program Files (x86)\\Google\\Chrome\\Application";
    private static final String DEFAULT_PATH_WIN10_2 = "C:\\Program Files\\Google\\Chrome\\Application";
    private static final String DEFAULT_PATH_UBUNTU = "/usr/bin/google-chrome-stable";
    private static final String DEFAULT_PATH_UBUNTU_2 = "/usr/bin/google-chrome";
    private static final String[] WHEREIS_COMMAND = {"whereis", EXECUTABLE_NAME_LINUX};
    private static final String[] LOCATE_COMMAND = {"locate", EXECUTABLE_NAME_LINUX};

    private final Preferences preferences = Preferences.userRoot().node(BrowserHandle.class.getName());

    /**
     * Searches for the path of the Google Chrome executable (its parent). If the executable is not found, returns null;
     *
     * @return path to chrome.exe (chrome binary) or null
     */
    public String resolve() throws IOException, InterruptedException {
        final String savedPath = preferences.get(CHROME_PATH_VARIABLE, null);
        if (savedPath != null) {
            if (new File(savedPath).exists()) {
                return savedPath;
            }
            unsave();
        }

        final String executable = (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) ? EXECUTABLE_NAME_WINDOWS : EXECUTABLE_NAME_LINUX;

        // First try if any of the default paths won't do
        File file;
        String path;
        final String[] possiblePaths = initPaths();

        for (final String s : possiblePaths) {
            path = s;
            if (path != null && path.length() != 0) {
                file = new File(Paths.get(path, executable).toString());
                if (file.exists()) {
                    savePath(file.getAbsolutePath());
                    return file.getAbsolutePath();
                }
            }
        }


        // We are gonna try to find the executable on our own now, using our very good friend, the shell

        final String shell = (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) ? SHELL_LOCATION_WINDOWS : SHELL_LOCATION_LINUX;
        final String delimeter = (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) ? SHELL_DELIMETER_WINDOWS : SHELL_DELIMETER_LINUX;

        final String[][] commands = initCommands();

        if (commands.length == 0) {
            return null;
        }

        final Process process = new ProcessBuilder(shell).start();
        final BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        final Scanner stdout = new Scanner(process.getInputStream());

        // first we try to execute all our commands
        for (final String[] command : commands) {
            String commandString = String.join(" ", command);
            logger.debug("Executing: " + commandString);
            stdin.write(commandString);
            stdin.newLine();
            stdin.flush();
        }

        stdin.write("exit");
        stdin.newLine();
        stdin.flush();
        stdin.close();

        // now we will look at their output
        // However we receive the complete stream which includes the commands we executed, so we gotta sort these out
        int counter = 0;
        boolean extracted = false;
        while (stdout.hasNextLine()) {
            extracted = false;
            path = stdout.nextLine();
            if (path.contains(delimeter)) { // means this is the line that executed the command (this is actually our input from before)
                continue;
            }
            if (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) { // this relates to windows-specific commands
                if (counter < 2) { // Skip the microsoft copyright stuff
                    counter++;
                    continue;
                }

                if (path.indexOf("\"") != path.lastIndexOf("\"")) { // means there is at least a pair of double quotes, which could indicate a path with spaces
                    path = path.substring(path.indexOf("\"") + 1, path.lastIndexOf("\""));
                    extracted = true;
                }

                if (path.contains("(Default)") && path.contains("REG_SZ")) { // this is what the registry output entry starts with
                    path = path.substring(path.indexOf("(Default)") + "(Default)".length() + 1);
                    path = path.substring(path.indexOf("REG_SZ") + "REG_SZ".length() + 1);
                    extracted = true;
                }
            } else if (BrowserHandle.getOS().equals(BrowserHandle.OS_LINUX)) {
                //TODO
                // need to check what the commands output on linux and implements similar path extracting as done for windows
            }

            if (!extracted) {
                continue; // let's not risk some unexpected output being used as a file path
            }

            path = path.trim();

            // now we check if the path we got exists
            if (path != null && path.length() != 0) {
                file = new File(path);
                if (file.exists()) {
                    path = file.getAbsolutePath();
                    logger.info("path found " + path);
                    savePath(path);
                    stdout.close();
                    return path;
                }
            }

        }

        // if we got here, we are fucked, we have no idea where the executable is (may or may not mean it does not exist on the machine)
        // we clean up and graciously return null

        stdout.close();

        return null;
    }

    private String[] initPaths() {
        if (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) {
            return new String[]{
                    preferences.get(CHROME_PATH_VARIABLE, null),
                    DEFAULT_PATH_WIN10_2,
                    DEFAULT_PATH_WIN10,
                    DEFAULT_PATH_WIN7,
                    DEFAULT_PATH_VISTA.replace(USERNAME_PLACEHOLDER, System.getProperty("user.name")),
                    DEFAULT_PATH_XP.replace(USERNAME_PLACEHOLDER, System.getProperty("user.name")),
            };
        }

        return new String[]{
                preferences.get(CHROME_PATH_VARIABLE, null),
                DEFAULT_PATH_UBUNTU,
                DEFAULT_PATH_UBUNTU_2
        };
    }

    private String[][] initCommands() {
        if (BrowserHandle.getOS().equals((BrowserHandle.OS_WINDOWS))) {
            return new String[][]{
                    WHERE_COMMAND,
                    //GET_COMMAND_COMMAND,
                    READ_REGISTRY_COMMAND,
                    READ_REGISTRY_COMMAND_2,
            };
        }

        return new String[][]{
                WHEREIS_COMMAND,
                LOCATE_COMMAND
        };
    }

    private void savePath(final String path) {
        preferences.put(CHROME_PATH_VARIABLE, path);
    }

    private void unsave() {
        preferences.remove(CHROME_PATH_VARIABLE);
    }

}
