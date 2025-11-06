import { Router } from 'express';
import { BillingService } from '../service/BillingService.js';

const router = Router();

// Internal endpoint to simulate event consumption
router.post('/user-created', async (req, res, next) => {
  try {
    const result = await BillingService.handleUserCreated(req.body);
    return res.status(200).json(result);
  } catch (err) {
    return next(err);
  }
});

export default router;
