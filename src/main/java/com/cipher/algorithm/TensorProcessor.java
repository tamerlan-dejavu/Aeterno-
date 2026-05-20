package com.cipher.algorithm;

public class TensorProcessor {
    private static final int DIM = 4;
    private static final int BLOCK = 64;

    private int flatIndex(int x, int y, int z) {
        return x * 16 + y * 4 + z;
    }

    public byte[][][] loadTensor(byte[] src, int offset) {
        byte[][][] tensor = new byte[DIM][DIM][DIM];
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    tensor[x][y][z] = src[offset + flatIndex(x, y, z)];
                }
            }
        }
        return tensor;
    }

    public void storeTensor(byte[][][] tensor, byte[] dst, int offset) {
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    dst[offset + flatIndex(x, y, z)] = tensor[x][y][z];
                }
            }
        }
    }

    public void applyXorMask(byte[][][] tensor, byte[] mask) {
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    tensor[x][y][z] ^= mask[flatIndex(x, y, z)];
                }
            }
        }
    }

    public void applyMixing(byte[][][] tensor) {
        // Process X-axis: XOR each layer with next (in pairs: 0^1, 2^3)
        for (int y = 0; y < DIM; y++) {
            for (int z = 0; z < DIM; z++) {
                byte mix = (byte) (tensor[0][y][z] ^ tensor[1][y][z]);
                tensor[0][y][z] ^= mix;
                tensor[1][y][z] ^= mix;
                mix = (byte) (tensor[2][y][z] ^ tensor[3][y][z]);
                tensor[2][y][z] ^= mix;
                tensor[3][y][z] ^= mix;
            }
        }
        // Process Y-axis
        for (int x = 0; x < DIM; x++) {
            for (int z = 0; z < DIM; z++) {
                byte mix = (byte) (tensor[x][0][z] ^ tensor[x][1][z]);
                tensor[x][0][z] ^= mix;
                tensor[x][1][z] ^= mix;
                mix = (byte) (tensor[x][2][z] ^ tensor[x][3][z]);
                tensor[x][2][z] ^= mix;
                tensor[x][3][z] ^= mix;
            }
        }
        // Process Z-axis
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                byte mix = (byte) (tensor[x][y][0] ^ tensor[x][y][1]);
                tensor[x][y][0] ^= mix;
                tensor[x][y][1] ^= mix;
                mix = (byte) (tensor[x][y][2] ^ tensor[x][y][3]);
                tensor[x][y][2] ^= mix;
                tensor[x][y][3] ^= mix;
            }
        }
    }

    public void applyInverseMixing(byte[][][] tensor) {
        // Exact inverse: reverse order of axis processing
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                byte mix = (byte) (tensor[x][y][2] ^ tensor[x][y][3]);
                tensor[x][y][2] ^= mix;
                tensor[x][y][3] ^= mix;
                mix = (byte) (tensor[x][y][0] ^ tensor[x][y][1]);
                tensor[x][y][0] ^= mix;
                tensor[x][y][1] ^= mix;
            }
        }
        for (int x = 0; x < DIM; x++) {
            for (int z = 0; z < DIM; z++) {
                byte mix = (byte) (tensor[x][2][z] ^ tensor[x][3][z]);
                tensor[x][2][z] ^= mix;
                tensor[x][3][z] ^= mix;
                mix = (byte) (tensor[x][0][z] ^ tensor[x][1][z]);
                tensor[x][0][z] ^= mix;
                tensor[x][1][z] ^= mix;
            }
        }
        for (int y = 0; y < DIM; y++) {
            for (int z = 0; z < DIM; z++) {
                byte mix = (byte) (tensor[2][y][z] ^ tensor[3][y][z]);
                tensor[2][y][z] ^= mix;
                tensor[3][y][z] ^= mix;
                mix = (byte) (tensor[0][y][z] ^ tensor[1][y][z]);
                tensor[0][y][z] ^= mix;
                tensor[1][y][z] ^= mix;
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
