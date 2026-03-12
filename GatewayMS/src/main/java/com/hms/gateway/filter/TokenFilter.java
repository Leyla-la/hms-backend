package com.hms.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class TokenFilter extends AbstractGatewayFilterFactory<TokenFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            if (path.equals("/users/login") || path.equals("/users/register")
                    || path.equals("/user/login") || path.equals("/user/register")) {
                return chain.filter(exchange.mutate().request(r -> r.header("X-Secret-Key", "SECRET")).build()); // Skip token validation for login and register endpoints
            }
            HttpHeaders headers = exchange.getRequest().getHeaders();
            if (headers.getFirst(HttpHeaders.AUTHORIZATION) == null) {
                throw new RuntimeException("Missing Authorization header");
            }

            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid Authorization header");
            }
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(jwtSecret.getBytes()) // Dùng biến jwtSecret, không dùng "jwtSecret"
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                exchange = exchange.mutate().request(r -> r.header("X-Secret-Key", "SECRET")).build(); // Skip token validation for login and register endpoints

            } catch (Exception e) {
                throw new RuntimeException("Invalid token");
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}
