import { Router } from 'express';
import { SubscriptionService } from '../service/SubscriptionService.js';

const router = Router();

// POST /webhooks/stripe
router.post('/stripe', async (req, res, next) => {
  try {
    const result = await SubscriptionService.handlePaymentWebhook(req.body);
    return res.status(200).json(result);
  } catch (err) {
    return next(err);
  }
});

export default router;
