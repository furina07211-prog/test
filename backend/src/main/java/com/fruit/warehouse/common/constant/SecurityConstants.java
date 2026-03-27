package com.fruit.warehouse.common.constant;

public final class SecurityConstants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String LOGIN_PATH = "/api/auth/login";
    public static final String[] PUBLIC_PATHS = {
        LOGIN_PATH,
        "/doc.html",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/actuator/health"
    };

    private SecurityConstants() {}
}
