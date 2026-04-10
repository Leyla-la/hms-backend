package com.hms.gateway.config;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignConfig {

    @Bean
    public Decoder feignDecoder() {
        ObjectFactory<HttpMessageConverters> messageConverters = () -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
    }

    @Bean
    public Encoder feignEncoder() {
        ObjectFactory<HttpMessageConverters> messageConverters = () -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
        return new SpringEncoder(messageConverters);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Secret-Key", "SECRET");
        };
    }
}
