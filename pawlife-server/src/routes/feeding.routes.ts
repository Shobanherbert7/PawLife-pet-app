import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';
import { broadcastEvent } from '../server';

const router = Router();
const prisma = new PrismaClient();

// GET all feeding logs for a pet
router.get('/:petId', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.petId);
    const logs = await prisma.feedingLog.findMany({
      where: { petId },
      orderBy: { fedAt: 'desc' },
      take: 20,
    });
    res.json(logs);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch feeding logs', details: error });
  }
});

// LOG a feeding event
router.post('/', async (req: Request, res: Response) => {
  try {
    const { petId, foodName, portionGrams, calories } = req.body;

    if (!petId || !foodName) {
      return res.status(400).json({ error: 'petId and foodName are required' });
    }

    const log = await prisma.feedingLog.create({
      data: {
        petId: parseInt(petId),
        foodName,
        portionGrams: parseFloat(portionGrams) || 150.0,
        calories: calories ? parseInt(calories) : null,
      },
    });

    // Broadcast real-time event to all connected Web Dashboard clients
    broadcastEvent({
      type: 'NEW_FEEDING',
      data: {
        id: `feed_${log.id}`,
        petId: log.petId,
        name: log.foodName,
        portion: Math.round(log.portionGrams),
        calories: log.calories || Math.round(log.portionGrams * 3.5),
        time: 'Just now',
        fedAt: new Date(log.fedAt).getTime(),
        source: 'ANDROID_ROOM_SYNC',
      },
    });

    res.status(201).json(log);
  } catch (error) {
    res.status(500).json({ error: 'Failed to log feeding', details: error });
  }
});

export default router;
