import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

// GET acoustic classification events for a pet
router.get('/:petId', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.petId);
    const events = await prisma.acousticEvent.findMany({
      where: { petId },
      orderBy: { timestamp: 'desc' },
      take: 20,
    });
    res.json(events);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch acoustic events', details: error });
  }
});

// LOG an acoustic mood event
router.post('/', async (req: Request, res: Response) => {
  try {
    const { petId, classification, confidence, stressScore, bpmCorrelation } = req.body;

    const event = await prisma.acousticEvent.create({
      data: {
        petId: parseInt(petId),
        classification: classification || 'Playful Bark',
        confidence: parseFloat(confidence) || 0.92,
        stressScore: parseInt(stressScore) || 20,
        bpmCorrelation: parseInt(bpmCorrelation) || 84,
      },
    });

    res.status(201).json(event);
  } catch (error) {
    res.status(500).json({ error: 'Failed to record acoustic event', details: error });
  }
});

export default router;
