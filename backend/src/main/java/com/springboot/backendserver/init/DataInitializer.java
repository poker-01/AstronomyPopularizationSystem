package com.springboot.backendserver.init;

import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.entity.UserStatus;
import com.springboot.backendserver.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void ensureAdmin() {
        String adminUsername = "admin";
        userRepository.findByUsername(adminUsername).ifPresentOrElse(this::patchAdminDefaults, () -> {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword("admin");
            admin.setRole("ADMIN");
            admin.setNickname("系统管理员");
            admin.setStatus(UserStatus.ACTIVE);
            admin.setDeleted(false);
            userRepository.save(admin);
        });
    }

    private void patchAdminDefaults(User admin) {
        boolean changed = false;
        if (admin.getStatus() == null) {
            admin.setStatus(UserStatus.ACTIVE);
            changed = true;
        }
        if (admin.getDeleted() == null) {
            admin.setDeleted(false);
            changed = true;
        }
        if (admin.getNickname() == null) {
            admin.setNickname("系统管理员");
            changed = true;
        }
        if (changed) {
            userRepository.save(admin);
        }
    }
}
