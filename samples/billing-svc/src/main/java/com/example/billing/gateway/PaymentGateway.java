package com.example.billing.gateway;

import java.util.Map;

public interface PaymentGateway {
    Map<String, Object> createCustomer(String email, String displayName);
}
