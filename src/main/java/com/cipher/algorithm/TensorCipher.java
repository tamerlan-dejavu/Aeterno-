package com.cipher.algorithm;

import org.springframework.stereotype.Component;
import java.util.Base64;

public class TensorCipher {

    private final KeyDeriver keyDeriver;
    private final TensorProcessor tensorProcessor;
    private final LayerShifter layerShifter;
    private final PaddingManager paddingManager;

    public TensorCipher() {
        this.keyDeriver = new KeyDeriver();
        this.tensorProcessor = new TensorProcessor();
        this.layerShifter = new LayerShifter();
        this.paddingManager = new PaddingManager();
    }

    public byte[] encryptBytes(byte[] plaintext, String key) {
        String upperKey = key.toUpperCase();
        KeyMaterial km = keyDeriver.deriveKey(upperKey);
        byte[] padded = paddingManager.pad(plaintext == null ? new byte[0] : plaintext);
        byte[] out = new byte[padded.length];

        int blockSize = tensorProcessor.getBlockSize();
        for (int block = 0; block < padded.length / blockSize; block++) {
            byte[][][] tensor = tensorProcessor.loadTensor(padded, block * blockSize);

            // First stage: 3 shifts
            layerShifter.shiftX(tensor, km.getShiftX(), false);
            layerShifter.shiftY(tensor, km.getShiftY(), false);
            layerShifter.shiftZ(tensor, km.getShiftZ(), false);
            tensorProcessor.applyXorMask(tensor, km.getMask());

            // Second stage: 3 shifts (for avalanche effect)
            layerShifter.shiftX(tensor, km.getShiftX(), false);
            layerShifter.shiftY(tensor, km.getShiftY(), false);
            layerShifter.shiftZ(tensor, km.getShiftZ(), false);
            tensorProcessor.applyXorMask(tensor, km.getMask());

            tensorProcessor.storeTensor(tensor, out, block * blockSize);
        }
        return out;
    }

    public byte[] decryptBytes(byte[] ciphertext, String key) {
        if (ciphertext == null || ciphertext.length == 0) {
            return new byte[0];
        }

        int blockSize = tensorProcessor.getBlockSize();
        if (ciphertext.length % blockSize != 0) {
            throw new IllegalArgumentException("Ciphertext length must be a multiple of " + blockSize);
        }

        String upperKey = key.toUpperCase();
        KeyMaterial km = keyDeriver.deriveKey(upperKey);
        byte[] out = new byte[ciphertext.length];

        for (int block = 0; block < ciphertext.length / blockSize; block++) {
            byte[][][] tensor = tensorProcessor.loadTensor(ciphertext, block * blockSize);

            // Reverse second stage: 3 inverse shifts
            tensorProcessor.applyXorMask(tensor, km.getMask());
            layerShifter.shiftZ(tensor, km.getShiftZ(), true);
            layerShifter.shiftY(tensor, km.getShiftY(), true);
            layerShifter.shiftX(tensor, km.getShiftX(), true);

            // Reverse first stage: 3 inverse shifts
            tensorProcessor.applyXorMask(tensor, km.getMask());
            layerShifter.shiftZ(tensor, km.getShiftZ(), true);
            layerShifter.shiftY(tensor, km.getShiftY(), true);
            layerShifter.shiftX(tensor, km.getShiftX(), true);

            tensorProcessor.storeTensor(tensor, out, block * blockSize);
        }
        return paddingManager.unpad(out);
    }

    public String encryptToBase64(byte[] plaintext, String key) {
        String upperKey = key.toUpperCase();
        return Base64.getEncoder().encodeToString(
                encryptBytes(plaintext == null ? new byte[0] : plaintext, upperKey));
    }

    public byte[] decryptFromBase64(String base64Ciphertext, String key) {
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) {
            return new byte[0];
        }
        try {
            String upperKey = key.toUpperCase();
            byte[] decoded = Base64.getDecoder().decode(base64Ciphertext);
            return decryptBytes(decoded, upperKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 input: " + e.getMessage());
        }
    }
}
