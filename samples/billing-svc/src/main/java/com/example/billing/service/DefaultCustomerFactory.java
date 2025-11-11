package com.example.billing.service;

import com.example.billing.domain.CustomerEntity;
import com.example.billing.dto.UserCreatedEventDTO;
import org.springframework.stereotype.Component;

@Component
public final class DefaultCustomerFactory implements CustomerFactory {

    @Override
    public CustomerEntity createCustomerFromEvent(final UserCreatedEventDTO eventDTO, final String stripeCustomerId) {
        return CustomerEntity.of(
                CustomerFactory.parseUserId(eventDTO),
                eventDTO.email(),
                eventDTO.displayName(),
                stripeCustomerId
        );
    }
}
