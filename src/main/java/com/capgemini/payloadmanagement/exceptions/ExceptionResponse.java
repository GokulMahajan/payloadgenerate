package com.capgemini.payloadmanagement.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExceptionResponse {

	private String status;
	@JsonProperty("error_code")
	private String errorCode;
	@JsonProperty("error_message")
	private String errorMessage;
}