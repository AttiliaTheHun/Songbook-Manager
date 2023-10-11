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

    public LoadIndex(SimpleProperty[] missingSongs, SimpleProperty[] outdatedSongs) {
        SimpleProperty[] missing = {new ArrayProperty<SimpleProperty>(missingSongs)};
        this.missing = new CompoundProperty(missing);
        SimpleProperty[] outdated = {new ArrayProperty<SimpleProperty>(outdatedSongs)};
        this.outdated = new CompoundProperty(outdated);
    }
}
