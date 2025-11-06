import { SubscriptionRepository } from '../repo/SubscriptionRepository.js';
import { CustomerRepository } from '../repo/CustomerRepository.js';
import { NotFoundException } from '../error/handlers.js';

export async function getSubscriptionByUserId(userId) {
  const subscription = SubscriptionRepository.findByCustomerId(String(userId));
  if (!subscription) {
    throw new NotFoundException('Subscription not found for user.');
  }
  const responseDTO = Object.freeze({
    customerId: subscription.customer.customerId,
    status: subscription.status,
    planId: subscription.planId,
    expiresAt: subscription.expiresAt,
  });
  return responseDTO;
}

export async function handlePaymentWebhook(webhookBody) {
  // Minimal validation stub
  if (!webhookBody || typeof webhookBody !== 'object') return Object.freeze({ ok: true });
  if (webhookBody.type === 'invoice.payment_succeeded') {
    const stripeId = webhookBody?.data?.customer;
    const customer = stripeId ? CustomerRepository.findByStripeId(stripeId) : null;
    if (customer) {
      const now = new Date();
      const expires = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);
      const sub = Object.freeze({
        status: 'active',
        planId: webhookBody?.data?.planId ?? 'basic',
        expiresAt: expires.toISOString(),
        customer: { customerId: customer.customerId },
      });
      SubscriptionRepository.save(sub);
    }
  }
  return Object.freeze({ ok: true });
}

export const SubscriptionService = Object.freeze({ getSubscriptionByUserId, handlePaymentWebhook });
