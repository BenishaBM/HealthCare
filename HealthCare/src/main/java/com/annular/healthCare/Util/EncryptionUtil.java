package com.annular.healthCare.Util;



import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String SECRET_KEY = "MySecretKey12345"; // must be 16 characters

    public static String encrypt(String plainText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getUrlEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
    }

    public static String decrypt(String encryptedText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getUrlDecoder().decode(encryptedText)));
    }

    public static String generateTokenWithExpiry(String email) throws Exception {
        long timestamp = System.currentTimeMillis(); // current time in millis
        String payload = email + "::" + timestamp;
        return encrypt(payload);
    }

    public static String validateTokenAndGetEmail(String token, long expiryInMillis) throws Exception {
        String decrypted = decrypt(token);
        String[] parts = decrypted.split("::");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid token format");
        }

        String email = parts[0];
        long tokenTime = Long.parseLong(parts[1]);
        long currentTime = System.currentTimeMillis();

        if ((currentTime - tokenTime) > expiryInMillis) {
            throw new IllegalArgumentException("Token expired");
        }

        return email;
    }
}