package security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
  private SecretKeyFactory factory;
  private KeySpec spec;
  private SecretKey key;
  private String message;
  private String encryptedMessage;

  public AES() {
    generateKey("aes", "8683fed3-db00-4028-804e-1aacc52886ff");
  }

  public AES(String password, String salt) {
    generateKey(password, salt);
  }

  public void generateKey(String password, String salt) {
    try {
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
      key = new SecretKeySpec(factory.generateSecret(spec)
          .getEncoded(), "AES");

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }
  }

  public String encrypt(String text) {
    byte[] encryptedMessageBytes;
    Cipher encrypter;
    message = text;
    try {
      encrypter = Cipher
          .getInstance("AES/ECB/PKCS5Padding");
      encrypter.init(Cipher.ENCRYPT_MODE, key);
      encryptedMessageBytes = encrypter.doFinal(message.getBytes());
      encryptedMessage = Base64
          .getEncoder()
          .encodeToString(encryptedMessageBytes);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    }
    return encryptedMessage;
  }

  public String decrypt(String text) {
    // Decriptação
    byte[] encryptedMessageBytes = Base64
        .getDecoder()
        .decode(text);
    Cipher decriptador;
    try {
      decriptador = Cipher.getInstance("AES/ECB/PKCS5Padding");
      decriptador.init(Cipher.DECRYPT_MODE, key);
      byte[] decryptedMessageBytes = decriptador.doFinal(encryptedMessageBytes);
      String decryptedMessage = new String(decryptedMessageBytes);
      /*
       * System.out.println("<< Mensagem decifrada = "
       * + mensagemDecifrada);
       */
      message = decryptedMessage;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    }
    return message;
  }
}
