package com.vsdya.taskflow.auth.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordByteLengthValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordByteLength {

    String message() default "Password must not exceed 72 bytes when encoded as UTF-8";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
