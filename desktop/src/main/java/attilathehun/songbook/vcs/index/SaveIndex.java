package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the object of metadata about a save request to the server.
 */
public class SaveIndex extends PartialIndex {
    private static final Logger logger = LogManager.getLogger(SaveIndex.class);

    private CompoundProperty additions;
    private CompoundProperty deletions;
    private CompoundProperty changes;
    private CompoundProperty collections;

    private long versionTimestamp;

    public SaveIndex(long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
        this.additions = new CompoundProperty();
        this.deletions = new CompoundProperty();
        this.collections = new CompoundProperty();
        this.changes = new CompoundProperty();
    }

    public void setAdditions(CompoundProperty additions) {
        this.additions = additions;
    }

    public void setDeletions(CompoundProperty deletions) {
        this.deletions = deletions;
    }

    public void setChanges(CompoundProperty changes) {
        this.changes = changes;
    }

    public void setCollections(CompoundProperty collections) {
        this.collections = collections;
    }

    public CompoundProperty getAdditions() {
        return additions;
    }

    public CompoundProperty getDeletions() {
        return deletions;
    }

    public CompoundProperty getChanges() {
        return changes;
    }

    public CompoundProperty getCollections() {
        return collections;
    }

    public long getVersionTimestamp() {
        return versionTimestamp;
    }
}
