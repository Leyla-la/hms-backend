package com.hms.notification.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignSecurityInterceptor implements RequestInterceptor {

    @Value("${hms.internal.secret-key:SECRET}")
    private String secretKey;

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Secret-Key", secretKey);
    }
}
