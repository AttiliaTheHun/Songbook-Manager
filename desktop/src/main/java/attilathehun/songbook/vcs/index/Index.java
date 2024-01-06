package attilathehun.songbook.vcs.index;

import attilathehun.songbook.collection.EasterCollectionManager;
import attilathehun.songbook.collection.StandardCollectionManager;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

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

    public Index() {}

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

    /**
     * Creates an empty Index object that is fully compatible with the framework and contains no or default values.
     * @return well-formed empty Index object
     */
    public static Index empty() {
        Index index = new Index();
        index.setData(new Property());
        index.getData().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>());
        index.getData().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>());
        index.setHashes(new Property());
        index.getHashes().put(StandardCollectionManager.getInstance().getCollectionName(), new ArrayList<>());
        index.getHashes().put(EasterCollectionManager.getInstance().getCollectionName(), new ArrayList<>());
        index.setMetadata(new Property());
        index.setCollections(new Property());
        index.getCollections().put(StandardCollectionManager.getInstance().getCollectionName(), "");
        index.getCollections().put(EasterCollectionManager.getInstance().getCollectionName(), "");
        index.setVersionTimestamp(-1);
        return index;
    }

}

