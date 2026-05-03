# Theoretical Analysis &mdash; Custom XOR + Circular Bit Shift Cipher

## 6.1 Algorithm description (diagram-ready)

The cipher is a **byte-oriented stream cipher** with two transformation stages per byte: a circular bit rotation and a dual XOR (key + position). Both stages are invertible.

### Pseudocode &mdash; encryption

```
function encrypt(plaintext[], key[]):
    for i in 0 .. len(plaintext) - 1:
        keyByte     = key[i mod len(key)]
        shiftAmount = (keyByte mod 7) + 1          // 1..7, never 0
        rotated     = circularLeftShift(plaintext[i], shiftAmount)
        ciphertext[i] = rotated XOR keyByte XOR (i mod 256)
    return ciphertext
```

### Pseudocode &mdash; decryption (strict inverse)

```
function decrypt(ciphertext[], key[]):
    for i in 0 .. len(ciphertext) - 1:
        keyByte     = key[i mod len(key)]
        shiftAmount = (keyByte mod 7) + 1
        decXor      = ciphertext[i] XOR keyByte XOR (i mod 256)
        plaintext[i] = circularRightShift(decXor, shiftAmount)
    return plaintext
```

### ASCII data-flow diagram

```
                       ┌─────────────┐
plaintext byte  ─────► │  Circular   │
   (8 bits)            │  Left Shift │  shift = (keyByte mod 7) + 1
                       └──────┬──────┘
                              ▼
                       ┌─────────────┐
                       │  XOR        │ ◄── keyByte = key[i mod len(key)]
                       └──────┬──────┘
                              ▼
                       ┌─────────────┐
                       │  XOR        │ ◄── (i mod 256)   positional whitening
                       └──────┬──────┘
                              ▼
                        ciphertext byte
```

The shift amount is in `[1, 7]` &mdash; never `0` &mdash; so the rotation always changes the byte's bit layout, and never 8 (which would be a no-op).

---

## 6.2 Key space and brute-force resistance

- The key is an arbitrary UTF-8 string. Any key length `n >= 1` is allowed.
- For a key of `n` bytes, the raw key space is `256^n`.

| Cipher        | Key space          |
|---------------|--------------------|
| Caesar        | 25                 |
| Vigenère      | 26^n (Latin only)  |
| **XOR-Shift** | **256^n** (any byte) |
| AES-128       | 2^128              |

Caesar can be brute-forced in milliseconds. Vigenère with a 6-letter key is `~3 * 10^8` &mdash; trivial today. The XOR-Shift cipher with even a modest 16-byte key gives `256^16 = 2^128` &mdash; the same order as AES-128 against pure brute force. The weakness is not the key space; it is the algebraic structure (see 6.4).

---

## 6.3 Why XOR-Shift resists frequency analysis better than Vigenère

Three reasons:

1. **Circular shift breaks byte-value preservation.** In Vigenère, `'A'` shifted by `'K'` is always `'K'` &mdash; same input + same key byte = same output. Our cipher rotates bits before XOR, and the rotation amount itself depends on the key byte, so the same input byte under the same key byte still produces a recognisable shift, *but* each plaintext byte's bit pattern is reshuffled inside the byte. Single-byte frequency tables no longer line up with character frequencies of any human language.

2. **Positional XOR `(i mod 256)` defeats key-period leakage.** Even if the same plaintext byte appears many times under the same key byte (because the key repeats), the positional term differs as long as the positions are not congruent mod 256. So two `'e'` characters one position apart give two **different** ciphertext bytes &mdash; a frequency histogram of the ciphertext is essentially flat.

3. **No alphabet, no period, no Kasiski.** Vigenère's classical attacks (Kasiski examination, Friedman index of coincidence) hinge on the fact that ciphertext stays inside the same 26-letter alphabet and that identical plaintext-key pairings produce identical ciphertext at distances that are multiples of the key length. Our cipher lives in `[0, 255]`, mixes bit positions, and breaks the period through the positional term. Standard alphabetic statistical attacks do not apply.

---

## 6.4 Weaknesses

1. **Known-Plaintext Attack (KPA).** Given a `(plaintext, ciphertext)` pair the attacker can solve for the key byte at every position. Since `shiftAmount` depends on `keyByte`, the attacker tries the seven possible shifts:
   ```
   for shift in 1..7:
       rotated  = circularRightShift(ciphertext[i] XOR (i mod 256), shift) inverse hint
       candidate = circularLeftShift(plaintext[i], shift) XOR ciphertext[i] XOR (i mod 256)
       if (candidate mod 7) + 1 == shift:
           keyByte[i] = candidate
   ```
   With one candidate surviving the consistency check per position, the attacker reconstructs `keyByte[i]` for every `i`, and because the key repeats with period `len(key)`, just `len(key)` known plaintext bytes are enough to recover the entire key. This is the most serious flaw.

2. **Short key vs long message.** For key length `k`, the *key byte* repeats every `k` bytes. The positional term spreads this out over a period of `lcm(k, 256)`, but ciphertext-only attacks that align text along multiples of `k` (chosen far enough apart that the positional term cancels, e.g., positions `i` and `i + 256k`) recover identical key contributions. With long messages and a short key, this leaks structural information.

3. **No integrity / authentication.** There is no MAC or HMAC. An attacker who flips a bit in the ciphertext flips the corresponding bit in the decrypted plaintext (after the inverse shift) and the receiver has no way to detect the change. The cipher provides confidentiality only, not integrity.

4. **Deterministic, no nonce / IV.** The same `(plaintext, key)` always produces the same ciphertext. This enables replay attacks and lets an attacker detect when the same message was sent twice. A real construction would mix in a per-message nonce.

---

## 6.5 Comparative table

| Algorithm        | Key space  | Frequency analysis resistance | Known-Plaintext resistance | Speed                 |
|------------------|------------|-------------------------------|----------------------------|-----------------------|
| Caesar           | 25         | None                          | Broken (1 char enough)     | Very fast             |
| Vigenère         | 26^n       | Partial (broken via Kasiski)  | Partial                    | Fast                  |
| **Our XOR-Shift**| **256^n**  | **Strong**                    | **Vulnerable**             | **Fast**              |
| AES-128          | 2^128      | Strong                        | Strong                     | Fast (HW-accelerated) |

### Bottom line

The custom XOR-Shift cipher is a substantial step up from classical ciphers for **ciphertext-only** scenarios &mdash; it neutralises frequency analysis and offers an enormous key space. However, its linear structure makes it **fragile under known-plaintext attacks**, and it lacks the integrity, authenticity, and nonce-based randomisation that any real-world deployment requires. It is an educational construction, not a replacement for AES-GCM or ChaCha20-Poly1305.
