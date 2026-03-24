package com.fruit.warehouse.security;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserContext {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private Long userId;
    private String username;
    private List<String> roles;
    private Set<String> permissions;

    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long getUserId() {
        UserContext ctx = get();
        return ctx != null ? ctx.userId : null;
    }

    public static String getUsername() {
        UserContext ctx = get();
        return ctx != null ? ctx.username : null;
    }

    public static boolean hasRole(String roleCode) {
        UserContext ctx = get();
        return ctx != null && ctx.roles != null && ctx.roles.contains(roleCode);
    }

    public static boolean hasPermission(String permissionCode) {
        UserContext ctx = get();
        if (ctx == null) return false;
        if (ctx.roles != null && ctx.roles.contains("ADMIN")) return true;
        return ctx.permissions != null && ctx.permissions.contains(permissionCode);
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
