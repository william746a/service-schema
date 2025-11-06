import { CustomerRepository } from '../repo/CustomerRepository.js';
import { StripePaymentGateway } from '../gateway/StripePaymentGateway.js';
import { EventBus } from '../events/EventBus.js';

function isUuidLike(s) { return typeof s === 'string' && s.length >= 8; }
function isEmailLike(s) { return typeof s === 'string' && s.includes('@'); }

export async function handleUserCreated(eventDTO) {
  // validation
  if (!eventDTO || typeof eventDTO !== 'object') {
    return Object.freeze({ status: 'ignored' });
  }
  const { userId, email, displayName } = eventDTO;
  if (!isUuidLike(userId) || !isEmailLike(email)) {
    return Object.freeze({ status: 'ignored' });
  }

  // decision: already exists?
  if (CustomerRepository.existsById(userId)) {
    return Object.freeze({ status: 'ignored' });
  }

  // domain-service-call: create stripe customer
  const stripeCustomer = StripePaymentGateway.createCustomer(email, displayName ?? '');

  // mapping to CustomerEntity
  const newCustomer = Object.freeze({
    customerId: String(userId),
    email: String(email),
    displayName: displayName ?? '',
    stripeCustomerId: stripeCustomer.id,
  });

  // persistence-call
  const savedCustomer = CustomerRepository.save(newCustomer);

  // publish-event: CustomerCreatedEvent
  EventBus.publish('CustomerCreatedEvent', savedCustomer);

  // return
  return Object.freeze({ status: 'created' });
}

export const BillingService = Object.freeze({ handleUserCreated });
