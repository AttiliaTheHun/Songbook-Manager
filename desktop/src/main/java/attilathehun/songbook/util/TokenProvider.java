package attilathehun.songbook.util;

import attilathehun.songbook.environment.SettingsManager;
import attilathehun.songbook.window.AlertDialog;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;

/**
 * This class can be used to acquire an authentication token. This class is not thread-safe, but there is no reason to require more than one instance anyway.
 */
public class TokenProvider {
    private static final Logger logger = LogManager.getLogger(TokenProvider.class);
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_ALGORITHM_SPECIFICATION = "AES/CBC/PKCS5Padding";
    private static final byte[] DEFAULT_AES_IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; //16-byte
    private static final String PBKDF_CONFIGURATION = "PBKDF2WithHmacSHA256";
    private static final byte[] SALT = "Wololo".getBytes(StandardCharsets.UTF_8); // secure, I know :)
    private static final char[] DEFAULT_KEY_GENERATION_PASSWORD = {72, 101, 108, 108, 111, 32, 116, 104, 101, 114, 101};


    /**
     * Attempts to acquire an authentication token for the HTTP API of the remote server. First the authentication file is read and decrypted to obtain a token.
     * If this is not possible, the user is prompted to enter the token manually. If the input is invalid (empty), null is returned.
     *
     * @return UTF-8 encoded byte array or null
     */
    public byte[] getAuthenticationToken() {
        byte[] token = null;
        try {
            token = getLocalAuthenticationToken();
            // if the decryption fails, null is returned and so it would simply ask to enter a token (undesirable)
            if (token == null) {
                token = requestAuthenticationToken();
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("I/O Error").setIcon(AlertDialog.Builder.Icon.ERROR).setMessage("Something went wrong while reading the auth file: " + e.getLocalizedMessage())
                    .addOkButton().build().open();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            new AlertDialog.Builder().setTitle("Decryption Error").setIcon(AlertDialog.Builder.Icon.ERROR).setMessage("Error occurred when decrypting the auth file: " + e.getLocalizedMessage())
                    .addOkButton().build().open();
        }

        return token;
    }

    /**
     * Loads and decrypts the local auth file to obtain a token. This token is then returned as a byte array. If a token is not available null is returned.
     *
     * @return UTF-8 encoded byte array or null
     * @throws IOException j
     * @throws NoSuchPaddingException o
     * @throws NoSuchAlgorithmException h
     * @throws InvalidKeySpecException n
     * @throws InvalidAlgorithmParameterException c
     * @throws InvalidKeyException e
     * @throws IllegalBlockSizeException n
     * @throws BadPaddingException a
     */
    private byte[] getLocalAuthenticationToken() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final File authFile = new File((String) SettingsManager.getInstance().getValue("AUTH_FILE_PATH"));
        if (!authFile.exists() || authFile.length() == 0) {
            return null;
        }

        byte[] encryptedToken;

        try (final InputStream inputStream = new FileInputStream(authFile)) {
            encryptedToken = inputStream.readAllBytes();
        }

        if (encryptedToken == null || encryptedToken.length == 0) {
            throw new IOException();
        }

        final IvParameterSpec ivspec = new IvParameterSpec(DEFAULT_AES_IV);
        final Cipher cipher = Cipher.getInstance(AES_ALGORITHM_SPECIFICATION);
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(getKeyPassword()), ivspec);

        final byte[] decryptedBytes = cipher.update(encryptedToken);

        final byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) {
            final byte[] token = new byte[decryptedBytes.length + finalBytes.length];
            System.arraycopy(decryptedBytes, 0, token, 0, decryptedBytes.length);
            System.arraycopy(finalBytes, 0, token, decryptedBytes.length, finalBytes.length);
            return token;
        }

        return decryptedBytes;
    }

    /**
     * Encrypts the token and saves it to the auth file.
     *
     * @param token the encrypted token
     * @param authFile the auth file
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private void saveToken(final byte[] token, final File authFile) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final IvParameterSpec ivspec = new IvParameterSpec(DEFAULT_AES_IV);
        final Cipher cipher = Cipher.getInstance(AES_ALGORITHM_SPECIFICATION);
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(getKeyPassword()), ivspec);

        final OutputStream outputStream = new FileOutputStream(authFile);
        final byte[] encryptedToken = cipher.update(token);
        outputStream.write(encryptedToken);
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
            outputStream.write(outputBytes);
        }
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Generates an AES {@link SecretKey} from a given password.
     *
     * @param password the password
     * @return the secret key object
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private SecretKey getKeyFromPassword(final char[] password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_CONFIGURATION);
        final KeySpec spec = new PBEKeySpec(password, SALT, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES_ALGORITHM);
    }

    /**
     * Returns the password for the generation of the encryption key. If enabled, the default password is used. Otherwise, the user is promted to provide a password.
     * This can result in the password being null.
     *
     * @return the key generation password or null
     */
    private char[] getKeyPassword() {
        if (SettingsManager.getInstance().getValue("USE_CUSTOM_ENCRYPTION_PASSWORD")) {
            final String k = openInputDialog("Enter custom encryption password", "Password", "Enter the encryption key derivation password");
            if (k == null || k.trim().length() == 0) {
                return null;
            }
            return k.toCharArray();
        }
        return DEFAULT_KEY_GENERATION_PASSWORD;
    }

    /**
     * Prompts the user to provide an authentication token and returns their input. In case of a problem the output can be null.
     *
     * @return UTF-8 encoded byte array or null
     */
    private byte[] requestAuthenticationToken() {
        final String k = openInputDialog("Enter authentication token", "Token", "Enter the API token");
        if (k == null || k.trim().length() == 0) {
            return null;
        }
        try {
            final int result = new AlertDialog.Builder().setTitle("Save token").setMessage("Do you want to save the token locally so that you don't have to enter it every time?")
                    .addOkButton("Save").addCloseButton("Don't save").setCancelable(false).build().awaitResult().get();
            if (result == AlertDialog.RESULT_OK) {
                saveToken(k.getBytes(StandardCharsets.UTF_8), new File((String) SettingsManager.getInstance().getValue("AUTH_FILE_PATH")));
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        return k.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Opens an {@link AlertDialog} with input field to ask the user for input. Some aspects of the dialog can be configured through the method arguments. The user input is not
     * verified and thus can be empty. In case of a problem null is returned.
     *
     * @param title title of the dialog
     * @param label label of the text input field
     * @param hint hint text of the text input field
     * @return the user input string or null
     */
    private String openInputDialog(final String title, final String label, final String hint) {
        try {
            final Pair<Integer, ArrayList<Node>> data = new AlertDialog.Builder().setTitle(title).addOkButton().addCloseButton().setCancelable(false)
                    .addTextInput(label, hint).build().awaitData().get();
            if (data.getKey() != AlertDialog.RESULT_OK) {
                return null;
            } else {
                return ((TextField) data.getValue().get(1)).getText();
            }

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }


}
