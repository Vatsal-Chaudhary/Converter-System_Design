package com.vat.authservice.service;

import com.vat.authservice.dto.RequestDTO;
import com.vat.authservice.execptions.AlreadyExists;
import com.vat.authservice.model.User;
import com.vat.authservice.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void registerUser(RequestDTO request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new AlreadyExists("User with this email already exists");
        }
        User user = new User(request.getEmail(), request.getPassword());
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            user.setRole(request.getRole());
        }
        userRepo.save(user);
    }

    public Optional<User> findByEmail(String username) {
        return userRepo.findByEmail(username);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public String deleteUser(String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            Optional<User> userOpt = userRepo.findById(uuid);

            if (userOpt.isEmpty()) {
                return "NOT_FOUND";
            }

            User user = userOpt.get();
            if ("ADMIN".equals(user.getRole())) {
                return "ADMIN_DELETE_FORBIDDEN";
            }

            userRepo.deleteById(uuid);
            return "DELETED";

        } catch (IllegalArgumentException e) {
            return "INVALID_UUID";
        }
    }
}
