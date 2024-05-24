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

    public String getDisplayId() {
        if (id < 0) {
            return name;
        }
        return String.valueOf(id);
    }

    public String name() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int id() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public int getFormerId() {
        return formerId;
    }

    public void setFormerId(final int formerId) {
        this.formerId = formerId;
    }

    public CollectionManager getManager() {
        return manager;
    }

    public void setManager(final CollectionManager manager) {
        this.manager = manager;
    }

    // TODO does it make sense to compare the active property?
    @Override
    public boolean equals(final Object o) {
        if (o == null || !o.getClass().equals(Song.class)) {
            return false;
        } else if (!(((Song) o).id == this.id)) {
            return false;
        } else if (!(Objects.equals(((Song) o).name, this.name))) {
            return false;
        } else if (!(((Song) o).active == this.active)) {
            return false;
        } else if(((Song) o).manager != null && manager != null) {
            if (!((Song) o).manager.equals(manager)) {
                return false;
            }
        }
        return true;
    }
}
