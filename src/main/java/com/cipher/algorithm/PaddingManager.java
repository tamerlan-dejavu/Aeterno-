package com.cipher.algorithm;

import java.util.Arrays;

/**
 * Handles PKCS#7 padding and unpadding for block alignment.
 * Block size is 64 bytes.
 */
public class PaddingManager {
    private static final int BLOCK_SIZE = 64;

    /**
     * Apply PKCS#7 padding to data.
     * Padding bytes = block_size - (data.length % block_size)
     */
    public byte[] pad(byte[] data) {
        if (data == null) {
            data = new byte[0];
        }

        int padLen = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        byte[] padded = Arrays.copyOf(data, data.length + padLen);
        Arrays.fill(padded, data.length, padded.length, (byte) padLen);
        return padded;
    }

    /**
     * Remove PKCS#7 padding from data.
     * Validates that all padding bytes match the declared padding length.
     */
    public byte[] unpad(byte[] data) {
        if (data == null || data.length == 0) {
            return data;
        }

        int padLen = data[data.length - 1] & 0xFF;

        if (padLen < 1 || padLen > BLOCK_SIZE || padLen > data.length) {
            throw new IllegalArgumentException("Invalid PKCS#7 padding");
        }

        for (int i = data.length - padLen; i < data.length; i++) {
            if ((data[i] & 0xFF) != padLen) {
                throw new IllegalArgumentException("Invalid PKCS#7 padding");
            }
        }

        return Arrays.copyOf(data, data.length - padLen);
    }

    public int getBlockSize() {
        return BLOCK_SIZE;
    }
}
