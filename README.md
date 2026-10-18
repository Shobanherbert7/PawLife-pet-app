# 🐾 PawLife — Next-Generation Smart Pet Health & Co-Parenting Ecosystem

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10-green.svg)](https://developer.android.com/jetpack/compose)
[![Node.js](https://img.shields.io/badge/Node.js-18%2B-brightgreen.svg)](https://nodejs.org/)
[![Express](https://img.shields.io/badge/Express-4.21-lightgrey.svg)](https://expressjs.com/)
[![Prisma](https://img.shields.io/badge/Prisma-5.22-blue.svg)](https://www.prisma.io/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF.svg)](https://vitejs.dev/)

PawLife is a comprehensive, multi-platform smart pet care platform featuring a native **Android mobile app**, a dedicated **Node.js/TypeScript backend** with **SQLite/Prisma ORM**, and a desktop **React/Vite Web Dashboard**.

---

## 🏛️ Monorepo Architecture

```text
PawLife-pet-app/
├── PawLife/             # 📱 Native Android Mobile App (Jetpack Compose, Room DB, Retrofit)
├── pawlife-server/      # ⚙️ Dedicated Backend API & Real-time IoT WebSocket Engine
├── pawlife-web/         # 💻 Web Dashboard with Google Auth & Live Telemetry
└── README.md            # 📖 Documentation & Setup Guide
```

---

## 📱 1. Android Mobile App (`/PawLife`)

A modern Android application designed with **Jetpack Compose** and **Clean Architecture**:

- **Real-Time Walk Tracker**: GPS polyline path tracking, pace calculation, calorie burn estimation, and Google Maps integration.
- **Smart Collar Wearable Sync**: Live Bluetooth Low Energy (BLE) simulation streaming pulse (BPM), body temperature, step count, and battery level.
- **Smart Kibble Pantry & Food Scanner**: Barcode scanning via OpenFoodFacts with automated caloric computation.
- **PawAcoustics AI**: Audio spectrum analyzer classifying pet vocalizations (Anxiety, Alert, Playful) with soothing 432Hz sound therapy.
- **Gamified Agility Training**: Virtual clicker training module with trick mastery leveling and XP rewards.
- **Bi-Directional Room Sync**: Offline-first Room SQLite database synchronizing walk sessions and feeding logs to `pawlife-server` via Retrofit.

### Quick Start (Android)
```bash
cd PawLife
./gradlew assembleDebug
# Install on device
./gradlew installDebug
```

---

## ⚙️ 2. Dedicated Backend Service (`/pawlife-server`)

A high-performance standalone REST API and real-time WebSocket server built with **Express 4**, **TypeScript**, and **Prisma ORM**:

- **Relational Models**: `User`, `Pet`, `TelemetryLog`, `WalkSession`, `FeedingLog`, `WeightRecord`, `Medication`, `FamilyMember`, `CommunityPost`, `AcousticEvent`, `MarketplaceOrder`.
- **Database**: Local SQLite relational database (`prisma/pawlife.db`).
- **Real-Time WebSockets (`ws://localhost:4000`)**: Broadcasts IoT smart collar telemetry frames every 3 seconds, plus instant `NEW_WALK` and `NEW_FEEDING` events.
- **Haversine Geofencing**: Automated 500m home safe-zone breach alerts.

### Quick Start (Backend)
```bash
cd pawlife-server
npm install
npx prisma db push
npm run build
npm start
# Visual database browser
npx prisma studio
```

---

## 💻 3. Web Dashboard (`/pawlife-web`)

A desktop-first web client built with **React**, **Vite**, and **Lucide Icons**:

- **Google OAuth Login**: One-click Google sign-in and persistent user session management.
- **Live Server & DB Pill**: Real-time health check indicator connecting to `pawlife-server`.
- **Live Collar Telemetry**: Dynamically reflects smart collar vitals received via WebSocket.
- **Family Hub & Co-Parenting**: Shared task management, feeding schedules, and appointment calendar.
- **BarkConnect Social**: Community forum and playdate requests.

### Quick Start (Web)
```bash
cd pawlife-web
npm install
npm run dev
```

---

## 👥 Authors & License
Developed for pet parents everywhere. Released under the MIT License.
