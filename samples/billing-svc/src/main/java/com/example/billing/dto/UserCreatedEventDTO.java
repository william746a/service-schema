package com.example.billing.dto;

public record UserCreatedEventDTO(
        String userId,
        String email,
        String displayName
) {}
