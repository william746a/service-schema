package com.example.billing.service;

import com.example.billing.domain.SubscriptionEntity;
import com.example.billing.dto.SubscriptionResponseDTO;
import com.example.billing.dto.StripeEventDTO;
import com.example.billing.repo.CustomerRepository;
import com.example.billing.repo.SubscriptionRepository;
import com.example.billing.service.webhook.WebhookStrategy;
import com.example.billing.service.webhook.WebhookStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final WebhookStrategyFactory webhookStrategyFactory;

    public SubscriptionService(final SubscriptionRepository subscriptionRepository,
                               final CustomerRepository customerRepository,
                               final WebhookStrategyFactory webhookStrategyFactory) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
        this.webhookStrategyFactory = webhookStrategyFactory;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getSubscriptions(final String customerId) {
        UUID cid = UUID.fromString(customerId);
        List<SubscriptionEntity> subs = subscriptionRepository.findAllByCustomer_CustomerId(cid);
        return subs.stream()
                .map(s -> new SubscriptionResponseDTO(
                        s.getId(),
                        s.getStatus(),
                        s.getPlanId(),
                        s.getExpiresAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> handleStripeWebhook(final StripeEventDTO event) {
        final String type = event.type();
        WebhookStrategy strategy = webhookStrategyFactory.getStrategy(type);
        strategy.handle(event.data());
        return Map.of("status", "ok");
    }
}
