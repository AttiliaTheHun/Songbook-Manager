package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public abstract class Property<T> implements Serializable {

    private static final Logger logger = LogManager.getLogger(Property.class);

    public abstract T getContent();
}
