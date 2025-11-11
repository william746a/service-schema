package com.example.billing.service.webhook;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class WebhookStrategyFactory {

    private final List<WebhookStrategy> strategies;

    public WebhookStrategyFactory(final List<WebhookStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public WebhookStrategy getStrategy(final String eventType) {
        return strategies.stream()
                .filter(s -> s.supports(eventType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy for eventType: " + eventType));
    }
}
