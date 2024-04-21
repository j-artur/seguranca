package security;

public class Security {
  public static String encryptWithoutHash(String message, RSAKey key) throws Exception {
    return RSA.encrypt(message, key);
  }

  public static String decryptWithoutHash(String message, RSAKey key) throws Exception {
    return RSA.decrypt(message, key);
  }

  public static String encrypt(String message, Keys keys, RSAKey privateKey) throws Exception {
    String encryptedMessage = AES.encrypt(message, keys.aesKey());

    String hash = HMAC.encrypt(encryptedMessage, keys.hmacKey());
    String encryptedHash = RSA.encrypt(hash, privateKey);

    return B64.encode(encryptedMessage + "%%%" + encryptedHash);
  }

  public static String decrypt(String message, Keys keys) throws Exception {
    String[] parts = B64.decode(message).split("%%%");
    String encryptedMessage = parts[0];
    String encryptedHash = parts[1];

    String decryptedMessage = AES.decrypt(encryptedMessage, keys.aesKey());
    String hash = RSA.decrypt(encryptedHash, keys.rsaKey());

    if (!HMAC.encrypt(encryptedMessage, keys.hmacKey()).equals(hash)) {
      throw new Exception("Hash verification failed");
    }

    return decryptedMessage;
  }
}
