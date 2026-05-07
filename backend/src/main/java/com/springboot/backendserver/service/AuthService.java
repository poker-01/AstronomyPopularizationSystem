package com.springboot.backendserver.service;

import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) throws IllegalArgumentException {
        if (username == null || password == null) {
            throw new IllegalArgumentException("username and password required");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(password); // plaintext as requested
        u.setRole("USER");
        return userRepository.save(u);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> ou = userRepository.findByUsername(username);
        if (ou.isEmpty()) return Optional.empty();
        User u = ou.get();
        if (!u.getPassword().equals(password)) return Optional.empty();
        String token = UUID.randomUUID().toString();
        u.setToken(token);
        userRepository.save(u);
        return Optional.of(u);
    }

    public Optional<User> findByToken(String token) {
        if (token == null) return Optional.empty();
        return userRepository.findByToken(token);
    }
}
