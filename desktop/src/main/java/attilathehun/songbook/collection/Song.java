package attilathehun.songbook.collection;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable {
    private int id;
    private String name;
    private boolean active = true;
    private String url = "";
    private transient int formerId = -1;
    private transient String author = "";

    private transient CollectionManager manager = null;

    public Song(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

    public Song(final int id, final String name, final boolean active, final String url) {
        this.name = name;
        this.id = id;
        this.active = active;
        this.url = url;
    }

    /**
     * Returns a displayable string that relates to the song. It is either the song's id, or if the id is invalid the song's name.
     *
     * @return the display id string
     */
    public String getDisplayId() {
        if (id < 0) {
            return name;
        }
        return String.valueOf(id);
    }

    /**
     * Returns the song name.
     *
     * @return song name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the song id.
     *
     * @return the song id
     */
    public int id() {
        return id;
    }

    /**
     * Returns whether the song is active. Inactive songs are not displayed in the songbook.
     *
     * @return true if the song is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the song associated URL. By default, the URL is an empty string.
     *
     * @return the associated URL (can be empty)
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the song author. By default, the value is an empty string.
     *
     * @return the song author (can be empty)
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the song's former id. The former id field is ignored by most of the framework and server as a way to pass some metadata alongside the song.
     *
     * @return the song former id
     */
    public int getFormerId() {
        return formerId;
    }

    /**
     * Returns the {@link CollectionManager} responsible for the song. It is not necessary for a song to have a manager to be valid, thus the default value is null.
     *
     * @return the song manager (can be null)
     */
    public CollectionManager getManager() {
        return manager;
    }

    /**
     * Sets the song id. The id can be invalid as this method does not perform any parameter checking.
     *
     * @param id the new song id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the song name.
     *
     * @param name the new song name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets whether is song is active. Inactive songs are not displayed in the songbook.
     *
     * @param active true if the song is meant to be active
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Sets the URL associated to the song. It is advised to provide an empty string rather than null.
     *
     * @param url the new song url
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Sets the song author. It is advised to provide an empty string rather than null.
     *
     * @param author the new song author
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    /**
     * Sets the song former id. The former id field is ignored by most of the framework and server as a way to pass some metadata alongside the song.
     *
     * @param formerId the new song former id
     */
    public void setFormerId(final int formerId) {
        this.formerId = formerId;
    }

    /**
     * Sets the song {@link CollectionManager}.
     *
     * @param manager the new song manager
     */
    public void setManager(final CollectionManager manager) {
        this.manager = manager;
    }

    /**
     * Compares whether the song objects represent the same song within the songbook.
     *
     * @param o the song to compare against
     * @return true if the song objects represent the same song
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null || !o.getClass().equals(Song.class)) {
            return false;
        } else if (!(((Song) o).id == this.id)) {
            return false;
        } else if (!(Objects.equals(((Song) o).name, this.name))) {
            return false;
        } else if(((Song) o).manager != null && manager != null) {
            return ((Song) o).manager.equals(manager);
        }
        return true;
    }
}
