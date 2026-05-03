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
1. Push the project to GitHub.
2. In Vercel, import the repo and set the **root directory** to `frontend/`.
3. Vercel auto-detects `vercel.json` and deploys the static site.

### Backend &mdash; Render / Railway (free tiers)

**Render:**
1. Create a new **Web Service**, point it at the repo.
2. Root directory: `backend`.
3. Build command: `mvn clean package -DskipTests`.
4. Start command: `java -jar target/custom-cipher-tool-1.0.0.jar`.
5. Environment: Java 17.
6. Once deployed, copy the public URL into `API_BASE` in the frontend.

**Railway:**
1. New project &rarr; Deploy from GitHub.
2. Set **Root Directory** to `backend/`.
3. Railway detects Maven and builds automatically.
4. Expose port 8080.

## Algorithm summary

For each plaintext byte at position `i`:
1. `keyByte = key[i % key.length]`
2. `shiftAmount = (keyByte % 7) + 1`  (1..7)
3. `rotated = circularLeftShift(byte, shiftAmount)`
4. `ciphertext[i] = rotated XOR keyByte XOR (i % 256)`

Decryption mirrors this exactly. See `THEORY.md` for full analysis.
