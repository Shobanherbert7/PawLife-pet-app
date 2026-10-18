import { WebSocketServer, WebSocket } from 'ws';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

export interface CollarTelemetryFrame {
  type: 'COLLAR_TELEMETRY';
  petId: number;
  bpm: number;
  bodyTemp: number;
  steps: number;
  battery: number;
  latitude: number;
  longitude: number;
  isSafe: boolean;
  timestamp: string;
}

export function startTelemetrySimulator(wss: WebSocketServer) {
  let stepCounter = 2840;
  let batteryLevel = 94;

  console.log('[TelemetrySimulator] Collar IoT beacon service initialized.');

  setInterval(async () => {
    // Only broadcast if there are connected clients
    if (wss.clients.size === 0) return;

    // Simulate realistic canine vital parameters
    // Normal dog heart rate: 70 - 120 bpm
    // Normal dog body temperature: 38.3 - 39.2 °C
    const baseBpm = 82;
    const pulseVariation = Math.floor(Math.sin(Date.now() / 5000) * 12) + (Math.random() > 0.7 ? 6 : -4);
    const bpm = Math.max(68, Math.min(115, baseBpm + pulseVariation));
    
    const bodyTemp = parseFloat((38.5 + (Math.random() * 0.4 - 0.2)).toFixed(1));
    stepCounter += Math.floor(Math.random() * 4) + 1;

    // Small jitter around home base coordinates (Chennai default)
    const latOffset = (Math.random() - 0.5) * 0.0004;
    const lngOffset = (Math.random() - 0.5) * 0.0004;
    const latitude = parseFloat((13.0827 + latOffset).toFixed(6));
    const longitude = parseFloat((80.2707 + lngOffset).toFixed(6));

    const frame: CollarTelemetryFrame = {
      type: 'COLLAR_TELEMETRY',
      petId: 1, // Default to primary pet (Max)
      bpm,
      bodyTemp,
      steps: stepCounter,
      battery: batteryLevel,
      latitude,
      longitude,
      isSafe: true,
      timestamp: new Date().toISOString(),
    };

    const payload = JSON.stringify(frame);

    wss.clients.forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(payload);
      }
    });

    // Periodically persist snapshots to Prisma DB (every 10 seconds)
    if (Date.now() % 10000 < 3500) {
      try {
        await prisma.telemetryLog.create({
          data: {
            petId: 1,
            bpm,
            bodyTemp,
            steps: stepCounter,
            battery: batteryLevel,
            latitude,
            longitude,
            isSafe: true,
          },
        });
      } catch (err) {
        // Suppress background persistence errors if pet not yet seeded
      }
    }
  }, 3000);
}
