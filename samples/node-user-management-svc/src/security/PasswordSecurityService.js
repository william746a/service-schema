import crypto from 'crypto';

function hashPassword(plain) {
  const salt = 'static-salt-for-sample-only';
  const hash = crypto.createHash('sha256').update(String(plain) + salt, 'utf8').digest('hex');
  return hash;
}

export const PasswordSecurityService = Object.freeze({ hashPassword });
