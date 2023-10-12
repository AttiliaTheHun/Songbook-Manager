package attilathehun.songbook.vcs;

public class VCSAgent {
    private static final Logger logger = LogManager.getLogger(VCSAgent.class);

    protected VCSAgent() {

    }

    public void runDiagnostics() {

    }

    public boolean verifyRemoteChanges() {
        return false;
    }

    public boolean verifyLocalChanges() {
        // check both version timestamp and zip hash?
        return false;
    }

}
