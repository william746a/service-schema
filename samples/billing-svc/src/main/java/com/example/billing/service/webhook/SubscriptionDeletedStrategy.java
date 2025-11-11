package com.example.billing.service.webhook;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class SubscriptionDeletedStrategy implements WebhookStrategy {

    @Override
    public boolean supports(final String eventType) {
        return "customer.subscription.deleted".equals(eventType);
    }

    @Override
    public void handle(final Map<String, Object> data) {
        // Minimal no-op for deletion event
    }
}
