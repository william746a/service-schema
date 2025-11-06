export function notFoundHandler(req, res, next) {
  res.status(404).json({ error: 'Not Found' });
}

export function globalErrorHandler(err, req, res, next) {
  const status = typeof err?.status === 'number' ? err.status : 500;
  const message = typeof err?.message === 'string' ? err.message : 'Internal Server Error';
  res.status(status).json({ error: message });
}

export class NotFoundException extends Error {
  constructor(message = 'Not Found') {
    super(message);
    this.name = 'NotFoundException';
    this.status = 404;
  }
}

export class BadRequestException extends Error {
  constructor(message = 'Bad Request') {
    super(message);
    this.name = 'BadRequestException';
    this.status = 400;
  }
}
