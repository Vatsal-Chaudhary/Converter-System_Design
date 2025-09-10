package com.vat.authservice.execptions;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlreadyExists.class)
    public ResponseEntity<Map<String, String>> handleAlreadyExists(AlreadyExists ex) {
        logger.warn("Email already exists: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "Email already exists");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> userNotFound(UserNotFoundException ex) {
        logger.warn("User with this id not exists: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "User with this id not exists");
        return ResponseEntity.badRequest().body(error);
    }
}
