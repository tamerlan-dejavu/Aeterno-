# Custom Cipher Tool

A symmetric encryption tool built with **Java 17 + Spring Boot 3** (backend) and **HTML + Tailwind CSS + Vanilla JS** (frontend). It implements a custom cipher based on **XOR + Circular Bit Shift** with positional whitening.

## Project structure

```
custom-cipher-tool/
├── backend/           Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/cipher/
│       ├── CipherApplication.java
│       ├── controller/CipherController.java
│       ├── service/CipherService.java
│       ├── service/CustomCipherServiceImpl.java
│       ├── algorithm/CustomCipher.java
│       ├── model/CipherRequest.java
│       ├── model/CipherResponse.java
│       └── config/CorsConfig.java
└── frontend/          Static UI (Vercel-ready)
    ├── index.html
    └── vercel.json
```

## Running the backend

Requirements: Java 17+, Maven 3.8+.

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Endpoints
- `POST /api/encrypt` &mdash; body `{ "text": "...", "key": "..." }` &rarr; `{ result, success, message }`
- `POST /api/decrypt` &mdash; body `{ "text": "<base64>", "key": "..." }` &rarr; `{ result, success, message }`
- `GET  /api/health`  &mdash; `{ "status": "UP" }`

## Running the frontend

The simplest way: open `frontend/index.html` directly in your browser.

If you prefer a tiny local server (avoids any browser quirks):

```bash
cd frontend
python -m http.server 5500
# then visit http://localhost:5500
```

The frontend talks to `http://localhost:8080` by default. To point it at a deployed backend, edit the `API_BASE` constant at the top of the `<script>` block in `index.html`.

## Deployment

### Frontend &mdash; Vercel
1. Go to https://vercel.com/dashboard
2. **Add New** → **Project**
3. Import `tamerlan-dejavu/Aeterno-` repo
4. In **Root Directory**, select `custom-cipher-tool/frontend`
5. Leave Build Command and Output Directory empty
6. Click **Deploy**
7. Wait for completion → you get a public URL like `https://aeterno.vercel.app`

### Backend &mdash; Railway (recommended)
1. Go to https://railway.app
2. **New Project** → **Deploy from GitHub Repo**
3. Select `tamerlan-dejavu/Aeterno-`
4. **Root Directory:** `custom-cipher-tool/backend`
5. Railway auto-detects Maven + builds via Docker
6. Wait ~5 minutes for deployment
7. Click the **Railway deployment** link to get your backend URL (e.g., `https://custom-cipher-tool-backend.railway.app`)
8. Update `frontend/index.html` line 183-185 with your actual backend URL:
   ```javascript
   const API_BASE = 'https://your-railway-backend-url';
   ```
9. Commit & push → Vercel auto-rebuilds

**Quick alternative:** Backend URL auto-detects based on hostname. If you name your Railway service `custom-cipher-tool-backend`, the frontend will find it automatically.

## Algorithm summary

For each plaintext byte at position `i`:
1. `keyByte = key[i % key.length]`
2. `shiftAmount = (keyByte % 7) + 1`  (1..7)
3. `rotated = circularLeftShift(byte, shiftAmount)`
4. `ciphertext[i] = rotated XOR keyByte XOR (i % 256)`

Decryption mirrors this exactly. See `THEORY.md` for full analysis.
