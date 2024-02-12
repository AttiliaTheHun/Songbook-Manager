package attilathehun.songbook.vcs.index;

import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents the object of metadata about a save request to the server.
 */
public class SaveIndex extends PartialIndex {
    private static final Logger logger = LogManager.getLogger(SaveIndex.class);

    private Property additions;
    private Property deletions;
    private Property changes;
    private Collection<String> collections;
    @SerializedName("version_timestamp")
    private long versionTimestamp;

    public SaveIndex(long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
    }

    public static SaveIndex empty(final Collection<String> collections) {
        SaveIndex index = new SaveIndex(-1);
        index.setAdditions(new Property());
        index.setCollections(new ArrayList<>());
        index.setChanges(new Property());
        index.setDeletions(new Property());
        for (final String collection : collections) {
            index.getAdditions().put(collection, new ArrayList<>());
            index.getDeletions().put(collection, new ArrayList<>());
            index.getChanges().put(collection, new ArrayList<>());
        }
        return index;
    }

    public Property getAdditions() {
        return additions;
    }

    public void setAdditions(Property additions) {
        this.additions = additions;
    }

    public Property getDeletions() {
        return deletions;
    }

    public void setDeletions(Property deletions) {
        this.deletions = deletions;
    }

    public Property getChanges() {
        return changes;
    }

    public void setChanges(Property changes) {
        this.changes = changes;
    }

    public Collection<String> getCollections() {
        return collections;
    }

    public void setCollections(Collection<String> collections) {
        this.collections = collections;
    }

    public long getVersionTimestamp() {
        return versionTimestamp;
    }
}
