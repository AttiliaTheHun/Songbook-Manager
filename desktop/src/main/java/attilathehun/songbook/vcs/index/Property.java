package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;

/**
 * A Property is a HashMap to simplify structuring JSON structures.
 */
public class Property extends HashMap<String, Object> {
    private static final Logger logger = LogManager.getLogger(Property.class);

    public Property() { }

    public Property(final HashMap<String, ?> data) {
        this.putAll(data);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Property)) {
            return false;
        }

        if (!((Property) o).keySet().equals(keySet())) {
            return false;
        }

        return ((Property) o).values().equals(values());
    }

    /**
     * Checks whether the property is effectively empty. Effectively empty does not mean its size is 0, because there may be some obligatory structures that need to be present
     * every time the property is used. Instead, effectively empty means there is no actual data stored in the property.
     *
     * @return true if the property is effectively empty, false otherwise
     */
    public boolean isEffectivelyEmpty() {
        for (final Object o : values()) {
            if (o instanceof Collection) {
                if (((Collection) o).size() != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
