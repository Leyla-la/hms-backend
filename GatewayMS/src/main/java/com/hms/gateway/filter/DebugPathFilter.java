package com.hms.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class DebugPathFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(DebugPathFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("Incoming request path: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }
}
