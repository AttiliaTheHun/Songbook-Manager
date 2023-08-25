package attilathehun.songbook.collection;

import java.io.Serializable;

public class Song implements Serializable {
    private int id;
    private String name;
    private boolean active = true;
    private String url = "";

    private transient int formerId = -1;

    private transient String author = "";

    public Song(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Song(int id, String name, boolean active, String url) {
        this.name = name;
        this.id = id;
        this.active = active;
        this.url = url;
    }

    public String getDisplayId() {
        if (id == -1) {
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

    public void setName(String name) {
        this.name = name;
    }

    public int id() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getFormerId() {
        return formerId;
    }

    public void setFormerId(int formerId) {
        this.formerId = formerId;
    }
}
