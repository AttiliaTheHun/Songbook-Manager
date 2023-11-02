package attilathehun.songbook.vcs.index;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * This class' instance represents an index of the songbook version control system. Upon versioning, the remote and the local indices are compared
 * and from their differences are then generated partial request indices.
 */
public class Index implements Serializable {

    private Property data;
    private Property hashes;
    private Property metadata;
    private Property collections;
    @SerializedName("version_timestamp")
    private long versionTimestamp;

    /**
     * An absolutely pointless constructor, use the default one instead.
     * @param object null object
     */
    public Index(Object object) {
        if (object != null) {
            throw new IllegalArgumentException();
        }
    }

    public void setData(Property data) {
        this.data = data;
    }

    public void setCollections(Property collections) {
        this.collections = collections;
    }


    public void setHashes(Property hashes) {
        this.hashes = hashes;
    }

    public void setMetadata(Property metadata) {
        this.metadata = metadata;
    }

    public Property getCollections() {
        return collections;
    }

    public Property getData() {
        return data;
    }

    public Property getHashes() {
        return hashes;
    }

    public Property getMetadata() {
        return metadata;
    }

    public long getVersionTimestamp() {
        return versionTimestamp;
    }

    public void setVersionTimestamp(long versionTimestamp) {
        this.versionTimestamp = versionTimestamp;
    }
}

