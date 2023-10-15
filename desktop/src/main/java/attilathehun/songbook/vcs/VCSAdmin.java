package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The Version Control System Administrator. This class is the main class of the Version Control System and provides a simple API to interact with.
 */
public class VCSAdmin {

    private static final Logger logger = LogManager.getLogger(VCSAdmin.class);

    private static final VCSAdmin instance = new VCSAdmin();
    private final VCSAgent defaultAgent = new VCSAgent();

    public static VCSAdmin getInstance() {
        return instance;
    }


    public VCSAgent getAgent() {
        return defaultAgent;
    }

    /**
     * Pushes local changes to the server. Upon overshadowing danger, asks the user what to do. Requests an access token if none
     * is loaded.
     */
    public void push() {
        push(null);
    }

    /**
     * Pushes local changes to the server using custom vcs agent. Upon overshadowing danger, asks the user what to do. Requests an
     * access token if none is loaded.
     */
    public void push(VCSAgent agent) {
        try {
            saveLocalChanges(agent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void pull() {
        pull(null);
    }

    public void pull(VCSAgent agent) {

    }

    private void saveLocalChanges(VCSAgent a) throws IOException, NoSuchAlgorithmException {
        if (!Environment.getInstance().settings.vcs.REMOTE_SAVE_LOAD_ENABLED) {
            Environment.showMessage("Remote saving and loading disabled","Remote saving and loading is disabled in the settings. Enable it and restart the client or read more in the documentation");
            return;
        }
        VCSAgent agent;
        if (a == null) {
            agent = new VCSAgent();
            if (!agent.verifyLocalChanges()) {
                Environment.showMessage("Already up to date", "The remote version of the songbook matches the local version.");
                return;
            }
        } else {
            //trust the custom agent it has already verified the state of the changes
            agent = a;
        }

        String token = Environment.getInstance().acquireToken(new Certificate());
        if (token == null || token.length() == 0) {
            token = requestOneTimeToken();
            if (token == null || token.length() == 0) {
                Environment.showErrorMessage("Error", "Invalid token!");
                return;
            }
        }
        IndexBuilder indexBuilder = new IndexBuilder();
        CacheManager.getInstance().cacheIndex(indexBuilder.createLocalIndex());
        RequestFileAssembler RFAssembler = new RequestFileAssembler().assembleSaveFile(new IndexBuilder().createSaveIndex(CacheManager.getInstance().getCachedIndex(), agent.getRemoteIndex(token)));
        //Client client = new Client().postFile(Environment.getInstance().settings.vcs.REMOTE_DATA_ZIP_FILE_UPLOAD_URL, RFAssembler.getOutputFilePath(), token);

       /* if (client.getStatus().getCode() == Client.Status.SUCCESS) {
            Environment.showMessage("Success", "Data saved successfully!");
        } else {
            Environment.showMessage("Failure", "The data could not be saved. Check the application log for more information.");
        }*/
    }

    public void loadRemoteChanges() {
        if (!Environment.getInstance().settings.vcs.REMOTE_SAVE_LOAD_ENABLED) {
            Environment.showMessage("Remote saving and loading disabled","Remote saving and loading is disabled in the settings. Enable it and restart the client or read more in the documentation");
            return;
        }

    }

    private String requestOneTimeToken() {
        JLabel label = new JLabel("Fill in the access token to access the remote server. Depending on the operation, the token must have READ or WRITE permission.");
        JTextField token = new JPasswordField();
        token.setToolTipText("The authentication token.");
        Object[] message = {
                label,
                "Token:", token
        };

        int option = JOptionPane.showConfirmDialog(Environment.getAlwaysOnTopJDialog(), message, "Load Remote Songbook", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return token.getText();
        }
        return null;
    }

    private void updateSongbookChangeLog() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        final String date = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(CacheManager.getInstance().getCachedSongbookVersionTimestamp()), ZoneId.systemDefault()));
        final String username = System.getProperty("user.name");
        PrintWriter printWriter = new PrintWriter(new FileWriter((Environment.getInstance().settings.environment.EDIT_LOG_FILE_PATH), true));
        printWriter.write(date + " " + username + "\n");
        printWriter.close();
    }




    public static class VCSSettings implements Serializable {
        private static final Logger logger = LogManager.getLogger(VCSSettings.class);

        public final boolean REMOTE_SAVE_LOAD_ENABLED;
        public final String REMOTE_DATA_DOWNLOAD_URL;
        public final String REMOTE_DATA_UPLOAD_URL;
        public final String REMOTE_DATA_INDEX_URL;
        public final String REMOTE_DATA_VERSION_TIMESTAMP_URL;
        public final String VCS_CACHE_FILE_PATH;
        public final String REQUEST_ZIP_TEMP_FILE_PATH;
        public final String CHANGE_LOG_FILE_PATH;
        public final String VERSION_TIMESTAMP_FILE_PATH;
        public final String LOCAL_INDEX_FILE_PATH;
        public final int VCS_THREAD_COUNT;



        public VCSSettings() {
            REMOTE_DATA_DOWNLOAD_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/download/";
            REMOTE_DATA_UPLOAD_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/upload/";
            REMOTE_DATA_INDEX_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/index/";
            REMOTE_DATA_VERSION_TIMESTAMP_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/version-timestamp/";
            REMOTE_SAVE_LOAD_ENABLED = false;
            REQUEST_ZIP_TEMP_FILE_PATH = Paths.get(new Environment.EnvironmentSettings().TEMP_FILE_PATH, "request.zip").toString();
            VCS_CACHE_FILE_PATH = Paths.get(System.getProperty("user.dir"), "vcs").toString();
            LOCAL_INDEX_FILE_PATH = Paths.get(VCS_CACHE_FILE_PATH, "index.json").toString();
            VCS_THREAD_COUNT = 20;
            CHANGE_LOG_FILE_PATH = Paths.get(new Environment.EnvironmentSettings().DATA_FILE_PATH, "change_log.txt").toString();
            VERSION_TIMESTAMP_FILE_PATH = Paths.get(VCS_CACHE_FILE_PATH, "timestamp.txt").toString();
        }


    }

    public static final class Certificate {
        private Certificate() {
        }
    }
}
