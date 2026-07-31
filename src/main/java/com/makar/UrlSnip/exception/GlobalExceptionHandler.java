package com.makar.UrlSnip.exception;

import com.makar.UrlSnip.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchUrlException.class)
    public ResponseEntity<ErrorResponseDto> handleNoSuchUrlException(NoSuchUrlException noSuchUrlException){
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity
                .status(status)
                .body(new ErrorResponseDto(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        noSuchUrlException.getMessage()
                ));
    }
    @ExceptionHandler(AliasNotAllowedException.class)
    public ResponseEntity<ErrorResponseDto> handleAliasNotAllowedException(AliasNotAllowedException aliasNotAllowedException) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity
                .status(status)
                .body(new ErrorResponseDto(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        aliasNotAllowedException.getMessage()
                ));
    }
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponseDto> handleUrlExpiredException(UrlExpiredException urlExpiredException) {
        HttpStatus status = HttpStatus.GONE;
        return ResponseEntity
                .status(status)
                .body(new ErrorResponseDto(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        urlExpiredException.getMessage()
                ));
    }
}
