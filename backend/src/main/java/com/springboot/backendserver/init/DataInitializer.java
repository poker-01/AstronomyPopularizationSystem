package com.springboot.backendserver.init;

import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void ensureAdmin() {
        String adminUsername = "admin";
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword("admin"); // plaintext admin password (change in production)
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }
}
