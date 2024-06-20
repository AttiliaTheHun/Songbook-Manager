package attilathehun.songbook.util;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Margin;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

// TODO work this out for Linux (shell search and chromium download)
public class BrowserFactory {
    private static final Logger logger = LogManager.getLogger(BrowserFactory.class);
    public static final String OS_WINDOWS = "Windows";
    public static final String OS_LINUX = "Linux";
    private static final String CHROMIUM_WINDOWS_DOWNLOAD_LINK = "https://storage.googleapis.com/chromium-browser-snapshots/Win_x64/1308506/chrome-win.zip";
    private static final String CHROMIUM_LINUX_DOWNLOAD_LINK = "";

    private static Playwright playwright;
    private static Browser browserInstance;
    private final String os = getOS();

    public BrowserFactory() {

    }

    /**
     * Initializes the current installation and settings for the runtime. The class will not work properly until this method is called.
     */
    public static void init() {
        final BrowserFactory factory = new BrowserFactory();
        if (!(Boolean) SettingsManager.getInstance().getValue("EXPORT_ENABLED")) {
            return;
        }

        final String path = factory.resolve();
        if (path == null) {
            if (!factory.requestInstallChromium()) {
                SettingsManager.getInstance().set("EXPORT_ENABLED", false);
                new AlertDialog.Builder().setTitle("Installation aborted").setMessage("The installation has been cancelled and exporting has been deactivated.")
                        .setIcon(AlertDialog.Builder.Icon.ERROR).addOkButton().build().open();
                return;
            }
        }
        logger.info("chromium browser executable found at " + path);
        playwright = factory.getPlaywright();
        if ((Boolean) SettingsManager.getInstance().getValue("EXPORT_KEEP_BROWSER_INSTANCE")) {
            browserInstance = factory.getBrowserInstance(playwright);
            logger.debug("default browser instance initialised");
        } else if (browserInstance != null) {
            browserInstance.close();
            browserInstance = null;
        }
    }

    /**
     * Creates a {@link Playwright} instance with the necessary create options. Returns null if things go wrong.
     *
     * @return Playwright instance or null
     */
    public Playwright getPlaywright() {
        final HashMap<String, String> playwrightEnv = new HashMap<>();
        playwrightEnv.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
        try {
            return Playwright.create(new Playwright.CreateOptions().setEnv(playwrightEnv));

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Creates a headless {@link Browser} instance bound to the chromium browser executable available to the program. The Browser is created upon the
     * given {@link Playwright} object and lives within the same scope (especially thread-wise).
     *
     * @param playwright parent Playwright instance to the browser (non-null)
     * @return a Browser instance
     */
    public Browser getBrowserInstance(final Playwright playwright) {
        if (playwright == null) {
            throw new IllegalArgumentException("playwright can not be null");
        }

        final BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true)
                .setExecutablePath(Path.of((String) SettingsManager.getInstance().getValue("EXPORT_BROWSER_EXECUTABLE_PATH")));

        return playwright.chromium().launch(options);
    }

    /**
     * Returns the static {@link Browser} instance if one exists, creates a new instance if it does not. This method should NEVER be called from a separate thread.
     *
     * @return a Browser instance
     */
    public static Browser getDefaultBrowserInstance() {
        return (browserInstance == null) ? new BrowserFactory().getBrowserInstance(playwright) : browserInstance;
    }

    /**
     * Returns the {@link Page.PdfOptions} for landscape printing.
     *
     * @return landscape pdf options object
     */
    public static Page.PdfOptions getPrintOptionsLandscape() {
        return new Page.PdfOptions().setDisplayHeaderFooter(false).setLandscape(true)
                .setMargin(new Margin().setRight("0").setTop("0").setBottom("0").setLeft("0")).setPrintBackground(false).setFormat("A4");
    }

    /**
     * Returns the {@link Page.PdfOptions} for portrait printing.
     *
     * @return portrait pdf options object
     */
    public static Page.PdfOptions getPrintOptionsPortrait() {
        return new Page.PdfOptions().setDisplayHeaderFooter(false).setLandscape(false)
                .setMargin(new Margin().setRight("0").setTop("0").setBottom("0").setLeft("0")).setPrintBackground(false).setFormat("A4");
    }

    /**
     * Resolves a chromium-based browser executable to a file path, if possible. It starts with verifying the available path, but it will stop at nothing
     * and eventually summon demons, if necessary. It may return null at will.
     *
     * @return chromium browser executable path or null
     */
    private String resolve() {
        final File file = new File((String) SettingsManager.getInstance().getValue("EXPORT_BROWSER_EXECUTABLE_PATH"));
        if (file.exists() && !file.isDirectory()) {
            return file.toString();
        }
        final String path = OS_WINDOWS.equals(os) ? resolveWindows() : resolveLinux();
        if (path == null) {
            return null;
        }
        final File file2 = new File(path);
        if (file2.exists() && !file2.isDirectory()) {
            SettingsManager.getInstance().set("EXPORT_BROWSER_EXECUTABLE_PATH", path);
            return file2.toString();
        }
        return path;
    }

    private String resolveWindows() {
        final String defaultPath = resolveDefaultPathsWindows();
        if (defaultPath != null) {
            return defaultPath;
        }
        final String terminalPath = askTerminalForExecutableWindows();
        if (terminalPath != null) {
            return terminalPath;
        }
        return null;
    }

    /**
     * Checks whether any of the default Windows paths are valid.
     *
     * @return windows browser executable path or null
     */
    private String resolveDefaultPathsWindows() {
        final String[] defaultPaths = new String[]{
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files (x86)\\Chromium\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Chromium\\chrome.exe"
        };
        for (final String path : defaultPaths) {
            final File file = new File(path);
            if (file.exists() && !file.isDirectory()) {
                return path;
            }
        }
        return null;
    }

    /**
     * Attempts to find a browser executable using the shell (Windows only). This thing may summon demons or start another world war.
     *
     * @return hopefully the exe path, probably destroys the PC
     */
    private String askTerminalForExecutableWindows() {
        final String SHELL_LOCATION_WINDOWS = "C:/Windows/System32/cmd.exe";
        final String SHELL_DELIMETER_WINDOWS = ">";
        final String[] EXECUTABLE_NAMES = new String[] { "chrome.exe", "msedge.exe" };
        final String[][] COMMANDS = new String[][] {
                {"reg", "query", "HKEY_CLASSES_ROOT\\ChromeHTML\\shell\\open\\command"},
                {"reg", "query", "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\", "/s", "/f", "\\chrome.exe", "|", "findstr", "Default"},
                {"reg", "query", "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\", "/s", "/f", "\\msedge.exe", "|", "findstr", "Default"}
        };

        try {
            // first execute all our commands
            final Process process = new ProcessBuilder(SHELL_LOCATION_WINDOWS).start();
            final BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            final Scanner stdout = new Scanner(process.getInputStream());

            for (final String executable : EXECUTABLE_NAMES) {
                String commandString = String.join("where", executable);
                logger.debug("Executing: " + commandString);
                stdin.write(commandString);
                stdin.newLine();
                stdin.flush();
            }

            for (final String[] command : COMMANDS) {
                String commandString = String.join(" ", command);
                logger.debug("Executing: " + commandString);
                stdin.write(commandString);
                stdin.newLine();
                stdin.flush();
            }

            // close the process
            stdin.write("exit");
            stdin.newLine();
            stdin.flush();
            stdin.close();

            // now we look through the output, hopefully getting a path
            // However we receive the complete stream which includes the commands we executed, so we gotta sort these out

            boolean extracted = false;
            String path;
            while (stdout.hasNextLine()) {
                extracted = false;
                path = stdout.nextLine();
                if (path.contains(SHELL_DELIMETER_WINDOWS)) { // means this is the line that executed the command (this is actually our input from before)
                    continue;
                }


                if (path.indexOf("\"") != path.lastIndexOf("\"")) { // means there is at least a pair of double quotes, which could indicate a path with spaces
                    path = path.substring(path.indexOf("\"") + 1, path.lastIndexOf("\""));
                    extracted = true;
                } else if (path.contains("(Default)") && path.contains("REG_SZ")) { // this is what the registry output entry starts with
                    path = path.substring(path.indexOf("(Default)") + "(Default)".length() + 1);
                    path = path.substring(path.indexOf("REG_SZ") + "REG_SZ".length() + 1);
                    extracted = true;
                }

                if (!extracted) {
                    continue; // let's not risk some unexpected output being used as a file path
                }

                path = path.trim();

                // now we check if the path we got exists
                if (path != null && path.length() != 0) {
                    final File file = new File(path);
                    if (file.exists()) {
                        path = file.getAbsolutePath();
                        logger.info("chromium browser path found " + path);
                        stdout.close();
                        return path;
                    }
                }

            }
            stdout.close();

        } catch (final IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    private String resolveLinux() {
        final String defaultPath = resolveDefaultPathsLinux();
        if (defaultPath != null) {
            return defaultPath;
        }

        final String terminalPath = askTerminalForExecutableLinux();
        if (terminalPath != null) {
            return terminalPath;
        }
        return null;
    }

    /**
     * Checks whether any of the default Linux paths are valid.
     *
     * @return linux browser executable path or null
     */
    private String resolveDefaultPathsLinux() {
        final String[] defaultPaths = new String[]{
                "/usr/bin/chromium-browser",
                "/usr/bin/chromium",
                "/usr/bin/microsoft-edge",
                "/usr/bin/microsoft-edge-stable",
                "/usr/bin/google-chrome-stable",
                "/usr/bin/google-chrome"
        };
        for (final String path : defaultPaths) {
            final File file = new File(path);
            if (file.exists() && !file.isDirectory()) {
                return path;
            }
        }
        return null;
    }

    private String askTerminalForExecutableLinux() {
        final String SHELL_LOCATION_LINUX = "/bin/bash";
        final String SHELL_DELIMETER_LINUX = "$"; // if this thing runs as su, we better deploy some ransomware
        final String[] EXECUTABLE_NAMES = new String[] { "google-chrome", "google-chrome-stable", "microsoft-edge", "microsoft-edge-stable", "chromium", "chromium-browser" };

        try {
            // first execute all our commands
            final Process process = new ProcessBuilder(SHELL_LOCATION_LINUX).start();
            final BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
           // final Scanner stdout = new Scanner(process.getInputStream());

            for (final String executable : EXECUTABLE_NAMES) {
                String commandString = String.join("whereis", executable);
                logger.debug("Executing: " + commandString);
                stdin.write(commandString);
                stdin.newLine();
                stdin.flush();

                commandString = String.join("locate", executable);
                logger.debug("Executing: " + commandString);
                stdin.write(commandString);
                stdin.newLine();
                stdin.flush();
            }

            // close the process
            stdin.write("exit");
            stdin.newLine();
            stdin.flush();
            stdin.close();

            // TODO I have no idea how the output of these commands looks like so this needs to be filled out


        } catch (final IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    private String requestChromiumInstallPath() {
        final HBox container = new HBox();
        final Label label = new Label("Installation path: ");
        final TextField browserDirField = new TextField(System.getProperty("user.dir"));
        HBox.setHgrow(browserDirField, Priority.ALWAYS);
       // HBox.setMargin(browserDirField, new Insets(3, 3, 0, 0));
        final Button browseFilesButton = new Button("Browse");
        browseFilesButton.setOnAction(event -> {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            final File file = directoryChooser.showDialog(new Stage());
            if (file != null) {
                browserDirField.setText(file.toString());
            }
        });
        container.getChildren().addAll(label, browserDirField, browseFilesButton);
        final AlertDialog.Builder.Action okAction = (result) -> {
            final File target = new File(browserDirField.getText());
            if (target.exists() && target.isDirectory()) {
                result.complete(AlertDialog.RESULT_OK);
                return true;
            }
            return false;
        };
        final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Choose installation path").addOkButton("Install", okAction)
                .addCloseButton("Cancel").setCancelable(false).setParent(SongbookApplication.getMainWindow()).addContentNode(container).build().awaitResult();
        final AtomicReference<String> path = new AtomicReference<>();
        result.thenAccept(data -> {
            if (data.equals(AlertDialog.RESULT_OK)) {
                final File target = new File(browserDirField.getText());
                if (target.exists() && target.isDirectory()) {
                    path.set(browserDirField.getText());
                } else {
                    path.set(null);
                }
            }
        });
        return path.get();
    }

    public boolean requestInstallChromium() {
        final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("No suitable browser found").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                .setMessage("The export of the songbook is done using a chromium-based headless web browser (Chrome, Edge), however no such browser was found on your computer. Do you wish to install Chromium on your computer? If you have such a browser installed, you can help the program find it in inside the program settings.")
                .setCancelable(false).addOkButton("Proceed").addCloseButton("Cancel").build().awaitResult();
        final AtomicReference<Boolean> success = new AtomicReference<>();
        result.thenAccept(code -> {
            if (code.equals(AlertDialog.RESULT_OK)) {
                success.set(installChromium());
            } else {
                SettingsManager.getInstance().set("EXPORT_ENABLED", false);
                success.set(false);
            }
        });
        return success.get();
    }


    private boolean installChromium() {
        final String path = requestChromiumInstallPath();
        if (path == null) {
            return false;
        }

        final String url = (getOS().equals(OS_WINDOWS)) ? CHROMIUM_WINDOWS_DOWNLOAD_LINK : CHROMIUM_LINUX_DOWNLOAD_LINK;

        final Path DOWNLOAD_FILE_NAME = Paths.get(path, "chromium.zip");
        logger.info("initiating download...");

        final HBox container = new HBox();
        final ProgressIndicator progressThingy = new ProgressIndicator();
        final Label label = new Label("Download in progress...");
        container.getChildren().add(progressThingy);
        container.getChildren().add(label);
        final AlertDialog dialog = new AlertDialog.Builder().setTitle("Downloading Chromium").setCancelable(false)
                .addContentNode(container).addContentNode(label).build();
        final AtomicBoolean output = new AtomicBoolean(false);
        new Thread(() -> {
            Platform.runLater(dialog::open);
            boolean skip = false;
            try (final InputStream in = new URL(url).openStream()) {
                final long bytesRead = Files.copy(in, DOWNLOAD_FILE_NAME);

                if (bytesRead == 0) {
                    skip = true;
                }
                if (!skip) {
                    logger.info("download finished");
                    final String executablePath = Paths.get(ZipBuilder.extract(DOWNLOAD_FILE_NAME.toString()), "chrome-win", "chrome.exe").toString();
                    final File file = new File(executablePath);
                    if (file.exists() && !file.isDirectory()) {
                        SettingsManager.getInstance().set("EXPORT_BROWSER_EXECUTABLE_PATH", executablePath);
                        logger.info("archive extracted successfully");
                        new File(DOWNLOAD_FILE_NAME.toString()).delete();
                        output.set(true);
                    }
                }

            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
                skip = true;
            }
            boolean finalSkip = skip;
            Platform.runLater(() -> {
                dialog.close();
                if (finalSkip) {
                    new AlertDialog.Builder().setTitle("Download failed").setMessage("Something happened while downloading. Check the log for more information!").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .addOkButton().setCancelable(true).build().open();
                } else {
                    new AlertDialog.Builder().setTitle("Download finished").setMessage("It appears that hte download was successful!").setIcon(AlertDialog.Builder.Icon.INFO)
                            .addOkButton().setCancelable(true).build().open();
                }

            });
        }).start();



        return output.get();
    }

    /**
     * Frees up any resources that may have been allocated.
     */
    public static void close() {
        if (browserInstance != null) {
            browserInstance.close();
            playwright.close();
        }
    }


    /**
     * Determines whether the host operating system is Windows or something else (most probably Linux of some kind).
     *
     * @return {@link #OS_WINDOWS} if Windows-based, {@link #OS_LINUX} otherwise
     */
    public static String getOS() {
        final String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return OS_WINDOWS;
        }
        return OS_LINUX;
    }
}
