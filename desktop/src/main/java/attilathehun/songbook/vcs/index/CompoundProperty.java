package attilathehun.songbook.vcs.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * A compound property is a HashMap of Properties to simplify structuring JSON structures.
 */
public class CompoundProperty extends HashMap<String, Property> implements Property {

    private static final Logger logger = LogManager.getLogger(CompoundProperty.class);



    @Override
    public HashMap<String, Property> getContent() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof CompoundProperty)) {
            return false;
        }

        if (!((CompoundProperty) o).keySet().equals(keySet())) {
            return false;
        }

        return ((CompoundProperty) o).values().equals(values());
    }
}
