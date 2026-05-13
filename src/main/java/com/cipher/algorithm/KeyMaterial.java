package com.cipher.algorithm;

/**
 * Holds derived key material from a master key.
 * Contains: shift values for X/Y/Z axes and XOR mask.
 */
public class KeyMaterial {
    private static final int DIM = 4;
    private static final int BLOCK = 64;

    final int[] shiftX = new int[DIM];
    final int[] shiftY = new int[DIM];
    final int[] shiftZ = new int[DIM];
    final byte[] mask = new byte[BLOCK];

    public int[] getShiftX() { return shiftX; }
    public int[] getShiftY() { return shiftY; }
    public int[] getShiftZ() { return shiftZ; }
    public byte[] getMask() { return mask; }
}
