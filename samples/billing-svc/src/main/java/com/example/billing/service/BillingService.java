package com.example.billing.service;

import com.example.billing.domain.CustomerEntity;
import com.example.billing.dto.UserCreatedEventDTO;
import com.example.billing.events.CustomerCreatedEvent;
import com.example.billing.gateway.StripePaymentGateway;
import com.example.billing.repo.CustomerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class BillingService {

    private final CustomerRepository customerRepository;
    private final StripePaymentGateway stripePaymentGateway;
    private final ApplicationEventPublisher eventPublisher;

    public BillingService(final CustomerRepository customerRepository,
                          final StripePaymentGateway stripePaymentGateway,
                          final ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.stripePaymentGateway = stripePaymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Map<String, String> handleUserCreated(final UserCreatedEventDTO eventDTO) {
        // validation (basic): userId UUID and email contains @
        try { UUID.fromString(eventDTO.userId()); } catch (Exception e) { throw new IllegalArgumentException("userId must be UUID"); }
        if (eventDTO.email() == null || !eventDTO.email().contains("@")) {
            throw new IllegalArgumentException("email must be valid");
        }

        UUID userId = UUID.fromString(eventDTO.userId());

        // exists check
        if (customerRepository.existsByCustomerId(userId)) {
            return Map.of("status", "ignored");
        }

        // domain-service-call: create customer in Stripe
        Map<String, Object> stripeCustomer = stripePaymentGateway.createCustomer(eventDTO.email(), eventDTO.displayName());

        // mapping to entity
        String stripeId = (String) stripeCustomer.get("id");
        CustomerEntity newCustomer = CustomerEntity.of(userId, eventDTO.email(), eventDTO.displayName(), stripeId);

        // persist
        CustomerEntity saved = customerRepository.save(newCustomer);

        // publish event
        eventPublisher.publishEvent(new CustomerCreatedEvent(this, saved.getCustomerId(), saved.getEmail()));

        return Map.of("status", "created");
    }
}
