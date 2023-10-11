package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * Simple property is just a wrapper around the actual value. It can represent an arbitrary data type which will
 * be subsequently serialized as JSON, giving us JavaScript-like freedom to structure JSON structures.
 * @param <T> the data we want to label
 */
public class SimpleProperty<T> implements Property, Serializable {

    private static final Logger logger = LogManager.getLogger(SimpleProperty.class);

    private T content;

    public SimpleProperty(T content) {
        this.content = content;
    }

    @Override
    public T getContent() {
        return  content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Property<?>)) {
            return false;
        }

        return ((Property<?>) o).getContent().equals(content);
    }

}
