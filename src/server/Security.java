package server;

import security.AES;
import security.B64;
import security.HMAC;
import security.Vernam;

public class Security {
  private AES aes = new AES();

  private String hmacKey;
  private String vernamKey = "xRh1hl/MSnxy87AT7zyHfXWKK3U1lXpGM8MZYRXix+A=";

  public Security(String hmacKey) {
    this.hmacKey = hmacKey;
  }

  public String encrypt(String message) throws Exception {
    // Encrypt Vernam
    String encryptedMessage = Vernam.encrypt(message, vernamKey);

    // Generate HMAC
    String hmacHash = HMAC.encrypt(message, hmacKey);
    String aesEncrypted = aes.encrypt(hmacHash);
    String base64String = B64.encode(aesEncrypted);

    String finalMessage = encryptedMessage + "%%%" + base64String;
    return finalMessage;
  }

  public String decrypt(String message) throws Exception {
    String[] parts = message.split("%%%");

    // Decrypt Vernam
    String encryptedMessage = parts[0];
    String decryptedMessage = Vernam.decrypt(encryptedMessage, vernamKey);

    // Verify HMAC
    String base64String = parts[1];
    String aesEncrypted = B64.decode(base64String);
    String hmacHash = aes.decrypt(aesEncrypted);
    String generatedHmac = HMAC.encrypt(decryptedMessage, hmacKey);
    if (!hmacHash.equals(generatedHmac)) {
      throw new Exception("HMAC verification failed");
    }

    return decryptedMessage;
  }
}
