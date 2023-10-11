package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.nio.file.Paths;

/**
 * The Version Control System Administrator. This class is the main class of the Version Control System and provides a simple API to interact with.
 */
public class VCSAdmin {

    private static final Logger logger = LogManager.getLogger(VCSAdmin.class);

    public void saveLocalChanges() {
        if (!Environment.getInstance().settings.vcs.REMOTE_SAVE_LOAD_ENABLED) {
            Environment.showMessage("Remote saving and loading disabled","Remote saving and loading is disabled in the settings. Enable it and restart the client or read more in the documentation");
            return;
        }
        if (!new VCSAgent().verifyLocalChanges()) {
            Environment.showMessage("Already up to date", "The remote version of the songbook matches the local version.");
            return;
        }
        String token = Environment.getInstance().acquireToken(new Certificate());
        if (token == null || token.length() == 0) {
            token = requestOneTimeToken();
            if (token == null || token.length() == 0) {
                Environment.showErrorMessage("Error", "Invalid token!");
                return;
            }
        }
        RequestFileAssembler RFAssembler = new RequestFileAssembler().assembleSaveFile(new IndexBuilder().createSaveIndex());
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
        //TODO: ask the user to fill the token to a textfield for a one-time use
        return "";
    }




    public static class VCSSettings implements Serializable {
        private static final Logger logger = LogManager.getLogger(VCSSettings.class);

        public final boolean REMOTE_SAVE_LOAD_ENABLED;
        public final String REMOTE_DATA_ZIP_FILE_DOWNLOAD_URL;
        public final String REMOTE_DATA_ZIP_FILE_UPLOAD_URL;
        public final String REMOTE_DATA_FILE_HASH_URL;
        public final String REMOTE_DATA_FILE_LAST_EDITED_URL;
        public final String REQUEST_ZIP_FILE_PATH;
        public final String VCS_CACHE_FILE_PATH;
        public final String LOCAL_INDEX_FILE_PATH;
        public final int VCS_THREAD_COUNT;

        public VCSSettings() {
            REMOTE_DATA_ZIP_FILE_DOWNLOAD_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/download/";
            REMOTE_DATA_ZIP_FILE_UPLOAD_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/upload/";
            REMOTE_DATA_FILE_HASH_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/hash/";
            REMOTE_DATA_FILE_LAST_EDITED_URL = "http://beta-hrabozpevnik.clanweb.eu/api/data/modify-date/";
            REMOTE_SAVE_LOAD_ENABLED = false;
            REQUEST_ZIP_FILE_PATH = "request.zip";
            VCS_CACHE_FILE_PATH = Paths.get(System.getProperty("user.dir"), "vcs").toString();
            LOCAL_INDEX_FILE_PATH = Paths.get(Environment.getInstance().settings.vcs.VCS_CACHE_FILE_PATH, "vcs").toString();
            VCS_THREAD_COUNT = 20;
        }


    }

    public static final class Certificate {
        private Certificate() {
        }
    }
}
