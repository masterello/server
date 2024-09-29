package com.masterello.auth.service;

import com.masterello.auth.exception.EncryptorException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int KEY_SIZE = 32;
    private static final int IV_SIZE = 16;
    private static final Base64.Encoder B64E = Base64.getEncoder();
    private static final Base64.Decoder B64D = Base64.getDecoder();


    private final SecretKey secretKey;
    private final byte[] iv;

    public EncryptionService() {
        // Initialize or load the secret key and IV
        this.secretKey = generateSecretKey();
        this.iv = generateIV();
    }

    private SecretKey generateSecretKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(AES_ALGORITHM);
            int keySizeBits = KEY_SIZE * 8;
            generator.init(keySizeBits, RANDOM);
            return generator.generateKey();
        } catch (Exception e) {
            throw new EncryptorException("Failed to generate secret key", e);
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE]; // IV size for AES/CBC is 16 bytes
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return B64E.encodeToString(concat(iv, encryptedBytes));
        } catch (Exception e) {
            throw new EncryptorException("Encryption failed", e);
        }
    }

    public String decrypt(String ciphertext) {
        try {
            byte[][] byteArrays = split(B64D.decode(ciphertext));
            byte[] receivedIV = byteArrays[0];
            byte[] encryptedBytes = byteArrays[1];

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(receivedIV));
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptorException("Decryption failed", e);
        }
    }

    byte[] concat(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    byte[][] split(byte[] concatenatedBytes) {
        byte[] array1 = new byte[IV_SIZE]; // IV size for AES/CBC is 16 bytes
        byte[] array2 = new byte[concatenatedBytes.length - IV_SIZE];
        System.arraycopy(concatenatedBytes, 0, array1, 0, IV_SIZE);
        System.arraycopy(concatenatedBytes, 16, array2, 0, concatenatedBytes.length - IV_SIZE);
        return new byte[][]{array1, array2};
    }
}

