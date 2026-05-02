package org.example.polify.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class AuditLogger {
    private static final Logger LOG = LoggerFactory.getLogger("AUDIT");

    private AuditLogger() {}

    public static void info(
        String event,
        String message,
        Long userId,
        Long surveyId,
        Long attemptId,
        Long ledgerEntryId,
        Integer amountBani,
        String status
    ) {
        withMdc(event, userId, surveyId, attemptId, ledgerEntryId, amountBani, status, () -> LOG.info(message));
    }

    private static void withMdc(
        String event,
        Long userId,
        Long surveyId,
        Long attemptId,
        Long ledgerEntryId,
        Integer amountBani,
        String status,
        Runnable action
    ) {
        put("event", event);
        put("userId", userId);
        put("surveyId", surveyId);
        put("attemptId", attemptId);
        put("ledgerEntryId", ledgerEntryId);
        put("amountBani", amountBani);
        put("status", status);
        try {
            action.run();
        } finally {
            MDC.remove("event");
            MDC.remove("userId");
            MDC.remove("surveyId");
            MDC.remove("attemptId");
            MDC.remove("ledgerEntryId");
            MDC.remove("amountBani");
            MDC.remove("status");
        }
    }

    private static void put(String key, Object value) {
        MDC.put(key, value == null ? "" : String.valueOf(value));
    }
}

