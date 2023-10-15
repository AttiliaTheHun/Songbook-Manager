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

    public String getHash(String message)  {
        byte[] encodedhash = digest.digest(
                message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    public String getHash(File file) throws IOException, NoSuchAlgorithmException {
        return getHash(String.join("\n", Files.readAllLines(file.toPath())));
    }

    /**
     * @source <a href="https://www.baeldung.com/sha-256-hashing-java">Baeldung</a>
     * @param hash message digest bytes array
     * @return hexadecimal hash String
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
