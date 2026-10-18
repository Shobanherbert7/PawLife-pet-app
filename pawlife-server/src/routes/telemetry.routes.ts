import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

// SAFE ZONE CONFIGURATION (Geofencing centered at Chennai / Home coordinates)
const HOME_GEOFENCE = {
  centerLat: 13.0827,
  centerLng: 80.2707,
  radiusKm: 0.5, // 500 meter safe perimeter
};

// Haversine formula for distance calculation in kilometers
function getDistanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Earth's radius in km
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) *
      Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// GET latest collar telemetry for a pet
router.get('/latest/:petId', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.petId);
    const latest = await prisma.telemetryLog.findFirst({
      where: { petId },
      orderBy: { timestamp: 'desc' },
    });

    if (!latest) {
      // Default fallback if no telemetry yet
      return res.json({
        petId,
        bpm: 82,
        bodyTemp: 38.5,
        steps: 2450,
        battery: 92,
        latitude: HOME_GEOFENCE.centerLat,
        longitude: HOME_GEOFENCE.centerLng,
        isSafe: true,
        timestamp: new Date().toISOString(),
        distanceFromHomeMeters: 0,
      });
    }

    const distKm = getDistanceKm(
      HOME_GEOFENCE.centerLat,
      HOME_GEOFENCE.centerLng,
      latest.latitude,
      latest.longitude
    );

    res.json({
      ...latest,
      distanceFromHomeMeters: Math.round(distKm * 1000),
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch telemetry', details: error });
  }
});

// GET telemetry history (last 50 pings)
router.get('/history/:petId', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.petId);
    const history = await prisma.telemetryLog.findMany({
      where: { petId },
      orderBy: { timestamp: 'desc' },
      take: 50,
    });
    res.json(history);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch history', details: error });
  }
});

// POST collar reading
router.post('/', async (req: Request, res: Response) => {
  try {
    const { petId, bpm, bodyTemp, steps, battery, latitude, longitude } = req.body;

    const lat = parseFloat(latitude) || HOME_GEOFENCE.centerLat;
    const lng = parseFloat(longitude) || HOME_GEOFENCE.centerLng;
    const distKm = getDistanceKm(HOME_GEOFENCE.centerLat, HOME_GEOFENCE.centerLng, lat, lng);
    const isSafe = distKm <= HOME_GEOFENCE.radiusKm;

    const log = await prisma.telemetryLog.create({
      data: {
        petId: parseInt(petId),
        bpm: parseInt(bpm) || 80,
        bodyTemp: parseFloat(bodyTemp) || 38.5,
        steps: parseInt(steps) || 0,
        battery: parseInt(battery) || 100,
        latitude: lat,
        longitude: lng,
        isSafe,
      },
    });

    res.status(201).json({
      ...log,
      distanceFromHomeMeters: Math.round(distKm * 1000),
      alert: isSafe ? null : 'SECURITY_ALERT: Pet has exited the safe perimeter!',
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to record telemetry', details: error });
  }
});

export default router;
