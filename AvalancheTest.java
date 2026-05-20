import com.cipher.algorithm.TensorCipher;

public class AvalancheTest {
    public static void main(String[] args) {
        TensorCipher cipher = new TensorCipher();
        String key = "test-key-12345";

        // Исходный текст (64 байта = 1 блок)
        byte[] plaintext = new byte[64];
        for (int i = 0; i < 64; i++) {
            plaintext[i] = (byte) i;
        }

        // Зашифруем оригинал
        byte[] ciphertext1 = cipher.encryptBytes(plaintext, key);

        // Измените 1 бит в позиции 0
        byte[] plaintext2 = plaintext.clone();
        plaintext2[0] ^= 0x01;  // Измени первый бит первого байта

        byte[] ciphertext2 = cipher.encryptBytes(plaintext2, key);

        // Подсчитаем различия в битах
        int bitDifferences = 0;
        for (int i = 0; i < ciphertext1.length; i++) {
            byte xor = (byte) (ciphertext1[i] ^ ciphertext2[i]);
            bitDifferences += Integer.bitCount(xor & 0xFF);
        }

        int totalBits = ciphertext1.length * 8;
        double percentage = (bitDifferences * 100.0) / totalBits;

        System.out.println("=== ТЕСТ ЛАВИННОГО ЭФФЕКТА ===");
        System.out.println("Изменили: 1 бит в исходном тексте");
        System.out.println("Различий в битах: " + bitDifferences + " из " + totalBits);
        System.out.println("Процент: " + String.format("%.2f%%", percentage));
        System.out.println();
        System.out.println("Ожидается для хорошего шифра: ~50%");
        System.out.println("Сейчас: " + (percentage >= 40 && percentage <= 60 ? "✓ ХОРОШО" : "✗ СЛАБО"));
    }
}
