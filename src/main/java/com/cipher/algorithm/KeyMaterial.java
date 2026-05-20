package com.cipher.algorithm;

public class KeyMaterial {
    private static final int DIM = 4;
    private static final int BLOCK = 64;
    private static final int ROUNDS = 4;

    private int[][] roundShiftX = new int[ROUNDS][DIM];
    private int[][] roundShiftY = new int[ROUNDS][DIM];
    private int[][] roundShiftZ = new int[ROUNDS][DIM];
    private byte[][] roundMask = new byte[ROUNDS][BLOCK];

    public int[] getShiftX(int round) { return roundShiftX[round]; }
    public int[] getShiftY(int round) { return roundShiftY[round]; }
    public int[] getShiftZ(int round) { return roundShiftZ[round]; }
    public byte[] getMask(int round) { return roundMask[round]; }

    public int getRounds() { return ROUNDS; }

    public int[] getShiftX() { return roundShiftX[0]; }
    public int[] getShiftY() { return roundShiftY[0]; }
    public int[] getShiftZ() { return roundShiftZ[0]; }
    public byte[] getMask() { return roundMask[0]; }
}
