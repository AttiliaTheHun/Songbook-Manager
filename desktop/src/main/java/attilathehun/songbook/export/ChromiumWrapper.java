package attilathehun.songbook.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static attilathehun.songbook.export.BrowserPathResolver.SHELL_LOCATION_LINUX;
import static attilathehun.songbook.export.BrowserPathResolver.SHELL_LOCATION_WINDOWS;
import static attilathehun.songbook.export.ChromiumPathResolver.*;

public class ChromiumWrapper extends BrowserWrapper {
    private static final Logger logger = LogManager.getLogger(ChromiumWrapper.class);
    private static final Preferences preferences = Preferences.userRoot().node(BrowserWrapper.class.getName());
    private boolean closed = false;

    private final String[] PRINT_COMMAND;

    BufferedWriter stdin;
    Scanner stderr;

    public ChromiumWrapper() throws IOException {
        final String shell = (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) ? SHELL_LOCATION_WINDOWS : SHELL_LOCATION_LINUX;
        final String executable = (BrowserWrapper.getOS().equals(BrowserWrapper.OS_WINDOWS)) ? EXECUTABLE_NAME_WINDOWS : EXECUTABLE_NAME_LINUX;
        Process process = new ProcessBuilder(shell).start();
        stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        stderr = new Scanner(process.getErrorStream());
        PRINT_COMMAND = Stream.concat(Arrays.stream(new String[]{executable}), Arrays.stream(DEFAULT_PRINT_ARGS))
                .toArray(String[]::new);
        exec(String.format("cd %s", preferences.get(CHROMIUM_PATH_VARIABLE, "")));
    }

    @Override
    public void print(String inputPath, String outputPath) throws IOException {
        if (closed) {
            throw new IllegalStateException("Wrapper already closed");
        }
        if (inputPath == null || inputPath.length() == 0 || outputPath == null || outputPath.length() == 0) {
            throw new IllegalArgumentException("Paths must not be null nor empty");
        }
        String command = String.join(" ", PRINT_COMMAND);

        exec(String.format(command, outputPath, inputPath));

        if (stderr.hasNextLine()) {
            //Environment.showErrorMessage("Error", "Errur", stderr.nextLine());
            System.out.println(stderr.nextLine());
        }

    }

    @Override
    public void close() throws IOException{
        if (closed) {
            throw new IllegalStateException("Wrapper already closed");
        }
        exec("exit");
        stdin.close();
        stderr.close();
        closed = true;
    }

    private void exec(String command) throws IOException {
        stdin.write(command);
        stdin.newLine();
        stdin.flush();
    }
}
