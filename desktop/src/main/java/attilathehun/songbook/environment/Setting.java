package attilathehun.songbook.environment;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


public final class Setting<T> implements Serializable {
    public static final int TYPE_STRING = 0;
    public static final int TYPE_NON_EMPTY_STRING = 1;
    public static final int TYPE_BOOLEAN = 2;
    public static final int TYPE_DOUBLE = 3;
    public static final int TYPE_INTEGER = 4;
    public static final int TYPE_POSITIVE_INTEGER = 5;
    public static final int TYPE_URL = 6;
    public static final int TYPE_URL_ALLOW_EMPTY = 7;
    public static final int TYPE_OTHER = 8;


    private final String name;
    private T value;
    private final transient T def;
    private final transient int type;
    private final transient String description;
    private final transient String inputFormatDescription;

    public Setting (final String name, final T value) {
        this.name = name;
        this.value = value;
        this.def = null;
        this.description = null;
        this.inputFormatDescription = null;
        this.type = TYPE_OTHER;
    }

    public Setting (final String name, final T value, final String description, final String inputFormatDescription) {
        this.name = name;
        this.value = value;
        this.def = null;
        this.type = TYPE_OTHER;
        this.description = description;
        this.inputFormatDescription = inputFormatDescription;
    }

    public Setting (final String name, final T value, final int type, final String description, final String inputFormatDescription) {
        this.name = name;
        this.value = value;
        this.def = null;
        this.type = type;
        this.description = description;
        this.inputFormatDescription = inputFormatDescription;
    }

    public Setting (final String name, final T value, final T def, final int type, final String description, final String inputFormatDescription) {
        this.name = name;
        this.value = value;
        this.def = def;
        this.type = type;
        this.description = description;
        this.inputFormatDescription = inputFormatDescription;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public void set(final Object value) {
        if (type == TYPE_INTEGER || type == TYPE_POSITIVE_INTEGER) {
            try {
                this.value = (T) Integer.valueOf((int) Math.round((Double) value));
                return;
            } catch (final Exception e) {}
            try {
                this.value = (T) Integer.valueOf((String) value);
                return;
            } catch (final Exception e) {}

        }
        this.value = (T) value;
    }

    /**
     * Returns the type of the setting. The type determines the way {@link #verifyValue(String)} performs the verification. The possible values are {@link #TYPE_STRING},
     * {@link #TYPE_NON_EMPTY_STRING}, {@link #TYPE_BOOLEAN}, {@link #TYPE_DOUBLE}, {@link #TYPE_INTEGER}, {@link #TYPE_POSITIVE_INTEGER}, {@link #TYPE_URL}, {@link #TYPE_OTHER}.
     *
     * @return the type of the setting
     */
    public int getType() {
        return type;
    }

    public boolean hasDefaultValue() {
        return def != null;
    }

    /**
     * Sets the default value of the setting as its actual value. Can only be performed on settings with a default value as it will
     * throw and exception otherwise.
     * @throws {@link UnsupportedOperationException} if the setting does not have a default value
     */
    public void setDefault() {
        if (!hasDefaultValue()) {
            throw new UnsupportedOperationException("Cannot use default value of a non-default setting");
        }
        this.value = def;
    }

    /**
     * Verifies whether the string input is in line with the constraints of the Setting type.
     *
     * @param s the input to be checked
     * @return true if the input is acceptable as a new value, false otherwise
     */
    public boolean verifyValue(final String s) {
        switch(type) {
            case TYPE_STRING -> {
                return true;
            }
            case TYPE_NON_EMPTY_STRING -> {
                return s.length() > 0;
            }
            case TYPE_BOOLEAN -> {
                if (Boolean.TRUE.toString().equalsIgnoreCase(s.trim())) {
                    return true;
                }
                return Boolean.FALSE.toString().equalsIgnoreCase(s.trim());
            }
            case TYPE_DOUBLE -> {
                try {
                    Double.parseDouble(s);
                }catch (final NumberFormatException e) {
                    return false;
                }
            }
            case TYPE_INTEGER -> {
                try {
                    Integer.parseInt(s);
                } catch (final NumberFormatException e) {
                    return false;
                }
            }
            case TYPE_POSITIVE_INTEGER -> {
                try {
                    return Integer.parseInt(s) > 0;
                } catch (final NumberFormatException e) {
                    return false;
                }
            }
            case TYPE_URL -> {
                try {
                    if (s.length() == 0) {
                        return false;
                    }
                    new URL(s);
                    return true;
                } catch (final MalformedURLException e) {
                    return false;
                }
            }
            case TYPE_URL_ALLOW_EMPTY -> {
                try {
                    if (s.length() == 0) {
                        return true;
                    }
                    new URL(s);
                    return true;
                } catch (final MalformedURLException e) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns the description of the setting (what part of the application it controls).
     *
     * @return setting description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the description of the expected value format that should be displayed to the user when editing the setting.
     *
     * @return description of the value format
     */
    public String getInputFormatDescription() {
        return inputFormatDescription;
    }

}
