import express from 'express';
import morgan from 'morgan';
import userRouter from './controllers/UserController.js';
import { globalErrorHandler, notFoundHandler } from './error/handlers.js';

const app = express();

app.use(express.json());
app.use(morgan('dev'));

app.use('/users', userRouter);

app.use(notFoundHandler);
app.use(globalErrorHandler);

export default app;
