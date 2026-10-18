import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';
import { broadcastEvent } from '../server';

const router = Router();
const prisma = new PrismaClient();

// GET walk history for a pet
router.get('/:petId', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.petId) || 1;
    const walks = await prisma.walkSession.findMany({
      where: { petId },
      orderBy: { createdAt: 'desc' },
      take: 20,
    });
    res.json(walks);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch walk sessions', details: error });
  }
});

// LOG / SAVE a walk session (synced from Android phone or client)
router.post('/', async (req: Request, res: Response) => {
  try {
    const { petId, startTime, endTime, durationSeconds, distanceMeters, caloriesBurned, routePointsJson } = req.body;

    const walk = await prisma.walkSession.create({
      data: {
        petId: parseInt(petId) || 1,
        startTime: startTime ? new Date(startTime) : new Date(),
        endTime: endTime ? new Date(endTime) : new Date(),
        durationSeconds: parseInt(durationSeconds) || 0,
        distanceMeters: parseFloat(distanceMeters) || 0.0,
        caloriesBurned: parseInt(caloriesBurned) || 0,
        routePointsJson: routePointsJson ? (typeof routePointsJson === 'string' ? routePointsJson : JSON.stringify(routePointsJson)) : null,
      },
    });

    console.log(`[Walk Sync] Saved walk for Pet #${walk.petId}: ${walk.distanceMeters}m in ${walk.durationSeconds}s`);

    // Broadcast real-time event to all connected Web Dashboard clients
    broadcastEvent({
      type: 'NEW_WALK',
      data: {
        id: `walk_${walk.id}`,
        petId: walk.petId,
        date: new Date(walk.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', month: 'short', day: 'numeric' }),
        distanceMeters: Math.round(walk.distanceMeters),
        durationSeconds: walk.durationSeconds,
        calories: walk.caloriesBurned || Math.round(walk.distanceMeters * 0.05),
        source: 'ANDROID_ROOM_SYNC',
      },
    });

    res.status(201).json(walk);
  } catch (error) {
    console.error('[Walk Sync Error]', error);
    res.status(500).json({ error: 'Failed to record walk', details: error });
  }
});

export default router;
