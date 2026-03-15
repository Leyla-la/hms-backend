package com.hms.user.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Adds headers required for downstream authorization on all OpenFeign calls.
 *
 * In this project, downstream services (e.g., ProfileMS) allow requests only if
 * X-Secret-Key == SECRET (see their SecurityConfig). When AppointmentMS calls
 * ProfileMS directly via Feign, the call does NOT pass through GatewayMS,
 * so Gateway's TokenFilter can't inject X-Secret-Key for us.
 */
@Component
public class FeignSecurityInterceptor implements RequestInterceptor {

    @Value("${hms.internal.secret-key:SECRET}")
    private String secretKey;

    @Override
    public void apply(RequestTemplate template) {
        // Always include internal shared-secret so downstream SecurityConfig permits the request.
        template.header("X-Secret-Key", secretKey);

        // Optional: propagate Authorization from the inbound HTTP request (if any).
        // This is useful if you later choose to validate JWT inside each microservice.
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            HttpServletRequest request = servletAttrs.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }
        }
    }
}

