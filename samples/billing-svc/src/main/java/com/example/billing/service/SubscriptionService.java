package com.example.billing.service;

import com.example.billing.domain.SubscriptionEntity;
import com.example.billing.dto.SubscriptionResponseDTO;
import com.example.billing.repo.CustomerRepository;
import com.example.billing.repo.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;

    public SubscriptionService(final SubscriptionRepository subscriptionRepository,
                               final CustomerRepository customerRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO getSubscriptionByUserId(final String userId) {
        UUID uid = UUID.fromString(userId);
        SubscriptionEntity subscription = subscriptionRepository.findByCustomer_CustomerId(uid)
                .orElseThrow(() -> new NotFoundException("Subscription not found for user."));

        return new SubscriptionResponseDTO(
                subscription.getCustomer().getCustomerId(),
                subscription.getStatus(),
                subscription.getPlanId(),
                subscription.getExpiresAt()
        );
    }

    @Transactional
    public Map<String, Object> handlePaymentWebhook(final Map<String, Object> webhookBody) {
        // minimal no-op per spec; demonstrate branch for invoice.payment_succeeded
        Object type = webhookBody.get("type");
        if ("invoice.payment_succeeded".equals(type)) {
            Map<String, Object> data = (Map<String, Object>) webhookBody.get("data");
            if (data != null) {
                Object customer = data.get("customer");
                if (customer != null) {
                    customerRepository.findByStripeCustomerId(customer.toString());
                }
            }
        }
        return Map.of("status", "ok");
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}
