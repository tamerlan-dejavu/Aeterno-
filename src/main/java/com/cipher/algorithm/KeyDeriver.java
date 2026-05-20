package com.cipher.algorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class KeyDeriver {
    private static final int DIM = 4;
    private static final int BLOCK = 64;
    private static final int ROUNDS = 4;

    public KeyMaterial deriveKey(String masterKey) {
        if (masterKey == null || masterKey.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }

        byte[] hash = sha256(masterKey.getBytes(StandardCharsets.UTF_8));
        KeyMaterial km = new KeyMaterial();

        for (int round = 0; round < ROUNDS; round++) {
            byte[] roundHash = deriveRoundHash(hash, round);
            deriveRoundKeys(km, roundHash, round);
        }

        return km;
    }

    private byte[] deriveRoundHash(byte[] baseHash, int round) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(baseHash);
            md.update((byte) round);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    private void deriveRoundKeys(KeyMaterial km, byte[] roundHash, int round) {
        for (int i = 0; i < DIM; i++) {
            km.getShiftX(round)[i] = ((roundHash[i] & 0xFF) % 12) + 1;
            km.getShiftY(round)[i] = ((roundHash[i + 4] & 0xFF) % 12) + 1;
            km.getShiftZ(round)[i] = ((roundHash[i + 8] & 0xFF) % 12) + 1;
        }

        long seed = 0;
        for (int i = 12; i < 20; i++) {
            seed = (seed << 8) | (roundHash[i] & 0xFF);
        }
        new Random(seed).nextBytes(km.getMask(round));
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
