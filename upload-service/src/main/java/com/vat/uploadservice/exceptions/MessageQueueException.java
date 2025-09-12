package com.vat.uploadservice.exceptions;

public class MessageQueueException extends RuntimeException{
    public MessageQueueException(String message){
        super(message);
    }
}
