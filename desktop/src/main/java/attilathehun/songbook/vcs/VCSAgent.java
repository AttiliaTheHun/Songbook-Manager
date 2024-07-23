package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.TokenProvider;
import attilathehun.songbook.vcs.index.Index;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.window.AdminPanel;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * This class can be used for higher-level communication with the remote server.
 */
public final class VCSAgent {
    public static final int STATUS_BEHIND = 0;
    public static final int STATUS_UP_TO_DATE = 1;
    public static final int STATUS_AHEAD = 2;
    private static final Logger logger = LogManager.getLogger(VCSAgent.class);

    private long lastRemoteVersionTimestamp = -1;

    private final transient String token;

    /**
     * The standard constructor. Upon instantiation, the agent tries to acquire an authentication token which is then stored in memory. For this reason the agent should be garbage
     * collected as soon as it is no longer used. To preserve agent's metadata, {@link #zeroPrivilegeAgent(VCSAgent)} can be used.
     */
    VCSAgent() {
        this(new String(new TokenProvider().getAuthenticationToken(new Certificate()), StandardCharsets.UTF_8));
    }

    /**
     * Creates a VCS agent. This method is only meant to be used by {@link AdminPanel}.
     *
     * @param certificate
     * @return the agent
     */
    public static VCSAgent getAdminPanelAgent(final AdminPanel.Certificate certificate) {
        return new VCSAgent();
    }

    private VCSAgent(final String token) {
        this.token = token;
    }

    /**
     * Creates an agent with no authentication capabilities.
     *
     * @return the agent
     */
    public static VCSAgent zeroPrivilegeAgent() {
        return new VCSAgent(null);
    }

    /**
     * Creates an agent with no authentication capabilities while preserving the metadata of another agent.
     *
     * @return the agent
     */
    public static VCSAgent zeroPrivilegeAgent(final VCSAgent agent) {
        final VCSAgent a = new VCSAgent(null);
        a.lastRemoteVersionTimestamp = agent.lastRemoteVersionTimestamp;
        return a;
    }

    /**
     * Queries the remote server for a timestamp and then returns the timestamp or -1 in case of error. The timestamp will be later accessible through {@link #getLastRemoteVersionTimestamp()}.
     *
     * @return the timestamp or -1
     */
    public long getRemoteVersionTimestamp() {
        try {
            final Client2.Result result = new Client2().http(SettingsManager.getInstance().getValue("REMOTE_VERSION_TIMESTAMP_URL"), Client2.HTTP_GET, null, null, null);
            if (result.responseCode() == HttpURLConnection.HTTP_OK) {
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
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Returns the last remote version timestamp this agent has fetch.
     *
     * @return the timestamp or -1
     */
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
     * @return remote songbook index or null
     * @throws IOException
     */
    public Index getRemoteIndex() throws Exception {
        final Client2.Result result = new Client2().http(SettingsManager.getInstance().getValue("REMOTE_INDEX_URL"), Client2.HTTP_GET, token, null, null);

        if (result.responseCode() == HttpURLConnection.HTTP_OK) {
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

    /**
     * Uploads a save request file to the server. Upon failure or success the user is notified via an {@link AlertDialog} window.
     *
     * @param requestFilePath path to the request file
     */
    public void saveDataRemotely(final String requestFilePath) {
        if (requestFilePath == null || requestFilePath.length() == 0) {
            throw new IllegalArgumentException("invalid request file path");
        }
        final String SERVER_EXPECTED_FILENAME = "save_request.zip";
        final File requestFile = new File(requestFilePath);
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_DATA_URL"), Client2.HTTP_POST, token, requestFile, SERVER_EXPECTED_FILENAME);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Something went wrong when uploading the data: " + e.getLocalizedMessage()).addOkButton().build().open();
            return;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_CREATED) {
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

    /**
     * Requests the entirety of the songbook from the remote server and temporarily stores it locally.
     *
     * @return path to the response file
     */
    public String loadRemoteData() {
        return loadRemoteData(null);
    }


    /**
     * Requests data from the remote server and stores them locally. Accepts a request index to specify the data to ask the server about.
     *
     * @param index the load request index
     * @return path to the response file
     */
    public String loadRemoteData(final LoadIndex index) {
        String SERVER_EXPECTED_FILENAME = null;
        String method = Client2.HTTP_GET;
        if (index != null) {
            SERVER_EXPECTED_FILENAME = "index.json";
            method = Client2.HTTP_POST;
        }
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_DATA_URL"), method, token, index, SERVER_EXPECTED_FILENAME);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Something went wrong when downloading the data: " + e.getLocalizedMessage()).addOkButton().build().open();
            return null;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_OK) {
            logger.info("data download successful");
            if (index != null) {
                logger.debug("load index: " + new Gson().toJson(index));
            }

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

    public String listRemoteBackups() {
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_BACKUPS_URL"), Client2.HTTP_GET, token, null, null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(SongbookApplication.getMainWindow())
                    .setMessage("Something went wrong when getting the backup list: " + e.getLocalizedMessage()).addOkButton().build().open();
            return null;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_OK) {
            logger.info("backup list fetched successfully");

            return result.metadata();
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("backup list access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("backup list unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(SongbookApplication.getMainWindow())
                    .setMessage(String.format("The server returned the response code %d when fetching the backup list. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }
        return null;
    }

    public void createRemoteBackup() {

    }

    public void restoreRemoteBackup() {

    }

    /**
     * Returns the list of authentication token data that is fetched from the server. This list is received as a json array and as such is stored locally, thus some form of
     * parsing needs to take place before it can be used in a meaningful way. The function returns the path to the file where the tokens are stored. Upon encountering an error,
     * the function notifies the user via an {@link AlertDialog} windows and return null.
     *
     * @return path to the file with the tokens or null
     */
    public String listTokensOnTheServer() {
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL"), Client2.HTTP_GET, token, null, null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(AdminPanel.getInstance())
                    .setMessage("Something went wrong when getting the token list: " + e.getLocalizedMessage()).addOkButton().build().open();
            return null;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_OK) {
            logger.info("token list download successful");
            return result.metadata();
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("token list download access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("token list download unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage(String.format("The server returned the response code %d when loading the tokens. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }
        return null;
    }

    /**
     * Sends a request to create a new authentication token to the server. In case of success or failure notifies the user via an {@link AlertDialog} window. The pemissions
     * should be provided as a bitmap from least important to the most important.
     *
     * @param name token name
     * @param permissions permission bitmap
     */
    public void createTokenOnTheServer(final String name, final String permissions) {
        if (name == null || name.length() == 0 || permissions == null || permissions.length() == 0) {
            throw new IllegalArgumentException();
        }
        final String bodyTemplate = "{\"name\": \"%s\", \"permissions\": \"%s\"}";
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL"), Client2.HTTP_POST, token, String.format(bodyTemplate, name, permissions), null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(AdminPanel.getInstance())
                    .setMessage("Something went wrong when creating the token: " + e.getLocalizedMessage()).addOkButton().build().open();
            return;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_CREATED) {
            logger.info("token creation successful");
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO).setParent(AdminPanel.getInstance())
                    .setMessage("Authentication token was successfully created.").addCloseButton("Close")
                    .addOkButton("Copy", (_result) -> {
                        ClipboardContent content = new ClipboardContent();
                        String token = new String(result.response(), StandardCharsets.UTF_8);
                        token = token.replace("\"", "").replace("{", "").replace("}", "").replace("message", "");
                        content.putString(token);
                        Clipboard.getSystemClipboard().setContent(content);
                        return true;
                    }).build().open();
            return;
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("token creation access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("token creation unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage(String.format("The server returned the response code %d when creating the token. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }

    }

    /**
     * Sends a request to freeze an authentication token to the server. In case of success or failure notifies the user via an {@link AlertDialog} window.
     *
     * @param index index of the token to be frozen. The index must comply the server-side indexing.
     */
    public void freezeToken(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        final String bodyTemplate = "{\"freeze\": [%d]}";
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_TOKENS_URL"), Client2.HTTP_PUT, token, String.format(bodyTemplate, index), null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(AdminPanel.getInstance())
                    .setMessage("Something went wrong when freezing the token: " + e.getLocalizedMessage()).addOkButton().build().open();
            return;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_OK) {
            logger.info("token frozen successfully");
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO).setParent(AdminPanel.getInstance())
                    .setMessage("Authentication token was successfully frozen.").addOkButton().build().open();
            return;
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("token freezing access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("token freezing unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage(String.format("The server returned the response code %d when freezing the token. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }
    }

    /**
     * Fetches the action log from the server and returns the path to where its local copy is stored.
     *
     * @return the path to the action log file
     */
    public String getRemoteActionLog() {
        final Client2 client = new Client2();
        Client2.Result result;
        try {
            result = client.http(SettingsManager.getInstance().getValue("REMOTE_ACTION_LOG_URL"), Client2.HTTP_GET, token, null, null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR).setParent(AdminPanel.getInstance())
                    .setMessage("Something went wrong when getting the action log: " + e.getLocalizedMessage()).addOkButton().build().open();
            return null;
        }
        if (result.responseCode() == HttpURLConnection.HTTP_OK) {
            logger.info("action log download successful");
            return result.metadata();
        } else if (result.responseCode() == Client2.ACCESS_DENIED) {
            logger.info("action log download access denied");
            new AlertDialog.Builder().setTitle("Access denied").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage("The token that was used does not have sufficient permissions. Use a different token or contact the VCS server admin.").addOkButton().build().open();
        } else {
            logger.warn("token list download unexpected HTTP code " + result.responseCode());
            if (result.response() != null && result.response().length != 0) {
                logger.warn("HTTP response: " + new String(result.response(), StandardCharsets.UTF_8));
            }
            if (result.error() != null && result.error().length != 0) {
                logger.error("HTTP error: " + new String(result.error(), StandardCharsets.UTF_8));
            }
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING).setParent(AdminPanel.getInstance())
                    .setMessage(String.format("The server returned the response code %d when downloading the data. Check the log for more information.", result.responseCode()))
                    .addOkButton().build().open();
        }
        return null;
    }

    public static final class Certificate {
        private Certificate() {

        }
    }

}
