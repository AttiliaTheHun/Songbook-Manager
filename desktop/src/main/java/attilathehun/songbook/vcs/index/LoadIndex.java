package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the object of metadata about a load request to the server.
 */
public class LoadIndex extends PartialIndex {

    private static final Logger logger = LogManager.getLogger(LoadIndex.class);
    private Property missing;
    private Property outdated;

    public LoadIndex() {
       this.missing = new Property();
       this.outdated = new Property();
    }


    public void setMissing(Property missing) {
        this.missing = missing;
    }

    public void setOutdated(Property outdated) {
        this.outdated = outdated;
    }


    public Property getMissing() {
        return missing;
    }

    public Property getOutdated() {
        return outdated;
    }
}
