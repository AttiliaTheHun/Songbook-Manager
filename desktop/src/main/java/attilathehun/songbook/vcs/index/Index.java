package attilathehun.songbook.vcs.index;

import java.io.Serializable;

/**
 * This class represents an index of the songbook from the server perspective. This class should never be instantiated directly,
 * only through deserialization.
 */
public class Index implements Serializable {
    public CompoundProperty data;
    public CompoundProperty hashes;
    public ArrayProperty metadata;
    public ArrayProperty collections;
    public ArrayProperty defaultSettings;
}
