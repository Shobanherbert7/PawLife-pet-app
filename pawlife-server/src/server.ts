import express from 'express';
import cors from 'cors';
import http from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import dotenv from 'dotenv';

import healthRoutes from './routes/health.routes';
import petsRoutes from './routes/pets.routes';
import telemetryRoutes from './routes/telemetry.routes';
import feedingRoutes from './routes/feeding.routes';
import familyRoutes from './routes/family.routes';
import acousticsRoutes from './routes/acoustics.routes';
import communityRoutes from './routes/community.routes';
import walksRoutes from './routes/walks.routes';
import { startTelemetrySimulator } from './services/telemetrySimulator';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 4000;

// Middleware
app.use(cors());
app.use(express.json());

// API Routes
app.use('/api/health', healthRoutes);
app.use('/api/pets', petsRoutes);
app.use('/api/telemetry', telemetryRoutes);
app.use('/api/feeding', feedingRoutes);
app.use('/api/family', familyRoutes);
app.use('/api/acoustics', acousticsRoutes);
app.use('/api/community', communityRoutes);
app.use('/api/walks', walksRoutes);

// Root Index route
app.get('/', (req, res) => {
  res.json({
    message: 'PawLife Dedicated Backend Engine & Database API is running',
    version: '1.0.0',
    documentation: {
      health: '/api/health',
      pets: '/api/pets',
      telemetry: '/api/telemetry/latest/1',
      feeding: '/api/feeding/1',
      family: '/api/family/1',
      acoustics: '/api/acoustics/1',
      community: '/api/community/posts',
      ws: `ws://localhost:${PORT}`,
    },
  });
});

// Create HTTP Server
const server = http.createServer(app);

// Create WebSocket Server
const wss = new WebSocketServer({ server });

wss.on('connection', (ws: WebSocket) => {
  console.log('[WebSocket] Client connected for live telemetry streaming.');

  ws.send(
    JSON.stringify({
      type: 'CONNECTION_ESTABLISHED',
      message: 'Connected to PawLife Real-time Telemetry Engine',
      timestamp: new Date().toISOString(),
    })
  );

  ws.on('message', (message: string) => {
    try {
      const data = JSON.parse(message.toString());
      console.log('[WebSocket] Received message:', data);
    } catch (e) {
      console.error('[WebSocket] Failed to parse client message');
    }
  });

  ws.on('close', () => {
    console.log('[WebSocket] Client disconnected.');
  });
});

// Helper to broadcast custom real-time events to all connected clients (Web Dashboard, Mobile)
export function broadcastEvent(event: any) {
  try {
    const payload = JSON.stringify(event);
    wss.clients.forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(payload);
      }
    });
  } catch (err) {
    console.error('[WebSocket Broadcast Error]', err);
  }
}

// Start background smart collar beacon telemetry simulator
startTelemetrySimulator(wss);

// Start Server Listener
server.listen(PORT, () => {
  console.log(`=======================================================`);
  console.log(`🚀 PawLife Backend Server is running on http://localhost:${PORT}`);
  console.log(`📡 WebSocket Telemetry Server listening on ws://localhost:${PORT}`);
  console.log(`🩺 Health check available at http://localhost:${PORT}/api/health`);
  console.log(`=======================================================`);
});

export default app;
