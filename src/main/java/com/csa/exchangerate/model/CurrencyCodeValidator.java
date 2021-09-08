package com.csa.exchangerate.model;

import java.util.Currency;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CurrencyCodeValidator implements ConstraintValidator<CurrencyCode, String> {

	@Override
	public void initialize(CurrencyCode constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {		
		try {
			Currency.getInstance(value);
			return true;
		} catch (IllegalArgumentException ae) {
			return false;
		}
	}

}
