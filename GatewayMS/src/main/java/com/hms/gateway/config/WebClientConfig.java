package com.hms.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Load-balanced builder used by Spring Cloud Gateway routing.
     * This uses Eureka for service discovery (lb://ServiceName URLs).
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Non-load-balanced builder for direct inter-service calls inside GatewayMS
     * (e.g., AdminDashboardAPI aggregation).
     * Using direct ports avoids Eureka dependency — dashboard works even if
     * Eureka hasn't yet propagated registrations or briefly goes down.
     */
    @Bean
    @Qualifier("directWebClientBuilder")
    public WebClient.Builder directWebClientBuilder() {
        return WebClient.builder();
    }

    // ── Dashboard aggregation WebClients (direct URLs, no Eureka needed) ──────

    @Bean
    public WebClient userWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.baseUrl("http://UserMS").build();
    }

    @Bean
    public WebClient profileWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.baseUrl("http://ProfileMS").build();
    }

    @Bean
    public WebClient appointmentWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.baseUrl("http://AppointmentMS").build();
    }

    @Bean
    public WebClient pharmacyWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.baseUrl("http://PharmacyMS").build();
    }
}