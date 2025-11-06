import { v4 as uuidv4 } from 'uuid';

// In-memory store
const users = new Map(); // id -> entity

function existsByEmail(email) {
  for (const u of users.values()) {
    if (u.email.toLowerCase() === String(email).toLowerCase()) return true;
  }
  return false;
}

function save(entity) {
  const id = entity.id ?? uuidv4();
  const toSave = Object.freeze({ ...entity, id });
  users.set(id, toSave);
  return toSave;
}

export const UserRepository = Object.freeze({ existsByEmail, save });
