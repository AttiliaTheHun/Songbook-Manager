package attilathehun.songbook.vcs;

import attilathehun.annotation.TODO;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.misc.Misc;
import attilathehun.songbook.vcs.index.Index;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * The Version Control System Administrator. This class is the main class of the Version Control System and provides a simple API to be used from other components.
 * The parts of the version control system are not meant to be used directly.
 */
public final class VCSAdmin {

    private static final Logger logger = LogManager.getLogger(VCSAdmin.class);

    private static final VCSAdmin INSTANCE = new VCSAdmin();
    private VCSSettings settings = getDefaultSettings();

    private VCSAgent defaultAgent = null;

    private VCSAdmin() {}

    public static VCSAdmin getInstance() {
        return INSTANCE;
    }


    public VCSAgent getAgent() {
        if (defaultAgent == null) {
            defaultAgent = new VCSAgent();
        }
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
     * Pushes local changes to the server using custom vcs agent. In this case, it is up to the agent to check status of the songbook and prevent
     * overshadowing of the remote changes.
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
        try {
            loadRemoteChanges(agent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveLocalChanges(VCSAgent a) throws IOException, NoSuchAlgorithmException {
        if (!(Boolean) settings.get("REMOTE_SAVE_LOAD_ENABLED")) {
            new AlertDialog.Builder().setTitle("Remote saving and loading disabled").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Remote saving and loading is disabled. You can enable it in settings.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        CacheManager.getInstance().cacheSongbookVersionTimestamp();
        VCSAgent agent;
        if (a == null) {
            agent = new VCSAgent();
            int status = agent.compare();
            try {
                if (status == VCSAgent.STATUS_UP_TO_DATE) {
                    new AlertDialog.Builder().setTitle("Already up to date").setIcon(AlertDialog.Builder.Icon.INFO)
                            .setMessage("The remote version of the songbook matches the local version.")
                            .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                    return;
                } else if (status == VCSAgent.STATUS_BEHIND) {
                    UIManager.put("OptionPane.yesButtonText", "Overwrite");
                    UIManager.put("OptionPane.noButtonText", "Cancel");

                    int resultCode = JOptionPane.showConfirmDialog(null, "Overwrite remote changes?", "The remote version has more recent changes than the local version. Continuing may overwrite those changes if that have been made to the same files as have been locally edited. Do you want to proceed?", JOptionPane.YES_NO_OPTION);

                    if (resultCode != JOptionPane.YES_OPTION) {
                        UIManager.put("OptionPane.yesButtonText", "Yes");
                        UIManager.put("OptionPane.noButtonText", "No");
                        return;
                    }

                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    UIManager.put("OptionPane.noButtonText", "No");
                }
            } catch (Exception e) {
                new AlertDialog.Builder().setTitle("Failure").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage(String.format("Something went wrong while pushing the changes: %s", e.getMessage()))
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
        } else {
            //trust the custom agent it has already verified the state of the changes
            agent = a;
        }

        String token = Environment.getInstance().acquireToken();
        if (token == null || token.length() == 0) {
            token = requestOneTimeToken();
            if (token == null || token.length() == 0) {
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage("Invalid token!")
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
        }
        updateSongbookChangeLog();
        IndexBuilder indexBuilder = new IndexBuilder();
        Index remote = agent.getRemoteIndex(token);
        Index local = indexBuilder.createLocalIndex();
        CacheManager.getInstance().cacheIndex(local);
        if (local == null || remote == null) {
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Something went wrong")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        RequestFileAssembler RFAssembler = new RequestFileAssembler().assembleSaveFile(new IndexBuilder().createSaveIndex(local, remote), IndexBuilder.compareCollections(local, remote));
        Client client = new Client();
        client.postRequestFile((String) settings.get("REMOTE_DATA_UPLOAD_URL"), RFAssembler.getOutputFilePath(), token);

        if (client.getStatus().getCode() == Client.Status.SUCCESS) {
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage("Data uploaded successfully!")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        } else {
            new AlertDialog.Builder().setTitle("Failure").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("The data could not be uploaded. Check the application log for more information.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        }
    }

    @TODO
    public void loadRemoteChanges(VCSAgent a) throws IOException, NoSuchAlgorithmException {
        if (!(Boolean) settings.get("REMOTE_SAVE_LOAD_ENABLED")) {
            new AlertDialog.Builder().setTitle("Remote saving and loading disabled").setIcon(AlertDialog.Builder.Icon.INFO)
                            .setMessage("Remote saving and loading is disabled in the settings. Enable it and restart the client or read more in the documentation.")
                                    .addOkButton().build().open();
            return;
        }
        CacheManager.getInstance().cacheSongbookVersionTimestamp();
        VCSAgent agent;
        if (a == null) {
            agent = new VCSAgent();
            int status = agent.compare();
            try {
                if (status == VCSAgent.STATUS_UP_TO_DATE) {
                    new AlertDialog.Builder().setTitle("Already up to date").setIcon(AlertDialog.Builder.Icon.INFO)
                            .setMessage("The remote version of the songbook matches the local version.")
                            .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                    return;
                } else if (status == VCSAgent.STATUS_AHEAD) {
                    UIManager.put("OptionPane.yesButtonText", "Overwrite");
                    UIManager.put("OptionPane.noButtonText", "Cancel");

                    int resultCode = JOptionPane.showConfirmDialog(null, "Overwrite local changes?", "The local version has more recent changes than the remote version. Continuing may overwrite those changes if that have been made to the same files as have been locally edited. Do you want to proceed?", JOptionPane.YES_NO_OPTION);

                    if (resultCode != JOptionPane.YES_OPTION) {
                        UIManager.put("OptionPane.yesButtonText", "Yes");
                        UIManager.put("OptionPane.noButtonText", "No");
                        return;
                    }

                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    UIManager.put("OptionPane.noButtonText", "No");
                }
            } catch (Exception e) {
                new AlertDialog.Builder().setTitle("Failure").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage(String.format("Something went wrong while pulling the changes: %s", e.getMessage()))
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
        } else {
            //trust the custom agent it has already verified the state of the changes
            agent = a;
        }

        String token = Environment.getInstance().acquireToken();
        if (token == null || token.length() == 0) {
            token = requestOneTimeToken();
            if (token == null || token.length() == 0) {
                new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                        .setMessage("Invalid token.")
                        .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
                return;
            }
        }

        IndexBuilder indexBuilder = new IndexBuilder();
        Index remote = agent.getRemoteIndex(token);
        Index local = indexBuilder.createLocalIndex();
        CacheManager.getInstance().cacheIndex(local);
        if (local == null || remote == null) {
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Something went wrong.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        LoadIndex index = indexBuilder.createLoadIndex(local, remote);
        String indexFilePath = Paths.get((String) Environment.getInstance().getSettings().get("TEMP_FILE_PATH"), "index.json").toString();
        Misc.saveObjectToFileInJSON(index, new File(indexFilePath));
        Client client = new Client();
        String responseFilePath = client.getResponseFile((String) settings.get("REMOTE_DATA_UPLOAD_URL"), indexFilePath, token);

        if (client.getStatus().getCode() != Client.Status.SUCCESS) {
            new AlertDialog.Builder().setTitle("Failure").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .setMessage("The data could not be uploaded. Check the application log for more information.")
                                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        RequestFileAssembler disassembler = RequestFileAssembler.disassemble(responseFilePath);
        if (disassembler.success()) {
            local = indexBuilder.createLocalIndex();
            //local.setVersionTimestamp(disassembler.index().getVersionTimestamp());
            CacheManager.getInstance().cacheIndex(local);
            CacheManager.getInstance().cacheSongbookVersionTimestamp(local.getVersionTimestamp());
            new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage("Remote data loaded successfully.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
        }
        new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                .setMessage("Something went wring when loading the remote data. Check the application log for more information.")
                .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();

    }

    private String requestOneTimeToken() {
        JLabel label = new JLabel("Fill in the access token to access the remote server. Depending on the operation, the token must have READ or WRITE permission. The token will be used this time only!");
        JTextField token = new JPasswordField();
        token.setToolTipText("The authentication token.");
        Object[] message = {
                label,
                "Token:", token
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Load Remote Songbook", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return token.getText();
        }
        return null;
    }

    private void updateSongbookChangeLog() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(CacheManager.getInstance().getCachedSongbookVersionTimestamp()), ZoneId.systemDefault());
        final String date = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(CacheManager.getInstance().getCachedSongbookVersionTimestamp()), ZoneId.systemDefault()));
        final String username = System.getProperty("user.name");
        PrintWriter printWriter = new PrintWriter(new FileWriter((String) settings.get("CHANGE_LOG_FILE_PATH"), true));
        printWriter.write(date + " " + username + "\n");
        printWriter.close();
    }

    public VCSSettings getDefaultSettings() {
        VCSSettings settings = new VCSSettings();
        settings.put("REMOTE_DATA_DOWNLOAD_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/download/");
        settings.put("REMOTE_DATA_UPLOAD_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/upload/");
        settings.put("REMOTE_DATA_INDEX_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/index/");
        settings.put("REMOTE_DATA_VERSION_TIMESTAMP_URL", "http://beta-hrabozpevnik.clanweb.eu/api/data/version-timestamp/");
        settings.put("REMOTE_SAVE_LOAD_ENABLED", Boolean.FALSE);
        settings.put("REQUEST_ZIP_TEMP_FILE_PATH", Paths.get((String) Environment.getInstance().getSettings().get("TEMP_FILE_PATH"), "request.zip").toString());
        settings.put("VCS_CACHE_FILE_PATH", Paths.get(System.getProperty("user.dir"), "vcs").toString());
        settings.put("LOCAL_INDEX_FILE_PATH", Paths.get((String) settings.get("VCS_CACHE_FILE_PATH"), "index.json").toString());
        settings.put("VCS_THREAD_COUNT", 20);
        settings.put("CHANGE_LOG_FILE_PATH", Paths.get((String) Environment.getInstance().getSettings().get("DATA_FILE_PATH"), "change_log.txt").toString());
        settings.put("VERSION_TIMESTAMP_FILE_PATH", Paths.get((String) settings.get("VCS_CACHE_FILE_PATH"), "timestamp.txt").toString());
        return settings;
    }

    public VCSSettings getSettings() {
        return settings;
    }

    public void setSettings(final VCSSettings s) {
        if (s == null) {
            return;
        }
        settings = s;
    }

    public static class VCSSettings extends HashMap<String, Object> implements Serializable {
        private static final Logger logger = LogManager.getLogger(VCSSettings.class);
    }
}
