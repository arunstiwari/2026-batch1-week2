package com.fil.week2.validator;

import com.fil.week2.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {UniqueEmailValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,  ElementType.PARAMETER,  ElementType.RECORD_COMPONENT})
public @interface UniqueEmail {

    String message() default "Email is already registered.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
