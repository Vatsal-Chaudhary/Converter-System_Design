package com.vat.uploadservice.exceptions;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<Map<String, String>> emptyFile(EmptyFileException ex) {
        logger.warn("Uploaded file is empty: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "Uploaded file seems to be empty");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, String>> invalidFile(InvalidFileException ex) {
        logger.warn("Uploaded file is invalid: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "Uploaded file is invalid");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(SizeExceedingException.class)
    public ResponseEntity<Map<String, String>> exceedsSize(SizeExceedingException ex) {
        logger.warn("File is too heavy to upload: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "can't upload this large file");
        return ResponseEntity.badRequest().body(error);
    }
}