package attilathehun.songbook.collection;

import java.io.Serializable;

public class Song implements Serializable {
    private int id;
    private String name;
    private boolean active = true;
    private String url = "";
    public Song(String name, int id) {
        this.name = name;
        this.id = id;
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
}
