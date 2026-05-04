package com.cipher.service;

public interface CipherService {

    String encrypt(String plaintext, String key);

    String decrypt(String ciphertext, String key);
}
