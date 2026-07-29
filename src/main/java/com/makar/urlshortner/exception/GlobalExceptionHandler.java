package com.makar.urlshortner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchUrlException.class)
    public ResponseEntity<String> handleNoSuchUrlException(NoSuchUrlException noSuchUrlException){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(noSuchUrlException.getMessage());
    }
}
