// Simple in-process pub/sub bus
const subscribers = new Map(); // eventName -> Set<handler>

export function publish(eventName, payload) {
  const set = subscribers.get(eventName);
  if (!set) return;
  for (const handler of set) {
    try {
      handler(payload);
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(`Event handler error for ${eventName}:`, e);
    }
  }
}

export function subscribe(eventName, handler) {
  const current = subscribers.get(eventName) ?? new Set();
  current.add(handler);
  subscribers.set(eventName, current);
  return () => {
    current.delete(handler);
  };
}

export const EventBus = Object.freeze({ publish, subscribe });
