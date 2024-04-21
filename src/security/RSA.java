package security;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
    public static KeyPair generateKeys() {
        SecureRandom rd = new SecureRandom();

        BigInteger p = BigInteger.probablePrime(64, rd);
        BigInteger q = BigInteger.probablePrime(64, rd);

        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = new BigInteger("65537");

        BigInteger d = e.modInverse(phi);

        var publicKey = new RSAKey(n, e);
        var privateKey = new RSAKey(n, d);

        return new KeyPair(publicKey, privateKey);
    }

    public static String encrypt(String message, RSAKey key) {
        byte[] bytes = message.getBytes();

        BigInteger n = key.first();
        BigInteger e = key.second();

        StringBuilder sb = new StringBuilder();

        BigInteger m = BigInteger.valueOf(bytes[0]);
        BigInteger c = m.modPow(e, n);
        sb.append(c.toString());
        for (int i = 1; i < bytes.length; i++) {
            sb.append(" ");
            m = BigInteger.valueOf(bytes[i]);
            c = m.modPow(e, n);
            sb.append(c.toString());
        }

        return sb.toString();
    }

    public static String decrypt(String cipher, RSAKey key) {
        String[] cs = cipher.split(" ");

        BigInteger n = key.first();
        BigInteger d = key.second();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cs.length; i++) {
            BigInteger c = new BigInteger(cs[i]);
            BigInteger m = c.modPow(d, n);
            sb.append((char) m.intValue());
        }

        return sb.toString();
    }

}
