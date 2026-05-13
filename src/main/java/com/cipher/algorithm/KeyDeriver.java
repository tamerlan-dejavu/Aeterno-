package com.cipher.algorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Derives cryptographic key material from a master key using SHA-256 and PRNG.
 * Produces shift values and XOR mask for the cipher.
 */
public class KeyDeriver {
    private static final int DIM = 4;

    public KeyMaterial deriveKey(String masterKey) {
        if (masterKey == null || masterKey.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }

        byte[] hash = sha256(masterKey.getBytes(StandardCharsets.UTF_8));
        KeyMaterial km = new KeyMaterial();

        // Bytes [0..3] → X shifts, [4..7] → Y shifts, [8..11] → Z shifts
        for (int i = 0; i < DIM; i++) {
            km.getShiftX()[i] = (hash[i] & 0xFF) % DIM;
            km.getShiftY()[i] = (hash[i + 4] & 0xFF) % DIM;
            km.getShiftZ()[i] = (hash[i + 8] & 0xFF) % DIM;
        }

        // Bytes [12..19] → PRNG seed for 64-byte XOR mask
        long seed = 0;
        for (int i = 12; i < 20; i++) {
            seed = (seed << 8) | (hash[i] & 0xFF);
        }
        Random rng = new Random(seed);
        rng.nextBytes(km.getMask());

        return km;
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
