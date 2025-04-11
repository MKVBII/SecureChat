import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/*
 * Utility class to handle AES encryption and decryption.
 */
public class AESUtil {
    private static final String ALGORITHM = "AES";
    public static final String KEY = "1234567890123456"; // 16-byte secret key (128-bit)

    /**
     * Encrypts a plain text string using AES.
     * @param data The plain text to encrypt.
     * @return The encrypted Base64-encoded string.
     */
    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted); // Encode to Base64 for safe transmission
    }

    /**
     * Decrypts an AES-encrypted Base64 string.
     * @param encryptedData The Base64-encoded encrypted text.
     * @return The decrypted plain text.
     */
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}
