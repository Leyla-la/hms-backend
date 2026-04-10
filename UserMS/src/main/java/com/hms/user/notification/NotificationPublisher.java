package com.hms.user.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void publish(String topic, String key, Object data, String eventType, String source, java.util.List<Long> recipientIds) {
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("eventId", UUID.randomUUID().toString());
            envelope.put("eventType", eventType);
            envelope.put("source", source);
            envelope.put("timestamp", java.time.Instant.now().toString());
            envelope.put("payload", data);
            envelope.put("recipientIds", recipientIds);

            String jsonPayload = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(topic, key, jsonPayload);
            log.info("✅ [ASYNC] Kafka event '{}' published to topic '{}'.", eventType, topic);
        } catch (Exception e) {
            log.error("❌ [CRITICAL] KAFKA PUBLISH FAILED! Topic: {}. Event: {}. Error: {}. Business logic will NOT be affected.", topic, eventType, e.getMessage());
        }
    }
}
