# Tensor-Cube Cipher

A custom symmetric encryption tool built with Java 17 + Spring Boot on the backend and plain HTML/CSS/JS on the frontend.

## What it does

Encrypts and decrypts text using a custom block cipher based on a 4×4×4 tensor (64-byte blocks). The key is derived via SHA-256 into 4 rounds of shift vectors and XOR masks. Each block goes through layer shifts along X, Y, Z axes and a mixing step that spreads changes across the entire block, achieving avalanche effect. Output is Base64-encoded.

## Stack

- **Backend** — Java 17, Spring Boot 3, Maven
- **Frontend** — HTML, Tailwind CSS, Vanilla JS
- **Deployed** — Render (backend), Vercel (frontend)

## API

```
POST /api/encrypt   { "text": "...", "key": "..." }
POST /api/decrypt   { "text": "...", "key": "..." }
GET  /api/health
```

## Authors

Bauyrzhan Tamerlan, Derevyanchenko Kirill
