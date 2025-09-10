package com.vat.authservice.controller;

import com.vat.authservice.dto.RequestDTO;
import com.vat.authservice.dto.ResponseDTO;
import com.vat.authservice.model.User;
import com.vat.authservice.service.AuthService;
import com.vat.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RequestDTO request) {
        authService.register(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody RequestDTO request) {
        Optional<ResponseDTO> authResponse = authService.authenticate(request);
        return authResponse.map(responseDTO ->
                                        new ResponseEntity<>(responseDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        return authService.validate(token) ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);

        if (!authService.validateAdmin(token)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization")  String authHeader, @RequestParam("User-Id") String userId) {
        if (!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);

        if (!authService.validateAdmin(token)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        String result = userService.deleteUser(userId);

        return switch (result) {
            case "DELETED" -> new ResponseEntity<>(HttpStatus.OK);
            case "NOT_FOUND" -> new ResponseEntity<>(HttpStatus.NOT_FOUND);
            case "ADMIN_DELETE_FORBIDDEN" -> new ResponseEntity<>(HttpStatus.FORBIDDEN);
            case "INVALID_UUID" -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
