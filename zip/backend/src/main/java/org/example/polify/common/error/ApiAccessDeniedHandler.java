package org.example.polify.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.example.polify.common.log.SecurityLogger;

public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        SecurityLogger.warn("AUTH_403", "Forbidden request", null, null, "FORBIDDEN");
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiError body = new ApiError(
            Instant.now().toString(),
            status.value(),
            status.getReasonPhrase(),
            ErrorCode.FORBIDDEN,
            "Forbidden",
            request.getRequestURI(),
            MDC.get("requestId"),
            List.of()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
