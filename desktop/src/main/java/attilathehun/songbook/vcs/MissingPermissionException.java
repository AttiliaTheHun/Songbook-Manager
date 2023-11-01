package attilathehun.songbook.vcs;

public class MissingPermissionException extends RuntimeException {
    public MissingPermissionException(String message) {
        super(message);
    }
}
