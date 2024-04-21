package security;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AES {
  public static SecretKey generateKey() throws NoSuchAlgorithmException {
    return KeyGenerator.getInstance("AES").generateKey();
  }

  public static String encrypt(String text, SecretKey key) {
    byte[] encryptedMessageBytes;
    Cipher encrypter;
    String encryptedMessage = "";
    String message = text;
    try {
      encrypter = Cipher
          .getInstance("AES/ECB/PKCS5Padding");
      encrypter.init(Cipher.ENCRYPT_MODE, key);
      encryptedMessageBytes = encrypter.doFinal(message.getBytes());
      encryptedMessage = Base64
          .getEncoder()
          .encodeToString(encryptedMessageBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return encryptedMessage;
  }

  public static String decrypt(String text, SecretKey key) {
    byte[] encryptedMessageBytes = Base64
        .getDecoder()
        .decode(text);
    Cipher decrypter;
    String message = "";
    try {
      decrypter = Cipher.getInstance("AES/ECB/PKCS5Padding");
      decrypter.init(Cipher.DECRYPT_MODE, key);
      byte[] decryptedMessageBytes = decrypter.doFinal(encryptedMessageBytes);
      String decryptedMessage = new String(decryptedMessageBytes);
      message = decryptedMessage;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return message;
  }
}
