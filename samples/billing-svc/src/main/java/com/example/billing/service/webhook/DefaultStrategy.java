package com.example.billing.service.webhook;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class DefaultStrategy implements WebhookStrategy {

    @Override
    public boolean supports(final String eventType) {
        return true; // fallback
    }

    @Override
    public void handle(final Map<String, Object> data) {
        // No-op
    }
}
