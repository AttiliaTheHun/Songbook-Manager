package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents the object of metadata about a load request to the server.
 */
public class LoadIndex extends PartialIndex {

    private static final Logger logger = LogManager.getLogger(LoadIndex.class);
    private Property missing;
    private Property outdated;
    private Collection<String> collections;

    public LoadIndex() {

    }


    public void setMissing(Property missing) {
        this.missing = missing;
    }

    public void setOutdated(Property outdated) {
        this.outdated = outdated;
    }

    public void setCollections(Collection<String> collections) {
        this.collections = collections;
    }

    public Property getMissing() {
        return missing;
    }

    public Property getOutdated() {
        return outdated;
    }

    public Collection<String> getCollections() {
        return collections;
    }

    public static LoadIndex empty() {
        LoadIndex index = new LoadIndex();
        index.setMissing(new Property());
        index.setOutdated(new Property());
        index.setCollections(new ArrayList<>());
        return index;
    }
}
