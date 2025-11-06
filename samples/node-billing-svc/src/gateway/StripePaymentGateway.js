// Deterministic stub for Stripe customer creation
import crypto from 'crypto';

function createCustomer(email, displayName) {
  const h = crypto.createHash('md5').update(`${email}|${displayName}`,'utf8').digest('hex').slice(0,12);
  const result = Object.freeze({ id: `cus_${h}` });
  return result;
}

export const StripePaymentGateway = Object.freeze({ createCustomer });
