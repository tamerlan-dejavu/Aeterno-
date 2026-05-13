package com.cipher.algorithm;

/**
 * Handles tensor I/O and XOR mask application.
 * Converts between byte arrays and 4×4×4 tensors using row-major order.
 */
public class TensorProcessor {
    private static final int DIM = 4;
    private static final int BLOCK = 64;

    /**
     * Load 64 bytes from src at offset into a 4×4×4 tensor (row-major).
     * Index mapping: tensor[x][y][z] ← src[offset + x*16 + y*4 + z]
     */
    public byte[][][] loadTensor(byte[] src, int offset) {
        byte[][][] tensor = new byte[DIM][DIM][DIM];
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    tensor[x][y][z] = src[offset + x * 16 + y * 4 + z];
                }
            }
        }
        return tensor;
    }

    /**
     * Store 4×4×4 tensor back into dst at offset (row-major order).
     */
    public void storeTensor(byte[][][] tensor, byte[] dst, int offset) {
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    dst[offset + x * 16 + y * 4 + z] = tensor[x][y][z];
                }
            }
        }
    }

    /**
     * Apply XOR mask to all 64 cells of the tensor.
     * Mask is applied in row-major order: mask[x*16 + y*4 + z]
     */
    public void applyXorMask(byte[][][] tensor, byte[] mask) {
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    tensor[x][y][z] ^= mask[x * 16 + y * 4 + z];
                }
            }
        }
    }

    public int getDimension() {
        return DIM;
    }

    public int getBlockSize() {
        return BLOCK;
    }
}
