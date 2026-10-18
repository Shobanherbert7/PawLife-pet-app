import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

// GET team members for a pet
router.get('/:petId', async (req: Request, res: Response) => {
  try {
    const petId = parseInt(req.params.petId);
    const members = await prisma.familyMember.findMany({
      where: { petId },
      orderBy: { createdAt: 'asc' },
    });
    res.json(members);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch family team', details: error });
  }
});

// ADD a new team member
router.post('/', async (req: Request, res: Response) => {
  try {
    const { petId, name, role, avatarEmoji = '👩' } = req.body;

    if (!petId || !name || !role) {
      return res.status(400).json({ error: 'petId, name, and role are required' });
    }

    const member = await prisma.familyMember.create({
      data: {
        petId: parseInt(petId),
        name,
        role,
        avatarEmoji,
      },
    });

    res.status(201).json(member);
  } catch (error) {
    res.status(500).json({ error: 'Failed to add family member', details: error });
  }
});

// REMOVE a team member
router.delete('/:id', async (req: Request, res: Response) => {
  try {
    const id = parseInt(req.params.id);
    await prisma.familyMember.delete({
      where: { id },
    });
    res.json({ success: true, message: 'Member removed' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to remove member', details: error });
  }
});

export default router;
