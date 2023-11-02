package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.vcs.index.Index;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * An agent to perform more complex tasks serving as a middleware between the VCS Administrator
 * and other components of the version control system.
 */
public class VCSAgent {
    private static final Logger logger = LogManager.getLogger(VCSAgent.class);

    private static boolean FLAG_USE_CACHE = true;

    public static final int STATUS_BEHIND = 0;
    public static final int STATUS_UP_TO_DATE = 1;
    public static final int STATUS_AHEAD = 2;

    protected VCSAgent() {
        try {
            if (!FLAG_USE_CACHE) {
                CacheManager.getInstance().cacheSongbookVersionTimestamp();
            }
        } catch (IOException e) {
            Environment.showErrorMessage("Error", "Could not acquire a version timestamp!");
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Automatically push/pull changes when there is local and remote version misintegrity.
     */
    public void runDiagnostics() {
        try {

            Client client = new Client();
            String resp = client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL);

            if (client.getStatus().getCode() != Client.Status.SUCCESS && client.getStatus().getError().length() != 0) {
                logger.info("Running diagnostics - Invalid server response value");
                return;
            }

            long remoteVersionTimestamp = Long.parseLong(resp);
            if (remoteVersionTimestamp > CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                VCSAdmin.getInstance().pull(this);
            } else if(remoteVersionTimestamp < CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                VCSAdmin.getInstance().push(this);
            } else {
                Environment.showMessage("Message", "Local version of the songbook is up to date with the remote one.");
            }
            logger.info("Running diagnostics - Success");
        } catch (IOException e) {
            logger.info("Running diagnostics - Failure");
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Compare local and remote version timestamps to determine the state of the local songbook.
     * @return status of the local songbook
     * @throws Exception if server is misconfigured
     */
    public int compareChanges() throws Exception {
        try {
            Client client = new Client();
            String resp = client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL);

            if (client.getStatus().getCode() != Client.Status.SUCCESS && client.getStatus().getError().length() != 0) {
                logger.info("Invalid server response value: ".join(resp));
                throw new RuntimeException("Could not compare local and remote changes due to server error.");
            }

            long remoteVersionTimestamp = Long.parseLong(resp);
            if (remoteVersionTimestamp > CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return STATUS_BEHIND;
            } else if (remoteVersionTimestamp < CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return STATUS_AHEAD;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return STATUS_UP_TO_DATE;
    }
    /**
     * Checks whether there are changes to the remote songbook that have not been downloaded. Performs versionTimestamp comparison.
     * @return true if there are changes to download; false otherwise
     */
    @Deprecated
    public boolean verifyRemoteChanges() throws Exception {
        try {
            Client client = new Client();
            String resp = client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL);

            if (client.getStatus().getCode() != Client.Status.SUCCESS && client.getStatus().getError().length() != 0) {
                logger.info("Running diagnostics - Invalid server response value");
                throw new RuntimeException("Could not compare local and remote changes due to server error.");
            }

            long remoteVersionTimestamp = Long.parseLong(resp);
            if (remoteVersionTimestamp > CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return true;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Checks whether there are changes to the songbook that have not been uploaded on the server. Performs versionTimestamp comparison.
     * @return true if there are changes to upload; false otherwise
     */
    @Deprecated
    public boolean verifyLocalChanges() throws Exception {
        try {
            Client client = new Client();
            String resp = client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL);

            if (client.getStatus().getCode() != Client.Status.SUCCESS && client.getStatus().getError().length() != 0) {
                logger.info("Running diagnostics - Invalid server response value");
                throw new RuntimeException("Could not compare local and remote changes due to server error.");
            }

            long remoteVersionTimestamp = Long.parseLong(resp);
            if (remoteVersionTimestamp < CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return true;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (NumberFormatException nfe) {
            logger.info("Running diagnostics - Invalid server response value");
            logger.error(nfe.getMessage(), nfe);
            throw new RuntimeException("Could not compare local and remote changes due to server error.");
        }
        return false;
    }

    /**
     * Obtain remote songbook index.
     * @param token the authentication token with read access
     * @return remote songbook index
     * @throws IOException
     */
    public Index getRemoteIndex(String token) throws IOException {
        Client client = new Client();
        Type targetClassType = new TypeToken<Index>() {
        }.getType();
        String resp = client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_INDEX_URL, token);
        if (client.getStatus().getCode() != Client.Status.SUCCESS && client.getStatus().getError().length() != 0) {
            return null;
        }

        return new Gson().fromJson(resp, targetClassType);
    }

    public static void useCache(boolean value) {
        FLAG_USE_CACHE = value;
    }

    public static boolean useCache() {
        return FLAG_USE_CACHE;
    }
}
