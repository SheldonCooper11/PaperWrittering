package com.youdao.paper.util;

import java.security.SecureRandom;

public class CodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeUtil() {
    }

    public static String sixDigitCode() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }
}
