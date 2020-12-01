package cs455.scaling.util;

// Java imports
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher
{
    public static Hasher hasher;

    private Hasher() {}

    public static Hasher getInstance()
    {
        if(hasher == null) {
            synchronized (Hasher.class) {
                if(hasher == null) {
                    hasher = new Hasher();
                }
            }
        }
        return hasher;
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException
    {
        // Convert byte array to hash
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInteger = new BigInteger(1, hash);
        return String.format("%40s", hashInteger.toString(16)).replace(" ", "0");
    }
}
