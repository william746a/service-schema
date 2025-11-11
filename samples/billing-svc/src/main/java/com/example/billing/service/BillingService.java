package com.example.billing.service;

import com.example.billing.domain.CustomerEntity;
import com.example.billing.dto.UserCreatedEventDTO;
import com.example.billing.events.CustomerCreatedEvent;
import com.example.billing.gateway.PaymentGateway;
import com.example.billing.repo.CustomerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public final class BillingService {

    private final CustomerRepository customerRepository;
    private final PaymentGateway paymentGateway;
    private final CustomerFactory customerFactory;
    private final ApplicationEventPublisher eventPublisher;

    public BillingService(final CustomerRepository customerRepository,
                          final PaymentGateway paymentGateway,
                          final CustomerFactory customerFactory,
                          final ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.paymentGateway = paymentGateway;
        this.customerFactory = customerFactory;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Map<String, String> handleUserCreated(final UserCreatedEventDTO eventDTO) {
        // validation (basic): userId UUID and email contains @
        try { UUID.fromString(eventDTO.userId()); } catch (Exception e) { throw new IllegalArgumentException("userId must be UUID"); }
        if (eventDTO.email() == null || !eventDTO.email().contains("@")) {
            throw new IllegalArgumentException("email must be valid");
        }

        final UUID userId = UUID.fromString(eventDTO.userId());

        // exists check
        if (customerRepository.existsByCustomerId(userId)) {
            return Map.of("status", "ignored");
        }

        // Adapter pattern: create customer in Stripe via PaymentGateway
        final Map<String, Object> stripeCustomer = paymentGateway.createCustomer(eventDTO.email(), eventDTO.displayName());
        final String stripeId = (String) stripeCustomer.get("id");

        // Factory pattern: create aggregate from event
        final CustomerEntity newCustomer = customerFactory.createCustomerFromEvent(eventDTO, stripeId);

        // persist
        final CustomerEntity saved = customerRepository.save(newCustomer);

        // publish event
        eventPublisher.publishEvent(new CustomerCreatedEvent(this, saved.getCustomerId(), saved.getEmail()));

        return Map.of("status", "created");
    }
}
