import express from 'express';
import morgan from 'morgan';
import { globalErrorHandler, notFoundHandler } from './error/handlers.js';
import subscriptionRouter from './controllers/SubscriptionController.js';
import webhookRouter from './controllers/WebhookController.js';
import eventsRouter from './controllers/EventsController.js';

const app = express();

app.use(express.json());
app.use(morgan('dev'));

app.use('/subscriptions', subscriptionRouter);
app.use('/webhooks', webhookRouter);
app.use('/events', eventsRouter);

app.use(notFoundHandler);
app.use(globalErrorHandler);

export default app;
