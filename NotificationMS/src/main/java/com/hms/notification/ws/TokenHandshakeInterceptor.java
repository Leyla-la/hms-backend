package com.hms.notification.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Runs DURING the HTTP handshake, while the underlying HttpServletRequest is
 * still alive. Extracts the Bearer token (from the Authorization header or the
 * "token" query parameter) and the explicit "userId" query parameter, then
 * stores them in the WebSocket session attributes so that the WebSocket layer
 * can read them safely — long after Tomcat has recycled the original request object.
 */
@Component
public class TokenHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TokenHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String token     = null;
        String userId    = null;
        String profileId = null;
        String role      = null;

        // --- 1. Query-string parameters (highest priority, works everywhere) ---
        String query = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            query = servletRequest.getServletRequest().getQueryString();
        } else {
            // Fallback for non-servlet environments
            if (request.getURI() != null) {
                query = request.getURI().getQuery();
            }
        }

        if (query != null) {
            for (String part : query.split("&")) {
                if (part.startsWith("token=") && token == null) {
                    token = part.substring("token=".length());
                }
                if (part.startsWith("userId=") && userId == null) {
                    userId = part.substring("userId=".length());
                }
                if (part.startsWith("profileId=") && profileId == null) {
                    profileId = part.substring("profileId=".length());
                }
                if (part.startsWith("role=") && role == null) {
                    role = part.substring("role=".length());
                }
            }
        }

        // --- 2. Authorization header (safe here — request is still alive) ---
        if (token == null) {
            String auth = request.getHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }

        // Store in session attributes for later use in afterConnectionEstablished
        if (token     != null) attributes.put("token",     token);
        if (userId    != null) attributes.put("userId",    userId);
        if (profileId != null) attributes.put("profileId", profileId);
        if (role      != null) attributes.put("role",      role);

        log.debug("[WS Handshake] token={}, userId={}, profileId={}, role={}", 
            token != null ? "present" : "absent", 
            userId != null ? userId : "N/A",
            profileId != null ? profileId : "N/A",
            role != null ? role : "N/A");
        return true; // always allow; auth enforcement happens in the handler
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                                ServerHttpResponse response,
                                WebSocketHandler wsHandler,
                                Exception exception) {
        // Nothing to do
    }
}
