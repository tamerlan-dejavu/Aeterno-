package com.cipher.algorithm;

public class LayerShifter {
    private static final int DIM = 4;

    private enum Axis { X, Y, Z }

    public void shiftX(byte[][][] tensor, int[] shifts, boolean inverse) {
        shiftAxis(tensor, shifts, inverse, Axis.X);
    }

    public void shiftY(byte[][][] tensor, int[] shifts, boolean inverse) {
        shiftAxis(tensor, shifts, inverse, Axis.Y);
    }

    public void shiftZ(byte[][][] tensor, int[] shifts, boolean inverse) {
        shiftAxis(tensor, shifts, inverse, Axis.Z);
    }

    private int calculateShift(int shiftValue, boolean inverse) {
        int planeSize = DIM * DIM;
        int normalizedShift = ((shiftValue % planeSize) + planeSize) % planeSize;
        if (inverse) {
            return (planeSize - normalizedShift) % planeSize;
        }
        return normalizedShift;
    }

    private void shiftAxis(byte[][][] tensor, int[] shifts, boolean inverse, Axis axis) {
        for (int i = 0; i < DIM; i++) {
            int s = calculateShift(shifts[i], inverse);
            if (s == 0) continue;

            byte[] plane = extractPlane(tensor, i, axis);
            plane = circularShift(plane, s);
            restorePlane(tensor, i, plane, axis);
        }
    }

    private byte[] extractPlane(byte[][][] tensor, int idx, Axis axis) {
        byte[] plane = new byte[DIM * DIM];
        switch (axis) {
            case X:
                for (int y = 0; y < DIM; y++) {
                    for (int z = 0; z < DIM; z++) {
                        plane[y * DIM + z] = tensor[idx][y][z];
                    }
                }
                break;
            case Y:
                for (int x = 0; x < DIM; x++) {
                    for (int z = 0; z < DIM; z++) {
                        plane[x * DIM + z] = tensor[x][idx][z];
                    }
                }
                break;
            case Z:
                for (int x = 0; x < DIM; x++) {
                    for (int y = 0; y < DIM; y++) {
                        plane[x * DIM + y] = tensor[x][y][idx];
                    }
                }
                break;
        }
        return plane;
    }

    private void restorePlane(byte[][][] tensor, int idx, byte[] plane, Axis axis) {
        switch (axis) {
            case X:
                for (int y = 0; y < DIM; y++) {
                    for (int z = 0; z < DIM; z++) {
                        tensor[idx][y][z] = plane[y * DIM + z];
                    }
                }
                break;
            case Y:
                for (int x = 0; x < DIM; x++) {
                    for (int z = 0; z < DIM; z++) {
                        tensor[x][idx][z] = plane[x * DIM + z];
                    }
                }
                break;
            case Z:
                for (int x = 0; x < DIM; x++) {
                    for (int y = 0; y < DIM; y++) {
                        tensor[x][y][idx] = plane[x * DIM + y];
                    }
                }
                break;
        }
    }

    private byte[] circularShift(byte[] arr, int s) {
        int len = arr.length;
        s = ((s % len) + len) % len;
        byte[] result = new byte[len];
        System.arraycopy(arr, s, result, 0, len - s);
        System.arraycopy(arr, 0, result, len - s, s);
        return result;
    }
}
