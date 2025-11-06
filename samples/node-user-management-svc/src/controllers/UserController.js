import { Router } from 'express';
import { validationResult } from 'express-validator';
import { UserService, createUserValidators } from '../service/UserService.js';
import { BadRequestException } from '../error/handlers.js';

const router = Router();

router.post('/', createUserValidators, async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      const msg = errors.array().map(e => `${e.param}: ${e.msg}`).join(', ');
      throw new BadRequestException(msg);
    }
    const dto = Object.freeze({
      email: req.body?.email,
      password: req.body?.password,
      displayName: req.body?.displayName,
    });
    const result = await UserService.createUser(dto);
    return res.status(201).json(result);
  } catch (err) {
    return next(err);
  }
});

export default router;
