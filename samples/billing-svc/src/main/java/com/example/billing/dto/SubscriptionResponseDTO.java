package com.example.billing.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResponseDTO(
        UUID subId,
        String status,
        String planId,
        OffsetDateTime expiresAt
) {}
