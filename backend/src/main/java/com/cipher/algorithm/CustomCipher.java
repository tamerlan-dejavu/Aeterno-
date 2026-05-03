package com.cipher.algorithm;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Custom symmetric cipher: per-byte Circular Bit Shift + dual XOR
 * (key byte XOR positional byte). Inverse operation is mirrored.
 */
@Component
public class CustomCipher {

    public byte[] encryptBytes(byte[] plaintext, String key) {
        validateKey(key);
        if (plaintext == null || plaintext.length == 0) {
            return new byte[0];
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[plaintext.length];

        for (int i = 0; i < plaintext.length; i++) {
            int keyByte = keyBytes[i % keyBytes.length] & 0xFF;
            int shiftAmount = (keyByte % 7) + 1;
            byte rotated = circularLeftShift(plaintext[i], shiftAmount);
            out[i] = (byte) ((rotated & 0xFF) ^ keyByte ^ (i % 256));
        }
        return out;
    }

    public byte[] decryptBytes(byte[] ciphertext, String key) {
        validateKey(key);
        if (ciphertext == null || ciphertext.length == 0) {
            return new byte[0];
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[ciphertext.length];

        for (int i = 0; i < ciphertext.length; i++) {
            int keyByte = keyBytes[i % keyBytes.length] & 0xFF;
            int shiftAmount = (keyByte % 7) + 1;
            byte decXor = (byte) ((ciphertext[i] & 0xFF) ^ keyByte ^ (i % 256));
            out[i] = circularRightShift(decXor, shiftAmount);
        }
        return out;
    }

    public String encryptToBase64(byte[] plaintext, String key) {
        if (plaintext == null || plaintext.length == 0) {
            validateKey(key);
            return "";
        }
        return Base64.getEncoder().encodeToString(encryptBytes(plaintext, key));
    }

    public byte[] decryptFromBase64(String base64Ciphertext, String key) {
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) {
            validateKey(key);
            return new byte[0];
        }
        byte[] decoded = Base64.getDecoder().decode(base64Ciphertext);
        return decryptBytes(decoded, key);
    }

    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }
    }

    /**
     * 8-bit circular left shift.
     */
    public static byte circularLeftShift(byte b, int n) {
        int v = b & 0xFF;
        int s = n & 7;
        return (byte) (((v << s) | (v >>> (8 - s))) & 0xFF);
    }

    /**
     * 8-bit circular right shift.
     */
    public static byte circularRightShift(byte b, int n) {
        int v = b & 0xFF;
        int s = n & 7;
        return (byte) (((v >>> s) | (v << (8 - s))) & 0xFF);
    }
}
