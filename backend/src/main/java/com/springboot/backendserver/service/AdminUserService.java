package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.AdminCreateUserRequest;
import com.springboot.backendserver.dto.AdminUpdateUserRequest;
import com.springboot.backendserver.dto.AdminUserDto;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.entity.UserStatus;
import com.springboot.backendserver.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResult<AdminUserDto> listUsers(int page, int size, String keyword, String role, UserStatus status) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("nickname")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }
            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(root.get("role"), role.trim().toUpperCase()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> result = userRepository.findAll(
                spec,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<AdminUserDto> content = result.getContent().stream().map(AdminUserDto::from).toList();
        return PageResult.of(content, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    public AdminUserDto getUser(Long id) {
        User user = findActiveUser(id);
        return AdminUserDto.from(user);
    }

    @Transactional
    public AdminUserDto createUser(AdminCreateUserRequest request) {
        validateCreateRequest(request);

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw BusinessException.badRequest("用户名已存在");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail().trim())) {
            throw BusinessException.badRequest("邮箱已被使用");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setRole(normalizeRole(request.getRole()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setNickname(trimToNull(request.getNickname()));
        user.setAvatar(trimToNull(request.getAvatar()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
        user.setDeleted(false);

        return AdminUserDto.from(userRepository.save(user));
    }

    @Transactional
    public AdminUserDto updateUser(Long id, AdminUpdateUserRequest request) {
        User user = findActiveUser(id);
        if (request == null) {
            throw BusinessException.badRequest("请求体不能为空");
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(request.getPassword());
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(normalizeRole(request.getRole()));
        }
        if (request.getEmail() != null) {
            String email = trimToNull(request.getEmail());
            if (email != null) {
                userRepository.findByEmail(email).ifPresent(existing -> {
                    if (!existing.getId().equals(user.getId())) {
                        throw BusinessException.badRequest("邮箱已被使用");
                    }
                });
            }
            user.setEmail(email);
        }
        if (request.getNickname() != null) {
            user.setNickname(trimToNull(request.getNickname()));
        }
        if (request.getAvatar() != null) {
            user.setAvatar(trimToNull(request.getAvatar()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        return AdminUserDto.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User current = AuthContext.require();
        if (current.getId().equals(id)) {
            throw BusinessException.badRequest("不能删除当前登录的管理员账户");
        }

        User user = findActiveUser(id);
        user.setDeleted(true);
        user.setToken(null);
        userRepository.save(user);
    }

    public long countActiveUsers() {
        return userRepository.count((root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.equal(root.get("status"), UserStatus.ACTIVE)
        ));
    }

    private User findActiveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    private void validateCreateRequest(AdminCreateUserRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        if (request.getPassword().length() < 4) {
            throw BusinessException.badRequest("密码至少 4 位");
        }
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "USER";
        }
        String normalized = role.trim().toUpperCase();
        if (!"USER".equals(normalized) && !"ADMIN".equals(normalized)) {
            throw BusinessException.badRequest("角色只能是 USER 或 ADMIN");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
