package com.forgery.detector.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

/**
 * Cryptographic Utility for file hashing, digital signature verification,
 * and hash-chaining in the audit log.
 * 
 * Demonstrates: java.security.*, MessageDigest, Exception Handling.
 */
public class CryptoUtils {

    private CryptoUtils() {
        // Utility class
    }

    /**
     * Computes the SHA-256 hexadecimal hash for raw byte data.
     */
    public static String calculateSha256(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Computes SHA-256 hash incrementally from an InputStream.
     */
    public static String calculateSha256(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("Error computing SHA-256 from stream", e);
        }
    }

    /**
     * Hashes string content (used for hash-chained audit trails).
     */
    public static String hashString(String input) {
        if (input == null) return "";
        return calculateSha256(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verifies an RSA/SHA256 digital signature against an image hash.
     */
    public static boolean verifyDigitalSignature(byte[] data, byte[] signatureBytes, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a byte array to a hex string.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

