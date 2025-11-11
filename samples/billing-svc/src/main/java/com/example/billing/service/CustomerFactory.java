package com.example.billing.service;

import com.example.billing.domain.CustomerEntity;
import com.example.billing.dto.UserCreatedEventDTO;

import java.util.UUID;

public interface CustomerFactory {
    CustomerEntity createCustomerFromEvent(UserCreatedEventDTO eventDTO, String stripeCustomerId);

    static UUID parseUserId(final UserCreatedEventDTO eventDTO) {
        return UUID.fromString(eventDTO.userId());
    }
}
