package com.hms.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@org.springframework.scheduling.annotation.EnableAsync
public class AsyncConfig {
    @Bean(name = "adminNotificationExecutor")
    public Executor adminNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AdminBroadcast-");
        
        /**
         * RELIABILITY: CallerRunsPolicy ensures that if the thread pool is overwhelmed, 
         * the Kafka consumer itself will process the task. This prevents data loss 
         * and provides natural backpressure.
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
}
