package com.springboot.backendserver.context;

import com.springboot.backendserver.entity.User;

public final class AuthContext {

    private static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(User user) {
        CURRENT.set(user);
    }

    public static User get() {
        return CURRENT.get();
    }

    public static User require() {
        User user = CURRENT.get();
        if (user == null) {
            throw new com.springboot.backendserver.common.BusinessException(401, "未登录或登录已失效");
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
