import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

// GET all active pets
router.get('/', async (req: Request, res: Response) => {
  try {
    const pets = await prisma.pet.findMany({
      where: { isActive: true },
      include: {
        telemetry: {
          take: 1,
          orderBy: { timestamp: 'desc' },
        },
      },
      orderBy: { name: 'asc' },
    });
    res.json(pets);
  } catch (error) {
    res.status(500).json({ error: 'Failed to retrieve pets', details: error });
  }
});

// GET pet by ID with detailed health history
router.get('/:id', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.id);
    const pet = await prisma.pet.findUnique({
      where: { id: petId },
      include: {
        telemetry: { take: 10, orderBy: { timestamp: 'desc' } },
        feedings: { take: 10, orderBy: { fedAt: 'desc' } },
        weights: { take: 10, orderBy: { recordedAt: 'desc' } },
        medications: { where: { isCompleted: false }, orderBy: { nextDue: 'asc' } },
        familyTeam: true,
      },
    });

    if (!pet) {
      return res.status(404).json({ error: 'Pet not found' });
    }
    res.json(pet);
  } catch (error) {
    res.status(500).json({ error: 'Failed to retrieve pet', details: error });
  }
});

// CREATE a new pet profile
router.post('/', async (req: Request, res: Response) => {
  try {
    const { name, breed, species = 'Dog', weight = 5.0, photoUrl, microchipId, ownerId } = req.body;
    
    if (!name || !breed) {
      return res.status(400).json({ error: 'Name and breed are required fields' });
    }

    const newPet = await prisma.pet.create({
      data: {
        name,
        breed,
        species,
        weight: parseFloat(weight.toString()) || 5.0,
        photoUrl: photoUrl || 'https://images.unsplash.com/photo-1552053831-71594a27632d',
        microchipId: microchipId || null,
        ownerId: ownerId || null,
      },
    });

    // Seed initial telemetry for instant dashboard readiness
    await prisma.telemetryLog.create({
      data: {
        petId: newPet.id,
        bpm: 84,
        bodyTemp: 38.6,
        steps: 1240,
        battery: 98,
        latitude: 13.0827,
        longitude: 80.2707,
        isSafe: true,
      },
    });

    res.status(201).json(newPet);
  } catch (error) {
    res.status(500).json({ error: 'Failed to create pet', details: error });
  }
});

// UPDATE a pet profile
router.put('/:id', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.id);
    const { name, breed, weight, photoUrl, microchipId } = req.body;

    const updated = await prisma.pet.update({
      where: { id: petId },
      data: {
        ...(name && { name }),
        ...(breed && { breed }),
        ...(weight !== undefined && { weight: parseFloat(weight.toString()) }),
        ...(photoUrl && { photoUrl }),
        ...(microchipId !== undefined && { microchipId }),
      },
    });

    res.json(updated);
  } catch (error) {
    res.status(500).json({ error: 'Failed to update pet', details: error });
  }
});

// SOFT DELETE a pet profile
router.delete('/:id', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.id);
    await prisma.pet.update({
      where: { id: petId },
      data: { isActive: false },
    });
    res.json({ success: true, message: 'Pet marked inactive' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to delete pet', details: error });
  }
});

export default router;
