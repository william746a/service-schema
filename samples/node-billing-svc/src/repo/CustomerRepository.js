// In-memory repository for CustomerEntity
const customers = new Map(); // customerId -> entity

function existsById(customerId) {
  return customers.has(String(customerId));
}

function findByStripeId(stripeCustomerId) {
  for (const c of customers.values()) {
    if (c.stripeCustomerId === stripeCustomerId) return c;
  }
  return null;
}

function save(entity) {
  if (!entity || !entity.customerId) throw new Error('CustomerEntity must have customerId');
  const toSave = Object.freeze({ ...entity });
  customers.set(String(entity.customerId), toSave);
  return toSave;
}

export const CustomerRepository = Object.freeze({ existsById, findByStripeId, save });
