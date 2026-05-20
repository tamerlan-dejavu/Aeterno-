package service;

import algorithm.TensorCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class CustomCipherServiceImpl implements CipherService {

    private final TensorCipher cipher;

    @Autowired
    public CustomCipherServiceImpl(TensorCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public String encrypt(String plaintext, String key) {
        if (plaintext == null) {
            plaintext = "";
        }
        // Normalize to uppercase before encryption (ASCII uppercase comes before lowercase)
        byte[] data = plaintext.toUpperCase().getBytes(StandardCharsets.UTF_8);
        return cipher.encryptToBase64(data, key);
    }

    @Override
    public String decrypt(String ciphertext, String key) {
        if (ciphertext == null) {
            ciphertext = "";
        }
        byte[] decrypted = cipher.decryptFromBase64(ciphertext, key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
