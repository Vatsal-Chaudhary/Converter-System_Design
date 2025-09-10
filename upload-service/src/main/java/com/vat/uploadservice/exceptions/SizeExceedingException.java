package com.vat.uploadservice.exceptions;

public class SizeExceedingException extends RuntimeException {
    public SizeExceedingException(String message) {
        super(message);
    }
}
