package com.springboot.backendserver.config;

import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.entity.UserStatus;
import com.springboot.backendserver.service.AuthService;
import com.springboot.backendserver.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = TokenUtils.extractBearer(request.getHeader("Authorization"));
        if (token == null) {
            token = request.getHeader("X-Auth-Token");
        }

        Optional<User> userOpt = authService.findByToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getDeleted()) || user.getStatus() == UserStatus.DISABLED) {
                writeError(response, 401, "账户已禁用或不存在");
                return false;
            }
            AuthContext.set(user);
        }

        String path = request.getRequestURI();
        boolean requiresAuth = path.startsWith("/api/admin")
                || (path.startsWith("/api/users") && !isPublicUserProfile(request));
        if (requiresAuth && userOpt.isEmpty()) {
            writeError(response, 401, "未登录或登录已失效");
            return false;
        }

        if (path.startsWith("/api/admin")) {
            User user = AuthContext.get();
            if (user == null || !"ADMIN".equals(user.getRole())) {
                writeError(response, 403, "需要管理员权限");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isPublicUserProfile(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.matches("/api/users/\\d+");
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code == 403 ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + escaped + "\",\"data\":null}");
    }
}
