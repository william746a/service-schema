package com.example.billing.dto;

import java.util.Map;

public record StripeEventDTO(
        String id,
        String type,
        Map<String, Object> data
) {}
