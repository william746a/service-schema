import { createServer } from 'http';
import app from './app.js';

const PORT = process.env.PORT ? Number(process.env.PORT) : 3001;

const server = createServer(app);
server.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`Billing service listening on http://localhost:${PORT}`);
});
