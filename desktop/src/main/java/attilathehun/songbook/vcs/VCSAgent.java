package attilathehun.songbook.vcs;

import attilathehun.songbook.vcs.index.Index;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VCSAgent {
    private static final Logger logger = LogManager.getLogger(VCSAgent.class);
    private Index remoteIndex = null;

    protected VCSAgent() {

    }

    public void runDiagnostics() {

    }

    public boolean verifyRemoteChanges() {
        return false;
    }

    /**
     * Checks whether there are changes to the songbook that have not been uploaded on the server. Performs versionTimestamp comparison.
     * @return true if there are changes to upload; false otherwise
     */
    public boolean verifyLocalChanges() {
        // check both version timestamp and zip hash?
        // set remoteIndex
        return false;
    }

    public Index getRemoteIndex() {
        return remoteIndex;
    }
}
