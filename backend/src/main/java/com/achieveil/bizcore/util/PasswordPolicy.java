package com.achieveil.bizcore.util;

import java.nio.charset.StandardCharsets;

/**
 * Centralizes password-related constraints that need to stay consistent across the application.
 */
public final class PasswordPolicy {

    private static final int MAX_PASSWORD_BYTES = 256;

    private PasswordPolicy() {
    }

    public static void ensureWithinSupportedLength(String password) {
        if (password == null) {
            throw new RuntimeException("密码不能为空");
        }
        int byteLength = password.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > MAX_PASSWORD_BYTES) {
            throw new RuntimeException(
                    String.format("密码过长，最多支持%d个字节（当前%d字节）", MAX_PASSWORD_BYTES, byteLength)
            );
        }
    }
}
