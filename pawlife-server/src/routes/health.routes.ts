import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

router.get('/', async (req: Request, res: Response) => {
  try {
    // Check database connectivity
    await prisma.$queryRaw`SELECT 1`;
    res.json({
      status: 'healthy',
      service: 'PawLife Dedicated Backend Engine',
      version: '1.0.0',
      timestamp: new Date().toISOString(),
      database: 'connected (SQLite / Prisma)',
      uptimeSeconds: Math.floor(process.uptime()),
    });
  } catch (error) {
    res.status(503).json({
      status: 'degraded',
      service: 'PawLife Dedicated Backend Engine',
      database: 'disconnected',
      error: error instanceof Error ? error.message : 'Unknown database error',
    });
  }
});

export default router;
