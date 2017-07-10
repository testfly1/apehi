package com.axa.api.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = UserValidation.class)
public @interface UniqueOptionalIdentifier {
	 
	String message();

    Class[] groups() default {};
    
    Class[] payload() default {};
}
