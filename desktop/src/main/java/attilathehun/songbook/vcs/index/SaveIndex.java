package attilathehun.songbook.vcs.index;

import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the object of metadata about a save request to the server.
 */
public class SaveIndex extends PartialIndex {
    private static final Logger logger = LogManager.getLogger(SaveIndex.class);

    private Property additions;
    private Property deletions;
    private Property changes;
    private Property collections;
    @SerializedName("version_timestamp")
    private long versionTimestamp;

    public SaveIndex(long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
        this.additions = new Property();
        this.deletions = new Property();
        this.collections = new Property();
        this.changes = new Property();
    }

    public void setAdditions(Property additions) {
        this.additions = additions;
    }

    public void setDeletions(Property deletions) {
        this.deletions = deletions;
    }

    public void setChanges(Property changes) {
        this.changes = changes;
    }

    public void setCollections(Property collections) {
        this.collections = collections;
    }

    public Property getAdditions() {
        return additions;
    }

    public Property getDeletions() {
        return deletions;
    }

    public Property getChanges() {
        return changes;
    }

    public Property getCollections() {
        return collections;
    }

    public long getVersionTimestamp() {
        return versionTimestamp;
    }
}
