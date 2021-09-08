package com.csa.exchangerate.model;

public class ApiErrorBuilder {

	public static ApiErrorBuilder newError(String message) {
		return new ApiErrorBuilder(message);
	}
	
	private ApiError error;
	
	private ApiErrorBuilder(String message) {
		error = new ApiError(message);
	}
	
	public ApiErrorBuilder andFieldError(String fieldName, String fieldError) {
		error.getMetadata().addFieldError(fieldName, fieldError);
		return this;
	}
	
	public ApiError build() {
		return error;
	}
}
