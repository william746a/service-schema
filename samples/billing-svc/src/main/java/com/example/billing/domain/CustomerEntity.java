package com.example.billing.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @Column(name = "customer_id", columnDefinition = "uuid")
    private UUID customerId;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "stripe_customer_id", length = 255, unique = true)
    private String stripeCustomerId;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SubscriptionEntity subscription;

    protected CustomerEntity() {}

    private CustomerEntity(UUID customerId, String email, String displayName, String stripeCustomerId) {
        this.customerId = customerId;
        this.email = email;
        this.displayName = displayName;
        this.stripeCustomerId = stripeCustomerId;
    }

    public static CustomerEntity of(final UUID customerId, final String email, final String displayName, final String stripeCustomerId) {
        return new CustomerEntity(customerId, email, displayName, stripeCustomerId);
    }

    public UUID getCustomerId() { return customerId; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public SubscriptionEntity getSubscription() { return subscription; }

    public void setSubscription(final SubscriptionEntity subscription) { this.subscription = subscription; }
}
