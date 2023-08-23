package attilathehun.songbook.environment;

import java.nio.file.Paths;

public class Installer {
    private static String RESOURCES_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "resources.zip").toString();
    private static String SCRIPTS_ZIP_FILE_PATH = Paths.get(System.getProperty("user.dir") + "scripts.zip").toString();

    public static void runDiagnostics() {
        Installer installer = new Installer();
        EnvironmentVerificator verificator = new EnvironmentVerificator();
        if (!verificator.verifyResources()) {
            installer.installResources();
        }
        if (!verificator.verifyScripts()) {
            installer.installScripts();
        }
    }

    public void installResources() {
        new EnvironmentManager().extractZip(RESOURCES_ZIP_FILE_PATH, Environment.getInstance().settings.RESOURCE_FILE_PATH);
    }

    public void installScripts() {
        new EnvironmentManager().extractZip(SCRIPTS_ZIP_FILE_PATH, Environment.getInstance().settings.SCRIPTS_FILE_PATH);
    }
}
