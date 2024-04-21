package security;

import java.math.BigInteger;

public record RSAKey(BigInteger first, BigInteger second) {
    @Override
    public String toString() {
        return first + "," + second;
    }

    public static RSAKey fromString(String tuple) {
        String[] parts = tuple.split(",");
        return new RSAKey(new BigInteger(parts[0]), new BigInteger(parts[1]));
    }
}
