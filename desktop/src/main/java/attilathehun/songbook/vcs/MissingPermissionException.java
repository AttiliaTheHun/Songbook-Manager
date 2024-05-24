package attilathehun.songbook.vcs;

public class MissingPermissionException extends RuntimeException {
    public MissingPermissionException(final String message) {
        super(message);
    }
}
