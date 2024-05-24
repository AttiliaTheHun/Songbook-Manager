package attilathehun.songbook.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A utility class to generate file and text checksums. This class uses MessageDigest which is not considered
 * thread-safe, thus this class is not thread-safe!
 */
public class SHA256HashGenerator {
    private final MessageDigest digest;

    public SHA256HashGenerator() throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance("SHA-256");
    }

    /**
     * @param hash message digest bytes array
     * @return hexadecimal hash String
     * @source <a href="https://www.baeldung.com/sha-256-hashing-java">Baeldung</a>
     */
    private static String bytesToHex(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            final String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getHash(final String message) {
        final byte[] encodedhash = digest.digest(
                message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    public String getHash(final File file) throws IOException {
        return getHash(String.join("\n", Files.readAllLines(file.toPath())));
    }

}
