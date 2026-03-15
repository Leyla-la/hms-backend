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

            if (path.equals("/users/login") || path.equals("/users/register")
                    || path.equals("/user/login") || path.equals("/user/register")) {
                log.debug("[Gateway TokenFilter] Public auth endpoint detected, injecting X-Secret-Key and skipping JWT validation.");
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

                // Useful debugging info (don’t log the raw token)
                log.debug("[Gateway TokenFilter] JWT validated. sub={}, role={}, userId={}, profileId={}, iat={}, exp={}",
                        claims.getSubject(),
                        claims.get("role"),
                        claims.get("userId"),
                        claims.get("profileId"),
                        claims.getIssuedAt(),
                        claims.getExpiration());

            } catch (Exception e) {
                log.warn("[Gateway TokenFilter] JWT validation failed for {} {}: {}", method, path, e.toString());
                throw new RuntimeException("Invalid token: " + e.getMessage());
            }

            log.debug("[Gateway TokenFilter] Injecting X-Secret-Key for downstream authorization.");
            return chain.filter(exchange.mutate().request(r -> r.header("X-Secret-Key", "SECRET")).build());
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}
