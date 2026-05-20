package com.cipher.algorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class KeyDeriver {
    private static final int DIM = 4;

    public KeyMaterial deriveKey(String masterKey) {
        if (masterKey == null || masterKey.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }

        byte[] hash = sha256(masterKey.getBytes(StandardCharsets.UTF_8));
        KeyMaterial km = new KeyMaterial();

        for (int i = 0; i < DIM; i++) {
            km.getShiftX()[i] = hash[i] % DIM;
            km.getShiftY()[i] = hash[i + 4] % DIM;
            km.getShiftZ()[i] = hash[i + 8] % DIM;
        }

        long seed = 0;
        for (int i = 12; i < 20; i++) {
            seed = (seed << 8) | (hash[i] & 0xFF);
        }
        new Random(seed).nextBytes(km.getMask());

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