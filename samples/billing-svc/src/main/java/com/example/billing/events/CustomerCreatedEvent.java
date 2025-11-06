package com.example.billing.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class CustomerCreatedEvent extends ApplicationEvent {
    private final UUID customerId;
    private final String email;

    public CustomerCreatedEvent(Object source, UUID customerId, String email) {
        super(source);
        this.customerId = customerId;
        this.email = email;
    }

    public UUID getCustomerId() { return customerId; }
    public String getEmail() { return email; }
}
