package com.cipher.algorithm;

/**
 * Performs circular layer shifts on tensor axes (X, Y, Z).
 * Each shift operation circularly shifts a 16-element plane (4×4 flat array).
 */
public class LayerShifter {
    private static final int DIM = 4;

    /**
     * X-axis shift: for each x, circularly shift the flat Y×Z plane.
     * Plane is read in row-major order: plane[y*DIM + z]
     */
    public void shiftX(byte[][][] tensor, int[] shifts, boolean inverse) {
        for (int x = 0; x < DIM; x++) {
            int s = calculateShift(shifts[x], inverse);
            if (s == 0) continue;

            byte[] plane = extractXPlane(tensor, x);
            plane = circularShift(plane, s);
            restoreXPlane(tensor, x, plane);
        }
    }

    /**
     * Y-axis shift: for each y, circularly shift the flat X×Z plane.
     */
    public void shiftY(byte[][][] tensor, int[] shifts, boolean inverse) {
        for (int y = 0; y < DIM; y++) {
            int s = calculateShift(shifts[y], inverse);
            if (s == 0) continue;

            byte[] plane = extractYPlane(tensor, y);
            plane = circularShift(plane, s);
            restoreYPlane(tensor, y, plane);
        }
    }

    /**
     * Z-axis shift: for each z, circularly shift the flat X×Y plane.
     */
    public void shiftZ(byte[][][] tensor, int[] shifts, boolean inverse) {
        for (int z = 0; z < DIM; z++) {
            int s = calculateShift(shifts[z], inverse);
            if (s == 0) continue;

            byte[] plane = extractZPlane(tensor, z);
            plane = circularShift(plane, s);
            restoreZPlane(tensor, z, plane);
        }
    }

    private int calculateShift(int shiftValue, boolean inverse) {
        if (inverse) {
            return (DIM * DIM - shiftValue % (DIM * DIM)) % (DIM * DIM);
        }
        return shiftValue % (DIM * DIM);
    }

    private byte[] extractXPlane(byte[][][] tensor, int x) {
        byte[] plane = new byte[DIM * DIM];
        for (int y = 0; y < DIM; y++) {
            for (int z = 0; z < DIM; z++) {
                plane[y * DIM + z] = tensor[x][y][z];
            }
        }
        return plane;
    }

    private void restoreXPlane(byte[][][] tensor, int x, byte[] plane) {
        for (int y = 0; y < DIM; y++) {
            for (int z = 0; z < DIM; z++) {
                tensor[x][y][z] = plane[y * DIM + z];
            }
        }
    }

    private byte[] extractYPlane(byte[][][] tensor, int y) {
        byte[] plane = new byte[DIM * DIM];
        for (int x = 0; x < DIM; x++) {
            for (int z = 0; z < DIM; z++) {
                plane[x * DIM + z] = tensor[x][y][z];
            }
        }
        return plane;
    }

    private void restoreYPlane(byte[][][] tensor, int y, byte[] plane) {
        for (int x = 0; x < DIM; x++) {
            for (int z = 0; z < DIM; z++) {
                tensor[x][y][z] = plane[x * DIM + z];
            }
        }
    }

    private byte[] extractZPlane(byte[][][] tensor, int z) {
        byte[] plane = new byte[DIM * DIM];
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                plane[x * DIM + y] = tensor[x][y][z];
            }
        }
        return plane;
    }

    private void restoreZPlane(byte[][][] tensor, int z, byte[] plane) {
        for (int x = 0; x < DIM; x++) {
            for (int y = 0; y < DIM; y++) {
                tensor[x][y][z] = plane[x * DIM + y];
            }
        }
    }

    /**
     * Circular left shift of byte array by s positions.
     */
    private byte[] circularShift(byte[] arr, int s) {
        int len = arr.length;
        s = ((s % len) + len) % len;
        byte[] result = new byte[len];
        System.arraycopy(arr, s, result, 0, len - s);
        System.arraycopy(arr, 0, result, len - s, s);
        return result;
    }
}
