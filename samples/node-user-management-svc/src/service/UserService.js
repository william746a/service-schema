import { body, validationResult } from 'express-validator';
import { UserRepository } from '../repo/UserRepository.js';
import { PasswordSecurityService } from '../security/PasswordSecurityService.js';
import { EventBus } from '../events/EventBus.js';
import { ConflictException, BadRequestException } from '../error/handlers.js';

// Validation chain reflecting spec rules
export const createUserValidators = [
  body('email').isEmail().withMessage('email must be a valid email'),
  body('password').isString().isLength({ min: 8 }).withMessage('password min length 8'),
  body('displayName').isString().isLength({ min: 1, max: 50 }).withMessage('displayName required, max 50'),
];

export async function createUser(userDTO) {
  // Additional runtime guard (controllers already run validators)
  if (!userDTO || typeof userDTO !== 'object') throw new BadRequestException('Invalid body');

  // decision: existsByEmail
  const exists = UserRepository.existsByEmail(userDTO.email);
  if (exists) {
    throw new ConflictException('A user with this email already exists.');
  }

  // domain-service-call: hashPassword
  const hashedPassword = PasswordSecurityService.hashPassword(userDTO.password);

  // mapping: DTO -> UserEntity
  const nowIso = new Date().toISOString();
  const newUserEntity = Object.freeze({
    email: String(userDTO.email),
    passwordHash: hashedPassword,
    displayName: String(userDTO.displayName),
    createdAt: nowIso,
  });

  // persistence-call: save
  const savedUser = UserRepository.save(newUserEntity);

  // mapping: event payload
  const eventPayload = Object.freeze({ userId: savedUser.id, email: savedUser.email });

  // publish-event: UserCreatedEvent
  EventBus.publish('UserCreatedEvent', eventPayload);

  // mapping: entity -> response DTO
  const responseDTO = Object.freeze({
    id: savedUser.id,
    email: savedUser.email,
    displayName: savedUser.displayName,
    createdAt: savedUser.createdAt,
  });

  // return
  return responseDTO;
}

export const UserService = Object.freeze({ createUser, createUserValidators });
