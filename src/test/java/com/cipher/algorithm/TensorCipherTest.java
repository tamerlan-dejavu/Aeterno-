package com.cipher.algorithm;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TensorCipherTest {

    private final TensorCipher cipher = new TensorCipher();

    @Test
    void roundTrip_variousInputs() {
        String key = "SECRET_KEY";
        String[] cases = {
                "HELLO, WORLD!",
                "0123456789ABCDEF",
                "X".repeat(64),
                "X".repeat(63),
                "X".repeat(65),
                "A"
        };
        for (String pt : cases) {
            byte[] ct = cipher.encryptBytes(pt.getBytes(StandardCharsets.UTF_8), key);
            byte[] dt = cipher.decryptBytes(ct, key);
            assertEquals(pt, new String(dt, StandardCharsets.UTF_8), "round trip failed for: " + pt);
        }
    }

    @Test
    void roundTrip_emptyInput() {
        String key = "SECRET_KEY";
        byte[] ct = cipher.encryptBytes(new byte[0], key);
        byte[] dt = cipher.decryptBytes(ct, key);
        assertEquals(0, dt.length);
    }

    @Test
    void avalanche_plaintextSingleBitFlip() {
        String key = "TEST_KEY";
        byte[] pt1 = "AAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8);
        byte[] pt2 = pt1.clone();
        pt2[pt2.length - 1] ^= 0x01; // flip one bit

        byte[] c1 = cipher.encryptBytes(pt1, key);
        byte[] c2 = cipher.encryptBytes(pt2, key);

        double avalanche = bitDiffRatio(c1, c2);
        System.out.println("Plaintext-flip avalanche: " + (avalanche * 100) + "%");
        assertTrue(avalanche >= 0.40,
                "Weak avalanche on plaintext bit-flip: " + avalanche);
    }

    @Test
    void avalanche_keySingleBitFlip() {
        byte[] pt = "AAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8);
        String k1 = "TESTKEY1";
        String k2 = "TESTKEY2"; // differs in low bits of last byte

        byte[] c1 = cipher.encryptBytes(pt, k1);
        byte[] c2 = cipher.encryptBytes(pt, k2);

        double avalanche = bitDiffRatio(c1, c2);
        System.out.println("Key-flip avalanche: " + (avalanche * 100) + "%");
        assertTrue(avalanche >= 0.40,
                "Weak avalanche on key change: " + avalanche);
    }

    @Test
    void avalanche_averageOverManyTrials() {
        String key = "AVERAGE_KEY";
        Random rng = new Random(42);
        int trials = 64;
        double sum = 0.0;
        for (int t = 0; t < trials; t++) {
            byte[] pt1 = new byte[64];
            rng.nextBytes(pt1);
            byte[] pt2 = pt1.clone();
            int bitIdx = rng.nextInt(pt1.length * 8);
            pt2[bitIdx / 8] ^= (byte) (1 << (bitIdx % 8));

            byte[] c1 = cipher.encryptBytes(pt1, key);
            byte[] c2 = cipher.encryptBytes(pt2, key);
            sum += bitDiffRatio(c1, c2);
        }
        double avg = sum / trials;
        System.out.println("Average avalanche over " + trials + " trials: " + (avg * 100) + "%");
        assertTrue(avg >= 0.45, "Average avalanche too low: " + avg);
        assertTrue(avg <= 0.55, "Average avalanche suspiciously high: " + avg);
    }

    @Test
    void deterministicEncryption() {
        String key = "KEY";
        byte[] pt = "DETERMINISTIC".getBytes(StandardCharsets.UTF_8);
        byte[] c1 = cipher.encryptBytes(pt, key);
        byte[] c2 = cipher.encryptBytes(pt, key);
        assertArrayEquals(c1, c2);
    }

    @Test
    void differentKeysProduceDifferentCiphertexts() {
        byte[] pt = "SAME PLAINTEXT".getBytes(StandardCharsets.UTF_8);
        byte[] c1 = cipher.encryptBytes(pt, "KEY_ONE");
        byte[] c2 = cipher.encryptBytes(pt, "KEY_TWO");
        assertFalse(java.util.Arrays.equals(c1, c2));
    }

    private static double bitDiffRatio(byte[] a, byte[] b) {
        assertEquals(a.length, b.length, "ciphertext length mismatch");
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff += Integer.bitCount((a[i] ^ b[i]) & 0xFF);
        }
        return (double) diff / (a.length * 8);
    }
}
