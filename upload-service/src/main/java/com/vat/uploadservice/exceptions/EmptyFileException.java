package com.vat.uploadservice.exceptions;

public class EmptyFileException extends RuntimeException{
    public EmptyFileException(String message){ super( message); }
}
