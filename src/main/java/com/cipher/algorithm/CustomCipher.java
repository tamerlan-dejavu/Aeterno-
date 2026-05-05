package com.cipher.algorithm;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

/**
 * Tensor-Cube Cipher — symmetric block cipher on a 4×4×4 byte tensor.
 *
 * Encryption per 64-byte block:
 *   1. Load into tensor[x][y][z], row-major: index = x*16 + y*4 + z
 *   2. Circular-shift X-planes, then Y-planes, then Z-planes
 *   3. XOR every cell with the 64-byte key-derived mask
 *
 * Decryption reverses steps: XOR (self-inverse), then Z/Y/X with negative shifts.
 */
@Component
public class CustomCipher {

    private static final int BLOCK = 64;
    private static final int DIM   = 4;

    // ── public API ──────────────────────────────────────────────────────────

    public byte[] encryptBytes(byte[] plaintext, String key) {
        validateKey(key);
        KeyMaterial km = deriveKey(key);
        byte[] padded = pkcs7Pad(plaintext == null ? new byte[0] : plaintext);
        byte[] out = new byte[padded.length];

        for (int block = 0; block < padded.length / BLOCK; block++) {
            byte[][][] tensor = loadTensor(padded, block * BLOCK);
            shiftX(tensor, km.shiftX, false);
            shiftY(tensor, km.shiftY, false);
            shiftZ(tensor, km.shiftZ, false);
            xorMask(tensor, km.mask);
            storeTensor(tensor, out, block * BLOCK);
        }
        return out;
    }

    public byte[] decryptBytes(byte[] ciphertext, String key) {
        validateKey(key);
        if (ciphertext == null || ciphertext.length == 0) return new byte[0];
        if (ciphertext.length % BLOCK != 0)
            throw new IllegalArgumentException("Ciphertext length must be a multiple of 64");

        KeyMaterial km = deriveKey(key);
        byte[] out = new byte[ciphertext.length];

        for (int block = 0; block < ciphertext.length / BLOCK; block++) {
            byte[][][] tensor = loadTensor(ciphertext, block * BLOCK);
            xorMask(tensor, km.mask);            // XOR is self-inverse
            shiftZ(tensor, km.shiftZ, true);
            shiftY(tensor, km.shiftY, true);
            shiftX(tensor, km.shiftX, true);
            storeTensor(tensor, out, block * BLOCK);
        }
        return pkcs7Unpad(out);
    }

    public String encryptToBase64(byte[] plaintext, String key) {
        validateKey(key);
        return Base64.getEncoder().encodeToString(
                encryptBytes(plaintext == null ? new byte[0] : plaintext, key));
    }

    public byte[] decryptFromBase64(String base64Ciphertext, String key) {
        validateKey(key);
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) return new byte[0];
        return decryptBytes(Base64.getDecoder().decode(base64Ciphertext), key);
    }

    // ── key derivation ───────────────────────────────────────────────────────

    private static final class KeyMaterial {
        final int[]  shiftX = new int[DIM];   // per x-layer shift
        final int[]  shiftY = new int[DIM];   // per y-layer shift
        final int[]  shiftZ = new int[DIM];   // per z-layer shift
        final byte[] mask   = new byte[BLOCK];
    }

    private KeyMaterial deriveKey(String key) {
        byte[] hash = sha256(key.getBytes(StandardCharsets.UTF_8));
        // first 12 bytes → shifts: [0..3]=X, [4..7]=Y, [8..11]=Z
        KeyMaterial km = new KeyMaterial();
        for (int i = 0; i < DIM; i++) {
            km.shiftX[i] = (hash[i]      & 0xFF) % DIM;
            km.shiftY[i] = (hash[i + 4]  & 0xFF) % DIM;
            km.shiftZ[i] = (hash[i + 8]  & 0xFF) % DIM;
        }
        // bytes 12..31 → seed for PRNG → 64-byte XOR mask
        long seed = 0;
        for (int i = 12; i < 20; i++) {
            seed = (seed << 8) | (hash[i] & 0xFF);
        }
        Random rng = new Random(seed);
        rng.nextBytes(km.mask);
        return km;
    }

    // ── tensor I/O ───────────────────────────────────────────────────────────

    /** Load 64 bytes starting at offset into a 4×4×4 tensor (row-major). */
    private byte[][][] loadTensor(byte[] src, int offset) {
        byte[][][] t = new byte[DIM][DIM][DIM];
        for (int x = 0; x < DIM; x++)
            for (int y = 0; y < DIM; y++)
                for (int z = 0; z < DIM; z++)
                    t[x][y][z] = src[offset + x * 16 + y * 4 + z];
        return t;
    }

    /** Write 4×4×4 tensor back into dst starting at offset. */
    private void storeTensor(byte[][][] t, byte[] dst, int offset) {
        for (int x = 0; x < DIM; x++)
            for (int y = 0; y < DIM; y++)
                for (int z = 0; z < DIM; z++)
                    dst[offset + x * 16 + y * 4 + z] = t[x][y][z];
    }

    // ── layer shifts ─────────────────────────────────────────────────────────

    /**
     * X-axis shift: for each x, circularly shift the flat 16-element Y×Z plane.
     * The plane is read in order (y=0..3, z=0..3).
     */
    private void shiftX(byte[][][] t, int[] shifts, boolean inverse) {
        for (int x = 0; x < DIM; x++) {
            int s = inverse ? (DIM * DIM - shifts[x] % (DIM * DIM)) % (DIM * DIM)
                            : shifts[x] % (DIM * DIM);
            if (s == 0) continue;
            byte[] plane = new byte[DIM * DIM];
            for (int y = 0; y < DIM; y++)
                for (int z = 0; z < DIM; z++)
                    plane[y * DIM + z] = t[x][y][z];
            plane = circularShift(plane, s);
            for (int y = 0; y < DIM; y++)
                for (int z = 0; z < DIM; z++)
                    t[x][y][z] = plane[y * DIM + z];
        }
    }

    /**
     * Y-axis shift: for each y, circularly shift the flat 16-element X×Z plane.
     */
    private void shiftY(byte[][][] t, int[] shifts, boolean inverse) {
        for (int y = 0; y < DIM; y++) {
            int s = inverse ? (DIM * DIM - shifts[y] % (DIM * DIM)) % (DIM * DIM)
                            : shifts[y] % (DIM * DIM);
            if (s == 0) continue;
            byte[] plane = new byte[DIM * DIM];
            for (int x = 0; x < DIM; x++)
                for (int z = 0; z < DIM; z++)
                    plane[x * DIM + z] = t[x][y][z];
            plane = circularShift(plane, s);
            for (int x = 0; x < DIM; x++)
                for (int z = 0; z < DIM; z++)
                    t[x][y][z] = plane[x * DIM + z];
        }
    }

    /**
     * Z-axis shift: for each z, circularly shift the flat 16-element X×Y plane.
     */
    private void shiftZ(byte[][][] t, int[] shifts, boolean inverse) {
        for (int z = 0; z < DIM; z++) {
            int s = inverse ? (DIM * DIM - shifts[z] % (DIM * DIM)) % (DIM * DIM)
                            : shifts[z] % (DIM * DIM);
            if (s == 0) continue;
            byte[] plane = new byte[DIM * DIM];
            for (int x = 0; x < DIM; x++)
                for (int y = 0; y < DIM; y++)
                    plane[x * DIM + y] = t[x][y][z];
            plane = circularShift(plane, s);
            for (int x = 0; x < DIM; x++)
                for (int y = 0; y < DIM; y++)
                    t[x][y][z] = plane[x * DIM + y];
        }
    }

    /** Circular left shift of a byte array by s positions. */
    private byte[] circularShift(byte[] arr, int s) {
        int len = arr.length;
        s = ((s % len) + len) % len;
        byte[] result = new byte[len];
        System.arraycopy(arr, s,   result, 0,       len - s);
        System.arraycopy(arr, 0,   result, len - s, s);
        return result;
    }

    // ── XOR mask ─────────────────────────────────────────────────────────────

    private void xorMask(byte[][][] t, byte[] mask) {
        for (int x = 0; x < DIM; x++)
            for (int y = 0; y < DIM; y++)
                for (int z = 0; z < DIM; z++)
                    t[x][y][z] ^= mask[x * 16 + y * 4 + z];
    }

    // ── PKCS#7 padding ───────────────────────────────────────────────────────

    private byte[] pkcs7Pad(byte[] data) {
        int padLen = BLOCK - (data.length % BLOCK);
        byte[] padded = Arrays.copyOf(data, data.length + padLen);
        Arrays.fill(padded, data.length, padded.length, (byte) padLen);
        return padded;
    }

    private byte[] pkcs7Unpad(byte[] data) {
        if (data.length == 0) return data;
        int padLen = data[data.length - 1] & 0xFF;
        if (padLen < 1 || padLen > BLOCK || padLen > data.length)
            throw new IllegalArgumentException("Invalid PKCS#7 padding");
        for (int i = data.length - padLen; i < data.length; i++)
            if ((data[i] & 0xFF) != padLen)
                throw new IllegalArgumentException("Invalid PKCS#7 padding");
        return Arrays.copyOf(data, data.length - padLen);
    }

    // ── utilities ────────────────────────────────────────────────────────────

    private void validateKey(String key) {
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty");
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
