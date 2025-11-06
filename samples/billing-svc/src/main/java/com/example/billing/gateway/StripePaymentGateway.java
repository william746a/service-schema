package com.example.billing.gateway;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class StripePaymentGateway {

    public Map<String, Object> createCustomer(final String email, final String displayName) {
        // In a real implementation, call Stripe SDK. Here we mock an ID.
        return Map.of(
                "id", "cus_" + UUID.randomUUID(),
                "email", email,
                "name", displayName
        );
    }
}
