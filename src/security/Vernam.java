package security;

public class Vernam {
  public static String encrypt(String message, String key) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < message.length(); i++) {
      char character = message.charAt(i);
      char charKey = key.charAt(i % key.length());
      char encrypted = (char) (character ^ charKey);
      result.append(encrypted);
    }
    return result.toString();
  }

  public static String decrypt(String encryptedMessage, String key) {
    return encrypt(encryptedMessage, key);
  }
}
