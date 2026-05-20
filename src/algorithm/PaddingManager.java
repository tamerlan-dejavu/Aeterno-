package algorithm;

import java.util.Arrays;

public class PaddingManager {
    private static final int BLOCK_SIZE = 64;
    public byte[] pad(byte[] data) {
        data = (data == null) ? new byte[0] : data;
        int padLen = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        byte[] padded = Arrays.copyOf(data, data.length + padLen);
        Arrays.fill(padded, data.length, padded.length, (byte) padLen);
        return padded;
    }

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
