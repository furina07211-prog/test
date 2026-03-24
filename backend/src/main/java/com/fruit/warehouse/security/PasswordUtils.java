package com.fruit.warehouse.security;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordUtils {

    private PasswordUtils() {}

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
