package com.hms.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class TokenFilter extends AbstractGatewayFilterFactory<TokenFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "";
            log.debug("[Gateway TokenFilter] Incoming request: {} {}", method, path);

            boolean hasInternalSecret = "SECRET".equals(exchange.getRequest().getHeaders().getFirst("X-Secret-Key"));
            if (hasInternalSecret) {
                log.debug("[Gateway TokenFilter] Internal service call detected. Permitting.");
                return chain.filter(exchange);
            }

            boolean isWebSocketPath = path.startsWith("/notification-ws") || path.startsWith("/ws/notifications");
            boolean isPublicPath = path.contains("/login") || path.contains("/register") || path.contains("/admin/dashboard") || path.contains("/profile/patient/count")
                    || path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/swagger-resources");

            if (isWebSocketPath || isPublicPath) {
                log.debug("[Gateway TokenFilter] Public or WS path detected: {}", path);
                return chain.filter(exchange.mutate().request(r -> r.header("X-Secret-Key", "SECRET")).build());
            }

            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null) {
                log.warn("[Gateway TokenFilter] Missing Authorization header for {} {}", method, path);
                throw new RuntimeException("Missing Authorization header");
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("[Gateway TokenFilter] Invalid Authorization header format for {} {}. Value startsWithBearer=false", method, path);
                throw new RuntimeException("Invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Useful debugging info
                log.debug("[Gateway TokenFilter] JWT validated. sub={}, role={}, userId={}, profileId={}, name={}",
                        claims.getSubject(),
                        claims.get("role"),
                        claims.get("userId"),
                        claims.get("profileId"),
                        claims.get("name"));

                // Inject headers for downstream services
                return chain.filter(exchange.mutate().request(r -> {
                    r.header("X-Secret-Key", "SECRET");
                    
                    Object userId = claims.get("userId");
                    if (userId != null) r.header("X-User-Id", String.valueOf(userId));
                    
                    Object role = claims.get("role");
                    if (role != null) r.header("X-User-Role", String.valueOf(role));
                    
                    Object profileId = claims.get("profileId");
                    if (profileId != null) r.header("X-Profile-Id", String.valueOf(profileId));

                    Object name = claims.get("name");
                    if (name != null) r.header("X-Actor-Name", String.valueOf(name));

                    // Backward compatibility / Actor info
                    if (userId != null) r.header("X-Actor-Id", String.valueOf(userId));
                    if (role != null) r.header("X-Actor-Role", String.valueOf(role));
                    
                }).build());

            } catch (Exception e) {
                log.warn("[Gateway TokenFilter] JWT validation failed for {} {}: {}", method, path, e.toString());
                throw new RuntimeException("Invalid token: " + e.getMessage());
            }
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}
