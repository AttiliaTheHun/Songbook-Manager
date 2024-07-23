package attilathehun.songbook.vcs;

import attilathehun.songbook.collection.CollectionManager;
import attilathehun.songbook.environment.Environment;
import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.util.Misc;
import attilathehun.songbook.vcs.index.Index;
import attilathehun.songbook.vcs.index.LoadIndex;
import attilathehun.songbook.vcs.index.SaveIndex;
import attilathehun.songbook.window.AlertDialog;
import attilathehun.songbook.window.SongbookApplication;
import attilathehun.songbook.window.SongbookController;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * The Version Control System Administrator. This class is the main class of the Version Control System and provides a simple API to be used from other components.
 * The parts of the version control system are not meant to be used directly.
 */
public final class VCSAdmin {
    private static final Logger logger = LogManager.getLogger(VCSAdmin.class);
    private static final VCSAdmin INSTANCE = new VCSAdmin();
    public static final String CHANGE_LOG_FILE_PATH = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"), "local", "changelog.txt").toString();
    public static final boolean FLAG_USE_CACHE = true;


    private VCSAdmin() {}

    public static VCSAdmin getInstance() {
        return INSTANCE;
    }

    public void push() {
        push(null);
    }


    public void push(final VCSAgent agent) {
        try {
            saveLocalChanges2(agent);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void pull() {
        pull(null);
    }

    public void pull(final VCSAgent agent) {
        try {
            loadRemoteChanges2(agent);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveLocalChanges2(final VCSAgent a) throws Exception {
        if (!(Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
            new AlertDialog.Builder().setTitle("Remote saving and loading disabled").setIcon(AlertDialog.Builder.Icon.WARNING)
                    .setMessage("Remote saving and loading is disabled. You can enable it in settings.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }

        CacheManager.getInstance().cacheSongbookVersionTimestamp();

        final VCSAgent agent = (a == null) ? new VCSAgent() : a;
        final int status = agent.compare();

        if (status == VCSAgent.STATUS_UP_TO_DATE) {
            new AlertDialog.Builder().setTitle("Already up to date").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage("The remote version of the songbook matches the local version.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        } else if (status == VCSAgent.STATUS_BEHIND) {
            final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Overwrite remote changes?").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .setMessage("The remote version of the songbook has some changes that are not present in the local version. If the changes have been made to the same files you are trying to upload to the server, these changes will be lost!")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton("Overwrite").addCloseButton("Cancel").build().awaitResult();
            if (result.get() != AlertDialog.RESULT_OK) {
                return;
            }
        }

        updateSongbookChangeLog();

        final IndexBuilder indexBuilder = new IndexBuilder();
        boolean useCachedRemoteIndex = false;

        final Index cachedRemoteIndex = CacheManager.getInstance().getCachedRemoteIndex();
        if (FLAG_USE_CACHE) {
            if (cachedRemoteIndex != null) {
                if (cachedRemoteIndex.getVersionTimestamp() == agent.getLastRemoteVersionTimestamp()) {
                    useCachedRemoteIndex = true;
                }
            }
        }
        final Index remote = (useCachedRemoteIndex) ? cachedRemoteIndex : agent.getRemoteIndex();

        boolean useCachedLocalIndex = false;
        final Index cachedLocalIndex = CacheManager.getInstance().getCachedIndex();

        if (FLAG_USE_CACHE) {
            if (cachedLocalIndex != null) {
                if (cachedLocalIndex.getVersionTimestamp() == CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                    useCachedLocalIndex = true;
                }
            }
        }

        final Index local = (useCachedRemoteIndex) ? cachedRemoteIndex : indexBuilder.createLocalIndex();

        if (!useCachedLocalIndex){
            CacheManager.getInstance().cacheIndex(local);
        }

        if (local == null || remote == null) {
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Something went wrong when indexing the changes.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        final SaveIndex saveIndex = indexBuilder.createSaveIndex(local, remote);

        if (saveIndex.isEmpty()) {
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                    .setMessage("There appear to be no changes in the local version of the songbook, but for some reason, the timestamps do not match!")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }

        final RequestFileAssembler RFAssembler = new RequestFileAssembler().assembleSaveFile(saveIndex, IndexBuilder.compareCollections(local, remote));
        agent.saveDataRemotely(RFAssembler.getOutputFilePath());


    }



    @Deprecated(forRemoval = true)
    private void saveLocalChanges(VCSAgent a) throws Exception {
        if (!(Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
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
        Index remote = agent.getRemoteIndex();
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
        client.postRequestFile(SettingsManager.getInstance().getValue("REMOTE_DATA_UPLOAD_URL"), RFAssembler.getOutputFilePath(), token);

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

    private void loadRemoteChanges2(final VCSAgent a) throws Exception {
        if (!(Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
            new AlertDialog.Builder().setTitle("Remote saving and loading disabled").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage("Remote saving and loading is disabled in the settings. Enable it and restart the client or read more in the documentation.")
                    .addOkButton().build().open();
            return;
        }

        CacheManager.getInstance().cacheSongbookVersionTimestamp();

        final VCSAgent agent = (a == null) ? new VCSAgent() : a;
        final int status = agent.compare();

        if (status == VCSAgent.STATUS_UP_TO_DATE) {
            new AlertDialog.Builder().setTitle("Already up to date").setIcon(AlertDialog.Builder.Icon.INFO)
                    .setMessage("The remote version of the songbook matches the local version.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        } else if (status == VCSAgent.STATUS_AHEAD) {
            final CompletableFuture<Integer> result = new AlertDialog.Builder().setTitle("Overwrite local changes?").setIcon(AlertDialog.Builder.Icon.CONFIRM)
                    .setMessage("The local version of the songbook has some changes that are not present in the remote version. If the changes have been made to the same files you are trying to download from the server, these changes will be lost!")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton("Overwrite").addCloseButton("Cancel").build().awaitResult();
            if (result.get() != AlertDialog.RESULT_OK) {
                return;
            }
        }

        final IndexBuilder indexBuilder = new IndexBuilder();
        boolean useCachedRemoteIndex = false;

        final Index cachedRemoteIndex = CacheManager.getInstance().getCachedRemoteIndex();
        if (FLAG_USE_CACHE) {
            if (cachedRemoteIndex != null) {
                if (cachedRemoteIndex.getVersionTimestamp() == agent.getLastRemoteVersionTimestamp()) {
                    useCachedRemoteIndex = true;
                }
            }
        }
        final Index remote = (useCachedRemoteIndex) ? cachedRemoteIndex : agent.getRemoteIndex();

        boolean useCachedLocalIndex = false;
        final Index cachedLocalIndex = CacheManager.getInstance().getCachedIndex();

        if (FLAG_USE_CACHE) {
            if (cachedLocalIndex != null) {
                if (cachedLocalIndex.getVersionTimestamp() == CacheManager.getInstance().getCachedSongbookVersionTimestamp()) {
                    useCachedLocalIndex = true;
                }
            }
        }

        Index local;
        if (useCachedLocalIndex)  {
            local = cachedLocalIndex;
        } else {
            local = indexBuilder.createLocalIndex();
            CacheManager.getInstance().cacheIndex(local);
        }


        if (local == null || remote == null) {
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Something went wrong when indexing the changes.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        final LoadIndex loadIndex = indexBuilder.createLoadIndex(local, remote);

        if (loadIndex.isEmpty()) {
            new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                    .setMessage("There appear to be no changes in the remote version of the songbook, but for some reason, the timestamps do not match!")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }

        final String responseFilePath = agent.loadRemoteData(loadIndex);

        if (responseFilePath == null) {
            return; // the agent has already shown the error message
        }

        if (!RequestFileAssembler.disassemble(responseFilePath, loadIndex)) {
            throw new RuntimeException("could not disassemble to request file");
        }

        local = indexBuilder.createLocalIndex();
        local.setVersionTimestamp(remote.getVersionTimestamp());
        CacheManager.getInstance().cacheIndex(local);
        CacheManager.getInstance().cacheSongbookVersionTimestamp(local.getVersionTimestamp());

        // we need to refresh some components to make the changes visible and persistent
        for (final CollectionManager manager : Environment.getInstance().getRegisteredManagers().values()) {
            manager.init();
        }
        Environment.getInstance().refresh();

        new AlertDialog.Builder().setTitle("Success").setIcon(AlertDialog.Builder.Icon.INFO)
                .setMessage("Remote data loaded successfully.")
                .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
    }

    @Deprecated(forRemoval = true)
    public void loadRemoteChanges(VCSAgent a) throws Exception {
        if (!(Boolean) SettingsManager.getInstance().getValue("REMOTE_SAVE_LOAD_ENABLED")) {
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
        Index remote = agent.getRemoteIndex();
        Index local = indexBuilder.createLocalIndex();
        CacheManager.getInstance().cacheIndex(local);
        if (local == null || remote == null) {
            new AlertDialog.Builder().setTitle("Error").setIcon(AlertDialog.Builder.Icon.ERROR)
                    .setMessage("Something went wrong.")
                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        LoadIndex index = indexBuilder.createLoadIndex(local, remote);
        String indexFilePath = Paths.get(SettingsManager.getInstance().getValue("TEMP_FILE_PATH"), "index.json").toString();
        Misc.saveObjectToFileInJSON(index, new File(indexFilePath));
        Client client = new Client();
        String responseFilePath = client.getResponseFile(SettingsManager.getInstance().getValue("REMOTE_DATA_UPLOAD_URL"), indexFilePath, token);

        if (client.getStatus().getCode() != Client.Status.SUCCESS) {
            new AlertDialog.Builder().setTitle("Failure").setIcon(AlertDialog.Builder.Icon.ERROR)
                            .setMessage("The data could not be uploaded. Check the application log for more information.")
                                    .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();
            return;
        }
        RequestFileAssembler.disassemble(responseFilePath, index);

        new AlertDialog.Builder().setTitle("Warning").setIcon(AlertDialog.Builder.Icon.WARNING)
                .setMessage("Something went wring when loading the remote data. Check the application log for more information.")
                .setParent(SongbookApplication.getMainWindow()).addOkButton().build().open();

    }

    @Deprecated(forRemoval = true)
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

    /**
     * Adds an entry about the current user and their last changes to the songbook changelog file.
     *
     * @throws IOException
     */
    private void updateSongbookChangeLog() throws IOException {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        final String date = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(CacheManager.getInstance().getCachedSongbookVersionTimestamp()), ZoneId.systemDefault()));
        final String username = System.getProperty("user.name");
        final PrintWriter printWriter = new PrintWriter(new FileWriter(CHANGE_LOG_FILE_PATH, true));
        printWriter.write(date + " " + username + "\n");
        printWriter.close();
    }

}
