package com.vat.authservice.service;

import com.vat.authservice.dto.RequestDTO;
import com.vat.authservice.dto.ResponseDTO;
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
        userService.registerUser(new RequestDTO(request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getRole()));
    }

    public Optional<ResponseDTO> authenticate(RequestDTO request) {
        return userService.findByUsername(request.getUsername())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .map(u -> new ResponseDTO(jwtUtils.generateToken(u.getUsername(), u.getRole()), u.getId()));
    }

    public boolean validate(String token) {
        try {
            jwtUtils.validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean validateAdmin(String token) {
        try {
            jwtUtils.validateToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            return "ADMIN".equals(role);
        } catch (JwtException e) {
            return false;
        }
    }
}
