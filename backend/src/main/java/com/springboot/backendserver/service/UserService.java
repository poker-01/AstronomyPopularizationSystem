package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.ChangePasswordRequest;
import com.springboot.backendserver.dto.UpdateProfileRequest;
import com.springboot.backendserver.dto.UserProfileDto;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileDto getCurrentProfile() {
        return UserProfileDto.from(AuthContext.require());
    }

    @Transactional
    public UserProfileDto updateProfile(UpdateProfileRequest request) {
        User user = AuthContext.require();
        if (request == null) {
            throw BusinessException.badRequest("请求体不能为空");
        }

        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim();
            userRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw BusinessException.badRequest("邮箱已被使用");
                }
            });
            user.setEmail(email);
        } else if (request.getEmail() != null && request.getEmail().isBlank()) {
            user.setEmail(null);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname().trim().isEmpty() ? null : request.getNickname().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim().isEmpty() ? null : request.getAvatar().trim());
        }

        return UserProfileDto.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (request == null || !StringUtils.hasText(request.getOldPassword()) || !StringUtils.hasText(request.getNewPassword())) {
            throw BusinessException.badRequest("请填写原密码和新密码");
        }
        if (request.getNewPassword().length() < 4) {
            throw BusinessException.badRequest("新密码至少 4 位");
        }

        User user = AuthContext.require();
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw BusinessException.badRequest("原密码不正确");
        }
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }
}
