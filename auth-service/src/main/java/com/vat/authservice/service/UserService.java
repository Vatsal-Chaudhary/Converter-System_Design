package com.vat.authservice.service;

import com.vat.authservice.dto.RequestDTO;
import com.vat.authservice.execptions.AlreadyExists;
import com.vat.authservice.model.User;
import com.vat.authservice.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void registerUser(RequestDTO request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new AlreadyExists("User with this email already exists");
        }
        User user = new User(request.getUsername(), request.getPassword());
        userRepo.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}
