package com.example.billing.service.webhook;

import java.util.Map;

public interface WebhookStrategy {
    boolean supports(String eventType);
    void handle(Map<String, Object> data);
}
