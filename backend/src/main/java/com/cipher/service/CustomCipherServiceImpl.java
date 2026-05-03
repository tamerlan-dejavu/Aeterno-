package com.cipher.service;

import com.cipher.algorithm.CustomCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class CustomCipherServiceImpl implements CipherService {

    private final CustomCipher cipher;

    @Autowired
    public CustomCipherServiceImpl(CustomCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public String encrypt(String plaintext, String key) {
        if (plaintext == null) {
            plaintext = "";
        }
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
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
