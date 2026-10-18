import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Starting PawLife Database Seeding...');

  // 1. Seed or Upsert User
  const user = await prisma.user.upsert({
    where: { email: 'shoban@example.com' },
    update: {},
    create: {
      email: 'shoban@example.com',
      name: 'Shoban Herbert',
      avatar: '👨‍💼',
      role: 'OWNER',
    },
  });

  console.log(`👤 User seeded: ${user.name} (${user.email})`);

  // 2. Clear existing pets to ensure idempotent clean seed
  const existingPet = await prisma.pet.findFirst({ where: { name: 'Max' } });
  let petId = existingPet?.id;

  if (!existingPet) {
    const pet = await prisma.pet.create({
      data: {
        name: 'Max',
        species: 'Dog',
        breed: 'Golden Retriever',
        dob: new Date('2021-06-15'),
        gender: 'Male',
        weight: 25.4,
        photoUrl: 'https://images.unsplash.com/photo-1552053831-71594a27632d',
        microchipId: '985141002941029',
        ownerId: user.id,
      },
    });
    petId = pet.id;
    console.log(`🐕 Pet profile seeded: ${pet.name} (${pet.breed}) [ID: ${pet.id}]`);
  }

  if (petId) {
    // 3. Seed Initial Telemetry
    await prisma.telemetryLog.create({
      data: {
        petId,
        bpm: 82,
        bodyTemp: 38.6,
        steps: 3240,
        battery: 95,
        latitude: 13.0827,
        longitude: 80.2707,
        isSafe: true,
      },
    });

    // 4. Seed Feeding History
    await prisma.feedingLog.createMany({
      data: [
        { petId, foodName: 'Royal Canin Golden Retriever Adult', portionGrams: 200, calories: 380, fedAt: new Date(Date.now() - 3600000 * 4) },
        { petId, foodName: 'Freeze-Dried Chicken Breast Treats', portionGrams: 40, calories: 95, fedAt: new Date(Date.now() - 3600000 * 8) },
      ],
    });

    // 5. Seed Weight History
    await prisma.weightRecord.createMany({
      data: [
        { petId, weightKg: 24.8, recordedAt: new Date(Date.now() - 86400000 * 30) },
        { petId, weightKg: 25.1, recordedAt: new Date(Date.now() - 86400000 * 15) },
        { petId, weightKg: 25.4, recordedAt: new Date() },
      ],
    });

    // 6. Seed Medications
    await prisma.medication.createMany({
      data: [
        { petId, name: 'Heartgard Plus (Ivermectin/Pyrantel)', dosage: '1 Chewable', frequency: 'Monthly', nextDue: new Date(Date.now() + 86400000 * 12) },
        { petId, name: 'NexGard Flea & Tick', dosage: '1 Tablet', frequency: 'Monthly', nextDue: new Date(Date.now() + 86400000 * 18) },
      ],
    });

    // 7. Seed Family Members
    await prisma.familyMember.createMany({
      data: [
        { petId, name: 'Shoban (You)', role: 'Primary Owner', avatarEmoji: '👑' },
        { petId, name: 'Sarah', role: 'Co-Parent', avatarEmoji: '👩' },
        { petId, name: 'Carlos', role: 'Dog Walker', avatarEmoji: '🐕' },
        { petId, name: 'Dr. Emily Watson', role: 'Primary Vet', avatarEmoji: '🩺' },
      ],
    });

    // 8. Seed Acoustic Mood Events
    await prisma.acousticEvent.createMany({
      data: [
        { petId, classification: 'Playful Bark', confidence: 0.94, stressScore: 12, bpmCorrelation: 86 },
        { petId, classification: 'Curious Whimper', confidence: 0.88, stressScore: 22, bpmCorrelation: 78 },
        { petId, classification: 'Excited Greeting', confidence: 0.96, stressScore: 16, bpmCorrelation: 92 },
      ],
    });
  }

  // 9. Seed Community Posts
  await prisma.communityPost.createMany({
    data: [
      {
        authorName: 'Menaka & Bella',
        authorAvatar: '🐩',
        content: 'Sunny morning at Semmozhi Poonga Dog Trail! Bella loved meeting all the golden pups today. 🌿🐾',
        likesCount: 14,
      },
      {
        authorName: 'Karthik & Rocky',
        authorAvatar: '🐕',
        content: 'Quick tip: Besant Nagar Beach is super calm today before 8 AM for active off-leash fetch! 🏖️',
        likesCount: 22,
      },
    ],
  });

  console.log('✅ PawLife Database Seeding Completed Successfully!');
}

main()
  .catch((e) => {
    console.error('❌ Seeding error:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
