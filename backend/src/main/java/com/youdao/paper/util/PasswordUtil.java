package com.youdao.paper.util;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        if (!encodedPassword.startsWith("$2")) {
            return rawPassword.equals(encodedPassword);
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
