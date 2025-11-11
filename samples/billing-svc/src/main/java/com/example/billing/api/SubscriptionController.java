package com.example.billing.api;

import com.example.billing.dto.SubscriptionResponseDTO;
import com.example.billing.dto.StripeEventDTO;
import com.example.billing.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(final SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/subscriptions/{customerId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptions(@PathVariable("customerId") String customerId) {
        List<SubscriptionResponseDTO> dtos = subscriptionService.getSubscriptions(customerId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/webhooks/stripe")
    public Map<String, Object> handleStripe(@RequestBody final StripeEventDTO event) {
        return subscriptionService.handleStripeWebhook(event);
    }
}
