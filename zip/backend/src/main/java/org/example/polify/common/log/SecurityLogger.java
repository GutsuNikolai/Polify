package org.example.polify.common.log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class SecurityLogger {
    private static final Logger LOG = LoggerFactory.getLogger("SECURITY");

    private SecurityLogger() {}

    public static void info(
        String event,
        String message,
        Long userId,
        String email,
        String status
    ) {
        put("event", event);
        put("userId", userId);
        put("emailHash", hashEmail(email));
        put("status", status);
        try {
            LOG.info(message);
        } finally {
            MDC.remove("event");
            MDC.remove("userId");
            MDC.remove("emailHash");
            MDC.remove("status");
        }
    }

    public static void warn(
        String event,
        String message,
        Long userId,
        String email,
        String status
    ) {
        put("event", event);
        put("userId", userId);
        put("emailHash", hashEmail(email));
        put("status", status);
        try {
            LOG.warn(message);
        } finally {
            MDC.remove("event");
            MDC.remove("userId");
            MDC.remove("emailHash");
            MDC.remove("status");
        }
    }

    private static void put(String key, Object value) {
        MDC.put(key, value == null ? "" : String.valueOf(value));
    }

    private static String hashEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String normalized = email.trim().toLowerCase();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}

