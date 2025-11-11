package com.example.billing.service.webhook;

import com.example.billing.repo.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class InvoicePaidStrategy implements WebhookStrategy {

    private final CustomerRepository customerRepository;

    public InvoicePaidStrategy(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public boolean supports(final String eventType) {
        return "invoice.paid".equals(eventType);
    }

    @Override
    public void handle(final Map<String, Object> data) {
        // Minimal no-op: ensure we can look up customer by Stripe ID if present
        if (data == null) return;
        Object obj = data.get("object");
        if (obj instanceof Map<?, ?> object) {
            Object customer = ((Map<?, ?>) object).get("customer");
            if (customer != null) {
                customerRepository.findByStripeCustomerId(customer.toString());
            }
        }
    }
}
