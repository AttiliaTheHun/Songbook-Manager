package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * A Property is a HashMap to simplify structuring JSON structures.
 */
public class Property extends HashMap<String, Object> {
    private static final Logger logger = LogManager.getLogger(Property.class);

    public Property() {
    }

    public Property(HashMap<String, ? extends Object> data) {
        this.putAll(data);
    }

    @Override
    public boolean equals(Object o) {
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
}
