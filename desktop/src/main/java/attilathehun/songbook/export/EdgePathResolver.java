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

@TODO(description = "Replace hardcoded paths with win path variables like %ProgramFile(x86)%")
public class EdgePathResolver extends BrowserPathResolver {
    public static final String EDGE_PATH_VARIABLE = "export.browser.edge.path";
    //Windows
    static final String EXECUTABLE_NAME_WINDOWS = "msedge.exe";
    static final String EXECUTABLE_NAME_LINUX = "microsoft-edge";
    private static final Logger logger = LogManager.getLogger(EdgePathResolver.class);
    private static final String[] WHERE_COMMAND = {"where", EXECUTABLE_NAME_WINDOWS};
    private static final String[] READ_REGISTRY_COMMAND = {"reg", "query", "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\", "/s", "/f", "\\msedge.exe", "|", "findstr", "Default"};
    private static final String DEFAULT_PATH_WIN10 = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application";

    // Linux
    private static final String DEFAULT_PATH_WIN10_2 = "C:\\Program Files\\Microsoft\\Edge\\Application";
    private static final String DEFAULT_PATH_UBUNTU = "/usr/bin/microsoft-edge-stable";
    private static final String DEFAULT_PATH_UBUNTU_2 = "/usr/bin/microsoft-edge";
    private static final String[] WHEREIS_COMMAND = {"whereis", EXECUTABLE_NAME_LINUX};
    private static final String[] LOCATE_COMMAND = {"locate", EXECUTABLE_NAME_LINUX};

    private final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());


    @Override
    public String resolve() throws IOException {
        final String executable = (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) ? EXECUTABLE_NAME_WINDOWS : EXECUTABLE_NAME_LINUX;

        // First try if any of the default paths won't do
        File file;
        String path;
        String[] possiblePaths = initPaths();

        for (String s : possiblePaths) {
            path = s;
            if (path != null && path.length() != 0) {
                file = new File(Paths.get(path, executable).toString());
                if (file.exists()) {
                    savePath(file.getParent());
                    return file.getParent();
                }
            }
        }


        // We are gonna try to find the executable on our own now, using our very good friend, the shell

        final String shell = (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) ? SHELL_LOCATION_WINDOWS : SHELL_LOCATION_LINUX;
        final String delimeter = (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) ? SHELL_DELIMETER_WINDOWS : SHELL_DELIMETER_LINUX;

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
        boolean extracted = false;
        while (stdout.hasNextLine()) {
            extracted = false;
            path = stdout.nextLine();
            if (path.contains(delimeter)) { // means this is the line that executed the command (this is actually our input from before)
                continue;
            }
            if (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) { // this relates to windows-specific commands
                if (counter < 2) { // Skip the microsoft copyright stuff
                    counter++;
                    continue;
                }

                if (path.indexOf("\"") != path.lastIndexOf("\"")) { // means there is at least a duo of double quotes, which could indicate a path with spaces
                    path = path.substring(path.indexOf("\"") + 1, path.lastIndexOf("\""));
                    extracted = true;
                }

                if (path.contains("(Default)") && path.contains("REG_SZ")) { // this is what the registry output entry starts with
                    path = path.substring(path.indexOf("(Default)") + "(Default)".length() + 1);
                    path = path.substring(path.indexOf("REG_SZ") + "REG_SZ".length() + 1);
                    extracted = true;
                }
            } else if (BrowserWrapper.getOS().equals(BrowserWrapper.OS_LINUX)) {
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
