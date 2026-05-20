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
        byte[][][] temp = new byte[DIM][DIM][DIM];
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    temp[x][y][z] = tensor[x][y][z];
                }
            }
        }
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    byte val = temp[x][y][z];
                    val ^= temp[(x + 1) % DIM][y][z];
                    val ^= temp[x][(y + 1) % DIM][z];
                    val ^= temp[x][y][(z + 1) % DIM];
                    tensor[x][y][z] = val;
                }
            }
        }
    }

    public void applyInverseMixing(byte[][][] tensor) {
        byte[][][] temp = new byte[DIM][DIM][DIM];
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    temp[x][y][z] = tensor[x][y][z];
                }
            }
        }
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                for (int z = 0; z < DIM; z++) {
                    byte val = temp[x][y][z];
                    val ^= temp[(x - 1 + DIM) % DIM][y][z];
                    val ^= temp[x][(y - 1 + DIM) % DIM][z];
                    val ^= temp[x][y][(z - 1 + DIM) % DIM];
                    tensor[x][y][z] = val;
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
