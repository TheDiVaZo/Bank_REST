package com.example.bankcards.service.crypto;

public interface CryptoService {
    /** Шифрует исходный текст и возвращает base64. **/
    String encrypt(String plaintext);

    /** Расшифровывает результат encrypt(...) обратно в исходный текст. */
    String decrypt(String ciphertextBlobBase64);

    /** Возвращает детерминированный отпечаток (hex HMAC-SHA256) от исходного текста. */
    String fingerprint(String plaintext);
}
