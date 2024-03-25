package security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC {
  public static final String ALG = "HmacSHA256";
  // public static final String ALG = "HmacSHA224";
  // public static final String ALG = "HmacSHA384";
  // public static final String ALG = "HmacSHA512";

  public static String encrypt(String key, String message) throws Exception {
    Mac shaHMAC = Mac.getInstance(ALG);
    SecretKeySpec MACKey = new SecretKeySpec(
        key.getBytes("UTF-8"),
        ALG);
    shaHMAC.init(MACKey);
    byte[] bytesHMAC = shaHMAC
        .doFinal(message.getBytes("UTF-8"));
    return byte2Hex(bytesHMAC);
  }

  public static String byte2Hex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append(String.format("%02x", b));
    return sb.toString();
  }
}
