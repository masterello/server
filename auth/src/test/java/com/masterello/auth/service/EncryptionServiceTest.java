package com.masterello.auth.service;

import com.masterello.auth.exception.EncryptorException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    void encryptAndDecrypt_WithValidData_ReturnsOriginalData() {
        // Arrange
        String originalText = "Hello, World!";

        // Act
        String encryptedText = encryptionService.encrypt(originalText);
        String decryptedText = encryptionService.decrypt(encryptedText);

        // Assert
        assertEquals(originalText, decryptedText);
    }

    @Test
    void encrypt_WithNullInput_ThrowsEncryptorException() {
        // Act & Assert
        assertThrows(EncryptorException.class, () -> encryptionService.encrypt(null));
    }

    @Test
    void decrypt_WithInvalidData_ThrowsEncryptorException() {
        // Act & Assert
        assertThrows(EncryptorException.class, () -> encryptionService.decrypt("invalidData"));
    }

    @Test
    void encryptAndDecrypt_MultipleTimes_ReturnsSameResult() {
        // Arrange
        String originalText = "Testing multiple encryption and decryption.";

        // Act
        String encryptedText1 = encryptionService.encrypt(originalText);
        String decryptedText1 = encryptionService.decrypt(encryptedText1);
        String encryptedText2 = encryptionService.encrypt(originalText);
        String decryptedText2 = encryptionService.decrypt(encryptedText2);

        // Assert
        assertEquals(originalText, decryptedText1);
        assertEquals(originalText, decryptedText2);
        assertEquals(encryptedText1, encryptedText2);
    }

    @Test
    void encryptAndDecrypt_EmptyString_ReturnsEmptyString() {
        // Arrange
        String originalText = "";

        // Act
        String encryptedText = encryptionService.encrypt(originalText);
        String decryptedText = encryptionService.decrypt(encryptedText);

        // Assert
        assertEquals(originalText, decryptedText);
    }

    @Test
    void encryptAndDecrypt_WithWhitespace_ReturnsOriginalData() {
        // Arrange
        String originalText = "  Some text with whitespace.  ";

        // Act
        String encryptedText = encryptionService.encrypt(originalText);
        String decryptedText = encryptionService.decrypt(encryptedText);

        // Assert
        assertEquals(originalText, decryptedText);
    }

    @Test
    void encryptAndDecrypt_LongText_ReturnsOriginalData() {
        // Arrange
        StringBuilder originalTextBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            originalTextBuilder.append("a");
        }
        String originalText = originalTextBuilder.toString();

        // Act
        String encryptedText = encryptionService.encrypt(originalText);
        String decryptedText = encryptionService.decrypt(encryptedText);

        // Assert
        assertEquals(originalText, decryptedText);
    }
}
