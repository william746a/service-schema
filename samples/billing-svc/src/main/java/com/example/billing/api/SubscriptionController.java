package com.example.billing.api;

import com.example.billing.dto.SubscriptionResponseDTO;
import com.example.billing.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(final SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/subscriptions/{userId}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscription(@PathVariable("userId") String userId) {
        SubscriptionResponseDTO dto = subscriptionService.getSubscriptionByUserId(userId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/webhooks/stripe")
    public Map<String, Object> handleStripe(@RequestBody Map<String, Object> body) {
        return subscriptionService.handlePaymentWebhook(body);
    }
}
