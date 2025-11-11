package com.example.billing.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    @Column(name = "sub_id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "plan_id", length = 100)
    private String planId;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    protected SubscriptionEntity() {}

    private SubscriptionEntity(UUID id, String status, String planId, OffsetDateTime expiresAt, CustomerEntity customer) {
        this.id = id;
        this.status = status;
        this.planId = planId;
        this.expiresAt = expiresAt;
        this.customer = customer;
    }

    public static SubscriptionEntity of(final UUID id, final String status, final String planId, final OffsetDateTime expiresAt, final CustomerEntity customer) {
        return new SubscriptionEntity(id, status, planId, expiresAt, customer);
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public String getPlanId() { return planId; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public CustomerEntity getCustomer() { return customer; }
}
