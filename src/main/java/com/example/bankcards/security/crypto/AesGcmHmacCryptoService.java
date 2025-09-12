package com.example.bankcards.security.crypto;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Реализация CryptoService:
 *  - AES/GCM/NoPadding с 96-битным IV и 128-битным тэгом аутентичности;
 *  - HMAC-SHA256 для fingerprint (возвращается в hex, 64 символа).
 *
 *  Очень помогли эти ответы в реализации:
 *  - https://stackoverflow.com/questions/76434457/how-to-write-java-code-for-aes-gcm-decryption-using-authentication-tags
 *  - https://gist.github.com/patrickfav/7e28d4eb4bf500f7ee8012c4a0cf7bbf
 *  Пофакту алгоритм был взят от туда. Я только дополнительно сделал кодирование в Base64
 */
public final class AesGcmHmacCryptoService implements CryptoService {
    private static final HexFormat HEX = HexFormat.of();

    private static final String ENC_ALG = "AES/GCM/NoPadding";
    private static final String HMAC_ALG = "HmacSHA256";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;

    private final SecretKey aesKey;
    private final SecretKey hmacKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmHmacCryptoService(SecretKey aesKey, SecretKey hmacKey) {
        this.aesKey = Objects.requireNonNull(aesKey, "aesKey");
        this.hmacKey = Objects.requireNonNull(hmacKey, "hmacKey");
        if (
                Arrays.stream(ENC_ALG.split("/"))
                        .noneMatch(alg -> alg.equalsIgnoreCase(HMAC_ALG))
        ) {
            throw new IllegalArgumentException("aesKey must be AES");
        }
        if (!"HmacSHA256".equalsIgnoreCase(hmacKey.getAlgorithm())) {
            throw new IllegalArgumentException("hmacKey must be HmacSHA256");
        }
    }

    // Для DI спинга
    public static AesGcmHmacCryptoService fromHex(String aesHex, String hmacHex) {
        return new AesGcmHmacCryptoService(
                new SecretKeySpec(hexToBytes(aesHex), "AES"),
                new SecretKeySpec(hexToBytes(hmacHex), "HmacSHA256")
        );
    }

    // Для спринга
    public static AesGcmHmacCryptoService fromBase64(String aesB64, String hmacB64) {
        return new AesGcmHmacCryptoService(
                new SecretKeySpec(Base64.getDecoder().decode(aesB64), "AES"),
                new SecretKeySpec(Base64.getDecoder().decode(hmacB64), "HmacSHA256")
        );
    }

    @Override
    public String encrypt(String plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");
        byte[] iv = new byte[IV_LEN];
        secureRandom.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(ENC_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // blob = iv, ciphertext+tag
            byte[] blob = ByteBuffer.allocate(iv.length + ct.length)
                    .put(iv)
                    .put(ct)
                    .array();
            return Base64.getEncoder().encodeToString(blob);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM encrypt failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertextBlobBase64) {
        Objects.requireNonNull(ciphertextBlobBase64, "ciphertextBlobBase64");
        byte[] blob = Base64.getDecoder().decode(ciphertextBlobBase64);
        if (blob.length <= IV_LEN) {
            throw new IllegalArgumentException("Ciphertext blob too short");
        }
        byte[] iv = new byte[IV_LEN];
        byte[] ct = new byte[blob.length - IV_LEN];
        System.arraycopy(blob, 0, iv, 0, IV_LEN);
        System.arraycopy(blob, IV_LEN, ct, 0, ct.length);

        try {
            Cipher cipher = Cipher.getInstance(ENC_ALG);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM decrypt failed (bad key/iv/tag/blob?)", e);
        }
    }

    @Override
    public String fingerprint(String plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(hmacKey);
            byte[] out = mac.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(out); // 64-символьный hex — удобно под VARCHAR(64)
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 failed", e);
        }
    }

    /* утилиты */

    public static String randomAesKeyHex(int bits) {
        byte[] key = new byte[bits / 8];
        new SecureRandom().nextBytes(key);
        return HEX.formatHex(key);
    }

    public static String randomHmacKeyHex() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return HEX.formatHex(key);
    }

    private static String bytesToHex(byte[] b) {
        return HEX.formatHex(b);
    }

    private static byte[] hexToBytes(String s) {
        return HEX.parseHex(s);
    }
}
