package attilathehun.songbook.vcs;

@Deprecated
public class MissingPermissionException extends RuntimeException {
    public MissingPermissionException(final String message) {
        super(message);
    }
}
