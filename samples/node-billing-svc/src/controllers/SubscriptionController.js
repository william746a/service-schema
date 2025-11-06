import { Router } from 'express';
import { SubscriptionService } from '../service/SubscriptionService.js';

const router = Router();

// GET /subscriptions/:userId
router.get('/:userId', async (req, res, next) => {
  try {
    const { userId } = req.params;
    const dto = await SubscriptionService.getSubscriptionByUserId(userId);
    return res.status(200).json(dto);
  } catch (err) {
    return next(err);
  }
});

export default router;
