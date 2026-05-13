package com.cipher.controller;

import com.cipher.model.CipherRequest;
import com.cipher.model.CipherResponse;
import com.cipher.service.CipherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CipherController {

    private static final Logger logger = LoggerFactory.getLogger(CipherController.class);
    private final CipherService cipherService;

    @Autowired
    public CipherController(CipherService cipherService) {
        this.cipherService = cipherService;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<CipherResponse> encrypt(@RequestBody CipherRequest request) {
        try {
            String result = cipherService.encrypt(request.getText(), request.getKey());
            return ResponseEntity.ok(CipherResponse.ok(result, "Encrypted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(CipherResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CipherResponse.error("Encryption failed: " + e.getMessage()));
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<CipherResponse> decrypt(@RequestBody CipherRequest request) {
        try {
            String result = cipherService.decrypt(request.getText(), request.getKey());
            return ResponseEntity.ok(CipherResponse.ok(result, "Decrypted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Decryption validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CipherResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            return ResponseEntity.badRequest()
                    .body(CipherResponse.error("Decryption failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
