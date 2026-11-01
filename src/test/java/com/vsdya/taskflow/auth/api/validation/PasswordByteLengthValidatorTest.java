package com.vsdya.taskflow.auth.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PasswordByteLengthValidatorTest {

    private final PasswordByteLengthValidator validator =
            new PasswordByteLengthValidator();

    private final ConstraintValidatorContext context =
            mock(ConstraintValidatorContext.class);

    @Test
    void shouldAcceptNullPassword() {
        assertThat(validator.isValid(null, context))
                .isTrue();
    }

    @Test
    void shouldAcceptPasswordLongerThan72CharactersForSizeValidation() {
        String password = "a".repeat(73);

        assertThat(validator.isValid(password, context))
                .isTrue();
    }

    @Test
    void shouldAcceptPasswordWithExactly72Utf8Bytes() {
        String password = "a".repeat(72);

        assertThat(validator.isValid(password, context))
                .isTrue();
    }

    @Test
    void shouldRejectPasswordExceeding72Utf8Bytes() {
        // Each emoji occupies 4 bytes in UTF-8.
        // 19 emoji = 76 bytes, but only 38 Java characters.
        String password = "😀".repeat(19);

        assertThat(validator.isValid(password, context))
                .isFalse();
    }
}