package attilathehun.songbook.vcs.index;

import java.io.Serializable;

/**
 * A partial index as opposed to full index serves as an index to the changes made upon the songbook rather than
 * as an index to the songbook itself. Partial indices are constructed only at the client side, the server
 * never does any changes to the songbook.
 */
public abstract class PartialIndex implements Serializable { }
