package com.vsdya.taskflow.auth.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class PasswordByteLengthValidator
        implements ConstraintValidator<PasswordByteLength, String> {

    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;
    private static final int MAX_PASSWORD_CHARACTERS = 72;

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null || password.length() > MAX_PASSWORD_CHARACTERS) {
            // Let @Size report the character-count violation. This validator
            // is specifically responsible for BCrypt's UTF-8 byte limit.
            return true;
        }

        return password.getBytes(StandardCharsets.UTF_8).length
                <= MAX_BCRYPT_PASSWORD_BYTES;
    }
}
