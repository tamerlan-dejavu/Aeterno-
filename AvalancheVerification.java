import com.cipher.algorithm.TensorCipher;
import java.util.Arrays;

public class AvalancheVerification {
    public static void main(String[] args) {
        System.out.println("=== AVALANCHE EFFECT VERIFICATION ===\n");

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

        System.out.println("Test 1: Avalanche Effect");
        System.out.println("  Changed: 1 bit in plaintext (byte 0, bit 0)");
        System.out.println("  Bit differences: " + bitDifferences + " out of " + totalBits);
        System.out.println("  Percentage: " + String.format("%.2f%%", percentage));
        System.out.println("  Expected: 40-60%");
        System.out.println("  Result: " + (percentage >= 40 && percentage <= 60 ? "✓ PASS" : "✗ FAIL"));

        System.out.println("\nTest 2: Decryption Correctness");
        byte[] decrypted = cipher.decryptBytes(ciphertext1, key);
        boolean matches = Arrays.equals(plaintext, decrypted);
        System.out.println("  Original length: " + plaintext.length);
        System.out.println("  Decrypted length: " + decrypted.length);
        System.out.println("  Match: " + (matches ? "✓ PASS" : "✗ FAIL"));

        System.out.println("\nTest 3: Multi-byte Change");
        byte[] plaintext3 = plaintext.clone();
        plaintext3[5] ^= 0xFF;

        byte[] ciphertext3 = cipher.encryptBytes(plaintext3, key);

        int bitDifferences2 = 0;
        for (int i = 0; i < ciphertext1.length; i++) {
            byte xor = (byte) (ciphertext1[i] ^ ciphertext3[i]);
            bitDifferences2 += Integer.bitCount(xor & 0xFF);
        }

        double percentage2 = (bitDifferences2 * 100.0) / totalBits;
        System.out.println("  Changed: 8 bits in plaintext (byte 5)");
        System.out.println("  Bit differences: " + bitDifferences2 + " out of " + totalBits);
        System.out.println("  Percentage: " + String.format("%.2f%%", percentage2));
        System.out.println("  Result: " + (percentage2 >= 30 ? "✓ PASS (good diffusion)" : "✗ WEAK"));

        System.out.println("\n=== SUMMARY ===");
        boolean allPass = matches && percentage >= 40 && percentage <= 60;
        System.out.println("Overall: " + (allPass ? "✓ ALL TESTS PASSED" : "✗ SOME TESTS FAILED"));
    }
}
