package com.csa.exchangerate.model;

import java.util.ArrayList;
import java.util.List;

public class ApiError {

	final private String message;
	final private Metadata metadata;

	ApiError(String message) {
		this.message = message;
		this.metadata = new Metadata();
	}

	public String getMessage() {
		return message;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public static class Metadata {
		
		private List<ApiFieldError> fields = new ArrayList<>();
		
		void addFieldError(String fieldName, String fieldError) {
			fields.add(new ApiFieldError(fieldName, fieldError));
		}
	}
	
	public static class ApiFieldError {
		final private String name;
		final private String error;
		
		protected ApiFieldError(String name, String error) {
			this.name = name;
			this.error = error;
		}
		
		public String getName() {
			return name;
		}
		public String getError() {
			return error;
		}
		
	}
}
