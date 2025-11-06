import { createServer } from 'http';
import app from './app.js';

const PORT = process.env.PORT ? Number(process.env.PORT) : 3000;

const server = createServer(app);
server.listen(PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`User Management service listening on http://localhost:${PORT}`);
});
