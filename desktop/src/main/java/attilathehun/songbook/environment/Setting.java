package attilathehun.songbook.environment;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class represents the actual settings that are used to modify the program's behavior.
 *
 * @param <T> the data type the setting keeps
 */
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
    @Deprecated
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

    /**
     * Returns the setting name.
     *
     * @return the setting name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current value of the setting.
     *
     * @return the setting value
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the new value of the setting.
     *
     * @param value the new setting value
     */
    public void setValue(final T value) {
        this.value = value;
    }

    /**
     * Sets the value of the setting to the new value. If the new value type is incompatible with the setting, this method will throw an exception.
     *
     * @param value the new setting value
     * @throws ClassCastException if the new value type is incorrect
     */
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

    /**
     * Returns true if the setting has a default value.
     *
     * @return true if it has a default value
     */
    public boolean hasDefaultValue() {
        return def != null;
    }

    /**
     * Sets the default value of the setting as its actual value. Can only be performed on settings with a default value as it will
     * throw and exception otherwise.
     *
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
                    // file URLs somehow do not work as standard URLs
                    try {
                        new File(s).toURI().toURL();
                        return true;
                    } catch (final MalformedURLException e2) {

                    }
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
        // signifies TYPE_CUSTOM
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
     * <br><br>
     * Since the only input type that would make sense to use this would be text type when used as hint text, this feature is deprecated. The problem is
     * that settings are implemented to automatically fill in default value when an input field is left empty, so the hint text is never actually shown.
     *
     * @return description of the value format
     */
    @Deprecated
    public String getInputFormatDescription() {
        return inputFormatDescription;
    }

}
