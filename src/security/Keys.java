package security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public record Keys(SecretKey hmacKey, SecretKey aesKey, RSAKey rsaKey) {
    public static SecretKey aesFromString(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static SecretKey hmacFromString(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    public static String toString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
