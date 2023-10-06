package attilathehun.songbook.vcs.index;

public class TextProperty extends Property {
    private String content;

    public TextProperty(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }
}
