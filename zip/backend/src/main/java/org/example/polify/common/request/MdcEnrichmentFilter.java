package org.example.polify.common.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MdcEnrichmentFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        MDC.put("path", request.getRequestURI());
        MDC.put("ip", clientIp(request));
        MDC.put("userAgent", safe(request.getHeader("User-Agent")));
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("path");
            MDC.remove("ip");
            MDC.remove("userAgent");
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int idx = forwarded.indexOf(',');
            return (idx >= 0 ? forwarded.substring(0, idx) : forwarded).trim();
        }
        return safe(request.getRemoteAddr());
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        // Keep logs one-line.
        return value.replace('\n', ' ').replace('\r', ' ');
    }
}

