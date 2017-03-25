package Utilities;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class Utilities {

    /**
     * Returns a hexadecimal encoded SHA-256 hash for the input String.
     *
     * @param data
     * @return string with Hash
     */
    public static String createHash(String data) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return bytesToHex(hash); // make it printable
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Use javax.xml.bind.DatatypeConverter class in JDK to convert byte array
     * to a hexadecimal string. Note that this generates hexadecimal in upper case.
     *
     * @param hash
     * @return string with hash in hexadecimal
     */
    public static String bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash);
    }
}
