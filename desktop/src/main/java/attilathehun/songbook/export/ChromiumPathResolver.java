package attilathehun.songbook.export;

import attilathehun.annotation.TODO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

@TODO(description = "Replace hardcoded paths with win path variables like %ProgramFile(x86)%")
@Deprecated
// TODO I do not have chromium installed so I can not test whether this is working, or the usual install paths
public class ChromiumPathResolver extends BrowserPathResolver {
    public static final String CHROMIUM_PATH_VARIABLE = "export.browser.chromium.path";
    //windows
    private static final String EXECUTABLE_NAME_WINDOWS = "chrome.exe"; // yup, it is same as with chrome
    private static final String EXECUTABLE_NAME_LINUX = "chromium-browser";
    private static final Logger logger = LogManager.getLogger(ChromiumPathResolver.class);
    private static final String[] WHERE_COMMAND = {"where", EXECUTABLE_NAME_WINDOWS};
    private static final String DEFAULT_PATH_WIN11 = "C:\\Program Files (x86)\\Chromium";
    //linux
    private static final String DEFAULT_PATH_WIN11_2 = "C:\\Program Files (x86)\\Chromium\\Application";
    private static final String EXECUTABLE_NAME_LINUX_2 = "chromium";
    private static final String DEFAULT_PATH_UBUNTU = "/usr/bin/chromium-browser";
    private static final String DEFAULT_PATH_UBUNTU_2 = "/usr/bin/chromium";
    private static final String[] WHEREIS_COMMAND = {"whereis", EXECUTABLE_NAME_LINUX};
    private static final String[] LOCATE_COMMAND = {"locate", EXECUTABLE_NAME_LINUX};
    private static final String[] WHEREIS_COMMAND_2 = {"whereis", EXECUTABLE_NAME_LINUX_2};
    private static final String[] LOCATE_COMMAND_2 = {"locate", EXECUTABLE_NAME_LINUX_2};

    private final Preferences preferences = Preferences.userRoot().node(BrowserHandle.class.getName());

    /**
     * This method is untested for I do not have chromium...
     *
     * @return noone knows (may be null)
     * @throws IOException
     */
    @Override
    public String resolve() throws IOException {
        final String savedPath = preferences.get(CHROMIUM_PATH_VARIABLE, null);
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
        String[] possiblePaths = initPaths();

        for (String s : possiblePaths) {
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
        // since this is untested, there is no reason to actually run it, let's just leave it here for later
        /*
        final String shell = (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) ? SHELL_LOCATION_WINDOWS : SHELL_LOCATION_LINUX;
        final String delimeter = (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) ? SHELL_DELIMETER_WINDOWS : SHELL_DELIMETER_LINUX;

        String[][] commands = initCommands();

        if (commands.length == 0) {
            return null;
        }

        Process process = new ProcessBuilder(shell).start();
        BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        Scanner stdout = new Scanner(process.getInputStream());

        // first we try to execute all our commands
        for (String[] command : commands) {
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

        while (stdout.hasNextLine()) {

            path = stdout.nextLine();
            if (path.contains(delimeter)) { // means this is the line that executed the command (this is actually our input from before)
                continue;
            }
            if (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) { // this relates to windows-specific commands
                if (counter < 2) { // Skip the microsoft copyright stuff
                    counter++;
                    continue;
                }
            } else if (BrowserHandle.getOS().equals(BrowserHandle.OS_LINUX)) {
                //TODO
                // need to check what the commands output on linux and implements similar path extracting as done for windows
            }

            path = path.trim();

            // now we check if the path we got exists
            if (path != null && path.length() != 0) {
                file = new File(path);
                if (file.exists()) {
                    path = file.getParent();
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

        */

        return null;
    }

    private String[] initPaths() {
        if (BrowserHandle.getOS().equals(BrowserHandle.OS_WINDOWS)) {
            return new String[]{
                    preferences.get(CHROMIUM_PATH_VARIABLE, null),
                    DEFAULT_PATH_WIN11_2,
                    DEFAULT_PATH_WIN11
            };
        }

        return new String[]{
                preferences.get(CHROMIUM_PATH_VARIABLE, null),
                DEFAULT_PATH_UBUNTU,
                DEFAULT_PATH_UBUNTU_2
        };
    }

    private String[][] initCommands() {
        if (BrowserHandle.getOS().equals((BrowserHandle.OS_WINDOWS))) {
            return new String[][]{
                    WHERE_COMMAND,
            };
        }

        return new String[][]{
                WHEREIS_COMMAND,
                LOCATE_COMMAND,
                WHEREIS_COMMAND_2,
                LOCATE_COMMAND_2
        };
    }

    private void savePath(final String path) {
        preferences.put(CHROMIUM_PATH_VARIABLE, path);
    }

    private void unsave() {
        preferences.remove(CHROMIUM_PATH_VARIABLE);
    }

}
