package attilathehun.songbook.vcs.index;

import com.google.gson.annotations.SerializedName;
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
    @Deprecated(forRemoval = true)
    @SerializedName("version_timestamp")
    private long versionTimestamp;

    public LoadIndex() {

    }

    public static LoadIndex empty() {
        LoadIndex index = new LoadIndex();
        index.setMissing(new Property());
        index.setOutdated(new Property());
        index.setCollections(new ArrayList<>());
        index.versionTimestamp = -1;
        return index;
    }

    public Property getMissing() {
        return missing;
    }

    public void setMissing(final Property missing) {
        this.missing = missing;
    }

    public Property getOutdated() {
        return outdated;
    }

    public void setOutdated(final Property outdated) {
        this.outdated = outdated;
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
}
