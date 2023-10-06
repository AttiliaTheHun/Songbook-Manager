package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArrayProperty<T> extends Property {

    private static final Logger logger = LogManager.getLogger(ArrayProperty.class);
    private T[] content;

    public ArrayProperty(T[] content) {
        this.content = content;
    }

    @Override
    public Object[] getContent() {
        return content;
    }
}
