// In-memory repository for SubscriptionEntity
const subs = new Map(); // subId -> entity

function findByCustomerId(customerId) {
  for (const s of subs.values()) {
    if (s.customer && s.customer.customerId === String(customerId)) return s;
  }
  return null;
}

function save(entity) {
  const id = entity.id ?? `sub_${Math.random().toString(36).slice(2, 10)}`;
  const toSave = Object.freeze({ ...entity, id });
  subs.set(id, toSave);
  return toSave;
}

export const SubscriptionRepository = Object.freeze({ findByCustomerId, save });
