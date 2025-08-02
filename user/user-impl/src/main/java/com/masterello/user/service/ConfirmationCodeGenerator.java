package com.masterello.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ConfirmationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();


    public String generateRandomDigitCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int digit = RANDOM.nextInt(10); // 0–9
            code.append(digit);
        }
        return code.toString();
    }
}
