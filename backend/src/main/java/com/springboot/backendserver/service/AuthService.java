package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.entity.UserStatus;
import com.springboot.backendserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) throws IllegalArgumentException {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("username and password required");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("password must be at least 4 characters");
        }
        if (userRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("username already exists");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole("USER");
        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);
        user.setNickname(username.trim());
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (Boolean.TRUE.equals(user.getDeleted())) {
            return Optional.empty();
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw BusinessException.badRequest("账户已被禁用，请联系管理员");
        }
        if (!user.getPassword().equals(password)) {
            return Optional.empty();
        }

        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);
        return Optional.of(user);
    }

    public Optional<User> findByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        return userRepository.findByToken(token.trim())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()));
    }
}
