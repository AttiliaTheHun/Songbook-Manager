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

    public LoadIndex(Property[] missingSongs, Property[] outdatedSongs) {
        Property[] missing = {new ArrayProperty<Property>(missingSongs)};
        this.missing = new CompoundProperty(missing);
        Property[] outdated = {new ArrayProperty<Property>(outdatedSongs)};
        this.outdated = new CompoundProperty(outdated);
    }
}
