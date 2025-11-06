package com.example.billing.repo;

import com.example.billing.domain.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    boolean existsByCustomerId(UUID customerId);
    Optional<CustomerEntity> findByStripeCustomerId(String stripeCustomerId);
}
