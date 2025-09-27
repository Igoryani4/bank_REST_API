package com.example.bankcards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;


@Service
@Slf4j
public class EncryptionService {
    private final SecretKeySpec secretKey;
    private final String algorithm = "AES/CBC/PKCS5Padding";

    public EncryptionService(@Value("${app.encryption.key}") String key) {
        byte[] keyBytes = normalizeKey(key);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        log.info("Encryption service initialized with key length: {} bytes", keyBytes.length);
    }

    public String encrypt(String data) {
        try {
            if (data == null) {
                throw new IllegalArgumentException("Data to encrypt cannot be null");
            }

            byte[] iv = new byte[16];
            java.security.SecureRandom.getInstanceStrong().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed for data: {}", data, e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.trim().isEmpty()) {
                log.error("Encrypted data is null or empty");
                return null;
            }

            String trimmedData = encryptedData.trim();

            if (!isValidBase64(trimmedData)) {
                log.error("Invalid Base64 format: {}", trimmedData);
                return null;
            }

            byte[] combined = Base64.getDecoder().decode(trimmedData);

            if (combined.length <= 16) {
                log.error("Invalid encrypted data length: {}", combined.length);
                return null;
            }

            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for encrypted data: '{}'", encryptedData, e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private byte[] normalizeKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        final int REQUIRED_LENGTH = 32; // 32 байта для AES-256

        if (keyBytes.length == REQUIRED_LENGTH) {
            return keyBytes;
        }

        if (keyBytes.length > REQUIRED_LENGTH) {
            byte[] normalized = new byte[REQUIRED_LENGTH];
            System.arraycopy(keyBytes, 0, normalized, 0, REQUIRED_LENGTH);
            log.warn("Key was truncated from {} to {} bytes", keyBytes.length, REQUIRED_LENGTH);
            return normalized;
        }

        byte[] normalized = new byte[REQUIRED_LENGTH];
        System.arraycopy(keyBytes, 0, normalized, 0, keyBytes.length);
        for (int i = keyBytes.length; i < REQUIRED_LENGTH; i++) {
            normalized[i] = 0;
        }
        log.warn("Key was padded from {} to {} bytes", keyBytes.length, REQUIRED_LENGTH);
        return normalized;
    }

    private boolean isValidBase64(String str) {
        if (str == null) return false;

        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean testEncryption() {
        try {
            String testData = "1234567890123456"; // тестовые данные карты
            String encrypted = encrypt(testData);
            String decrypted = decrypt(encrypted);
            boolean success = testData.equals(decrypted);
            log.info("Encryption test: {}", success ? "PASSED" : "FAILED");

            if (success) {
                log.info("Sample encrypted value: {}", encrypted);
            }

            return success;
        } catch (Exception e) {
            log.error("Encryption test failed", e);
            return false;
        }
    }

    // Дополнительный метод для проверки совместимости с существующими данными
    public boolean canDecryptExisting(String existingEncryptedData) {
        try {
            String decrypted = decrypt(existingEncryptedData);
            return decrypted != null && decrypted.matches("\\d+"); // Проверяем, что это числа (номер карты/CVV)
        } catch (Exception e) {
            log.debug("Cannot decrypt existing data: {}", e.getMessage());
            return false;
        }
    }
}