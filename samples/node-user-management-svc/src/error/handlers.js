export function notFoundHandler(req, res, next) {
  res.status(404).json({ error: 'Not Found' });
}

export function globalErrorHandler(err, req, res, next) {
  // Domain exceptions carry a status; default to 500
  const status = typeof err?.status === 'number' ? err.status : 500;
  const message = typeof err?.message === 'string' ? err.message : 'Internal Server Error';
  res.status(status).json({ error: message });
}

export class ConflictException extends Error {
  constructor(message = 'Conflict') {
    super(message);
    this.name = 'ConflictException';
    this.status = 409;
  }
}

export class BadRequestException extends Error {
  constructor(message = 'Bad Request') {
    super(message);
    this.name = 'BadRequestException';
    this.status = 400;
  }
}
