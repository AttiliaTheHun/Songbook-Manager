package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.TokenProvider;
import attilathehun.songbook.vcs.index.Index;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


public class VCSAgent {
    public static final int STATUS_BEHIND = 0;
    public static final int STATUS_UP_TO_DATE = 1;
    public static final int STATUS_AHEAD = 2;
    private static final Logger logger = LogManager.getLogger(VCSAgent.class);

    private long lastRemoteVersionTimestamp = -1;

    private final transient String token;

    protected VCSAgent() {
        this(new String(new TokenProvider().getAuthenticationToken(new Certificate()), StandardCharsets.UTF_8));
    }

    private VCSAgent(final String token) {
        this.token = token;
    }

    public static VCSAgent zeroPrivilegeAgent() {
        return new VCSAgent(null);
    }

    public long getRemoteVersionTimestamp() {
        try {
            final Client2.Result result = new Client2().http(SettingsManager.getInstance().getValue("REMOTE_VERSION_TIMESTAMP_URL"), Client2.HTTP_GET, null, null, null);
            if (result.responseCode() == Client2.OK) {
                final long timestamp = Long.parseLong(new String(result.response(), StandardCharsets.UTF_8));
                lastRemoteVersionTimestamp = timestamp;
                return timestamp;
            } else {
                logger.warn("remote index unexpected HTTP code " + result.responseCode());
                if (result.response() != null && result.response().length != 0) {
                    logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
                }
                if (result.error() != null && result.error().length != 0) {
                    logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
                }
                new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                        .setMessage(String.format("The server returned the response code %d while fetching the remote songbook version timestamp. Check the log for more information.", result.responseCode()))
                        .addOkButton().build().open();
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public long getLastRemoteVersionTimestamp() {
        if (!VCSAdmin.FLAG_USE_CACHE) {
            return getRemoteVersionTimestamp();
        }
        return lastRemoteVersionTimestamp;
    }

    /**
     * Automatically push/pull changes when there is local and remote version misintegrity.
     */
    @Deprecated(forRemoval = true)
    public void runDiagnostics() {
        try {
            if (!VCSAdmin.FLAG_USE_CACHE) {
                CacheManager.getInstance().cacheSongbookVersionTimestamp();
            }
            Client client = new Client();
            String resp = client.httpGet((String) SettingsManager.getInstance().getValue("REMOTE_DATA_VERSION_TIMESTAMP_URL"));

            if (client.getStatus().getCode() != Client.Status.SUCCESS && client.getStatus().getError().length() != 0) {
                logger.info("Running diagnostics - Invalid server response value");
                return;
            }

            long remoteVersionTimestamp = Long.parseLong(resp);
            if (remoteVersionTimestamp > CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                VCSAdmin.getInstance().pull(this);
            } else if (remoteVersionTimestamp < CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                VCSAdmin.getInstance().push(this);
            } else {
                new AlertDialog.Builder().setTitle("Message").setIcon(AlertDialog.Builder.Icon.INFO)
                                .setMessage("Local version of the songbook is up to date with the remote one.")
                                .addOkButton()
                                .setParent(SongbookApplication.getMainWindow()).build().open();
            }
            logger.info("Running diagnostics - Success");
        } catch (IOException e) {
            logger.info("Running diagnostics - Failure");
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Compare local and remote version timestamps to determine the state of the local songbook.
     *
     * @return status of the local songbook
     */
    public int compare() {
        final long remoteVersionTimestamp = getRemoteVersionTimestamp();
        try {
            if (!VCSAdmin.FLAG_USE_CACHE) {
                CacheManager.getInstance().cacheSongbookVersionTimestamp();
            }

            if (remoteVersionTimestamp > CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return STATUS_BEHIND;
            } else if (remoteVersionTimestamp < CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                return STATUS_AHEAD;
            }

        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Could not obtain local version timestamp. Check the log for more information.").addOkButton().build().open();
            return -1;
        }
        return STATUS_UP_TO_DATE;
    }

    /**
     * Checks whether there are changes to the remote songbook that have not been downloaded. Performs versionTimestamp comparison.
     *
     * @return true if there are changes to download; false otherwise
     */
    @Deprecated(forRemoval = true)
    public boolean verifyRemoteChanges() throws Exception {
        try {
            Client client = new Client();
            String resp = client.httpGet(SettingsManager.getInstance().getValue("REMOTE_VERSION_TIMESTAMP_URL"));

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
     *
     * @return true if there are changes to upload; false otherwise
     */
    @Deprecated(forRemoval = true)
    public boolean verifyLocalChanges() throws Exception {
        try {
            Client client = new Client();
            String resp = client.httpGet(SettingsManager.getInstance().getValue("REMOTE_VERSION_TIMESTAMP_URL"));

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
     * Obtains remote songbook index.
     *
     * @return remote songbook index
     * @throws IOException
     */
    public Index getRemoteIndex() throws IOException {
        final Client2.Result result = new Client2().http(SettingsManager.getInstance().getValue("REMOTE_INDEX_URL"), Client2.HTTP_GET, token, null, null);

        if (result.responseCode() == Client2.OK) {
            final File index = new File(result.metadata());
            try (final FileInputStream inputStream = new FileInputStream(index)) {
                final String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                final Type targetClassType = new TypeToken<Index>() {}.getType();
                return new Gson().fromJson(json, targetClassType);
            }
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("remote index access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
            return null;
        }

        logger.warn("remote index unexpected HTTP code " + result.responseCode());
        if (result.response() != null && result.response().length != 0) {
            logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
        }
        if (result.error() != null && result.error().length != 0) {
            logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
        }
        new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                .setMessage(String.format("The server returned the response code %d while fetching the remote songbook index. Check the log for more information.", result.responseCode()))
                .addOkButton().build().open();

        return null;
    }

    public void saveDataRemotely(final String requestFilePath) {
        if (requestFilePath == null || requestFilePath.length() == 0) {
            throw new IllegalArgumentException("invalid request file path");
        }
        final String SERVER_EXPECTED_FILENAME = "request.zip";
        final File requestFile = new File(requestFilePath);
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_DATA_URL"), Client2.HTTP_POST, token, requestFile, SERVER_EXPECTED_FILENAME);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Something went wrong when uploading the data: " + e.getLocalizedMessage()).addOkButton().build().open();
            return;
        }
        if (result.responseCode() == Client2.CREATED) {
            logger.info("data upload successful");
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Data has been uploaded to the remote server.").addOkButton().build().open();
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("data upload access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("data upload unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage(String.format("The server returned the response code %d when uploading the data. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }

    }

    public String loadRemoteData(final LoadIndex index) {
        String SERVER_EXPECTED_FILENAME = null;
        if (index != null) {
            SERVER_EXPECTED_FILENAME = "index.json";
        }
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_DATA_URL"), Client2.HTTP_GET, token, index, SERVER_EXPECTED_FILENAME);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Something went wrong when downloading the data: " + e.getLocalizedMessage()).addOkButton().build().open();
            return null;
        }
        if (result.responseCode() == Client2.OK) {
            logger.info("data download successful");
            if (index != null) {
                logger.debug("load index: " + new Gson().toJson(index));
            }

            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Data has been downloaded from the remote server.").addOkButton().build().open();
            return result.metadata();
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("data download access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("data download unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage(String.format("The server returned the response code %d when downloading the data. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }
        return null;
    }

    public void listRemoteBackups() {

    }

    public void createRemoteBackup() {

    }

    public void restoreRemoteBackup() {

    }

    public void listTokensOnTheServer() {

    }

    public void createTokenOnTheServer() {

    }

    public void freezeToken() {

    }

    public static final class Certificate {
        private Certificate() {

        }
    }

}
