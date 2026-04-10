package com.hms.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration for NotificationMS.
 *
 * Access model:
 *  - All traffic from Gateway already carries X-Secret-Key: SECRET (injected by TokenFilter).
 *  - WebSocket SockJS handshake paths must be open (the Gateway's TokenFilter skips them).
 *  - Everything else is blocked unless the shared internal secret is present.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String INTERNAL_SECRET = "SECRET";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/notification-ws/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/ws/notifications/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/health")).permitAll()
                .requestMatchers(request -> INTERNAL_SECRET.equals(request.getHeader("X-Secret-Key"))).permitAll()
                .anyRequest().denyAll()
            );
        return http.build();
    }
}
