package com.cipher.algorithm;

import org.springframework.stereotype.Component;
import java.util.Base64;

/**
 * Main cipher orchestrator that combines tensor operations, layer shifts,
 * key derivation, and padding management.
 *
 * Algorithm: 4×4×4 Tensor-Cube Cipher
 * - Key derivation: SHA-256 → shift values + XOR mask
 * - Per-block operations: X-shift, Y-shift, Z-shift, XOR-mask
 * - Padding: PKCS#7
 * - Encoding: Base64
 */
@Component
public class CustomCipher {

    private final KeyDeriver keyDeriver;
    private final TensorProcessor tensorProcessor;
    private final LayerShifter layerShifter;
    private final PaddingManager paddingManager;

    public CustomCipher() {
        this.keyDeriver = new KeyDeriver();
        this.tensorProcessor = new TensorProcessor();
        this.layerShifter = new LayerShifter();
        this.paddingManager = new PaddingManager();
    }

    /**
     * Encrypt plaintext bytes using a string key.
     * Returns raw encrypted bytes (not Base64 encoded).
     */
    public byte[] encryptBytes(byte[] plaintext, String key) {
        KeyMaterial km = keyDeriver.deriveKey(key);
        byte[] padded = paddingManager.pad(plaintext == null ? new byte[0] : plaintext);
        byte[] out = new byte[padded.length];

        int blockSize = tensorProcessor.getBlockSize();
        for (int block = 0; block < padded.length / blockSize; block++) {
            byte[][][] tensor = tensorProcessor.loadTensor(padded, block * blockSize);
            layerShifter.shiftX(tensor, km.getShiftX(), false);
            layerShifter.shiftY(tensor, km.getShiftY(), false);
            layerShifter.shiftZ(tensor, km.getShiftZ(), false);
            tensorProcessor.applyXorMask(tensor, km.getMask());
            tensorProcessor.storeTensor(tensor, out, block * blockSize);
        }
        return out;
    }

    /**
     * Decrypt ciphertext bytes using a string key.
     * Input must be raw encrypted bytes (not Base64 encoded).
     * Output is unpadded plaintext.
     */
    public byte[] decryptBytes(byte[] ciphertext, String key) {
        if (ciphertext == null || ciphertext.length == 0) {
            return new byte[0];
        }

        int blockSize = tensorProcessor.getBlockSize();
        if (ciphertext.length % blockSize != 0) {
            throw new IllegalArgumentException("Ciphertext length must be a multiple of " + blockSize);
        }

        KeyMaterial km = keyDeriver.deriveKey(key);
        byte[] out = new byte[ciphertext.length];

        for (int block = 0; block < ciphertext.length / blockSize; block++) {
            byte[][][] tensor = tensorProcessor.loadTensor(ciphertext, block * blockSize);
            tensorProcessor.applyXorMask(tensor, km.getMask());
            layerShifter.shiftZ(tensor, km.getShiftZ(), true);
            layerShifter.shiftY(tensor, km.getShiftY(), true);
            layerShifter.shiftX(tensor, km.getShiftX(), true);
            tensorProcessor.storeTensor(tensor, out, block * blockSize);
        }
        return paddingManager.unpad(out);
    }

    /**
     * Encrypt plaintext and encode as Base64 string.
     */
    public String encryptToBase64(byte[] plaintext, String key) {
        return Base64.getEncoder().encodeToString(
                encryptBytes(plaintext == null ? new byte[0] : plaintext, key));
    }

    /**
     * Decode Base64 string and decrypt to plaintext bytes.
     */
    public byte[] decryptFromBase64(String base64Ciphertext, String key) {
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) {
            return new byte[0];
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Ciphertext);
            return decryptBytes(decoded, key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 input: " + e.getMessage());
        }
    }
}
