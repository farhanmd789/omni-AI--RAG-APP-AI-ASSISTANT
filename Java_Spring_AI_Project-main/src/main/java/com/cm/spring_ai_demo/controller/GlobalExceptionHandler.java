package com.cm.spring_ai_demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Catches Spring's MaxUploadSizeExceededException and returns a clean JSON 413 response
     * instead of letting Tomcat reset the TCP connection (ERR_CONNECTION_RESET).
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of(
                        "status", "error",
                        "message", "The uploaded PDF is too large. Maximum allowed size is 50MB. Please compress or split the file.",
                        "code", "FILE_TOO_LARGE"
                ));
    }

    /**
     * Generic fallback handler for any other unhandled exception in controllers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        String causeMessage = (ex.getCause() != null && ex.getCause().getMessage() != null) 
                ? ex.getCause().getMessage() 
                : ex.getMessage();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", "error",
                        "message", "An unexpected error occurred: " + ex.getMessage() + (ex.getCause() != null ? " (" + causeMessage + ")" : ""),
                        "code", "INTERNAL_ERROR"
                ));
    }
}
