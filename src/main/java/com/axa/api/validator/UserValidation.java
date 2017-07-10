package com.axa.api.validator;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.axa.api.model.input.TokenInput;

public class UserValidation implements ConstraintValidator<Annotation, TokenInput> {

	@Override
	public void initialize(Annotation arg0) {
	}

	@Override
	public boolean isValid(TokenInput input, ConstraintValidatorContext arg1) {
    	return !((input.getMail() != null && !input.getMail().isEmpty()) 
    			&& (input.getPhone() != null && !input.getPhone().isEmpty())); 
	}
	 
}
