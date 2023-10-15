package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.vcs.index.Index;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;

public class VCSAgent {
    private static final Logger logger = LogManager.getLogger(VCSAgent.class);

    protected VCSAgent() {

    }

    /**
     * Automatically push/pull changes when there is local and remote version misintegrity.
     */
    public void runDiagnostics() {
        try {

            Client client = new Client();
            long remoteVersionTimestamp = Long.parseLong(client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL));
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
     * Checks whether there are changes to the remote songbook that have not been downloaded. Performs versionTimestamp comparison.
     * @return true if there are changes to download; false otherwise
     */
    public boolean verifyRemoteChanges() {
        try {
            Client client = new Client();
            long remoteVersionTimestamp = Long.parseLong(client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL));
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
    public boolean verifyLocalChanges() {
        try {
            Client client = new Client();
            long remoteVersionTimestamp = Long.parseLong(client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_VERSION_TIMESTAMP_URL));
            if (remoteVersionTimestamp < CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return true;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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

        return new Gson().fromJson(client.httpGet(Environment.getInstance().settings.vcs.REMOTE_DATA_INDEX_URL, token), targetClassType);
    }
}
