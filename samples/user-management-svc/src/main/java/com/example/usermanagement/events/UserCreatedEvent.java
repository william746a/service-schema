package com.example.usermanagement.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class UserCreatedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;

    public UserCreatedEvent(Object source, UUID userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
