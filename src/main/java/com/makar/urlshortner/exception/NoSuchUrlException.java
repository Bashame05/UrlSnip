package com.makar.urlshortner.exception;

public class NoSuchUrlException extends RuntimeException {
    public NoSuchUrlException(String message) {
        super(message);
    }
}
