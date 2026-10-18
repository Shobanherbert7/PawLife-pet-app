import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

// GET all community posts
router.get('/posts', async (req: Request, res: Response) => {
  try {
    const posts = await prisma.communityPost.findMany({
      orderBy: { createdAt: 'desc' },
      take: 25,
    });
    res.json(posts);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch community posts', details: error });
  }
});

// CREATE community post
router.post('/posts', async (req: Request, res: Response) => {
  try {
    const { authorName, authorAvatar = '🐕', content, imageUrl } = req.body;

    if (!authorName || !content) {
      return res.status(400).json({ error: 'authorName and content are required' });
    }

    const post = await prisma.communityPost.create({
      data: {
        authorName,
        authorAvatar,
        content,
        imageUrl,
        likesCount: 0,
      },
    });

    res.status(201).json(post);
  } catch (error) {
    res.status(500).json({ error: 'Failed to create post', details: error });
  }
});

// LIKE a post
router.post('/posts/:id/like', async (req: Request, res: Response) => {
  try {
    const id = parseInt(req.params.id);
    const updated = await prisma.communityPost.update({
      where: { id },
      data: { likesCount: { increment: 1 } },
    });
    res.json(updated);
  } catch (error) {
    res.status(500).json({ error: 'Failed to like post', details: error });
  }
});

export default router;
