import com.cipher.algorithm.TensorCipher;

public class AvalancheTest {
    public static void main(String[] args) {
        TensorCipher cipher = new TensorCipher();
        String key = "test-key-12345";

        byte[] plaintext = new byte[64];
        for (int i = 0; i < 64; i++) {
            plaintext[i] = (byte) i;
        }

        byte[] ciphertext1 = cipher.encryptBytes(plaintext, key);

        byte[] plaintext2 = plaintext.clone();
        plaintext2[0] ^= 0x01;

        byte[] ciphertext2 = cipher.encryptBytes(plaintext2, key);

        int bitDifferences = 0;
        for (int i = 0; i < ciphertext1.length; i++) {
            byte xor = (byte) (ciphertext1[i] ^ ciphertext2[i]);
            bitDifferences += Integer.bitCount(xor & 0xFF);
        }

        int totalBits = ciphertext1.length * 8;
        double percentage = (bitDifferences * 100.0) / totalBits;

        System.out.println("=== AVALANCHE EFFECT TEST ===");
        System.out.println("Changed: 1 bit in plaintext");
        System.out.println("Bit differences: " + bitDifferences + " out of " + totalBits);
        System.out.println("Percentage: " + String.format("%.2f%%", percentage));
        System.out.println();
        System.out.println("Expected for good cipher: 40-60%");
        System.out.println("Current: " + (percentage >= 40 && percentage <= 60 ? "✓ GOOD" : "✗ WEAK"));

        byte[] decrypted = cipher.decryptBytes(ciphertext1, key);
        boolean matches = java.util.Arrays.equals(plaintext, decrypted);
        System.out.println("Decryption test: " + (matches ? "✓ PASS" : "✗ FAIL"));
    }
}
