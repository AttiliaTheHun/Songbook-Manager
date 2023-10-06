package attilathehun.songbook.vcs.index;

import attilathehun.songbook.environment.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class CompoundProperty extends Property {

    private static final Logger logger = LogManager.getLogger(CompoundProperty.class);
    public Property[] content;

    public CompoundProperty(Property[] content) {
        this.content = content;
    }

    public CompoundProperty() {
        this.content = new Property[0];
    }

    public void addProperty(Property property) {
        if (property == null) {
            throw new IllegalArgumentException();
        }
        this.content = Arrays.copyOf(content, content.length + 1);
        this.content[this.content.length - 1] = property;
    }

    public void setProperties(Property[] properties) {
        this.content = properties;
    }

    @Override
    public Object[] getContent() {
        return content;
    }
}
