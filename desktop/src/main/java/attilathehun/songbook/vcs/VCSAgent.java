package attilathehun.songbook.vcs;

public class VCSAgent {

    public boolean verifyRemoteChanges() {
        return false;
    }

    public boolean verifyLocalChanges() {
        // check both version timestamp and zip hash?
        return false;
    }

}
