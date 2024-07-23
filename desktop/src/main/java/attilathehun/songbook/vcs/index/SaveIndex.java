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

    public SaveIndex() {};

    public SaveIndex(long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
    }

    /**
     * Creates an empty save index that is fully compatible with the rest of the framework.
     *
     * @param collections the collections to include in the index
     * @return the empty index
     */
    public static SaveIndex empty(final Collection<String> collections) {
        final SaveIndex index = new SaveIndex(-1);
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

    public void setAdditions(final Property additions) {
        this.additions = additions;
    }

    public Property getDeletions() {
        return deletions;
    }

    public void setDeletions(final Property deletions) {
        this.deletions = deletions;
    }

    public Property getChanges() {
        return changes;
    }

    public void setChanges(final Property changes) {
        this.changes = changes;
    }

    public Collection<String> getCollections() {
        return collections;
    }

    public void setCollections(final Collection<String> collections) {
        this.collections = collections;
    }

    public long getVersionTimestamp() {
        return versionTimestamp;
    }

    public void setVersionTimestamp(final long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
    }

    @Override
    public boolean isEmpty() {
        if (!additions.isEffectivelyEmpty()) {
            return false;
        }
        if (!deletions.isEffectivelyEmpty()) {
            return false;
        }
        if (!changes.isEffectivelyEmpty()) {
            return false;
        }
        return collections.isEmpty();
    }
}
