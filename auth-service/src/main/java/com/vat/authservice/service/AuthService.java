package com.vat.authservice.service;

import com.vat.authservice.dto.RequestDTO;
import com.vat.authservice.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthService(PasswordEncoder passwordEncoder, UserService userService,  JwtUtils jwtUtils) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }


    public void register(RequestDTO request) {
        userService.registerUser(new RequestDTO(request.getUsername(), passwordEncoder.encode(request.getPassword())));
    }

    public Optional<String> authenticate(RequestDTO request) {
        return userService.findByUsername(request.getUsername())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .map(u -> jwtUtils.generateToken(u.getUsername(), u.getRole()));
    }

    public boolean validate(String token) {
        try {
            jwtUtils.validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
