package com.capgemini.payloadmanagement.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;



import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String EXCEPTION_STRING = "Exception Occured :";
	private static final String MESSAGE = "ERROR";

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ExceptionResponse> badRequest(BadRequestException ex) {
		ExceptionResponse response=new ExceptionResponse();
		response.setStatus(MESSAGE);
		response.setErrorCode(HttpStatus.BAD_REQUEST.toString());
		response.setErrorMessage(ex.getMessage());
		log.error(EXCEPTION_STRING + ex.getMessage());
		return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(SchemaNotFountException.class)
	public ResponseEntity<ExceptionResponse> resourceNotFound(SchemaNotFountException ex) {
		ExceptionResponse response=new ExceptionResponse();
		response.setStatus(MESSAGE);
		response.setErrorCode(HttpStatus.NOT_FOUND.toString());
		response.setErrorMessage(ex.getMessage());
		log.error(EXCEPTION_STRING + ex.getMessage());
		return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
	}

	

}