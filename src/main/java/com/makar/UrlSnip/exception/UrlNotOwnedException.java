package com.makar.UrlSnip.exception;

public class UrlNotOwnedException extends RuntimeException {
    public UrlNotOwnedException(String message) {
        super(message);
    }
}
