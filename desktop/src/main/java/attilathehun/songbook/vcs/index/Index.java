package attilathehun.songbook.vcs.index;

import java.io.Serializable;

/**
 * This class represents an index of the songbook version control system. Upon versioning, the remote and the local indices are compared
 * and from their differences are then generated partial request indices.
 */
public class Index implements Serializable {

    public CompoundProperty data;
    public CompoundProperty hashes;
    public CompoundProperty metadata;
    public CompoundProperty collections;
    public CompoundProperty defaultSettings;
    public long versionTimestamp;

    /**
     * An absolutely pointless constructor, use the default one instead.
     * @param object null object
     */
    public Index(Object object) {
        if (object != null) {
            throw new IllegalArgumentException();
        }
    }

    public void setData(CompoundProperty data) {
        this.data = data;
    }

    public void setCollections(CompoundProperty collections) {
        this.collections = collections;
    }

    public void setDefaultSettings(CompoundProperty defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    public void setHashes(CompoundProperty hashes) {
        this.hashes = hashes;
    }

    public void setMetadata(CompoundProperty metadata) {
        this.metadata = metadata;
    }

    public CompoundProperty getCollections() {
        return collections;
    }

    public CompoundProperty getData() {
        return data;
    }

    public CompoundProperty getDefaultSettings() {
        return defaultSettings;
    }

    public CompoundProperty getHashes() {
        return hashes;
    }

    public CompoundProperty getMetadata() {
        return metadata;
    }

    public void setVersionTimestamp(long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
    }

    public long getVersionTimestamp() {
        return versionTimestamp;
    }
}

