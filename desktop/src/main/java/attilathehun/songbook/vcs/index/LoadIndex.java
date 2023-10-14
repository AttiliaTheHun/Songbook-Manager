package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the object of metadata about a load request to the server.
 */
public class LoadIndex extends PartialIndex {

    private static final Logger logger = LogManager.getLogger(LoadIndex.class);
    private CompoundProperty missing;
    private CompoundProperty outdated;

    public LoadIndex() {
       this.missing = new CompoundProperty();
       this.outdated = new CompoundProperty();
    }


    public void setMissing(CompoundProperty missing) {
        this.missing = missing;
    }

    public void setOutdated(CompoundProperty outdated) {
        this.outdated = outdated;
    }


    public CompoundProperty getMissing() {
        return missing;
    }

    public CompoundProperty getOutdated() {
        return outdated;
    }
}
