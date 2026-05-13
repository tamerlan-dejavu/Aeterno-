# Tensor-Cube Cipher Tool

A symmetric encryption tool built with **Java 17 + Spring Boot 3** (backend) and **HTML + Tailwind CSS + Vanilla JS** (frontend). 

It implements a **4×4×4 Tensor-Cube Cipher** with key derivation, circular layer shifts, and XOR masking.

## Algorithm Overview

**Tensor-Cube Cipher** is a block cipher operating on 64-byte (4×4×4) tensors:
- **Key Derivation**: SHA-256 hash produces shift vectors (X/Y/Z axes) and PRNG seed
- **Encryption per block**:
  1. Load 64 bytes as 4×4×4 tensor (row-major)
  2. Circular X-layer shift
  3. Circular Y-layer shift
  4. Circular Z-layer shift
  5. XOR mask (PRNG-generated)
  6. Store back
- **Decryption**: Same steps in reverse order
- **Padding**: PKCS#7 (64-byte blocks)
- **Encoding**: Base64

## Project Structure

```
custom-cipher-tool/
├── pom.xml
├── README.md
├── frontend/
│   ├── index.html        (Minimal GUI)
│   └── vercel.json
├── src/main/java/com/cipher/
│   ├── CipherApplication.java
│   ├── controller/
│   │   └── CipherController.java
│   ├── service/
│   │   ├── CipherService.java
│   │   └── CustomCipherServiceImpl.java
│   ├── algorithm/
│   │   ├── CustomCipher.java          (Main orchestrator)
│   │   ├── KeyDeriver.java            (SHA-256 key derivation)
│   │   ├── KeyMaterial.java           (Shift vectors + XOR mask)
│   │   ├── TensorProcessor.java       (Load/store tensor, apply XOR)
│   │   ├── LayerShifter.java          (X/Y/Z circular shifts)
│   │   └── PaddingManager.java        (PKCS#7)
│   ├── model/
│   │   ├── CipherRequest.java
│   │   └── CipherResponse.java
│   └── config/
│       └── CorsConfig.java
└── src/main/resources/
    └── application.properties
```

## OOP Design

The code is organized into **clear responsibility classes**:

- **`KeyDeriver`** — Converts master key string → shift values + XOR mask
- **`TensorProcessor`** — Handles I/O conversions, applies XOR mask
- **`LayerShifter`** — Performs circular shifts on X, Y, Z axes
- **`PaddingManager`** — PKCS#7 padding/unpadding
- **`CustomCipher`** — Orchestrates all components into encrypt/decrypt flow
- **`CustomCipherServiceImpl`** — Bridges cipher algorithm to REST endpoints
- **`CipherController`** — HTTP API with error handling and logging

## Installation & Running

### Requirements
- Java 17+
- Maven 3.8+

### Backend

```bash
cd custom-cipher-tool
mvn clean package
mvn spring-boot:run
```

API available at `http://localhost:8080`

#### Endpoints
- `POST /api/encrypt` — `{ "text": "plaintext", "key": "secret" }` → Base64 ciphertext
- `POST /api/decrypt` — `{ "text": "base64-cipher", "key": "secret" }` → plaintext
- `GET  /api/health`  — `{ "status": "UP" }`

### Frontend

Open `frontend/index.html` in a browser (or use `python -m http.server 5500`).

Edit `API_BASE` in the HTML script block to point to your backend.

## Features

✓ Symmetric encryption/decryption  
✓ SHA-256 key derivation  
✓ 4×4×4 tensor operations  
✓ Circular layer shifting (invertible)  
✓ PKCS#7 padding  
✓ Base64 encoding  
✓ File upload support  
✓ Copy to clipboard  
✓ REST API with proper error handling  
✓ Minimal, responsive GUI  

## Implementation Requirements ✓

- **Language**: Java (allowed per spec)
- **Modular code**: Split into OOP classes by responsibility
- **Readable & commented**: Each class has clear purpose and Javadoc
- **Minimal GUI**: Single HTML page with Tailwind CSS, no bloat

## Authors

Bauyrzhan Tamerlan  
Derevyanchenko Kirill
