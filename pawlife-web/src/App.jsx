import React, { useState, useEffect } from 'react';
import { 
  Sparkles, Activity, Video, Users, Calendar, Plus, 
  Trash2, Phone, AlertTriangle, CheckCircle, Clock, 
  ChevronRight, Mic, MicOff, VideoOff, Camera, X, Award,
  Share2, Heart, Shield, RefreshCw, Cloud, CloudOff,
  Compass, Battery, BatteryCharging, MapPin, Navigation, Zap, Flame, Play, Square, Bluetooth, ExternalLink,
  Printer, Download, ShoppingBag, ShoppingCart, Tag, CheckSquare, Bell, Volume2, VolumeX,
  BarChart3, TrendingUp, Upload, Utensils, Star, Info, QrCode, MessageSquare,
  LogOut, UserCheck, ChevronDown
} from 'lucide-react';
import { auth, db, collection, doc, setDoc, addDoc, onSnapshot, query, orderBy, limit } from './firebase';
import AuthScreen from './AuthScreen';
import { fetchServerHealth, subscribeToTelemetryWebSocket } from './services/api';
import './App.css';

const INITIAL_PETS = [
  {
    id: 1,
    name: "Max",
    species: "Dog",
    breed: "Golden Retriever",
    age: "3 yrs",
    weight: 25.5,
    healthScore: 94,
    avatar: "🐕",
    photoUrl: "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=400&q=80",
    status: "Healthy & Active"
  },
  {
    id: 2,
    name: "Luna",
    species: "Cat",
    breed: "Persian Longhair",
    age: "2 yrs",
    weight: 4.2,
    healthScore: 98,
    avatar: "🐈",
    photoUrl: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=400&q=80",
    status: "Resting"
  },
  {
    id: 3,
    name: "Rocky",
    species: "Dog",
    breed: "French Bulldog",
    age: "1.5 yrs",
    weight: 12.0,
    healthScore: 89,
    avatar: "🐶",
    photoUrl: "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=400&q=80",
    status: "Playful"
  }
];

const INITIAL_MEALS = [
  { id: 'm1', petId: 1, name: "Premium Royal Canin Kibble", portion: 120, calories: 420, time: "Today, 8:30 AM", fedAt: Date.now() - 36000000 },
  { id: 'm2', petId: 1, name: "Salmon Oil & Veggie Topper", portion: 50, calories: 150, time: "Yesterday, 7:00 PM", fedAt: Date.now() - 86400000 },
  { id: 'm3', petId: 2, name: "Whiskas Tuna & Gravy", portion: 85, calories: 95, time: "Today, 9:00 AM", fedAt: Date.now() - 30000000 }
];

const INITIAL_APPOINTMENTS = [
  { id: 'a1', petId: 1, clinic: "Anna Nagar Pet Hospital", purpose: "Annual Rabies Booster", date: "Tomorrow, 10:30 AM", status: "Confirmed", doctor: "Dr. Priya Sharma" },
  { id: 'a2', petId: 2, clinic: "Happy Paws Clinic - T Nagar", purpose: "Dental Hygiene Checkup", date: "Nov 2, 2:00 PM", status: "Scheduled", doctor: "Dr. K. Raman" }
];

const INITIAL_WEIGHTS = [
  { id: 'w1', petId: 1, weight: 24.8, date: "Aug 15", notes: "Healthy baseline", recordedAt: Date.now() - 5000000000 },
  { id: 'w2', petId: 1, weight: 25.1, date: "Sep 15", notes: "Normal weight gain", recordedAt: Date.now() - 2500000000 },
  { id: 'w3', petId: 1, weight: 25.5, date: "Oct 15", notes: "Active & muscular", recordedAt: Date.now() }
];

export default function App() {
  // Web Dashboard Authentication State
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem('pawlife_auth_user');
      return saved ? JSON.parse(saved) : null;
    } catch (_e) {
      return null;
    }
  });
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [serverStatus, setServerStatus] = useState({ online: false, database: 'Connecting...', wsActive: false });

  const handleLogout = async () => {
    try {
      await auth.signOut();
    } catch (_e) {}
    localStorage.removeItem('pawlife_auth_user');
    setCurrentUser(null);
    setShowProfileMenu(false);
  };

  const handleSwitchPersona = (persona) => {
    const authUser = {
      uid: `demo_${persona.id}`,
      displayName: persona.name,
      email: persona.email,
      role: persona.role,
      avatar: persona.avatar,
      badge: persona.badge,
      color: persona.color
    };
    localStorage.setItem('pawlife_auth_user', JSON.stringify(authUser));
    setCurrentUser(authUser);
    setShowProfileMenu(false);
  };

  const [activeTab, setActiveTab] = useState('overview');
  const [pets, setPets] = useState(() => {
    const saved = localStorage.getItem('pawlife_pets');
    return saved ? JSON.parse(saved) : INITIAL_PETS;
  });
  const [activePetId, setActivePetId] = useState(1);
  const activePet = pets.find(p => p.id === activePetId) || pets[0] || INITIAL_PETS[0];
  const [meals, setMeals] = useState(() => {
    const saved = localStorage.getItem('pawlife_meals');
    return saved ? JSON.parse(saved) : INITIAL_MEALS;
  });
  const [appointments, setAppointments] = useState(() => {
    const saved = localStorage.getItem('pawlife_appts');
    return saved ? JSON.parse(saved) : INITIAL_APPOINTMENTS;
  });
  const [weights, setWeights] = useState(() => {
    const saved = localStorage.getItem('pawlife_weights');
    return saved ? JSON.parse(saved) : INITIAL_WEIGHTS;
  });

  // Smart Collar & Live Walk Telemetry
  const [collarTelemetry, setCollarTelemetry] = useState(() => {
    const saved = localStorage.getItem('pawlife_collar');
    return saved ? JSON.parse(saved) : {
      petId: 1,
      deviceName: "PawLife Collar BLE-89X",
      isConnected: true,
      heartRateBpm: 84,
      stepCount: 1420,
      batteryLevel: 92,
      updatedAt: Date.now()
    };
  });

  const [activeWalk, setActiveWalk] = useState(() => {
    const saved = localStorage.getItem('pawlife_walk');
    return saved ? JSON.parse(saved) : {
      petId: 1,
      isActive: false,
      durationSeconds: 1540,
      distanceMeters: 2450,
      latitude: 13.0827,
      longitude: 80.2707,
      routePoints: [
        "13.0827,80.2707", "13.0835,80.2715", "13.0848,80.2728", "13.0856,80.2740"
      ],
      updatedAt: Date.now()
    };
  });

  const [walkHistory, setWalkHistory] = useState([
    { id: 'w1', petId: 1, date: 'Today, 7:15 AM', distanceMeters: 2450, durationSeconds: 1540, calories: 122 },
    { id: 'w2', petId: 1, date: 'Yesterday, 6:00 PM', distanceMeters: 3100, durationSeconds: 1980, calories: 155 },
    { id: 'w3', petId: 1, date: 'Oct 15, 7:00 AM', distanceMeters: 1800, durationSeconds: 1200, calories: 90 }
  ]);

  const [simulatingWalk, setSimulatingWalk] = useState(false);

  // 1. Geofence & Instant Escape Siren State
  const [geofenceStatus, setGeofenceStatus] = useState({
    isBreached: false,
    distanceFromHome: 38,
    safeRadiusMeters: 150,
    latitude: 13.0827,
    longitude: 80.2707,
    updatedAt: Date.now()
  });
  const [sirenMuted, setSirenMuted] = useState(false);

  // 2. AI Health Diagnostics & Clinical Vet Report States
  const [aiTriageReports, setAiTriageReports] = useState([
    {
      id: 'r1',
      petName: 'Max',
      symptoms: 'Mild redness around left ear and frequent scratching',
      findings: 'Otitis externa or mild seasonal contact allergy. Auricular canal shows mild localized erythema without deep purulent discharge. Clean canal with veterinary antiseptic solution; schedule clinic visit if head-shaking persists > 48h.',
      urgency: 'MODERATE',
      timestamp: Date.now() - 28000000
    }
  ]);
  const [currentAiReport, setCurrentAiReport] = useState(null);
  const [showVetReportModal, setShowVetReportModal] = useState(false);
  const [selectedSymptom, setSelectedSymptom] = useState("Persistent dry cough after morning exercise");
  const [isAnalyzingAi, setIsAnalyzingAi] = useState(false);

  // 3. Family Hub & Multi-User Co-Parenting States
  const [familyTasks, setFamilyTasks] = useState([
    { id: 't1', title: 'Feed Max (Morning Kibble - 120g)', time: '08:30 AM', assignee: 'You', isCompleted: true },
    { id: 't2', title: 'Administer Monthly Flea / Tick Chew', time: '10:00 AM', assignee: 'Sarah (Co-Parent)', isCompleted: false },
    { id: 't3', title: 'Evening 2.5 km Park Walk', time: '06:00 PM', assignee: 'Alex (Walker)', isCompleted: false },
    { id: 't4', title: 'Brush Coat & Dental Plaque Chew', time: '08:30 PM', assignee: 'Unassigned', isCompleted: false }
  ]);
  const [newChoreTitle, setNewChoreTitle] = useState("");
  const [newChoreAssignee, setNewChoreAssignee] = useState("You");
  const [coParentTeam] = useState([
    { name: 'You (Shoban)', role: 'Primary Owner', avatar: '👑', status: 'Active' },
    { name: 'Sarah', role: 'Co-Parent', avatar: '👩', status: 'Online' },
    { name: 'Alex', role: 'Dog Walker', avatar: '🐕', status: 'Active' },
    { name: 'Dr. Priya Sharma', role: 'Attending Vet', avatar: '🩺', status: 'Available' }
  ]);

  // 4. Smart Kibble Pantry & Marketplace States
  const [pantryLevel, setPantryLevel] = useState({
    foodName: 'Royal Canin Golden Retriever Adult',
    totalCapacityGrams: 5000,
    remainingGrams: 900,
    daysLeft: 3
  });
  const [marketplaceItems] = useState([
    { id: 'p1', name: 'Royal Canin Adult Kibble (3 kg)', price: 1850, category: 'Food', rating: 4.9, icon: '🍲', desc: 'Breed-tailored formula for optimal cardiac and joint health.' },
    { id: 'p2', name: 'Wild Alaskan Salmon Oil (500ml)', price: 890, category: 'Supplements', rating: 4.8, icon: '🐟', desc: 'Omega-3 EPA & DHA for shiny coat and reduced shedding.' },
    { id: 'p3', name: 'PawLife Smart Collar Replacement Band', price: 650, category: 'Wearables', rating: 5.0, icon: '⌚', desc: 'Waterproof antimicrobial silicone strap with safety clasp.' },
    { id: 'p4', name: 'Organic Calming Hemp Bites (60 chews)', price: 780, category: 'Treats', rating: 4.7, icon: '🌿', desc: 'Chamomile & L-theanine formula for anxiety and fireworks.' },
    { id: 'p5', name: 'Enzymatic Dental Care Chews', price: 450, category: 'Dental', rating: 4.8, icon: '🦷', desc: 'Veterinarian-approved plaque and tartar prevention chews.' }
  ]);
  const [cart, setCart] = useState([]);
  const [showCartModal, setShowCartModal] = useState(false);
  const [promoCode, setPromoCode] = useState('');
  const [discountPercent, setDiscountPercent] = useState(0);
  const [orderSuccess, setOrderSuccess] = useState(false);

  // Sprint 5 States: Nearby Parks & 24/7 Vets Directory
  const [nearbyPlaces] = useState([
    {
      id: "p1",
      name: "Anna Nagar 24/7 Veterinary Hospital",
      category: "Emergency Vet",
      distanceKm: 1.4,
      address: "2nd Avenue, Anna Nagar, Chennai",
      phone: "+91 98401 22334",
      rating: 4.9,
      is24Hours: true,
      openTime: "Open 24 Hours • Emergency Critical Care",
      queryAddress: "Anna Nagar Veterinary Hospital Chennai"
    },
    {
      id: "p2",
      name: "Semmozhi Poonga Dog Trail & Botanical Park",
      category: "Dog Park",
      distanceKm: 2.8,
      address: "Cathedral Road, Teynampet, Chennai",
      phone: "+91 98401 55667",
      rating: 4.8,
      is24Hours: false,
      openTime: "06:00 AM - 08:30 PM • Off-Leash Mornings",
      queryAddress: "Semmozhi Poonga Chennai"
    },
    {
      id: "p3",
      name: "Barking Deer Pet Cafe & Play Arena",
      category: "Pet Cafe",
      distanceKm: 3.2,
      address: "Khadar Nawaz Khan Rd, Nungambakkam, Chennai",
      phone: "+91 98401 88990",
      rating: 4.7,
      is24Hours: false,
      openTime: "11:00 AM - 10:00 PM • Canine Treat Menu",
      queryAddress: "Pet Cafe Nungambakkam Chennai"
    },
    {
      id: "p4",
      name: "Madras Veterinary College Emergency Trauma Center",
      category: "Emergency Vet",
      distanceKm: 4.1,
      address: "Vepery High Road, Vepery, Chennai",
      phone: "+91 44 2530 4000",
      rating: 4.9,
      is24Hours: true,
      openTime: "Open 24 Hours • ICU & Surgical Suites",
      queryAddress: "Madras Veterinary College Vepery Chennai"
    },
    {
      id: "p5",
      name: "Besant Nagar Beach Coastal Dog Run",
      category: "Dog Park",
      distanceKm: 7.5,
      address: "6th Avenue, Elliot's Beach, Besant Nagar, Chennai",
      phone: "+91 98401 99112",
      rating: 4.8,
      is24Hours: false,
      openTime: "Sunrise to Sunset • Wide Sand Run",
      queryAddress: "Elliots Beach Besant Nagar Chennai"
    }
  ]);
  const [nearbyFilter, setNearbyFilter] = useState("All");

  // Sprint 5 States: AI Vision Food Scanner
  const [showAiFoodModal, setShowAiFoodModal] = useState(false);
  const [aiFoodState, setAiFoodState] = useState({
    name: "Royal Canin Golden Retriever Adult Kibble",
    category: "Dry Extruded Kibble",
    kcalPer100g: 365,
    proteinPercent: 26,
    fatPercent: 14,
    portionGrams: 120,
    confidencePercent: 95,
    icon: "🍲"
  });
  const [isScanningFood, setIsScanningFood] = useState(false);

  // Sprint 5 States: Longitudinal Wellness Trends (7 Days)
  const [wellnessTrends] = useState([
    { day: "Mon", walkKm: 2.1, calories: 520, restingBpm: 82, weight: 25.4 },
    { day: "Tue", walkKm: 3.4, calories: 590, restingBpm: 80, weight: 25.4 },
    { day: "Wed", walkKm: 1.8, calories: 480, restingBpm: 84, weight: 25.5 },
    { day: "Thu", walkKm: 2.9, calories: 550, restingBpm: 81, weight: 25.5 },
    { day: "Fri", walkKm: 2.5, calories: 540, restingBpm: 83, weight: 25.5 },
    { day: "Sat", walkKm: 4.2, calories: 680, restingBpm: 79, weight: 25.4 },
    { day: "Sun", walkKm: 3.1, calories: 570, restingBpm: 82, weight: 25.5 }
  ]);

  // Sprint 6 States: Pet Health Passport & QR Public Credential
  const [petPassport, setPetPassport] = useState({
    passportId: "PAW-IN-2026-98514",
    petName: "Max",
    species: "Canine",
    breed: "Golden Retriever",
    gender: "Male (Intact)",
    birthDate: "14 Oct 2023",
    microchipId: "985141002348123",
    microchipStandard: "ISO 11784/11785 FDX-B (134.2 kHz)",
    officialRegistry: "Kennel Club of India (KCI) & PawLife Global",
    guardianName: "Shoban Herbert",
    guardianContact: "+91 98401 23456",
    guardianCity: "Chennai, Tamil Nadu, India",
    rabiesCertificateNo: "RAB-TN-2025-4491",
    immunityStatus: "FULLY VACCINATED",
    publicVerificationUrl: "http://localhost:5173/?passport=1"
  });

  const [vaccinations, setVaccinations] = useState([
    {
      id: "v1",
      vaccineName: "Rabies 3-Year Inactivated Booster",
      category: "Core Canine",
      administeredDate: "15 Oct 2024",
      expiryDate: "14 Oct 2027",
      batchLotNumber: "RAB-8812-DEF",
      clinicName: "Anna Nagar Pet Hospital",
      doctorName: "Dr. Priya Sharma, BVSc",
      status: "VALID",
      timestamp: Date.now() - 31536000000
    },
    {
      id: "v2",
      vaccineName: "Canine DHPPi (5-in-1 Distemper & Parvo)",
      category: "Core Canine",
      administeredDate: "02 Sep 2024",
      expiryDate: "01 Sep 2027",
      batchLotNumber: "NOV-4401-DH",
      clinicName: "Anna Nagar Pet Hospital",
      doctorName: "Dr. Priya Sharma, BVSc",
      status: "VALID",
      timestamp: Date.now() - 35000000000
    },
    {
      id: "v3",
      vaccineName: "Bordetella Bronchiseptica (Kennel Cough)",
      category: "Lifestyle / Boarding",
      administeredDate: "20 Apr 2026",
      expiryDate: "19 Apr 2027",
      batchLotNumber: "BOR-1092-KC",
      clinicName: "Madras Veterinary College Trauma Center",
      doctorName: "Dr. K. Raman, MVSc",
      status: "VALID",
      timestamp: Date.now() - 15000000000
    },
    {
      id: "v4",
      vaccineName: "Broad-Spectrum Deworming (Praziquantel)",
      category: "Preventive Parasite",
      administeredDate: "01 Oct 2026",
      expiryDate: "01 Jan 2027",
      batchLotNumber: "DWM-3310-Z",
      clinicName: "Anna Nagar Pet Hospital",
      doctorName: "Dr. Priya Sharma, BVSc",
      status: "VALID",
      timestamp: Date.now() - 1400000000
    }
  ]);

  const [showQrModal, setShowQrModal] = useState(false);
  const [showAddVaccineModal, setShowAddVaccineModal] = useState(false);
  const [newVaccineName, setNewVaccineName] = useState("");
  const [newVaccineCategory, setNewVaccineCategory] = useState("Core Canine");
  const [newVaccineLot, setNewVaccineLot] = useState("LOT-2026-X8");
  const [newVaccineClinic, setNewVaccineClinic] = useState("Anna Nagar Pet Hospital");
  const [newVaccineExpiry, setNewVaccineExpiry] = useState("1 Year Valid");

  const [isPublicPassportView, setIsPublicPassportView] = useState(() => {
    return typeof window !== 'undefined' && new URLSearchParams(window.location.search).get('passport') !== null;
  });

  // Sprint 7: BarkConnect Live Dog Parks & Playdates State
  const [dogParks, setDogParks] = useState([
    {
      id: "semmozhi_poonga",
      name: "Semmozhi Poonga Dog Trail & Botanical Arena",
      location: "Cathedral Road, Teynampet, Chennai",
      distanceKm: 2.8,
      rating: 4.8,
      isOpen: true,
      openHours: "06:00 AM - 08:30 PM",
      totalDogsActive: 3,
      amenities: ["Water Stations", "Fenced Agility Track", "Waste Stations", "Shaded Benches"],
      activeDogs: [
        { id: "d1", name: "Cooper", breed: "Golden Retriever", age: "2 yrs", avatar: "🐕", ownerName: "Vikram R." },
        { id: "d2", name: "Bella", breed: "Beagle", age: "1.5 yrs", avatar: "🐶", ownerName: "Meera K." },
        { id: "d3", name: "Simba", breed: "Shih Tzu", age: "3 yrs", avatar: "🐩", ownerName: "Arun S." }
      ]
    },
    {
      id: "besant_nagar",
      name: "Besant Nagar Beach Coastal Dog Run",
      location: "6th Avenue, Elliot's Beach, Besant Nagar, Chennai",
      distanceKm: 7.5,
      rating: 4.9,
      isOpen: true,
      openHours: "Sunrise to Sunset",
      totalDogsActive: 5,
      amenities: ["Open Sand Agility", "Salt Water Rinse", "Pet Cafe Nearby", "Off-Leash Zone"],
      activeDogs: [
        { id: "d4", name: "Bruno", breed: "German Shepherd", age: "3 yrs", avatar: "🐕", ownerName: "Karthik N." },
        { id: "d5", name: "Coco", breed: "Labrador", age: "4 yrs", avatar: "🦮", ownerName: "Pooja D." },
        { id: "d6", name: "Milo", breed: "Indie Dog", age: "2 yrs", avatar: "🐕", ownerName: "Divya T." }
      ]
    },
    {
      id: "tower_park",
      name: "Anna Nagar Tower Park Canine Meadow",
      location: "3rd Main Road, Anna Nagar, Chennai",
      distanceKm: 1.4,
      rating: 4.7,
      isOpen: true,
      openHours: "05:30 AM - 08:00 PM",
      totalDogsActive: 2,
      amenities: ["Grass Lawn", "Agility Hurdles", "Drinking Fountains", "CCTV Secured"],
      activeDogs: [
        { id: "d7", name: "Leo", breed: "French Bulldog", age: "1 yr", avatar: "🐶", ownerName: "Siddharth J." },
        { id: "d8", name: "Daisy", breed: "Pomeranian", age: "2.5 yrs", avatar: "🐕", ownerName: "Ananya M." }
      ]
    }
  ]);
  const [userCheckedInParkId, setUserCheckedInParkId] = useState(null);

  const [playdateCandidates, setPlaydateCandidates] = useState([
    {
      id: "pd1",
      name: "Cooper",
      breed: "Golden Retriever",
      age: "2 yrs",
      avatar: "🐕",
      energyLevel: "High",
      compatibilityScore: 96,
      distanceKm: 1.2,
      bio: "Super friendly, ball obsessed, loves swimming & chasing tennis balls at Semmozhi Poonga!",
      ownerName: "Vikram R.",
      status: "Available for Playdate"
    },
    {
      id: "pd2",
      name: "Bella",
      breed: "Beagle",
      age: "1.5 yrs",
      avatar: "🐶",
      energyLevel: "Moderate",
      compatibilityScore: 89,
      distanceKm: 2.4,
      bio: "Gentle sniffer, loves group pack walks and playing tug-of-war. Very friendly.",
      ownerName: "Meera K.",
      status: "Available for Playdate"
    },
    {
      id: "pd3",
      name: "Bruno",
      breed: "German Shepherd",
      age: "3 yrs",
      avatar: "🐕",
      energyLevel: "High",
      compatibilityScore: 84,
      distanceKm: 4.1,
      bio: "Agility trained, loves fetch and active beach runs at Elliot's Beach on weekends.",
      ownerName: "Karthik N.",
      status: "Available for Playdate"
    },
    {
      id: "pd4",
      name: "Luna",
      breed: "Cocker Spaniel",
      age: "2 yrs",
      avatar: "🐩",
      energyLevel: "Calm",
      compatibilityScore: 92,
      distanceKm: 1.8,
      bio: "Sweet and calm temperament. Enjoys relaxing grassy park sessions and gentle trots.",
      ownerName: "Sangeetha V.",
      status: "Available for Playdate"
    }
  ]);
  const [playdateFilter, setPlaydateFilter] = useState("All");
  const [invitedPlaydates, setInvitedPlaydates] = useState({});

  const [communityPosts, setCommunityPosts] = useState([
    {
      id: "cp1",
      authorName: "Vikram & Cooper",
      authorRole: "Golden Parent • Teynampet",
      content: "Sunny morning at Semmozhi Poonga! Cooper made 3 new friends on the agility track today. The grass is freshly mowed and water stations are fully stocked! 🐾✨",
      likesCount: 18,
      isLiked: false,
      timestamp: Date.now() - 3600000,
      badge: "Dog Park Check-in",
      location: "Semmozhi Poonga Dog Trail"
    },
    {
      id: "cp2",
      authorName: "Dr. Priya Sharma",
      authorRole: "Attending Vet • Anna Nagar",
      content: "Reminder for Chennai pet parents: With humidity rising, ensure your dogs are hydrated during park playdates. Avoid midday asphalt walks to protect paw pads from burns! 🩺💧",
      likesCount: 34,
      isLiked: true,
      timestamp: Date.now() - 14400000,
      badge: "Vet Tip",
      location: "Anna Nagar Pet Hospital"
    },
    {
      id: "cp3",
      authorName: "Karthik & Bruno",
      authorRole: "GSD Parent • Besant Nagar",
      content: "Coastal run at Elliot's Beach was incredible this morning! Bruno chased Frisbees in the shallows. Anyone up for an evening pack run tomorrow around 5 PM?",
      likesCount: 22,
      isLiked: false,
      timestamp: Date.now() - 28800000,
      badge: "Pack Run",
      location: "Besant Nagar Beach"
    }
  ]);
  const [newPostContent, setNewPostContent] = useState("");
  const [newPostLocation, setNewPostLocation] = useState("Semmozhi Poonga Dog Trail");
  const [showNewPostModal, setShowNewPostModal] = useState(false);
  const [communitySubTab, setCommunitySubTab] = useState("parks");

  // Sprint 8: AR Pet Agility & Interactive Trick Coach (PawAR)
  const [agilityXp, setAgilityXp] = useState(480);
  const [agilityMasteryLevel, setAgilityMasteryLevel] = useState("Novice Agility");
  const [placedObstacles, setPlacedObstacles] = useState([
    { id: 'obs-1', type: 'HURDLE', label: 'Hurdle Leap', x: 25, y: 35, cleared: false, icon: '🚧' },
    { id: 'obs-2', type: 'CONE', label: 'Slalom Weave', x: 50, y: 55, cleared: false, icon: '🔻' },
    { id: 'obs-3', type: 'TUNNEL', label: 'Speed Tunnel', x: 75, y: 38, cleared: false, icon: '🌀' },
    { id: 'obs-4', type: 'HOOP', label: 'Tire Hoop Jump', x: 50, y: 80, cleared: false, icon: '⭕' }
  ]);
  const [activeTrick, setActiveTrick] = useState({
    id: 'sit',
    name: 'Sit & Hold',
    cueWord: 'SIT',
    difficulty: 'Basic',
    description: 'Hold a treat above nose level, angle backward until hips touch the floor, then immediately mark behavior with clicker.',
    repsCompleted: 4,
    repsTarget: 5,
    xpReward: 30
  });
  const [clickerCount, setClickerCount] = useState(0);
  const [isClickingAnim, setIsClickingAnim] = useState(false);
  const [agilitySubTab, setAgilitySubTab] = useState("arena");
  const [agilitySessions, setAgilitySessions] = useState([
    { id: 's1', petName: 'Max', obstacleCourse: 'Living Room 4-Obstacle Course', durationSec: 320, obstaclesCleared: 4, xpEarned: 80, timestamp: Date.now() - 3600000 },
    { id: 's2', petName: 'Max', obstacleCourse: 'Slalom Cones & Hoop Practice', durationSec: 240, obstaclesCleared: 3, xpEarned: 60, timestamp: Date.now() - 86400000 },
    { id: 's3', petName: 'Max', obstacleCourse: 'Novice Hurdle Drill', durationSec: 180, obstaclesCleared: 2, xpEarned: 40, timestamp: Date.now() - 172800000 }
  ]);
  const [trickCurriculum, setTrickCurriculum] = useState([
    { id: 'sit', name: 'Sit & Hold', cue: 'SIT', level: 'Basic', reps: 5, targetReps: 5, xp: 30, mastered: true, tip: 'Reward immediately upon rump touch.' },
    { id: 'stay', name: 'Stay & Release', cue: 'STAY', level: 'Novice', reps: 3, targetReps: 5, xp: 45, mastered: false, tip: 'Take 2 steps back, return and reward.' },
    { id: 'paw', name: 'High Paw Shake', cue: 'PAW', level: 'Basic', reps: 5, targetReps: 5, xp: 30, mastered: true, tip: 'Gently lift wrist while saying cue.' },
    { id: 'spin', name: '360° Spin Circle', cue: 'SPIN', level: 'Intermediate', reps: 2, targetReps: 5, xp: 60, mastered: false, tip: 'Lure in a complete 360 loop.' },
    { id: 'hurdle', name: 'Virtual Hurdle Leap', cue: 'OVER', level: 'Advanced', reps: 1, targetReps: 5, xp: 75, mastered: false, tip: 'Guide pet with target stick over hurdle.' }
  ]);

  const [syncConnected, setSyncConnected] = useState(false);

  // Sprint 9: AI Vocalization & Acoustic Mood Classifier (PawAcoustics)
  const [isMicListening, setIsMicListening] = useState(true);
  const [ambientDecibels, setAmbientDecibels] = useState(44);
  const [spectrogramBars, setSpectrogramBars] = useState(() => 
    Array.from({ length: 24 }, (_, i) => 0.15 + 0.65 * Math.sin((i / 24) * Math.PI))
  );
  const [latestAcousticEpisode, setLatestAcousticEpisode] = useState({
    id: 'ep-init',
    petName: 'Max',
    moodState: 'PLAYFUL',
    moodLabel: 'Playful & Joyful',
    emoji: '🎾',
    confidenceScore: 0.94,
    peakDecibels: 78,
    fundamentalPitchHz: 520,
    collarHeartRateBpm: 94,
    stressIndex: 28,
    summary: 'Bouncy high pitch with playful pause. Engage in interactive play or agility training.',
    timestamp: Date.now() - 120000
  });
  const [acousticEvents, setAcousticEvents] = useState([
    {
      id: 'ep-1',
      petName: 'Max',
      moodState: 'PLAYFUL',
      moodLabel: 'Playful & Joyful',
      emoji: '🎾',
      confidenceScore: 0.94,
      peakDecibels: 78,
      fundamentalPitchHz: 520,
      collarHeartRateBpm: 94,
      stressIndex: 28,
      summary: 'Bouncy high pitch with playful pause.',
      timestamp: Date.now() - 180000
    },
    {
      id: 'ep-2',
      petName: 'Max',
      moodState: 'ALERT',
      moodLabel: 'Alert & Territorial',
      emoji: '🚨',
      confidenceScore: 0.89,
      peakDecibels: 84,
      fundamentalPitchHz: 360,
      collarHeartRateBpm: 112,
      stressIndex: 74,
      summary: 'Territorial alert bark triggered by delivery arrival.',
      timestamp: Date.now() - 3600000
    },
    {
      id: 'ep-3',
      petName: 'Max',
      moodState: 'ANXIETY',
      moodLabel: 'Separation Anxiety',
      emoji: '🥺',
      confidenceScore: 0.91,
      peakDecibels: 68,
      fundamentalPitchHz: 710,
      collarHeartRateBpm: 104,
      stressIndex: 65,
      summary: 'Ascending whines during mid-morning alone time.',
      timestamp: Date.now() - 14400000
    }
  ]);
  const [activeTherapyTrackId, setActiveTherapyTrackId] = useState(null);
  const [activeOscillator, setActiveOscillator] = useState(null);

  // Modal States
  const [showMealModal, setShowMealModal] = useState(false);
  const [showWeightModal, setShowWeightModal] = useState(false);
  const [showVetModal, setShowVetModal] = useState(false);
  const [showVideoCall, setShowVideoCall] = useState(false);

  // Form States
  const [newMealName, setNewMealName] = useState("");
  const [newMealPortion, setNewMealPortion] = useState(100);
  const [newMealCalories, setNewMealCalories] = useState(350);

  const [newWeight, setNewWeight] = useState("");
  const [newWeightNotes, setNewWeightNotes] = useState("");

  const [newClinic, setNewClinic] = useState("Anna Nagar Pet Hospital");
  const [newPurpose, setNewPurpose] = useState("Routine Wellness Exam");

  // Call States
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoOff, setIsVideoOff] = useState(false);
  const [callDuration, setCallDuration] = useState(0);

  // 1. Live Firestore Sync for Feeding Logs
  useEffect(() => {
    try {
      const q = query(collection(db, 'feeding_logs'), orderBy('fedAt', 'desc'), limit(50));
      const unsubscribe = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const cloudMeals = snapshot.docs.map(doc => {
            const data = doc.data();
            const fedAtMs = data.fedAt || Date.now();
            return {
              id: doc.id,
              petId: Number(data.petId) || 1,
              name: data.foodName || data.name || "Logged Food",
              portion: Number(data.portionGrams || data.portion || 100),
              calories: Number(data.calories || 350),
              time: new Date(fedAtMs).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', month: 'short', day: 'numeric' }),
              fedAt: fedAtMs
            };
          });
          setMeals(cloudMeals);
          setSyncConnected(true);
        }
      }, (err) => {
        console.warn("Firestore feeding_logs listener notice:", err.message);
      });
      return () => unsubscribe();
    } catch (e) {
      console.warn("Firestore error:", e);
    }
  }, []);

  // 2. Live Firestore Sync for Weight Records
  useEffect(() => {
    try {
      const q = query(collection(db, 'weight_records'), orderBy('recordedAt', 'asc'), limit(50));
      const unsubscribe = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const cloudWeights = snapshot.docs.map(doc => {
            const data = doc.data();
            const recordedAtMs = data.recordedAt || Date.now();
            return {
              id: doc.id,
              petId: Number(data.petId) || 1,
              weight: parseFloat(data.weightKg || data.weight || 0),
              date: new Date(recordedAtMs).toLocaleDateString([], { month: 'short', day: 'numeric' }),
              notes: data.notes || "Recorded via Cloud Sync",
              recordedAt: recordedAtMs
            };
          });
          setWeights(cloudWeights);
          setSyncConnected(true);
        }
      }, (err) => {
        console.warn("Firestore weight_records listener notice:", err.message);
      });
      return () => unsubscribe();
    } catch (e) {
      console.warn("Firestore error:", e);
    }
  }, []);

  // 3. Live Firestore Sync for Smart Collar Telemetry
  useEffect(() => {
    try {
      const unsubCollar = onSnapshot(doc(db, 'collar_telemetry', 'current'), (docSnap) => {
        if (docSnap.exists()) {
          const data = docSnap.data();
          setCollarTelemetry(prev => ({
            ...prev,
            ...data,
            updatedAt: data.updatedAt || Date.now()
          }));
          setSyncConnected(true);
        }
      }, (err) => {
        console.warn("Collar telemetry listener notice:", err.message);
      });
      return () => unsubCollar();
    } catch (e) {
      console.warn("Firestore collar error:", e);
    }
  }, []);

  // 3b. Dedicated pawlife-server & WebSocket Live Collar Telemetry
  useEffect(() => {
    let isMounted = true;
    fetchServerHealth().then(res => {
      if (isMounted && res && res.status === 'healthy') {
        setServerStatus({
          online: true,
          database: res.database || 'SQLite / Prisma',
          wsActive: false
        });
      }
    });

    const unsubscribeWs = subscribeToTelemetryWebSocket((telemetryMsg) => {
      if (isMounted && telemetryMsg && telemetryMsg.data) {
        const d = telemetryMsg.data;

        if (telemetryMsg.type === 'NEW_WALK') {
          setWalkHistory(prev => [d, ...prev.filter(item => item.id !== d.id)]);
        } else if (telemetryMsg.type === 'NEW_FEEDING') {
          setMeals(prev => [d, ...prev.filter(item => item.id !== d.id)]);
        } else {
          // Collar Telemetry
          setCollarTelemetry(prev => ({
            ...prev,
            heartRateBpm: d.heartRateBpm ?? prev.heartRateBpm,
            stepCount: d.stepCount ?? prev.stepCount,
            batteryLevel: d.batteryLevel ?? prev.batteryLevel,
            temperature: d.temperature ?? prev.temperature,
            latitude: d.latitude ?? prev.latitude,
            longitude: d.longitude ?? prev.longitude,
            updatedAt: Date.now()
          }));
          setServerStatus(s => ({
            ...s,
            online: true,
            wsActive: true
          }));
        }
      }
    });

    return () => {
      isMounted = false;
      if (unsubscribeWs) unsubscribeWs();
    };
  }, []);

  // 4. Live Firestore Sync for Active Walk
  useEffect(() => {
    try {
      const unsubWalk = onSnapshot(doc(db, 'active_walk', 'current'), (docSnap) => {
        if (docSnap.exists()) {
          const data = docSnap.data();
          setActiveWalk(prev => ({
            ...prev,
            ...data,
            updatedAt: data.updatedAt || Date.now()
          }));
          if (data.heartRateBpm) {
            setCollarTelemetry(c => ({
              ...c,
              heartRateBpm: data.heartRateBpm,
              stepCount: data.stepCount || c.stepCount,
              batteryLevel: data.batteryLevel || c.batteryLevel
            }));
          }
          setSyncConnected(true);
        }
      }, (err) => {
        console.warn("Active walk listener notice:", err.message);
      });
      return () => unsubWalk();
    } catch (e) {
      console.warn("Firestore walk error:", e);
    }
  }, []);

  // 5. Live Firestore Sync for Walk History
  useEffect(() => {
    try {
      const q = query(collection(db, 'walk_history'), orderBy('endTime', 'desc'), limit(15));
      const unsubHistory = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const cloudWalks = snapshot.docs.map(d => {
            const data = d.data();
            const dur = Math.round(((data.endTime || 0) - (data.startTime || 0)) / 1000) || 1200;
            return {
              id: d.id,
              petId: Number(data.petId) || 1,
              date: new Date(data.startTime || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', month: 'short', day: 'numeric' }),
              distanceMeters: Math.round(data.distanceMeters || 0),
              durationSeconds: dur,
              calories: Math.round((data.distanceMeters || 0) * 0.05)
            };
          });
          setWalkHistory(cloudWalks);
          setSyncConnected(true);
        }
      }, (err) => {
        console.warn("Walk history listener notice:", err.message);
      });
      return () => unsubHistory();
    } catch (e) {
      console.warn("Firestore history error:", e);
    }
  }, []);

  // 6. Live Firestore Sync for Geofence Status
  useEffect(() => {
    try {
      const unsubGeofence = onSnapshot(doc(db, 'geofence_status', 'current'), (docSnap) => {
        if (docSnap.exists()) {
          const data = docSnap.data();
          setGeofenceStatus(prev => ({ ...prev, ...data }));
        }
      }, (err) => console.warn("Geofence listener notice:", err.message));
      return () => unsubGeofence();
    } catch (_e) {}
  }, []);

  // 7. Live Firestore Sync for Family Tasks
  useEffect(() => {
    try {
      const unsubTasks = onSnapshot(collection(db, 'family_tasks'), (snapshot) => {
        if (!snapshot.empty) {
          const cloudTasks = snapshot.docs.map(d => {
            const data = d.data();
            return {
              id: d.id,
              title: data.title || 'Task',
              time: data.time || 'Today',
              assignee: data.assignee || 'Unassigned',
              isCompleted: data.isCompleted || false
            };
          });
          setFamilyTasks(cloudTasks);
        }
      }, (err) => console.warn("Family tasks listener notice:", err.message));
      return () => unsubTasks();
    } catch (_e) {}
  }, []);

  // 8. Live Firestore Sync for Diagnostic Reports
  useEffect(() => {
    try {
      const q = query(collection(db, 'diagnostic_reports'), orderBy('timestamp', 'desc'), limit(10));
      const unsubReports = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const reports = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
          setAiTriageReports(reports);
        }
      }, (err) => console.warn("Diagnostic reports listener notice:", err.message));
      return () => unsubReports();
    } catch (_e) {}
  }, []);

  // 10. Live Firestore Sync for Vaccinations (Sprint 6)
  useEffect(() => {
    try {
      const q = query(collection(db, 'vaccinations'), orderBy('timestamp', 'desc'), limit(50));
      const unsubVaccines = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const cloudVaxes = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
          setVaccinations(prev => {
            const combined = [...cloudVaxes, ...prev];
            return combined.filter((v, idx, arr) => arr.findIndex(t => t.id === v.id || t.vaccineName === v.vaccineName) === idx);
          });
        }
      }, (err) => console.warn("Vaccinations listener notice:", err.message));
      return () => unsubVaccines();
    } catch (_e) {}
  }, []);

  // 11. Live Firestore Sync for BarkConnect Dog Park Check-Ins (Sprint 7)
  useEffect(() => {
    try {
      const unsubParks = onSnapshot(collection(db, 'park_checkins'), (snapshot) => {
        if (!snapshot.empty) {
          const checkins = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
          // Find if user is checked in
          const myCheckin = checkins.find(c => c.checkedIn && (c.id === 'user_current' || c.petName === activePet.name || c.id === c.parkId));
          if (myCheckin && myCheckin.checkedIn) {
            setUserCheckedInParkId(myCheckin.parkId);
          } else {
            setUserCheckedInParkId(null);
          }

          // Dynamically reflect attendee counts on each Chennai park
          setDogParks(prev => prev.map(park => {
            const liveParkAttendees = checkins.filter(c => c.parkId === park.id && c.checkedIn);
            if (liveParkAttendees.length > 0) {
              const attendees = liveParkAttendees.map((c, i) => ({
                id: c.id || `live_${i}`,
                name: c.petName || 'PawBuddy',
                breed: c.breed || 'Dog',
                age: '2 yrs',
                avatar: '🐕',
                ownerName: c.ownerName || 'Pet Parent'
              }));
              const merged = [...attendees, ...park.activeDogs.filter(d => !attendees.some(a => a.name === d.name))];
              return {
                ...park,
                totalDogsActive: merged.length,
                activeDogs: merged
              };
            }
            return park;
          }));
          setSyncConnected(true);
        }
      }, (err) => console.warn("Park checkins listener notice:", err.message));
      return () => unsubParks();
    } catch (_e) {}
  }, [activePet.name]);

  // 12. Live Firestore Sync for AR Agility Sessions & Trick Mastery (Sprint 8)
  useEffect(() => {
    try {
      const q = query(collection(db, 'ar_agility_sessions'), orderBy('timestamp', 'desc'), limit(20));
      const unsubAgility = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const sessions = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
          setAgilitySessions(sessions);
          setSyncConnected(true);
        }
      }, (err) => console.warn("Agility sessions listener notice:", err.message));
      return () => unsubAgility();
    } catch (_e) {}
  }, []);

  useEffect(() => {
    try {
      const unsubMastery = onSnapshot(doc(db, 'pet_trick_mastery', 'active'), (docSnap) => {
        if (docSnap.exists()) {
          const data = docSnap.data();
          if (data.totalAgilityXp) setAgilityXp(data.totalAgilityXp);
          if (data.masteryLevel) setAgilityMasteryLevel(data.masteryLevel);
        }
      }, (err) => console.warn("Trick mastery listener notice:", err.message));
      return () => unsubMastery();
    } catch (_e) {}
  }, []);

  // 13. Sprint 9: Real-time Ambient Spectrogram Loop & Audio Therapy Synthesizer
  useEffect(() => {
    if (!isMicListening) return;
    const interval = setInterval(() => {
      const baseDb = activeTherapyTrackId ? 38 : 44;
      const jitter = Math.floor(Math.random() * 8) - 3;
      setAmbientDecibels(Math.max(30, Math.min(95, baseDb + jitter)));

      setSpectrogramBars(Array.from({ length: 24 }, (_, i) => {
        const factor = Math.sin((i / 24) * Math.PI);
        const noise = Math.random() * 0.35;
        return Math.min(1.0, Math.max(0.1, factor * 0.65 + noise));
      }));
    }, 140);
    return () => clearInterval(interval);
  }, [isMicListening, activeTherapyTrackId]);

  // 14. Sprint 9: Live Firestore Sync for PawAcoustics AI events
  useEffect(() => {
    try {
      const q = query(collection(db, 'acoustic_mood_events'), orderBy('timestamp', 'desc'), limit(20));
      const unsub = onSnapshot(q, (snapshot) => {
        if (!snapshot.empty) {
          const events = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
          setAcousticEvents(events);
          if (events.length > 0) {
            setLatestAcousticEpisode(events[0]);
          }
          setSyncConnected(true);
        }
      }, (err) => console.warn("Acoustic events notice:", err.message));
      return () => unsub();
    } catch (_e) {}
  }, []);

  // Web Audio Calming Therapy Synthesizer
  const playTherapyTone = (track) => {
    stopTherapyTone();
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(track.freq, ctx.currentTime);
      gain.gain.setValueAtTime(0.12, ctx.currentTime);

      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();

      setActiveTherapyTrackId(track.id);
      setActiveOscillator({ ctx, osc, gain });
    } catch (e) {
      console.warn("Audio therapy error:", e);
    }
  };

  const stopTherapyTone = () => {
    if (activeOscillator) {
      try {
        activeOscillator.osc.stop();
        activeOscillator.ctx.close();
      } catch (_e) {}
      setActiveOscillator(null);
    }
    setActiveTherapyTrackId(null);
  };

  const handleSimulateBark = async (moodKey) => {
    const moods = {
      ALERT: {
        label: 'Alert & Territorial',
        emoji: '🚨',
        pitch: 340,
        bpm: 116,
        stress: 78,
        db: 84,
        advice: 'Pet detects something outside. Redirect focus calmly with positive reinforcement.'
      },
      ANXIETY: {
        label: 'Separation Anxiety',
        emoji: '🥺',
        pitch: 740,
        bpm: 108,
        stress: 82,
        db: 70,
        advice: 'Pet feels isolated or stressed. Activating anti-anxiety sound therapy.'
      },
      PLAYFUL: {
        label: 'Playful & Joyful',
        emoji: '🎾',
        pitch: 540,
        bpm: 96,
        stress: 28,
        db: 76,
        advice: 'High excitement! Engage in interactive play, fetch, or agility training.'
      },
      PAIN: {
        label: 'Pain / Distress',
        emoji: '🩹',
        pitch: 820,
        bpm: 124,
        stress: 95,
        db: 86,
        advice: 'Potential physical discomfort detected. Check paws/joints and consult tele-vet.'
      }
    };
    const m = moods[moodKey] || moods.ALERT;
    setAmbientDecibels(m.db);
    setSpectrogramBars(Array.from({ length: 24 }, () => 0.6 + Math.random() * 0.4));

    const newEp = {
      id: `ep-${Date.now()}`,
      petName: activePet.name || 'Max',
      moodState: moodKey,
      moodLabel: m.label,
      emoji: m.emoji,
      confidenceScore: +(0.92 + Math.random() * 0.06).toFixed(2),
      peakDecibels: m.db,
      fundamentalPitchHz: m.pitch,
      collarHeartRateBpm: m.bpm,
      stressIndex: m.stress,
      summary: `Classified as ${m.label}. ${m.advice}`,
      timestamp: Date.now()
    };

    setLatestAcousticEpisode(newEp);
    setAcousticEvents(prev => [newEp, ...prev.slice(0, 15)]);

    if (moodKey === 'ANXIETY') {
      playTherapyTone({ id: 'delta', title: 'Delta-Wave 432Hz', freq: 432 });
    }

    try {
      await setDoc(doc(db, 'acoustic_mood_events', newEp.id), newEp);
    } catch (_e) {}
  };

  // Web Audio Clicker Synthesizer
  const playWebClickerSound = () => {
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(1400, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(750, ctx.currentTime + 0.04);
      gain.gain.setValueAtTime(0.4, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.04);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 0.045);
    } catch (e) {
      console.warn("Web audio error:", e);
    }
  };

  const handleMarkClicker = async () => {
    playWebClickerSound();
    setIsClickingAnim(true);
    setTimeout(() => setIsClickingAnim(false), 300);
    setClickerCount(c => c + 1);

    const updatedReps = Math.min(activeTrick.repsTarget, activeTrick.repsCompleted + 1);
    const addedXp = updatedReps >= activeTrick.repsTarget ? activeTrick.xpReward : 5;
    const newTotalXp = agilityXp + addedXp;
    setAgilityXp(newTotalXp);

    const updatedActiveTrick = { ...activeTrick, repsCompleted: updatedReps };
    setActiveTrick(updatedActiveTrick);

    setTrickCurriculum(prev => prev.map(t => {
      if (t.id === activeTrick.id) {
        const nextReps = Math.min(t.targetReps, t.reps + 1);
        return { ...t, reps: nextReps, mastered: nextReps >= t.targetReps };
      }
      return t;
    }));

    try {
      await setDoc(doc(db, 'pet_trick_mastery', 'active'), {
        petId: activePetId,
        petName: activePet.name,
        totalAgilityXp: newTotalXp,
        masteryLevel: newTotalXp > 800 ? "Grand Champion" : newTotalXp > 400 ? "Park Prodigy" : "Novice Agility",
        lastTrickPracticed: activeTrick.name,
        updatedAt: Date.now()
      }, { merge: true });
    } catch (err) {
      console.warn("Error updating trick mastery:", err);
    }
  };

  const handleToggleObstacle = (id) => {
    playWebClickerSound();
    setPlacedObstacles(prev => prev.map(obs => {
      if (obs.id === id) {
        const nowCleared = !obs.cleared;
        if (nowCleared) {
          setAgilityXp(xp => xp + 20);
        }
        return { ...obs, cleared: nowCleared };
      }
      return obs;
    }));
  };

  const handleAddObstacle = (type) => {
    const obstacleMeta = {
      HURDLE: { label: 'Hurdle Leap', icon: '🚧' },
      CONE: { label: 'Slalom Cones', icon: '🔻' },
      TUNNEL: { label: 'Agility Tunnel', icon: '🌀' },
      HOOP: { label: 'Tire Hoop', icon: '⭕' }
    }[type] || { label: 'Obstacle', icon: '🚩' };

    const newObs = {
      id: `obs-${Date.now()}`,
      type,
      label: obstacleMeta.label,
      icon: obstacleMeta.icon,
      x: Math.floor(20 + Math.random() * 60),
      y: Math.floor(20 + Math.random() * 60),
      cleared: false
    };
    setPlacedObstacles(prev => [...prev, newObs]);
  };

  const handleResetCourse = () => {
    setPlacedObstacles(prev => prev.map(o => ({ ...o, cleared: false })));
  };

  const handleSaveAgilitySession = async () => {
    const clearedCount = placedObstacles.filter(o => o.cleared).length;
    const sessionDoc = {
      petId: activePetId,
      petName: activePet.name,
      obstacleCourse: `${placedObstacles.length}-Obstacle Custom AR Arena`,
      durationSec: 240,
      obstaclesCleared: clearedCount,
      xpEarned: clearedCount * 25 + 30,
      timestamp: Date.now()
    };
    try {
      const ref = await addDoc(collection(db, 'ar_agility_sessions'), sessionDoc);
      setAgilitySessions(prev => [{ id: ref.id, ...sessionDoc }, ...prev]);
      alert("Agility training session recorded and synced to mobile PawLife app!");
    } catch (err) {
      console.warn("Error saving session:", err);
      setAgilitySessions(prev => [{ id: `s_${Date.now()}`, ...sessionDoc }, ...prev]);
    }
  };

  const handleAddVaccine = async (e) => {
    e.preventDefault();
    if (!newVaccineName.trim()) return;
    const now = Date.now();
    const newRecord = {
      id: `v_${now}`,
      petId: activePetId,
      vaccineName: newVaccineName.trim(),
      category: newVaccineCategory || "Core Canine",
      administeredDate: "Today",
      expiryDate: newVaccineExpiry || "1 Year Valid",
      batchLotNumber: newVaccineLot || "LOT-2026-X8",
      clinicName: newVaccineClinic || "Anna Nagar Pet Hospital",
      doctorName: "Dr. Priya Sharma, BVSc",
      status: "VALID",
      timestamp: now
    };
    setVaccinations(prev => [newRecord, ...prev]);
    setShowAddVaccineModal(false);
    setNewVaccineName("");

    try {
      await setDoc(doc(db, 'vaccinations', newRecord.id), newRecord);
      setSyncConnected(true);
    } catch (err) {
      console.warn("Vaccine sync notice:", err);
    }
  };

  // 9. Live Web Simulation Runner (for manual test / demonstration)
  useEffect(() => {
    let interval;
    if (simulatingWalk) {
      interval = setInterval(() => {
        setActiveWalk(w => {
          const nextDur = (w.durationSeconds || 0) + 2;
          const nextDist = (w.distanceMeters || 0) + 5;
          const nextLat = (w.latitude || 13.0827) + 0.00005;
          const nextLng = (w.longitude || 80.2707) + 0.00007;
          const newPoint = `${nextLat.toFixed(5)},${nextLng.toFixed(5)}`;
          const pts = [...(w.routePoints || []), newPoint].slice(-40);
          
          return {
            ...w,
            isActive: true,
            durationSeconds: nextDur,
            distanceMeters: nextDist,
            latitude: nextLat,
            longitude: nextLng,
            routePoints: pts,
            updatedAt: Date.now()
          };
        });

        setCollarTelemetry(c => {
          const nextSteps = (c.stepCount || 1420) + 4;
          const nextBpm = 112 + Math.floor(Math.random() * 16);
          return {
            ...c,
            isConnected: true,
            stepCount: nextSteps,
            heartRateBpm: nextBpm,
            updatedAt: Date.now()
          };
        });
      }, 2000);
    }
    return () => clearInterval(interval);
  }, [simulatingWalk]);

  // Cache to LocalStorage
  useEffect(() => {
    localStorage.setItem('pawlife_pets', JSON.stringify(pets));
    localStorage.setItem('pawlife_meals', JSON.stringify(meals));
    localStorage.setItem('pawlife_appts', JSON.stringify(appointments));
    localStorage.setItem('pawlife_weights', JSON.stringify(weights));
    localStorage.setItem('pawlife_collar', JSON.stringify(collarTelemetry));
    localStorage.setItem('pawlife_walk', JSON.stringify(activeWalk));
  }, [pets, meals, appointments, weights, collarTelemetry, activeWalk]);

  useEffect(() => {
    let timer;
    if (showVideoCall) {
      timer = setInterval(() => setCallDuration(c => c + 1), 1000);
    } else {
      setCallDuration(0);
    }
    return () => clearInterval(timer);
  }, [showVideoCall]);

  const petMeals = meals.filter(m => m.petId === activePetId);
  const petWeights = weights.filter(w => w.petId === activePetId);
  const petAppts = appointments.filter(a => a.petId === activePetId);

  const handleLogMeal = async (e) => {
    e.preventDefault();
    if (!newMealName) return;
    const now = Date.now();
    const item = {
      id: `m_${now}`,
      petId: activePetId,
      name: newMealName,
      portion: Number(newMealPortion),
      calories: Number(newMealCalories),
      time: "Just now",
      fedAt: now
    };
    // Optimistic UI update
    setMeals([item, ...meals]);
    setNewMealName("");
    setShowMealModal(false);

    // Sync to Cloud Firestore
    try {
      await addDoc(collection(db, 'feeding_logs'), {
        petId: activePetId,
        foodName: newMealName,
        portionGrams: Number(newMealPortion),
        calories: Number(newMealCalories),
        fedAt: now,
        notes: "Logged from Web Dashboard",
        createdAt: now
      });
      setSyncConnected(true);
    } catch (err) {
      console.warn("Cloud meal upload notice:", err.message);
    }
  };

  const handleLogWeight = async (e) => {
    e.preventDefault();
    const val = parseFloat(newWeight);
    if (!val || val <= 0) return;
    const now = Date.now();
    const entry = {
      id: `w_${now}`,
      petId: activePetId,
      weight: val,
      date: "Today",
      notes: newWeightNotes || "Routine weight check",
      recordedAt: now
    };
    // Optimistic UI update
    setWeights([...weights, entry]);
    setPets(pets.map(p => p.id === activePetId ? { ...p, weight: val } : p));
    setNewWeight("");
    setNewWeightNotes("");
    setShowWeightModal(false);

    // Sync to Cloud Firestore
    try {
      await addDoc(collection(db, 'weight_records'), {
        petId: activePetId,
        weightKg: val,
        recordedAt: now,
        notes: newWeightNotes || "Logged from Web Dashboard"
      });
      setSyncConnected(true);
    } catch (err) {
      console.warn("Cloud weight upload notice:", err.message);
    }
  };

  const handleBookAppointment = async (e) => {
    e.preventDefault();
    const now = Date.now();
    const appt = {
      id: `a_${now}`,
      petId: activePetId,
      clinic: newClinic,
      purpose: newPurpose,
      date: "In 2 Days, 11:00 AM",
      status: "Confirmed",
      doctor: "Dr. Priya Sharma"
    };
    setAppointments([appt, ...appointments]);
    setShowVetModal(false);

    try {
      await addDoc(collection(db, 'vet_appointments'), {
        petId: activePetId,
        clinicName: newClinic,
        purpose: newPurpose,
        appointmentTime: now + 172800000,
        doctor: "Dr. Priya Sharma",
        status: "Confirmed"
      });
    } catch (err) {
      console.warn("Cloud appointment notice:", err.message);
    }
  };

  // Sprint 4 Handlers: AI Health Triage
  const handleRunAiTriage = async (symptomOverride) => {
    const text = symptomOverride || selectedSymptom;
    if (!text) return;
    setIsAnalyzingAi(true);

    setTimeout(async () => {
      let urgency = "MODERATE";
      let findings = "Mild localized inflammation or allergic response observed. Clean the affected area with veterinary antiseptic solution; schedule a routine clinical exam if symptoms persist > 48h.";
      const lower = text.toLowerCase();
      if (lower.includes("bleed") || lower.includes("seizure") || lower.includes("toxic") || lower.includes("vomit") || lower.includes("breath")) {
        urgency = "EMERGENCY";
        findings = "Critical respiratory distress or acute toxic ingestion risk detected. Vitals require immediate stabilization. Transport pet immediately to nearest emergency animal hospital.";
      } else if (lower.includes("itch") || lower.includes("rash") || lower.includes("clean") || lower.includes("wax") || lower.includes("mild")) {
        urgency = "MILD";
        findings = "Benign superficial irritation or dry skin. Auricular canal and dermis appear intact. Continue regular monitoring and wipe with veterinary antiseptic wipes.";
      }

      const reportObj = {
        petName: activePet.name,
        symptoms: text,
        findings,
        urgency,
        timestamp: Date.now()
      };

      try {
        const docRef = await addDoc(collection(db, 'diagnostic_reports'), reportObj);
        reportObj.id = docRef.id;
      } catch (e) {
        reportObj.id = 'r_' + Date.now();
      }

      setAiTriageReports(prev => [reportObj, ...prev]);
      setCurrentAiReport(reportObj);
      setIsAnalyzingAi(false);
    }, 1000);
  };

  const handleOpenVetReport = (report) => {
    setCurrentAiReport(report || aiTriageReports[0] || {
      petName: activePet.name,
      symptoms: selectedSymptom,
      findings: "Physical exam baseline: Normal cardiac rhythm, active reflexes, hydrated mucous membranes.",
      urgency: "MILD",
      timestamp: Date.now()
    });
    setShowVetReportModal(true);
  };

  // Sprint 4 Handlers: Family Hub Co-Parenting
  const handleToggleFamilyTask = async (task) => {
    const newStatus = !task.isCompleted;
    setFamilyTasks(prev => prev.map(t => t.id === task.id ? { ...t, isCompleted: newStatus } : t));
    try {
      await setDoc(doc(db, 'family_tasks', task.id), {
        title: task.title,
        time: task.time,
        assignee: task.assignee,
        isCompleted: newStatus,
        updatedAt: Date.now()
      }, { merge: true });
    } catch (e) {
      console.warn("Family task update notice:", e);
    }
  };

  const handleAddFamilyTask = async (e) => {
    e.preventDefault();
    if (!newChoreTitle.trim()) return;
    const newTask = {
      title: newChoreTitle.trim(),
      assignee: newChoreAssignee || 'You',
      time: 'Today',
      isCompleted: false,
      createdAt: Date.now()
    };
    try {
      const docRef = await addDoc(collection(db, 'family_tasks'), newTask);
      newTask.id = docRef.id;
    } catch (err) {
      newTask.id = 't_' + Date.now();
    }
    setFamilyTasks(prev => [...prev, newTask]);
    setNewChoreTitle('');
  };

  // Sprint 4 Handlers: Store & Smart Pantry
  const handleAddToCart = (item) => {
    setCart(prev => {
      const existing = prev.find(i => i.id === item.id);
      if (existing) {
        return prev.map(i => i.id === item.id ? { ...i, qty: i.qty + 1 } : i);
      }
      return [...prev, { ...item, qty: 1 }];
    });
    setShowCartModal(true);
  };

  const handleApplyPromo = (e) => {
    e.preventDefault();
    if (promoCode.trim().toUpperCase() === 'PAWFIRST' || promoCode.trim().toUpperCase() === 'SAVE20') {
      setDiscountPercent(20);
    } else {
      alert("Invalid code. Try 'PAWFIRST' for 20% discount!");
    }
  };

  const handleCheckout = () => {
    setPantryLevel(prev => ({
      ...prev,
      remainingGrams: prev.totalCapacityGrams,
      daysLeft: 24
    }));
    setOrderSuccess(true);
    setCart([]);
    setTimeout(() => {
      setOrderSuccess(false);
      setShowCartModal(false);
    }, 2000);
  };

  const handleSimulateGeofence = async (breached) => {
    const updated = {
      isBreached: breached,
      distanceFromHome: breached ? 192 : 36,
      safeRadiusMeters: 150,
      latitude: breached ? 13.0855 : 13.0827,
      longitude: breached ? 80.2735 : 80.2707,
      updatedAt: Date.now()
    };
    setGeofenceStatus(updated);
    try {
      await setDoc(doc(db, 'geofence_status', 'current'), updated);
    } catch (err) {
      console.warn("Geofence update notice:", err);
    }
  };

  // Sprint 5 Handlers: AI Vision Food Scanner
  const handleRunAiFoodScan = (preset) => {
    setIsScanningFood(true);
    setTimeout(() => {
      const foodPresets = [
        {
          name: "Royal Canin Golden Retriever Adult Kibble",
          category: "Dry Extruded Kibble",
          kcalPer100g: 365,
          proteinPercent: 26,
          fatPercent: 14,
          icon: "🍲"
        },
        {
          name: "Pedigree Tender Chicken Gravy Wet Food",
          category: "Canned Gravy Loaf",
          kcalPer100g: 115,
          proteinPercent: 9.5,
          fatPercent: 5.0,
          icon: "🥣"
        },
        {
          name: "Fresh Lean Beef & Sweet Potato",
          category: "Raw / Cooked Fresh Meal",
          kcalPer100g: 175,
          proteinPercent: 21,
          fatPercent: 8.5,
          icon: "🥩"
        },
        {
          name: "Wild Alaskan Salmon & Pumpkin Puree",
          category: "Nutritional Digestive Topper",
          kcalPer100g: 95,
          proteinPercent: 8,
          fatPercent: 4,
          icon: "🐟"
        }
      ];
      const selected = preset || foodPresets[Math.floor(Math.random() * foodPresets.length)];
      setAiFoodState(prev => ({
        ...prev,
        ...selected,
        confidencePercent: 92 + Math.floor(Math.random() * 7)
      }));
      setIsScanningFood(false);
    }, 1200);
  };

  const handleLogAiFood = async () => {
    const calories = Math.round((aiFoodState.portionGrams * aiFoodState.kcalPer100g) / 100);
    const now = Date.now();
    const item = {
      id: `m_${now}`,
      petId: activePetId,
      name: `${aiFoodState.name} (AI Vision)`,
      portion: Number(aiFoodState.portionGrams),
      calories: calories,
      time: "Just now",
      fedAt: now
    };

    setMeals([item, ...meals]);
    setPantryLevel(prev => ({
      ...prev,
      remainingGrams: Math.max(0, prev.remainingGrams - Number(aiFoodState.portionGrams))
    }));
    setShowAiFoodModal(false);

    try {
      await addDoc(collection(db, 'feeding_logs'), {
        petId: activePetId,
        foodName: aiFoodState.name,
        portionGrams: Number(aiFoodState.portionGrams),
        calories: calories,
        fedAt: now,
        notes: `AI Vision Scan: ${aiFoodState.category} (${aiFoodState.confidencePercent}% confidence)`,
        createdAt: now
      });
      setSyncConnected(true);
    } catch (err) {
      console.warn("AI Food log sync notice:", err.message);
    }
  };

  // Sprint 7 Handlers: BarkConnect Dog Parks & Social
  const handleToggleParkCheckIn = async (parkId) => {
    const isCurrentlyCheckedIn = userCheckedInParkId === parkId;
    const nextStatus = !isCurrentlyCheckedIn;
    const targetParkId = nextStatus ? parkId : null;
    setUserCheckedInParkId(targetParkId);

    try {
      // Key document by parkId to match Android CommunityViewModel structure
      await setDoc(doc(db, 'park_checkins', parkId), {
        parkId: parkId,
        checkedIn: nextStatus,
        petName: activePet.name,
        breed: activePet.breed,
        ownerName: "You (Shoban)",
        timestamp: Date.now()
      }, { merge: true });

      // Also set user_current
      await setDoc(doc(db, 'park_checkins', 'user_current'), {
        parkId: parkId,
        checkedIn: nextStatus,
        petName: activePet.name,
        breed: activePet.breed,
        ownerName: "You (Shoban)",
        timestamp: Date.now()
      }, { merge: true });
      setSyncConnected(true);
    } catch (e) {
      console.warn("Park check-in Firestore sync notice:", e);
    }
  };

  const handleSendPlaydateInvite = (candidate) => {
    setInvitedPlaydates(prev => ({ ...prev, [candidate.id]: true }));
    alert(`🎉 Playdate meetup invitation sent to ${candidate.ownerName} & ${candidate.name}! You'll receive a confirmation notice.`);
  };

  const handleTogglePostLike = (postId) => {
    setCommunityPosts(prev => prev.map(p => {
      if (p.id === postId) {
        return {
          ...p,
          isLiked: !p.isLiked,
          likesCount: p.isLiked ? p.likesCount - 1 : p.likesCount + 1
        };
      }
      return p;
    }));
  };

  const handleCreateCommunityPost = async (e) => {
    e.preventDefault();
    if (!newPostContent.trim()) return;
    const now = Date.now();
    const newPost = {
      authorName: `You & ${activePet.name}`,
      authorRole: `${activePet.breed} Parent • Chennai`,
      content: newPostContent.trim(),
      likesCount: 1,
      isLiked: true,
      timestamp: now,
      badge: "Community Post",
      location: newPostLocation
    };
    try {
      const docRef = await addDoc(collection(db, 'community_posts'), newPost);
      newPost.id = docRef.id;
      setSyncConnected(true);
    } catch (e) {
      newPost.id = 'cp_' + now;
    }
    setCommunityPosts(prev => [newPost, ...prev]);
    setNewPostContent('');
    setShowNewPostModal(false);
  };

  if (isPublicPassportView) {
    return (
      <div style={{ minHeight: '100vh', background: '#0B0F19', color: '#F8FAFC', padding: '40px 20px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ maxWidth: '680px', width: '100%' }}>
          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Sparkles size={22} color="#000" />
              </div>
              <div>
                <h2 style={{ margin: 0, fontSize: '20px' }}>PawLife VetPass Portal</h2>
                <div style={{ fontSize: '12px', color: 'var(--color-primary)', fontWeight: 700 }}>✓ OFFICIAL PUBLIC HEALTH CREDENTIAL</div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button 
                onClick={() => window.print()}
                style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'rgba(255,255,255,0.1)', color: '#FFF', border: '1px solid rgba(255,255,255,0.2)', padding: '8px 14px', borderRadius: '10px', fontSize: '13px', cursor: 'pointer', fontWeight: 600 }}
              >
                <Printer size={16} />
                <span>Print Pass</span>
              </button>
              <button 
                onClick={() => setIsPublicPassportView(false)}
                style={{ background: 'var(--color-primary)', color: '#000', border: 'none', padding: '8px 16px', borderRadius: '10px', fontSize: '13px', cursor: 'pointer', fontWeight: 700 }}
              >
                Open Dashboard
              </button>
            </div>
          </div>

          {/* Official Document Card */}
          <div className="glass-panel" style={{ padding: '32px', border: '2px solid rgba(245, 158, 11, 0.5)', background: 'linear-gradient(180deg, #1E293B, #0F172A)', borderRadius: '24px', boxShadow: '0 24px 60px rgba(0,0,0,0.6)' }}>
            {/* Top Seal */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '20px', marginBottom: '24px' }}>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#F59E0B', letterSpacing: '1.5px' }}>OFFICIAL VETERINARY HEALTH PASSPORT</span>
                <h1 style={{ fontSize: '28px', margin: '4px 0 0', color: '#FFF' }}>{petPassport.petName}</h1>
                <p style={{ margin: '2px 0 0', color: 'var(--color-text-secondary)', fontSize: '14px' }}>
                  {petPassport.breed} • {petPassport.species} • {petPassport.gender}
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: 'var(--color-primary)', padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: 800 }}>
                  ✓ FULL IMMUNITY VERIFIED
                </span>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '6px', fontFamily: 'monospace' }}>
                  ID: {petPassport.passportId}
                </div>
              </div>
            </div>

            {/* Transponder & Registry */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', background: 'rgba(0,0,0,0.3)', padding: '16px', borderRadius: '14px', marginBottom: '24px' }}>
              <div>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>Microchip Transponder (ISO 11784)</div>
                <div style={{ fontSize: '15px', fontWeight: 700, color: '#FFF', fontFamily: 'monospace', letterSpacing: '1px', marginTop: '2px' }}>
                  {petPassport.microchipId}
                </div>
              </div>
              <div>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>Rabies Certificate Number</div>
                <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--color-primary)', fontFamily: 'monospace', marginTop: '2px' }}>
                  {petPassport.rabiesCertificateNo}
                </div>
              </div>
            </div>

            {/* Guardian Info */}
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 16px', background: 'rgba(255,255,255,0.03)', borderRadius: '12px', marginBottom: '28px', fontSize: '13px' }}>
              <div>
                <span style={{ color: 'var(--color-text-secondary)' }}>Guardian / Owner: </span>
                <strong style={{ color: '#FFF' }}>{petPassport.guardianName}</strong>
              </div>
              <div>
                <span style={{ color: 'var(--color-text-secondary)' }}>Emergency Contact: </span>
                <strong style={{ color: 'var(--color-primary)' }}>{petPassport.guardianContact}</strong>
              </div>
            </div>

            {/* Immunization History Table */}
            <h3 style={{ fontSize: '18px', margin: '0 0 16px' }}>Verified Immunization Ledger</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '28px' }}>
              {vaccinations.map(v => (
                <div key={v.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', background: 'rgba(255, 255, 255, 0.04)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.06)' }}>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '14px', color: '#FFF' }}>{v.vaccineName}</div>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                      Lot: {v.batchLotNumber} • Provider: {v.clinicName}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: 'var(--color-primary)', padding: '2px 8px', borderRadius: '8px', fontSize: '11px', fontWeight: 700 }}>
                      Valid Till {v.expiryDate}
                    </span>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                      Administered: {v.administeredDate}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Official Stamp */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '18px', fontSize: '12px', color: 'var(--color-text-secondary)' }}>
              <div>
                <div>Issued by: Anna Nagar Veterinary Hospital Network</div>
                <div>Registry: Kennel Club of India & PawLife Global</div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontWeight: 700, color: '#FFF' }}>Dr. Priya Sharma, BVSc</div>
                <div style={{ color: 'var(--color-primary)', fontStyle: 'italic' }}>✓ Cryptographically Verified Credential</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // If user is not authenticated, render the AuthScreen
  if (!currentUser) {
    return <AuthScreen onLogin={(user) => setCurrentUser(user)} />;
  }

  return (
    <div style={{ minHeight: '100vh', background: 'var(--color-bg-dark)', color: 'var(--color-text-primary)' }}>
      {/* Top Navbar */}
      <header style={{
        position: 'sticky',
        top: 0,
        zIndex: 50,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '16px 36px',
        background: 'rgba(11, 15, 25, 0.85)',
        backdropFilter: 'blur(16px)',
        borderBottom: '1px solid var(--color-card-border)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            background: 'var(--color-primary)',
            borderRadius: '12px',
            width: '42px',
            height: '42px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Sparkles color="#000" size={24} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 style={{ fontSize: '22px', margin: 0, letterSpacing: '-0.03em' }}>PawLife</h2>
              <span style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '5px',
                fontSize: '11px',
                fontWeight: 700,
                padding: '3px 8px',
                borderRadius: '8px',
                background: syncConnected ? 'rgba(16, 185, 129, 0.2)' : 'rgba(251, 191, 36, 0.2)',
                color: syncConnected ? 'var(--color-primary)' : 'var(--color-warning)',
                border: `1px solid ${syncConnected ? 'rgba(16, 185, 129, 0.4)' : 'rgba(251, 191, 36, 0.4)'}`
              }}>
                <span style={{
                  width: '6px',
                  height: '6px',
                  borderRadius: '50%',
                  background: syncConnected ? 'var(--color-primary)' : 'var(--color-warning)',
                  animation: 'pulse 1.5s infinite'
                }} />
                {syncConnected ? 'LIVE CLOUD SYNC ACTIVE' : 'CLOUD READY'}
              </span>
            </div>
            <p style={{ margin: 0, fontSize: '12px', color: 'var(--color-text-secondary)' }}>Connected with your mobile phone app</p>
          </div>
        </div>

        {/* Pet Switcher Bar */}
        <div style={{ display: 'flex', gap: '8px', background: 'rgba(255, 255, 255, 0.05)', padding: '6px', borderRadius: '16px', border: '1px solid var(--color-card-border)' }}>
          {pets.map(pet => (
            <button
              key={pet.id}
              onClick={() => setActivePetId(pet.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 16px',
                borderRadius: '12px',
                background: activePetId === pet.id ? 'var(--color-primary)' : 'transparent',
                color: activePetId === pet.id ? '#000' : 'var(--color-text-secondary)',
                fontWeight: activePetId === pet.id ? 700 : 500,
                border: 'none',
                cursor: 'pointer',
                transition: 'all 0.2s ease'
              }}
            >
              <span>{pet.avatar}</span>
              <span>{pet.name}</span>
            </button>
          ))}
        </div>

        {/* Quick Actions & Live Tele-Vet */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {/* Dedicated Backend & DB Live Indicator */}
          <div 
            title={`Database: ${serverStatus.database} | REST API: http://localhost:4000/api | WS: ws://localhost:4000`}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              background: serverStatus.online ? 'rgba(16, 185, 129, 0.12)' : 'rgba(239, 68, 68, 0.12)',
              border: `1px solid ${serverStatus.online ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
              padding: '7px 14px',
              borderRadius: '12px',
              fontSize: '12px',
              color: serverStatus.online ? '#34D399' : '#F87171',
              fontWeight: 600,
              cursor: 'default'
            }}
          >
            <span style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              background: serverStatus.online ? '#10B981' : '#EF4444',
              display: 'inline-block',
              boxShadow: serverStatus.online ? '0 0 10px #10B981' : 'none'
            }} />
            <span>{serverStatus.online ? 'Server & SQLite Active' : 'Server Offline'}</span>
            {serverStatus.wsActive && (
              <span style={{
                fontSize: '10px',
                background: 'rgba(16, 185, 129, 0.25)',
                color: '#6EE7B7',
                padding: '2px 6px',
                borderRadius: '6px',
                fontWeight: 700,
                letterSpacing: '0.5px'
              }}>
                WS IOT
              </span>
            )}
          </div>

          <button 
            onClick={() => setShowVideoCall(true)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              background: 'linear-gradient(135deg, #0EA5E9, #0284C7)',
              color: '#FFF',
              border: 'none',
              borderRadius: '12px',
              padding: '10px 18px',
              fontWeight: 600,
              cursor: 'pointer',
              boxShadow: '0 4px 14px rgba(14, 165, 233, 0.3)'
            }}
          >
            <Video size={18} />
            <span>Tele-Vet Live</span>
          </button>
          {/* User Profile Badge & Dropdown */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setShowProfileMenu(!showProfileMenu)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                background: 'rgba(255, 255, 255, 0.05)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                padding: '6px 14px 6px 8px',
                borderRadius: '16px',
                cursor: 'pointer',
                color: '#FFF',
                transition: 'all 0.2s ease'
              }}
            >
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: 'rgba(16, 185, 129, 0.2)',
                border: '2px solid var(--color-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '18px',
                overflow: 'hidden'
              }}>
                {currentUser?.photoURL ? (
                  <img src={currentUser.photoURL} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  currentUser?.avatar || '🐕'
                )}
              </div>
              <div style={{ textAlign: 'left' }}>
                <div style={{ fontSize: '13px', fontWeight: 700, color: '#FFF', lineHeight: 1.2 }}>
                  {currentUser?.displayName || 'Shoban'}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--color-primary)', fontWeight: 600 }}>
                  {currentUser?.role || 'Pet Parent'}
                </div>
              </div>
              <ChevronDown size={14} color="#94A3B8" />
            </button>

            {/* Profile Dropdown Menu */}
            {showProfileMenu && (
              <div style={{
                position: 'absolute',
                top: '52px',
                right: 0,
                width: '280px',
                background: 'rgba(15, 23, 42, 0.96)',
                backdropFilter: 'blur(20px)',
                WebkitBackdropFilter: 'blur(20px)',
                border: '1px solid rgba(255, 255, 255, 0.12)',
                borderRadius: '18px',
                padding: '16px',
                boxShadow: '0 16px 40px rgba(0, 0, 0, 0.6), 0 0 20px rgba(16, 185, 129, 0.15)',
                zIndex: 100,
                display: 'flex',
                flexDirection: 'column',
                gap: '12px'
              }}>
                {/* User Details */}
                <div style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                      width: '42px',
                      height: '42px',
                      borderRadius: '50%',
                      background: 'rgba(16, 185, 129, 0.2)',
                      border: '2px solid var(--color-primary)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '22px',
                      overflow: 'hidden',
                      flexShrink: 0
                    }}>
                      {currentUser?.photoURL ? (
                        <img src={currentUser.photoURL} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      ) : (
                        currentUser?.avatar || '🐕'
                      )}
                    </div>
                    <div>
                      <div style={{ fontWeight: 800, fontSize: '14px', color: '#FFF' }}>
                        {currentUser?.displayName}
                      </div>
                      <div style={{ fontSize: '12px', color: '#94A3B8' }}>
                        {currentUser?.email}
                      </div>
                    </div>
                  </div>
                  <div style={{
                    marginTop: '8px',
                    display: 'inline-block',
                    background: 'rgba(16, 185, 129, 0.15)',
                    color: 'var(--color-primary)',
                    fontSize: '11px',
                    fontWeight: 700,
                    padding: '2px 8px',
                    borderRadius: '8px'
                  }}>
                    ✓ {currentUser?.badge || 'Authenticated'}
                  </div>
                </div>

                {/* Quick Persona Switcher inside dropdown */}
                <div>
                  <div style={{ fontSize: '11px', fontWeight: 800, color: '#94A3B8', textTransform: 'uppercase', marginBottom: '8px', letterSpacing: '0.5px' }}>
                    Switch Demo Persona
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    {[
                      { id: 'shoban', name: 'Shoban', role: 'Primary Pet Parent', avatar: '🐕', email: 'shoban@pawlife.app', badge: 'Owner of Max & Rocky', color: '#10B981' },
                      { id: 'priya', name: 'Dr. Priya Sharma', role: 'Attending Veterinarian', avatar: '🩺', email: 'vet.priya@pawlife.app', badge: 'BVSc • Anna Nagar Pet Hospital', color: '#0EA5E9' },
                      { id: 'sarah', name: 'Sarah', role: 'Family Co-Parent', avatar: '👨‍👩‍👧', email: 'sarah@pawlife.app', badge: 'Dog Walker & Family Hub', color: '#F59E0B' }
                    ].filter(p => p.email !== currentUser?.email).map(p => (
                      <button
                        key={p.id}
                        onClick={() => handleSwitchPersona(p)}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          padding: '8px 10px',
                          borderRadius: '8px',
                          background: 'rgba(255, 255, 255, 0.04)',
                          border: 'none',
                          color: '#FFF',
                          cursor: 'pointer',
                          fontSize: '12px',
                          textAlign: 'left'
                        }}
                      >
                        <span style={{ fontSize: '16px' }}>{p.avatar}</span>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontWeight: 600 }}>{p.name}</div>
                          <div style={{ fontSize: '10px', color: '#94A3B8' }}>{p.role}</div>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Sign Out Button */}
                <div style={{ borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '10px' }}>
                  <button
                    onClick={handleLogout}
                    style={{
                      width: '100%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                      padding: '10px',
                      borderRadius: '10px',
                      border: '1px solid rgba(239, 68, 68, 0.3)',
                      background: 'rgba(239, 68, 68, 0.12)',
                      color: '#F87171',
                      cursor: 'pointer',
                      fontWeight: 700,
                      fontSize: '13px',
                      transition: 'all 0.2s ease'
                    }}
                  >
                    <LogOut size={15} />
                    <span>Sign Out</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Main Layout */}
      <div style={{ display: 'flex', minHeight: 'calc(100vh - 75px)' }}>
        {/* Sidebar Nav */}
        <aside style={{
          width: '240px',
          padding: '24px 16px',
          borderRight: '1px solid var(--color-card-border)',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px'
        }}>
          {[
            { id: 'overview', label: 'Overview', icon: Activity },
            { id: 'acoustics', label: 'PawAcoustics AI', icon: Mic },
            { id: 'bark_connect', label: 'BarkConnect Social', icon: Share2 },
            { id: 'ar_coach', label: 'AR Agility Coach', icon: Play },
            { id: 'passport', label: 'Pet Passport & QR', icon: Shield },
            { id: 'collar', label: 'Collar & Walk GPS', icon: Compass },
            { id: 'nearby_map', label: 'Parks & 24/7 Vets', icon: MapPin },
            { id: 'analytics', label: 'Health Trends & Vitals', icon: BarChart3 },
            { id: 'ai_triage', label: 'AI Health Triage', icon: Sparkles },
            { id: 'family', label: 'Family Hub', icon: Users },
            { id: 'marketplace', label: 'Store & Pantry', icon: ShoppingBag },
            { id: 'feeding', label: 'Feeding Tracker', icon: Clock },
            { id: 'weight', label: 'Weight & Growth', icon: Award },
            { id: 'vet', label: 'Tele-Vet & Booking', icon: Video },
            { id: 'lostpet', label: 'Lost Pet Mode', icon: AlertTriangle }
          ].map(tab => {
            const Icon = tab.icon;
            const isSelected = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '14px 16px',
                  borderRadius: '14px',
                  background: isSelected ? 'rgba(16, 185, 129, 0.15)' : 'transparent',
                  color: isSelected ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                  fontWeight: isSelected ? 700 : 500,
                  border: isSelected ? '1px solid rgba(16, 185, 129, 0.3)' : '1px solid transparent',
                  cursor: 'pointer',
                  textAlign: 'left',
                  width: '100%',
                  transition: 'all 0.2s ease'
                }}
              >
                <Icon size={20} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </aside>

        {/* Content Area */}
        <main style={{ flex: 1, padding: '36px', overflowY: 'auto' }}>
          {/* Top Escape Siren / Perimeter Breach Alert Banner */}
          {geofenceStatus.isBreached && (
            <div style={{
              margin: '0 0 28px 0',
              padding: '18px 24px',
              background: 'linear-gradient(90deg, #DC2626, #991B1B)',
              borderRadius: '16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              boxShadow: '0 8px 32px rgba(220, 38, 38, 0.45)',
              border: '1px solid rgba(254, 202, 202, 0.4)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '50%', background: 'rgba(255,255,255,0.25)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <AlertTriangle size={24} color="#FFF" />
                </div>
                <div>
                  <div style={{ fontWeight: 800, fontSize: '16px', color: '#FFF', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span>🚨 PERIMETER BREACH DETECTED: {activePet.name} EXITED SAFE ZONE!</span>
                    {!sirenMuted && <span style={{ fontSize: '11px', background: '#FEE2E2', color: '#DC2626', padding: '2px 8px', borderRadius: '8px', fontWeight: 700 }}>SIREN ACTIVE</span>}
                  </div>
                  <div style={{ fontSize: '13px', color: '#FEE2E2', marginTop: '3px' }}>
                    Current Distance: <strong>{geofenceStatus.distanceFromHome || 192}m from Home</strong> (Perimeter: {geofenceStatus.safeRadiusMeters || 150}m) • Collar GPS Streaming Live
                  </div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <button 
                  onClick={() => setSirenMuted(!sirenMuted)}
                  style={{
                    background: 'rgba(255, 255, 255, 0.2)',
                    color: '#FFF',
                    border: 'none',
                    padding: '8px 14px',
                    borderRadius: '10px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    fontWeight: 600,
                    fontSize: '13px'
                  }}
                >
                  {sirenMuted ? <VolumeX size={16} /> : <Volume2 size={16} />}
                  <span>{sirenMuted ? 'Muted' : 'Mute'}</span>
                </button>
                <button
                  onClick={() => setActiveTab('lostpet')}
                  style={{
                    background: '#FFF',
                    color: '#DC2626',
                    border: 'none',
                    padding: '8px 16px',
                    borderRadius: '10px',
                    cursor: 'pointer',
                    fontWeight: 800,
                    fontSize: '13px'
                  }}
                >
                  Lost Pet Map & Poster
                </button>
                <button
                  onClick={() => handleSimulateGeofence(false)}
                  style={{
                    background: 'rgba(0, 0, 0, 0.35)',
                    color: '#FFF',
                    border: '1px solid rgba(255,255,255,0.2)',
                    padding: '8px 12px',
                    borderRadius: '10px',
                    cursor: 'pointer',
                    fontSize: '12px'
                  }}
                >
                  Reset Safe Zone
                </button>
              </div>
            </div>
          )}

          {/* TAB: OVERVIEW */}
          {activeTab === 'overview' && (
            <div>
              {/* Pet Banner */}
              <div className="glass-panel" style={{ padding: '32px', marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '24px', alignItems: 'center' }}>
                  <img 
                    src={activePet.photoUrl} 
                    alt={activePet.name} 
                    style={{ width: '96px', height: '96px', borderRadius: '50%', objectFit: 'cover', border: '3px solid var(--color-primary)' }} 
                  />
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <h1 style={{ fontSize: '32px', margin: 0 }}>{activePet.name}</h1>
                      <span style={{ background: 'rgba(74, 222, 128, 0.15)', color: 'var(--color-success)', padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
                        {activePet.status}
                      </span>
                    </div>
                    <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px', fontSize: '16px' }}>
                      {activePet.breed} • {activePet.age} • {activePet.weight} kg
                    </p>
                  </div>
                </div>

                {/* Health Score Pill */}
                <div style={{ textAlign: 'center', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.3)', padding: '16px 28px', borderRadius: '20px' }}>
                  <div style={{ fontSize: '36px', fontWeight: 800, color: 'var(--color-primary)' }}>{activePet.healthScore}%</div>
                  <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>Overall Health Index</div>
                </div>
              </div>

              {/* Smart Collar Live Telemetry & GPS Walk Widget */}
              <div className="glass-panel" style={{ padding: '24px', marginBottom: '32px', border: activeWalk.isActive ? '1px solid rgba(16, 185, 129, 0.4)' : '1px solid var(--color-card-border)', background: activeWalk.isActive ? 'rgba(16, 185, 129, 0.05)' : 'rgba(21, 28, 44, 0.6)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div className="live-beacon" style={{ background: collarTelemetry.isConnected ? 'var(--color-primary)' : 'var(--color-text-secondary)' }} />
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span>{collarTelemetry.deviceName || "PawLife Collar BLE-89X"}</span>
                        <span style={{ fontSize: '11px', padding: '2px 8px', borderRadius: '12px', background: collarTelemetry.isConnected ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255,255,255,0.1)', color: collarTelemetry.isConnected ? 'var(--color-primary)' : 'var(--color-text-secondary)' }}>
                          {collarTelemetry.isConnected ? 'BLE Connected' : 'Offline'}
                        </span>
                      </div>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                        Real-Time Collar Telemetry • Android BLE & GPS Live Stream
                      </div>
                    </div>
                  </div>

                  <button 
                    onClick={() => setActiveTab('collar')}
                    className="btn-primary" 
                    style={{ padding: '8px 16px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Compass size={16} />
                    <span>Open Live GPS Walk Map</span>
                  </button>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
                  {/* Heart Rate */}
                  <div style={{ background: 'rgba(255,255,255,0.03)', padding: '16px', borderRadius: '14px', border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Live Heart Rate</span>
                      <Heart size={16} className="heart-beat" color="#FB7185" fill="#FB7185" />
                    </div>
                    <div style={{ fontSize: '24px', fontWeight: 800, color: '#FB7185' }}>
                      {collarTelemetry.heartRateBpm || 84} <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>BPM</span>
                    </div>
                    <div style={{ fontSize: '11px', color: (collarTelemetry.heartRateBpm || 84) > 100 ? 'var(--color-warning)' : 'var(--color-success)', marginTop: '4px' }}>
                      {(collarTelemetry.heartRateBpm || 84) > 100 ? '🔥 Active / Walking' : '💚 Resting Zone'}
                    </div>
                  </div>

                  {/* Steps Walked */}
                  <div style={{ background: 'rgba(255,255,255,0.03)', padding: '16px', borderRadius: '14px', border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Collar Steps</span>
                      <Zap size={16} color="var(--color-secondary)" />
                    </div>
                    <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--color-secondary)' }}>
                      {collarTelemetry.stepCount?.toLocaleString() || '1,420'}
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                      Goal: 10k ({Math.min(100, Math.round(((collarTelemetry.stepCount || 1420) / 10000) * 100))}%)
                    </div>
                  </div>

                  {/* Collar Battery */}
                  <div style={{ background: 'rgba(255,255,255,0.03)', padding: '16px', borderRadius: '14px', border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Collar Battery</span>
                      <Battery size={16} color="var(--color-success)" />
                    </div>
                    <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--color-success)' }}>
                      {collarTelemetry.batteryLevel || 92}%
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                      ~4 days battery left
                    </div>
                  </div>

                  {/* Walk Status */}
                  <div style={{ background: 'rgba(255,255,255,0.03)', padding: '16px', borderRadius: '14px', border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>GPS Walk Status</span>
                      <MapPin size={16} color={activeWalk.isActive ? 'var(--color-primary)' : 'var(--color-text-secondary)'} />
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: 800, color: activeWalk.isActive ? 'var(--color-primary)' : 'var(--color-text-primary)' }}>
                      {activeWalk.isActive ? `${((activeWalk.distanceMeters || 0) / 1000).toFixed(2)} km` : "Standby"}
                    </div>
                    <div style={{ fontSize: '11px', color: activeWalk.isActive ? 'var(--color-primary)' : 'var(--color-text-secondary)', marginTop: '4px' }}>
                      {activeWalk.isActive ? `⏱️ Active (${Math.round((activeWalk.durationSeconds || 0) / 60)}m)` : 'Ready for daily walk'}
                    </div>
                  </div>
                </div>
              </div>

              {/* Metric Cards Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px', marginBottom: '36px' }}>
                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginBottom: '8px' }}>Today's Nutrition</div>
                  <div style={{ fontSize: '28px', fontWeight: 700 }}>
                    {petMeals.reduce((acc, m) => acc + (m.calories || 0), 0)} <span style={{ fontSize: '16px', color: 'var(--color-text-secondary)' }}>kcal</span>
                  </div>
                  <div style={{ marginTop: '12px', fontSize: '13px', color: 'var(--color-primary)' }}>
                    ✓ {petMeals.length} Meals recorded (Cloud Synced)
                  </div>
                </div>

                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginBottom: '8px' }}>Weight Status</div>
                  <div style={{ fontSize: '28px', fontWeight: 700 }}>
                    {activePet.weight} <span style={{ fontSize: '16px', color: 'var(--color-text-secondary)' }}>kg</span>
                  </div>
                  <div style={{ marginTop: '12px', fontSize: '13px', color: 'var(--color-success)' }}>
                    ✓ Ideal target range
                  </div>
                </div>

                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginBottom: '8px' }}>Upcoming Vet Care</div>
                  <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--color-warning)' }}>
                    {petAppts[0]?.purpose || "All vaccinations up to date"}
                  </div>
                  <div style={{ marginTop: '12px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                    {petAppts[0]?.date || "Next checkup in 3 months"}
                  </div>
                </div>
              </div>

              {/* Sprint 4 Highlights: Smart Pantry Alert & AI Diagnostic Summary */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '32px' }}>
                {/* Smart Pantry Low Food Warning Card */}
                <div className="glass-panel" style={{ padding: '24px', border: pantryLevel.remainingGrams < 1000 ? '1px solid rgba(251, 191, 36, 0.4)' : '1px solid var(--color-card-border)', background: pantryLevel.remainingGrams < 1000 ? 'rgba(251, 191, 36, 0.04)' : 'rgba(21, 28, 44, 0.6)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div style={{ width: '42px', height: '42px', borderRadius: '12px', background: 'rgba(251, 191, 36, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-warning)' }}>
                        <ShoppingBag size={22} />
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '16px' }}>Smart Pantry Inventory</div>
                        <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                          {pantryLevel.foodName}
                        </div>
                      </div>
                    </div>
                    <span style={{
                      fontSize: '11px',
                      fontWeight: 700,
                      padding: '4px 10px',
                      borderRadius: '10px',
                      background: pantryLevel.remainingGrams < 1000 ? 'rgba(251, 191, 36, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                      color: pantryLevel.remainingGrams < 1000 ? 'var(--color-warning)' : 'var(--color-primary)'
                    }}>
                      {pantryLevel.remainingGrams < 1000 ? '⚠️ LOW STOCK' : 'IN STOCK'}
                    </span>
                  </div>

                  <div style={{ marginBottom: '16px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', marginBottom: '6px' }}>
                      <span style={{ color: 'var(--color-text-secondary)' }}>Kibble Remaining:</span>
                      <strong>{pantryLevel.remainingGrams}g / {pantryLevel.totalCapacityGrams}g ({Math.round((pantryLevel.remainingGrams / pantryLevel.totalCapacityGrams) * 100)}%)</strong>
                    </div>
                    <div style={{ width: '100%', height: '8px', background: 'rgba(255, 255, 255, 0.08)', borderRadius: '4px', overflow: 'hidden' }}>
                      <div style={{
                        width: `${Math.round((pantryLevel.remainingGrams / pantryLevel.totalCapacityGrams) * 100)}%`,
                        height: '100%',
                        background: pantryLevel.remainingGrams < 1000 ? 'linear-gradient(90deg, #F59E0B, #EF4444)' : 'linear-gradient(90deg, #10B981, #059669)',
                        borderRadius: '4px'
                      }} />
                    </div>
                    <div style={{ fontSize: '12px', color: pantryLevel.remainingGrams < 1000 ? 'var(--color-warning)' : 'var(--color-text-secondary)', marginTop: '6px' }}>
                      ~{pantryLevel.daysLeft} days of food remaining at current daily feeding rate
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                      onClick={() => {
                        handleAddToCart(marketplaceItems[0]);
                        setActiveTab('marketplace');
                      }}
                      className="btn-primary" 
                      style={{ flex: 1, padding: '10px', fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
                    >
                      <ShoppingCart size={15} />
                      <span>1-Click Auto Refill</span>
                    </button>
                    <button 
                      onClick={() => setActiveTab('marketplace')}
                      style={{ padding: '10px 14px', borderRadius: '12px', background: 'rgba(255,255,255,0.06)', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px', cursor: 'pointer' }}
                    >
                      Store
                    </button>
                  </div>
                </div>

                {/* AI Diagnostic Triage Summary Card */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div style={{ width: '42px', height: '42px', borderRadius: '12px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                        <Sparkles size={22} />
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '16px' }}>AI Clinical Triage</div>
                        <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                          Multi-Modal Symptom & Exam Analysis
                        </div>
                      </div>
                    </div>
                    <span style={{
                      fontSize: '11px',
                      fontWeight: 700,
                      padding: '4px 10px',
                      borderRadius: '10px',
                      background: aiTriageReports[0]?.urgency === 'EMERGENCY' ? 'rgba(239, 68, 68, 0.2)' : aiTriageReports[0]?.urgency === 'MODERATE' ? 'rgba(245, 158, 11, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                      color: aiTriageReports[0]?.urgency === 'EMERGENCY' ? '#EF4444' : aiTriageReports[0]?.urgency === 'MODERATE' ? '#F59E0B' : 'var(--color-primary)'
                    }}>
                      {aiTriageReports[0]?.urgency || 'MILD'} URGENCY
                    </span>
                  </div>

                  <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.5, marginBottom: '16px', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                    <strong>Latest Assessment:</strong> {aiTriageReports[0]?.findings || 'Normal healthy physical baseline. Vital signs within expected range.'}
                  </p>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                      onClick={() => handleOpenVetReport(aiTriageReports[0])}
                      className="btn-primary" 
                      style={{ flex: 1, padding: '10px', fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
                    >
                      <Printer size={15} />
                      <span>Export Clinical Vet PDF</span>
                    </button>
                    <button 
                      onClick={() => setActiveTab('ai_triage')}
                      style={{ padding: '10px 14px', borderRadius: '12px', background: 'rgba(255,255,255,0.06)', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px', cursor: 'pointer' }}
                    >
                      Scan Symptoms
                    </button>
                  </div>
                </div>
              </div>

              {/* Recent Activity Timeline */}
              <div className="glass-panel" style={{ padding: '28px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <h3 style={{ fontSize: '20px', margin: 0 }}>Recent Activity Stream</h3>
                    <span style={{ fontSize: '11px', color: 'var(--color-primary)', background: 'rgba(16, 185, 129, 0.1)', padding: '2px 8px', borderRadius: '6px' }}>Real-Time Stream</span>
                  </div>
                  <button className="btn-primary" onClick={() => setShowMealModal(true)} style={{ padding: '8px 16px', fontSize: '14px' }}>
                    + Quick Log Meal
                  </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {petMeals.slice(0, 5).map(meal => (
                    <div key={meal.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '14px 18px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '14px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                        <div style={{ background: 'rgba(16, 185, 129, 0.15)', color: 'var(--color-primary)', width: '38px', height: '38px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          🍲
                        </div>
                        <div>
                          <div style={{ fontWeight: 600 }}>{meal.name}</div>
                          <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Portion: {meal.portion}g • {meal.calories} kcal</div>
                        </div>
                      </div>
                      <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>{meal.time}</div>
                    </div>
                  ))}
                  {petMeals.length === 0 && (
                    <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '24px' }}>No meals logged yet today.</div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* TAB: DIGITAL PET PASSPORT & QR CREDENTIAL */}
          {activeTab === 'passport' && (
            <div>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>Digital Pet Health Passport</h1>
                    <span style={{ 
                      background: 'rgba(245, 158, 11, 0.2)', 
                      color: 'var(--color-warning)', 
                      padding: '4px 12px', 
                      borderRadius: '20px', 
                      fontSize: '13px', 
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <Shield size={14} />
                      <span>ISO 11784/11785 Verified</span>
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Tamper-evident clinical passport, microchip transponder registry, and scan-to-verify public QR credentials.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <button 
                    onClick={() => setShowQrModal(true)}
                    className="btn-primary" 
                    style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'linear-gradient(135deg, #10B981, #059669)' }}
                  >
                    <QrCode size={18} />
                    <span>Show QR Pass</span>
                  </button>

                  <button 
                    onClick={() => setShowAddVaccineModal(true)}
                    style={{
                      background: 'rgba(255,255,255,0.06)',
                      color: '#FFF',
                      border: '1px solid var(--color-card-border)',
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}
                  >
                    <Plus size={16} />
                    <span>Log Vaccine Booster</span>
                  </button>

                  <button 
                    onClick={() => window.print()}
                    style={{
                      background: 'rgba(255,255,255,0.06)',
                      color: '#FFF',
                      border: '1px solid var(--color-card-border)',
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}
                  >
                    <Printer size={16} />
                    <span>Print Travel Pass</span>
                  </button>
                </div>
              </div>

              {/* Passport Official Card */}
              <div className="glass-panel" style={{
                padding: '32px',
                marginBottom: '32px',
                border: '2px solid rgba(245, 158, 11, 0.4)',
                background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.9), rgba(15, 23, 42, 0.95))',
                borderRadius: '24px',
                position: 'relative',
                overflow: 'hidden'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '24px', marginBottom: '24px' }}>
                  <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                    <div style={{
                      width: '84px',
                      height: '84px',
                      borderRadius: '50%',
                      background: '#334155',
                      border: '3px solid #F59E0B',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '44px',
                      boxShadow: '0 8px 24px rgba(245, 158, 11, 0.25)'
                    }}>
                      🐕
                    </div>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span style={{ fontSize: '11px', fontWeight: 800, color: '#F59E0B', letterSpacing: '1.2px', textTransform: 'uppercase' }}>
                          REPUBLIC OF PETS • GLOBAL PASSPORT
                        </span>
                        <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: 'var(--color-primary)', padding: '2px 8px', borderRadius: '10px', fontSize: '11px', fontWeight: 700 }}>
                          ✓ VET VERIFIED
                        </span>
                      </div>
                      <h1 style={{ fontSize: '32px', margin: '4px 0 2px' }}>{petPassport.petName}</h1>
                      <div style={{ color: 'var(--color-text-secondary)', fontSize: '14px' }}>
                        {petPassport.breed} • {petPassport.species} • {petPassport.gender} • Born {petPassport.birthDate}
                      </div>
                    </div>
                  </div>

                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>Passport Number</div>
                    <div style={{ fontSize: '18px', fontWeight: 800, color: '#FFF', fontFamily: 'monospace', letterSpacing: '1px', marginTop: '2px' }}>
                      {petPassport.passportId}
                    </div>
                    <div style={{ fontSize: '12px', color: '#10B981', fontWeight: 600, marginTop: '4px' }}>
                      KCI Registry Registered
                    </div>
                  </div>
                </div>

                {/* Microchip Transponder & Rabies Barcode Section */}
                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px', background: 'rgba(0, 0, 0, 0.35)', padding: '20px 24px', borderRadius: '16px', marginBottom: '24px', border: '1px solid rgba(255,255,255,0.06)' }}>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)', textTransform: 'uppercase', fontWeight: 700 }}>
                        ISO 11784/11785 Microchip Transponder
                      </span>
                      <span style={{ fontSize: '11px', color: 'var(--color-primary)', background: 'rgba(16, 185, 129, 0.15)', padding: '2px 8px', borderRadius: '6px' }}>
                        134.2 kHz FDX-B
                      </span>
                    </div>

                    {/* Barcode graphic */}
                    <div style={{ display: 'flex', gap: '3px', height: '36px', alignItems: 'center', background: '#FFF', padding: '6px 12px', borderRadius: '8px', marginBottom: '8px' }}>
                      {[4, 2, 3, 1, 4, 1, 2, 3, 1, 4, 2, 1, 3, 2, 4, 1, 3, 2, 1, 4, 2, 3, 1, 4, 2, 1, 3, 4, 2, 1, 3].map((w, i) => (
                        <div key={i} style={{ width: `${w}px`, height: '100%', background: '#000' }} />
                      ))}
                    </div>

                    <div style={{ fontSize: '14px', fontWeight: 800, color: '#FFF', fontFamily: 'monospace', letterSpacing: '2px' }}>
                      {petPassport.microchipId}
                    </div>
                  </div>

                  <div style={{ borderLeft: '1px solid rgba(255,255,255,0.1)', paddingLeft: '20px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>Rabies Immunization</div>
                    <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--color-primary)', marginTop: '2px' }}>
                      {petPassport.rabiesCertificateNo}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--color-success)', marginTop: '4px' }}>
                      ✓ Valid until Oct 14, 2027
                    </div>
                  </div>
                </div>

                {/* Guardian & Emergency Deep Link */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                  <div>
                    <span>Primary Guardian: </span>
                    <strong style={{ color: '#FFF' }}>{petPassport.guardianName}</strong> • {petPassport.guardianCity}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div>
                      <span>Contact: </span>
                      <strong style={{ color: 'var(--color-primary)' }}>{petPassport.guardianContact}</strong>
                    </div>
                    <a 
                      href={petPassport.publicVerificationUrl}
                      target="_blank"
                      rel="noreferrer"
                      style={{ color: 'var(--color-secondary)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 600 }}
                    >
                      <ExternalLink size={14} />
                      <span>Test Public QR URL</span>
                    </a>
                  </div>
                </div>
              </div>

              {/* Quick Immunity Metrics Cards */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginBottom: '32px' }}>
                <div className="glass-panel" style={{ padding: '20px', borderLeft: '4px solid var(--color-primary)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Rabies Protection Status</div>
                  <div style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-primary)', marginTop: '4px' }}>
                    Active (3-Yr Booster)
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                    Valid until Oct 14, 2027 • 0 Days Overdue
                  </div>
                </div>

                <div className="glass-panel" style={{ padding: '20px', borderLeft: '4px solid var(--color-secondary)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Boarding & Travel Clearance</div>
                  <div style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-secondary)', marginTop: '4px' }}>
                    Eligible for Kennels
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                    DHPPi & Bordetella up to date
                  </div>
                </div>

                <div className="glass-panel" style={{ padding: '20px', borderLeft: '4px solid #10B981' }}>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Parasite Prevention</div>
                  <div style={{ fontSize: '20px', fontWeight: 800, color: '#10B981', marginTop: '4px' }}>
                    Quarterly Dewormed
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                    Next Dose Due: Jan 01, 2027
                  </div>
                </div>
              </div>

              {/* Verified Immunization History Ledger */}
              <div className="glass-panel" style={{ padding: '28px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <div>
                    <h3 style={{ fontSize: '20px', margin: 0 }}>Verified Immunization Ledger</h3>
                    <p style={{ margin: '4px 0 0', color: 'var(--color-text-secondary)', fontSize: '13px' }}>
                      Cryptographically recorded vaccination history synced across Android & Web.
                    </p>
                  </div>
                  <button 
                    onClick={() => setShowAddVaccineModal(true)}
                    className="btn-primary" 
                    style={{ padding: '8px 16px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Plus size={16} />
                    <span>Add Vaccine Record</span>
                  </button>
                </div>

                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--color-card-border)', color: 'var(--color-text-secondary)', fontSize: '13px' }}>
                      <th style={{ padding: '12px 16px' }}>Vaccine / Booster</th>
                      <th style={{ padding: '12px 16px' }}>Category</th>
                      <th style={{ padding: '12px 16px' }}>Batch / Lot #</th>
                      <th style={{ padding: '12px 16px' }}>Veterinary Provider</th>
                      <th style={{ padding: '12px 16px' }}>Administered</th>
                      <th style={{ padding: '12px 16px' }}>Valid Until</th>
                      <th style={{ padding: '12px 16px' }}>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vaccinations.map(v => (
                      <tr key={v.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                        <td style={{ padding: '16px', fontWeight: 600, color: '#FFF' }}>{v.vaccineName}</td>
                        <td style={{ padding: '16px', color: 'var(--color-text-secondary)', fontSize: '13px' }}>{v.category}</td>
                        <td style={{ padding: '16px', fontFamily: 'monospace', fontSize: '12px', color: 'var(--color-text-secondary)' }}>{v.batchLotNumber}</td>
                        <td style={{ padding: '16px', fontSize: '13px' }}>{v.clinicName}</td>
                        <td style={{ padding: '16px', color: 'var(--color-text-secondary)', fontSize: '13px' }}>{v.administeredDate}</td>
                        <td style={{ padding: '16px', color: 'var(--color-primary)', fontWeight: 600, fontSize: '13px' }}>{v.expiryDate}</td>
                        <td style={{ padding: '16px' }}>
                          <span style={{
                            background: v.status === 'VALID' ? 'rgba(16, 185, 129, 0.2)' : 'rgba(239, 68, 68, 0.2)',
                            color: v.status === 'VALID' ? 'var(--color-primary)' : '#EF4444',
                            padding: '4px 10px',
                            borderRadius: '10px',
                            fontSize: '11px',
                            fontWeight: 700
                          }}>
                            {v.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB: PAWACOUSTICS AI (SPRINT 9) */}
          {activeTab === 'acoustics' && (
            <div>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', flexWrap: 'wrap', gap: '16px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0, fontWeight: 800 }}>PawAcoustics AI • Acoustic Mood Classifier</h1>
                    <span style={{
                      background: isMicListening ? 'rgba(16, 185, 129, 0.2)' : 'rgba(148, 163, 184, 0.2)',
                      color: isMicListening ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                      padding: '4px 12px',
                      borderRadius: '20px',
                      fontSize: '13px',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <span style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: isMicListening ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                        boxShadow: isMicListening ? '0 0 8px var(--color-primary)' : 'none'
                      }} />
                      {isMicListening ? 'LIVE AUDIO SAMPLING' : 'MIC STANDBY'}
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', margin: '6px 0 0', fontSize: '15px' }}>
                    Real-time canine vocal spectrogram, AI emotional triage, collar BPM correlation & anti-anxiety sound therapy
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <button
                    onClick={() => setIsMicListening(!isMicListening)}
                    className="btn"
                    style={{
                      background: isMicListening ? 'rgba(239, 68, 68, 0.15)' : 'rgba(16, 185, 129, 0.15)',
                      color: isMicListening ? '#EF4444' : 'var(--color-primary)',
                      border: `1px solid ${isMicListening ? 'rgba(239, 68, 68, 0.3)' : 'rgba(16, 185, 129, 0.3)'}`,
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      cursor: 'pointer'
                    }}
                  >
                    {isMicListening ? <MicOff size={18} /> : <Mic size={18} />}
                    <span>{isMicListening ? 'Pause Mic' : 'Resume Mic'}</span>
                  </button>
                  <div style={{
                    padding: '8px 16px',
                    borderRadius: '12px',
                    background: ambientDecibels > 75 ? 'rgba(239, 68, 68, 0.15)' : 'rgba(14, 165, 233, 0.15)',
                    border: `1px solid ${ambientDecibels > 75 ? 'rgba(239, 68, 68, 0.3)' : 'rgba(14, 165, 233, 0.3)'}`,
                    color: ambientDecibels > 75 ? '#EF4444' : 'var(--color-secondary)',
                    fontWeight: 700,
                    fontSize: '14px'
                  }}>
                    {ambientDecibels} dB SPL
                  </div>
                </div>
              </div>

              {/* Grid 1: Spectrogram Waveform & Emotional Triage */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
                gap: '24px',
                marginBottom: '28px'
              }}>
                {/* 24-Band Spectrogram Canvas */}
                <div className="card" style={{
                  padding: '24px',
                  borderRadius: '20px',
                  background: 'rgba(21, 28, 44, 0.75)',
                  backdropFilter: 'blur(16px)',
                  border: '1px solid rgba(255, 255, 255, 0.08)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <Activity size={20} color="var(--color-secondary)" />
                      <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 700 }}>24-Band Acoustic Frequency Spectrogram</h3>
                    </div>
                    <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>44.1 kHz • PCM Stream</span>
                  </div>

                  {/* Equalizer Bars Container */}
                  <div style={{
                    height: '140px',
                    background: 'rgba(11, 15, 25, 0.85)',
                    borderRadius: '16px',
                    padding: '16px 20px',
                    display: 'flex',
                    alignItems: 'flex-end',
                    justifyContent: 'space-between',
                    gap: '6px',
                    border: '1px solid rgba(255, 255, 255, 0.05)',
                    boxShadow: 'inset 0 2px 10px rgba(0, 0, 0, 0.5)'
                  }}>
                    {spectrogramBars.map((amp, idx) => {
                      const heightPercent = Math.max(12, Math.min(100, Math.round(amp * 100)));
                      const barColor = amp > 0.75 
                        ? '#FB7185' 
                        : amp > 0.45 
                          ? '#0EA5E9' 
                          : '#10B981';
                      return (
                        <div key={idx} style={{
                          flex: 1,
                          height: `${heightPercent}%`,
                          background: `linear-gradient(to top, ${barColor}99, ${barColor})`,
                          borderRadius: '4px',
                          transition: 'height 0.12s ease-in-out',
                          boxShadow: `0 0 6px ${barColor}44`
                        }} />
                      );
                    })}
                  </div>

                  {/* Frequency Labels */}
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    marginTop: '10px',
                    fontSize: '11px',
                    color: 'var(--color-text-secondary)',
                    fontWeight: 600
                  }}>
                    <span>60 Hz</span>
                    <span>250 Hz</span>
                    <span>1 kHz</span>
                    <span>4 kHz</span>
                    <span>12 kHz</span>
                    <span>20 kHz</span>
                  </div>

                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginTop: '14px',
                    padding: '10px 14px',
                    background: 'rgba(255, 255, 255, 0.03)',
                    borderRadius: '12px',
                    fontSize: '12px'
                  }}>
                    <span style={{ color: 'var(--color-text-secondary)' }}>Status: {isMicListening ? 'Actively analyzing pitch signatures' : 'Microphone paused'}</span>
                    <span style={{
                      fontWeight: 700,
                      color: ambientDecibels > 75 ? '#FB7185' : 'var(--color-primary)'
                    }}>
                      {ambientDecibels > 75 ? 'Barking Burst Detected' : 'Resting Baseline Sound'}
                    </span>
                  </div>
                </div>

                {/* AI Emotional Triage Hero Card */}
                <div className="card" style={{
                  padding: '24px',
                  borderRadius: '20px',
                  background: 'rgba(21, 28, 44, 0.75)',
                  backdropFilter: 'blur(16px)',
                  border: '1.5px solid rgba(16, 185, 129, 0.3)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                      <div style={{
                        width: '54px',
                        height: '54px',
                        borderRadius: '16px',
                        background: 'rgba(255, 255, 255, 0.06)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '30px',
                        boxShadow: '0 4px 14px rgba(0,0,0,0.2)'
                      }}>
                        {latestAcousticEpisode.emoji}
                      </div>
                      <div>
                        <h3 style={{ margin: 0, fontSize: '20px', fontWeight: 800 }}>
                          {latestAcousticEpisode.moodLabel}
                        </h3>
                        <div style={{
                          display: 'inline-block',
                          color: 'var(--color-primary)',
                          fontSize: '12px',
                          fontWeight: 700,
                          marginTop: '2px'
                        }}>
                          AI Confidence {Math.round(latestAcousticEpisode.confidenceScore * 100)}% Match
                        </div>
                      </div>
                    </div>

                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      padding: '6px 12px',
                      borderRadius: '12px',
                      background: 'rgba(245, 158, 11, 0.15)',
                      border: '1px solid rgba(245, 158, 11, 0.3)',
                      color: '#F59E0B',
                      fontSize: '13px',
                      fontWeight: 700
                    }}>
                      <Heart size={14} />
                      <span>{latestAcousticEpisode.collarHeartRateBpm} BPM</span>
                    </div>
                  </div>

                  {/* Summary & Clinical Advice */}
                  <div style={{
                    padding: '14px 16px',
                    borderRadius: '14px',
                    background: 'rgba(11, 15, 25, 0.7)',
                    border: '1px solid rgba(255, 255, 255, 0.06)',
                    marginBottom: '16px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                      <Sparkles size={16} color="var(--color-secondary)" />
                      <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--color-secondary)' }}>
                        AI Canine Behaviorist Triage
                      </span>
                    </div>
                    <p style={{ margin: 0, fontSize: '13px', lineHeight: 1.5, color: 'var(--color-text-primary)' }}>
                      {latestAcousticEpisode.summary}
                    </p>
                  </div>

                  {/* Stress Level & Fundamental Pitch Gauges */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--color-text-secondary)' }}>
                      Physiological Stress Index: <strong>{latestAcousticEpisode.stressIndex}/100</strong>
                    </span>
                    <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                      Pitch: <strong>{latestAcousticEpisode.fundamentalPitchHz} Hz</strong>
                    </span>
                  </div>

                  <div style={{
                    width: '100%',
                    height: '8px',
                    background: 'rgba(255, 255, 255, 0.08)',
                    borderRadius: '4px',
                    overflow: 'hidden'
                  }}>
                    <div style={{
                      width: `${latestAcousticEpisode.stressIndex}%`,
                      height: '100%',
                      background: latestAcousticEpisode.stressIndex > 70 
                        ? 'linear-gradient(to right, #F59E0B, #EF4444)' 
                        : latestAcousticEpisode.stressIndex > 40 
                          ? 'linear-gradient(to right, #10B981, #F59E0B)' 
                          : '#10B981',
                      borderRadius: '4px',
                      transition: 'width 0.4s ease'
                    }} />
                  </div>
                </div>
              </div>

              {/* Grid 2: Canine Bark Simulator & Anti-Anxiety Sound Therapy */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
                gap: '24px',
                marginBottom: '28px'
              }}>
                {/* Canine Bark Event Simulator */}
                <div className="card" style={{
                  padding: '24px',
                  borderRadius: '20px',
                  background: 'rgba(21, 28, 44, 0.75)',
                  backdropFilter: 'blur(16px)',
                  border: '1px solid rgba(255, 255, 255, 0.08)'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
                    <Volume2 size={20} color="#F59E0B" />
                    <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 700 }}>Simulate Canine Bark Episode</h3>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', fontSize: '13px', margin: '0 0 16px' }}>
                    Trigger real-time audio surges to test AI classification, stress correlation, and Cloud Firestore sync.
                  </p>

                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(2, 1fr)',
                    gap: '12px'
                  }}>
                    {[
                      { key: 'ALERT', label: 'Alert Bark', emoji: '🚨', color: '#F59E0B', desc: '340 Hz • Staccato perimeter bark' },
                      { key: 'ANXIETY', label: 'Separation Whine', emoji: '🥺', color: '#FB7185', desc: '740 Hz • High pitch distress' },
                      { key: 'PLAYFUL', label: 'Playful Bark', emoji: '🎾', color: '#0EA5E9', desc: '540 Hz • Bouncy joyful burst' },
                      { key: 'PAIN', label: 'Distress / Yelp', emoji: '🩹', color: '#EF4444', desc: '820 Hz • Acute discomfort sound' }
                    ].map(btn => (
                      <button
                        key={btn.key}
                        onClick={() => handleSimulateBark(btn.key)}
                        style={{
                          background: `${btn.color}15`,
                          border: `1.5px solid ${btn.color}40`,
                          borderRadius: '14px',
                          padding: '14px',
                          cursor: 'pointer',
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'flex-start',
                          gap: '4px',
                          textAlign: 'left',
                          transition: 'all 0.2s ease'
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span style={{ fontSize: '20px' }}>{btn.emoji}</span>
                          <span style={{ color: btn.color, fontWeight: 700, fontSize: '14px' }}>{btn.label}</span>
                        </div>
                        <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>{btn.desc}</span>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Anti-Anxiety Acoustic Sound Therapy Player */}
                <div className="card" style={{
                  padding: '24px',
                  borderRadius: '20px',
                  background: 'rgba(21, 28, 44, 0.75)',
                  backdropFilter: 'blur(16px)',
                  border: '1px solid rgba(16, 185, 129, 0.3)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <Sparkles size={20} color="var(--color-primary)" />
                      <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 700 }}>Anti-Anxiety Sound Therapy</h3>
                    </div>
                    {activeTherapyTrackId && (
                      <button
                        onClick={stopTherapyTone}
                        style={{
                          background: 'rgba(239, 68, 68, 0.2)',
                          color: '#EF4444',
                          border: '1px solid rgba(239, 68, 68, 0.4)',
                          borderRadius: '8px',
                          padding: '4px 10px',
                          fontSize: '11px',
                          fontWeight: 700,
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '4px'
                        }}
                      >
                        <Square size={12} />
                        <span>STOP THERAPY</span>
                      </button>
                    )}
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', fontSize: '13px', margin: '0 0 16px' }}>
                    Generates soothing bioharmonic sine tones directly in-browser using Web Audio API to reduce stress.
                  </p>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    {[
                      { id: 'delta', title: 'Delta-Wave Canine Lullaby', category: 'Deep Rest', freq: 432, label: '432 Hz Bioharmonic', emoji: '🎵' },
                      { id: 'pink', title: 'Calming Rain & Pink Noise', category: 'Acoustic Masking', freq: 220, label: '220 Hz Low Spectrum', emoji: '🌧️' },
                      { id: 'heartbeat', title: 'Maternal Heartbeat Rhythm', category: 'Puppy Security', freq: 110, label: '110 Hz Pulse', emoji: '💓' }
                    ].map(track => {
                      const isPlaying = activeTherapyTrackId === track.id;
                      return (
                        <div
                          key={track.id}
                          onClick={() => isPlaying ? stopTherapyTone() : playTherapyTone(track)}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '12px 16px',
                            borderRadius: '12px',
                            background: isPlaying ? 'rgba(16, 185, 129, 0.15)' : 'rgba(11, 15, 25, 0.6)',
                            border: `1px solid ${isPlaying ? 'var(--color-primary)' : 'rgba(255, 255, 255, 0.06)'}`,
                            cursor: 'pointer',
                            transition: 'all 0.2s ease'
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <span style={{ fontSize: '22px' }}>{track.emoji}</span>
                            <div>
                              <div style={{
                                fontWeight: 700,
                                fontSize: '13px',
                                color: isPlaying ? 'var(--color-primary)' : 'var(--color-text-primary)'
                              }}>
                                {track.title}
                              </div>
                              <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                                {track.label} • {track.category}
                              </div>
                            </div>
                          </div>

                          <div style={{
                            width: '32px',
                            height: '32px',
                            borderRadius: '50%',
                            background: isPlaying ? 'var(--color-primary)' : 'rgba(255, 255, 255, 0.08)',
                            color: isPlaying ? '#0B0F19' : '#FFF',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                          }}>
                            {isPlaying ? <Square size={14} /> : <Play size={14} style={{ marginLeft: '2px' }} />}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* Section 3: Cross-Platform Acoustic Event History */}
              <div className="card" style={{
                padding: '24px',
                borderRadius: '20px',
                background: 'rgba(21, 28, 44, 0.75)',
                backdropFilter: 'blur(16px)',
                border: '1px solid rgba(255, 255, 255, 0.08)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <Clock size={18} color="var(--color-primary)" />
                    <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 700 }}>Acoustic Mood History & Cloud Sync</h3>
                  </div>
                  <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                    Synced from Android Collar & App via Cloud Firestore
                  </span>
                </div>

                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.08)', textAlign: 'left', color: 'var(--color-text-secondary)' }}>
                        <th style={{ padding: '10px 12px' }}>Time</th>
                        <th style={{ padding: '10px 12px' }}>Pet</th>
                        <th style={{ padding: '10px 12px' }}>Mood Classification</th>
                        <th style={{ padding: '10px 12px' }}>Decibels & Pitch</th>
                        <th style={{ padding: '10px 12px' }}>Collar BPM</th>
                        <th style={{ padding: '10px 12px' }}>Stress Index</th>
                        <th style={{ padding: '10px 12px' }}>Clinical Notes</th>
                      </tr>
                    </thead>
                    <tbody>
                      {acousticEvents.map((ep, idx) => (
                        <tr key={ep.id || idx} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                          <td style={{ padding: '12px', whiteSpace: 'nowrap', color: 'var(--color-text-secondary)' }}>
                            {new Date(ep.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </td>
                          <td style={{ padding: '12px', fontWeight: 600 }}>{ep.petName || 'Max'}</td>
                          <td style={{ padding: '12px' }}>
                            <span style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '6px',
                              padding: '4px 10px',
                              borderRadius: '8px',
                              background: 'rgba(255, 255, 255, 0.06)',
                              fontWeight: 700,
                              fontSize: '12px'
                            }}>
                              <span>{ep.emoji}</span>
                              <span>{ep.moodLabel || ep.moodState}</span>
                            </span>
                          </td>
                          <td style={{ padding: '12px' }}>
                            {ep.peakDecibels} dB • {ep.fundamentalPitchHz} Hz
                          </td>
                          <td style={{ padding: '12px', fontWeight: 600, color: '#F59E0B' }}>
                            {ep.collarHeartRateBpm} BPM
                          </td>
                          <td style={{ padding: '12px' }}>
                            <span style={{
                              padding: '2px 8px',
                              borderRadius: '6px',
                              fontWeight: 700,
                              fontSize: '11px',
                              background: ep.stressIndex > 70 ? 'rgba(239, 68, 68, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                              color: ep.stressIndex > 70 ? '#EF4444' : 'var(--color-primary)'
                            }}>
                              {ep.stressIndex}/100
                            </span>
                          </td>
                          <td style={{ padding: '12px', color: 'var(--color-text-secondary)', maxWidth: '300px' }}>
                            {ep.summary}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* TAB: BARKCONNECT SOCIAL & LIVE DOG PARKS (SPRINT 7) */}
          {activeTab === 'bark_connect' && (
            <div>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>BarkConnect Community & Park Radar</h1>
                    <span style={{
                      background: userCheckedInParkId ? 'rgba(16, 185, 129, 0.2)' : 'rgba(14, 165, 233, 0.2)',
                      color: userCheckedInParkId ? 'var(--color-primary)' : 'var(--color-secondary)',
                      padding: '4px 12px',
                      borderRadius: '20px',
                      fontSize: '13px',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <span style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: userCheckedInParkId ? 'var(--color-primary)' : 'var(--color-secondary)',
                        boxShadow: `0 0 8px ${userCheckedInParkId ? 'var(--color-primary)' : 'var(--color-secondary)'}`
                      }} />
                      <span>{userCheckedInParkId ? 'CHECKED IN AT PARK' : 'CHENNAI RADAR LIVE'}</span>
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Real-time dog park check-ins, canine compatibility matchmaker, and local Chennai pet social stream.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <button
                    onClick={() => setShowNewPostModal(true)}
                    className="btn-primary"
                    style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'linear-gradient(135deg, #10B981, #059669)' }}
                  >
                    <Plus size={18} />
                    <span>Share Park Update</span>
                  </button>
                </div>
              </div>

              {/* Sub-tab Navigation */}
              <div style={{ display: 'flex', gap: '12px', marginBottom: '28px', background: 'rgba(255, 255, 255, 0.03)', padding: '6px', borderRadius: '16px', border: '1px solid var(--color-card-border)' }}>
                {[
                  { id: 'parks', label: `Live Dog Parks (${dogParks.length})`, icon: MapPin },
                  { id: 'playdates', label: `Playdate Matchmaker (${playdateCandidates.length})`, icon: Heart },
                  { id: 'feed', label: `Neighborhood Feed (${communityPosts.length})`, icon: MessageSquare }
                ].map(sub => {
                  const SubIcon = sub.icon;
                  const isCurrent = communitySubTab === sub.id;
                  return (
                    <button
                      key={sub.id}
                      onClick={() => setCommunitySubTab(sub.id)}
                      style={{
                        flex: 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '8px',
                        padding: '12px 18px',
                        borderRadius: '12px',
                        background: isCurrent ? 'var(--color-primary)' : 'transparent',
                        color: isCurrent ? '#000' : 'var(--color-text-secondary)',
                        fontWeight: isCurrent ? 700 : 500,
                        border: 'none',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease',
                        fontSize: '14px'
                      }}
                    >
                      <SubIcon size={16} />
                      <span>{sub.label}</span>
                    </button>
                  );
                })}
              </div>

              {/* SUBTAB 1: LIVE DOG PARKS */}
              {communitySubTab === 'parks' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                  {dogParks.map(park => {
                    const isCheckedInHere = userCheckedInParkId === park.id;
                    return (
                      <div
                        key={park.id}
                        className="glass-panel"
                        style={{
                          padding: '24px',
                          border: isCheckedInHere ? '2px solid rgba(16, 185, 129, 0.6)' : '1px solid var(--color-card-border)',
                          background: isCheckedInHere ? 'rgba(16, 185, 129, 0.06)' : 'rgba(21, 28, 44, 0.6)'
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                              <h3 style={{ fontSize: '20px', margin: 0, color: '#FFF' }}>{park.name}</h3>
                              <span style={{
                                background: 'rgba(16, 185, 129, 0.15)',
                                color: 'var(--color-primary)',
                                padding: '4px 10px',
                                borderRadius: '12px',
                                fontSize: '12px',
                                fontWeight: 700,
                                display: 'flex',
                                alignItems: 'center',
                                gap: '5px'
                              }}>
                                <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--color-primary)' }} />
                                {park.totalDogsActive} Dogs Active Now
                              </span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-text-secondary)', fontSize: '13px', marginTop: '6px' }}>
                              <MapPin size={14} />
                              <span>{park.location}</span>
                              <span>•</span>
                              <span>{park.distanceKm} km away</span>
                              <span>•</span>
                              <span>⭐ {park.rating}</span>
                              <span>•</span>
                              <span>🕒 {park.openHours}</span>
                            </div>
                          </div>

                          <button
                            onClick={() => handleToggleParkCheckIn(park.id)}
                            style={{
                              background: isCheckedInHere ? 'rgba(16, 185, 129, 0.2)' : 'var(--color-primary)',
                              color: isCheckedInHere ? 'var(--color-primary)' : '#000',
                              border: isCheckedInHere ? '1px solid var(--color-primary)' : 'none',
                              padding: '10px 20px',
                              borderRadius: '12px',
                              fontWeight: 700,
                              fontSize: '13px',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '6px',
                              transition: 'all 0.2s ease',
                              boxShadow: isCheckedInHere ? '0 0 16px rgba(16, 185, 129, 0.3)' : 'none'
                            }}
                          >
                            {isCheckedInHere ? (
                              <>
                                <CheckCircle size={16} />
                                <span>Checked In (Leave)</span>
                              </>
                            ) : (
                              <>
                                <Plus size={16} />
                                <span>Check In with {activePet.name}</span>
                              </>
                            )}
                          </button>
                        </div>

                        {/* Amenities */}
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '20px' }}>
                          {park.amenities.map((amenity, i) => (
                            <span
                              key={i}
                              style={{
                                background: 'rgba(255, 255, 255, 0.04)',
                                border: '1px solid rgba(255, 255, 255, 0.08)',
                                padding: '4px 10px',
                                borderRadius: '8px',
                                fontSize: '12px',
                                color: 'var(--color-text-secondary)'
                              }}
                            >
                              ✓ {amenity}
                            </span>
                          ))}
                        </div>

                        {/* Active dogs badges */}
                        <div style={{ background: 'rgba(0, 0, 0, 0.25)', padding: '16px', borderRadius: '14px', border: '1px solid rgba(255,255,255,0.04)' }}>
                          <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', fontWeight: 600, textTransform: 'uppercase', marginBottom: '12px', letterSpacing: '0.5px' }}>
                            Currently Playing ({park.activeDogs.length} Pups):
                          </div>
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
                            {park.activeDogs.map(dog => (
                              <div
                                key={dog.id}
                                style={{
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '10px',
                                  background: 'rgba(255, 255, 255, 0.05)',
                                  border: '1px solid rgba(255, 255, 255, 0.08)',
                                  padding: '8px 14px',
                                  borderRadius: '12px'
                                }}
                              >
                                <div style={{ fontSize: '20px' }}>{dog.avatar}</div>
                                <div>
                                  <div style={{ fontWeight: 700, fontSize: '13px', color: '#FFF' }}>{dog.name}</div>
                                  <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                                    {dog.breed} • {dog.ownerName}
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

              {/* SUBTAB 2: PLAYDATE MATCHMAKER */}
              {communitySubTab === 'playdates' && (
                <div>
                  {/* Energy Filter */}
                  <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
                    {['All', 'High', 'Moderate', 'Calm'].map(filt => (
                      <button
                        key={filt}
                        onClick={() => setPlaydateFilter(filt)}
                        style={{
                          padding: '6px 16px',
                          borderRadius: '20px',
                          background: playdateFilter === filt ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.04)',
                          color: playdateFilter === filt ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                          border: playdateFilter === filt ? '1px solid var(--color-primary)' : '1px solid rgba(255, 255, 255, 0.06)',
                          fontSize: '12px',
                          fontWeight: 600,
                          cursor: 'pointer'
                        }}
                      >
                        {filt === 'All' ? 'All Temperaments' : `${filt} Energy`}
                      </button>
                    ))}
                  </div>

                  {/* Candidates Grid */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '20px' }}>
                    {playdateCandidates
                      .filter(c => playdateFilter === 'All' || c.energyLevel.toLowerCase() === playdateFilter.toLowerCase())
                      .map(candidate => {
                        const isInvited = invitedPlaydates[candidate.id];
                        return (
                          <div
                            key={candidate.id}
                            className="glass-panel"
                            style={{
                              padding: '24px',
                              display: 'flex',
                              flexDirection: 'column',
                              justifyContent: 'space-between'
                            }}
                          >
                            <div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                                <div style={{ display: 'flex', gap: '14px', alignItems: 'center' }}>
                                  <div style={{
                                    width: '54px',
                                    height: '54px',
                                    borderRadius: '16px',
                                    background: 'rgba(255, 255, 255, 0.06)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontSize: '28px',
                                    border: '1px solid rgba(255, 255, 255, 0.1)'
                                  }}>
                                    {candidate.avatar}
                                  </div>
                                  <div>
                                    <div style={{ fontWeight: 800, fontSize: '18px', color: '#FFF' }}>{candidate.name}</div>
                                    <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                                      {candidate.breed} • {candidate.age}
                                    </div>
                                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                                      Parent: <strong style={{ color: '#FFF' }}>{candidate.ownerName}</strong> • {candidate.distanceKm} km away
                                    </div>
                                  </div>
                                </div>

                                <div style={{ textAlign: 'right' }}>
                                  <div style={{
                                    background: 'rgba(16, 185, 129, 0.15)',
                                    color: 'var(--color-primary)',
                                    padding: '4px 10px',
                                    borderRadius: '12px',
                                    fontSize: '12px',
                                    fontWeight: 800
                                  }}>
                                    ⭐ {candidate.compatibilityScore}% MATCH
                                  </div>
                                  <div style={{
                                    fontSize: '11px',
                                    color: candidate.energyLevel === 'High' ? '#F59E0B' : candidate.energyLevel === 'Moderate' ? 'var(--color-secondary)' : '#10B981',
                                    marginTop: '4px',
                                    fontWeight: 600
                                  }}>
                                    ⚡ {candidate.energyLevel} Energy
                                  </div>
                                </div>
                              </div>

                              <p style={{
                                fontSize: '13px',
                                color: 'var(--color-text-secondary)',
                                lineHeight: 1.5,
                                background: 'rgba(0,0,0,0.2)',
                                padding: '12px 14px',
                                borderRadius: '12px',
                                margin: '0 0 16px 0',
                                border: '1px solid rgba(255,255,255,0.04)'
                              }}>
                                "{candidate.bio}"
                              </p>
                            </div>

                            <button
                              onClick={() => handleSendPlaydateInvite(candidate)}
                              style={{
                                width: '100%',
                                padding: '10px 16px',
                                borderRadius: '12px',
                                background: isInvited ? 'rgba(16, 185, 129, 0.2)' : 'linear-gradient(135deg, #0EA5E9, #0284C7)',
                                color: isInvited ? 'var(--color-primary)' : '#FFF',
                                border: isInvited ? '1px solid var(--color-primary)' : 'none',
                                fontWeight: 700,
                                fontSize: '13px',
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                gap: '8px',
                                transition: 'all 0.2s ease'
                              }}
                            >
                              {isInvited ? (
                                <>
                                  <CheckCircle size={16} />
                                  <span>Invitation Sent!</span>
                                </>
                              ) : (
                                <>
                                  <Heart size={16} />
                                  <span>Send Playdate Meetup Invite</span>
                                </>
                              )}
                            </button>
                          </div>
                        );
                      })}
                  </div>
                </div>
              )}

              {/* SUBTAB 3: NEIGHBORHOOD FEED */}
              {communitySubTab === 'feed' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  {communityPosts.map(post => (
                    <div
                      key={post.id}
                      className="glass-panel"
                      style={{ padding: '24px' }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                          <div style={{
                            width: '44px',
                            height: '44px',
                            borderRadius: '50%',
                            background: 'rgba(16, 185, 129, 0.15)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 700,
                            color: 'var(--color-primary)',
                            fontSize: '16px'
                          }}>
                            {post.authorName.charAt(0)}
                          </div>
                          <div>
                            <div style={{ fontWeight: 700, fontSize: '15px', color: '#FFF' }}>{post.authorName}</div>
                            <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                              {post.authorRole} • {new Date(post.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </div>
                          </div>
                        </div>

                        <div style={{ display: 'flex', gap: '8px' }}>
                          {post.location && (
                            <span style={{
                              background: 'rgba(255, 255, 255, 0.05)',
                              color: 'var(--color-text-secondary)',
                              padding: '4px 10px',
                              borderRadius: '10px',
                              fontSize: '11px',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px'
                            }}>
                              <MapPin size={12} />
                              {post.location}
                            </span>
                          )}
                          <span style={{
                            background: 'rgba(14, 165, 233, 0.15)',
                            color: 'var(--color-secondary)',
                            padding: '4px 10px',
                            borderRadius: '10px',
                            fontSize: '11px',
                            fontWeight: 700
                          }}>
                            {post.badge || 'Community'}
                          </span>
                        </div>
                      </div>

                      <p style={{ fontSize: '14px', lineHeight: 1.6, color: '#F1F5F9', margin: '0 0 16px 0' }}>
                        {post.content}
                      </p>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px', borderTop: '1px solid rgba(255, 255, 255, 0.06)', paddingTop: '14px' }}>
                        <button
                          onClick={() => handleTogglePostLike(post.id)}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px',
                            background: post.isLiked ? 'rgba(251, 113, 133, 0.15)' : 'transparent',
                            color: post.isLiked ? '#FB7185' : 'var(--color-text-secondary)',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '13px',
                            fontWeight: 600,
                            transition: 'all 0.2s ease'
                          }}
                        >
                          <Heart size={16} fill={post.isLiked ? '#FB7185' : 'none'} color="#FB7185" />
                          <span>{post.likesCount} Helpful</span>
                        </button>

                        <button
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px',
                            background: 'transparent',
                            color: 'var(--color-text-secondary)',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '13px'
                          }}
                        >
                          <MessageSquare size={16} />
                          <span>Reply</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB: AR PET AGILITY & INTERACTIVE TRICK COACH (SPRINT 8: PawAR) */}
          {activeTab === 'ar_coach' && (
            <div>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>PawAR Agility & Trick Coach</h1>
                    <span style={{
                      background: 'rgba(245, 158, 11, 0.2)',
                      color: 'var(--color-warning)',
                      padding: '4px 12px',
                      borderRadius: '20px',
                      fontSize: '13px',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <Zap size={14} />
                      <span>{agilityMasteryLevel} • {agilityXp} XP</span>
                    </span>
                    <span style={{
                      background: 'rgba(16, 185, 129, 0.15)',
                      color: 'var(--color-primary)',
                      padding: '4px 12px',
                      borderRadius: '20px',
                      fontSize: '12px',
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '5px'
                    }}>
                      <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--color-primary)' }} />
                      ARCore & Mobile Sync Active
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Interactive virtual obstacle courses, acoustic clicker positive reinforcement, and trick proficiency curriculum.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <button
                    onClick={handleSaveAgilitySession}
                    className="btn-primary"
                    style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'linear-gradient(135deg, #10B981, #059669)' }}
                  >
                    <Award size={18} />
                    <span>Save Course Session</span>
                  </button>
                  <button
                    onClick={handleResetCourse}
                    style={{
                      background: 'rgba(255,255,255,0.06)',
                      color: '#FFF',
                      border: '1px solid var(--color-card-border)',
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 600,
                      cursor: 'pointer'
                    }}
                  >
                    Reset Obstacles
                  </button>
                </div>
              </div>

              {/* Sub-tab Navigation */}
              <div style={{ display: 'flex', gap: '12px', marginBottom: '28px', background: 'rgba(255, 255, 255, 0.03)', padding: '6px', borderRadius: '16px', border: '1px solid var(--color-card-border)' }}>
                {[
                  { id: 'arena', label: `Virtual Agility Arena (${placedObstacles.length} Obstacles)`, icon: Play },
                  { id: 'tricks', label: 'Canine Clicker & Trick Engine', icon: Zap },
                  { id: 'mastery', label: `Agility XP & Trophy Shelf (${agilitySessions.length} Sessions)`, icon: Award }
                ].map(sub => {
                  const SubIcon = sub.icon;
                  const isCurrent = agilitySubTab === sub.id;
                  return (
                    <button
                      key={sub.id}
                      onClick={() => setAgilitySubTab(sub.id)}
                      style={{
                        flex: 1,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '8px',
                        padding: '12px 18px',
                        borderRadius: '12px',
                        background: isCurrent ? 'var(--color-primary)' : 'transparent',
                        color: isCurrent ? '#000' : 'var(--color-text-secondary)',
                        fontWeight: isCurrent ? 700 : 500,
                        border: 'none',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease',
                        fontSize: '14px'
                      }}
                    >
                      <SubIcon size={16} />
                      <span>{sub.label}</span>
                    </button>
                  );
                })}
              </div>

              {/* SUBTAB 1: VIRTUAL AGILITY ARENA */}
              {agilitySubTab === 'arena' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                  {/* Arena Controls Bar */}
                  <div className="glass-panel" style={{ padding: '16px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-secondary)' }}>ADD TO ARENA:</span>
                      <button
                        onClick={() => handleAddObstacle('HURDLE')}
                        style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)', color: '#FFF', padding: '6px 14px', borderRadius: '10px', fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        <span>🚧</span>
                        <span>+ Hurdle</span>
                      </button>
                      <button
                        onClick={() => handleAddObstacle('CONE')}
                        style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)', color: '#FFF', padding: '6px 14px', borderRadius: '10px', fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        <span>🔻</span>
                        <span>+ Slalom Cone</span>
                      </button>
                      <button
                        onClick={() => handleAddObstacle('TUNNEL')}
                        style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)', color: '#FFF', padding: '6px 14px', borderRadius: '10px', fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        <span>🌀</span>
                        <span>+ Speed Tunnel</span>
                      </button>
                      <button
                        onClick={() => handleAddObstacle('HOOP')}
                        style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)', color: '#FFF', padding: '6px 14px', borderRadius: '10px', fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        <span>⭕</span>
                        <span>+ Tire Hoop</span>
                      </button>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <span style={{ fontSize: '13px', color: 'var(--color-primary)', fontWeight: 700 }}>
                        {placedObstacles.filter(o => o.cleared).length} / {placedObstacles.length} Cleared (+{placedObstacles.filter(o => o.cleared).length * 20} XP)
                      </span>
                    </div>
                  </div>

                  {/* Interactive 2D/3D Obstacle Canvas */}
                  <div
                    style={{
                      position: 'relative',
                      height: '420px',
                      background: 'radial-gradient(ellipse at center, #064E3B 0%, #022C22 55%, #0B0F19 100%)',
                      borderRadius: '24px',
                      border: '2px solid rgba(16, 185, 129, 0.4)',
                      overflow: 'hidden',
                      boxShadow: '0 12px 40px rgba(0,0,0,0.5)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}
                  >
                    {/* Isometric Grid Floor pattern */}
                    <div style={{
                      position: 'absolute',
                      inset: 0,
                      backgroundImage: 'linear-gradient(rgba(16, 185, 129, 0.12) 1px, transparent 1px), linear-gradient(90deg, rgba(16, 185, 129, 0.12) 1px, transparent 1px)',
                      backgroundSize: '40px 40px',
                      opacity: 0.8
                    }} />

                    {/* Arena Target Center Ring */}
                    <div style={{
                      position: 'absolute',
                      width: '260px',
                      height: '260px',
                      borderRadius: '50%',
                      border: '2px dashed rgba(16, 185, 129, 0.3)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      pointerEvents: 'none'
                    }}>
                      <div style={{
                        width: '120px',
                        height: '120px',
                        borderRadius: '50%',
                        border: '1px solid rgba(245, 158, 11, 0.3)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'rgba(255, 255, 255, 0.4)',
                        fontSize: '12px',
                        fontWeight: 700,
                        letterSpacing: '1px'
                      }}>
                        AGILITY CORE
                      </div>
                    </div>

                    {/* Pet Avatar in Arena */}
                    <div style={{
                      position: 'absolute',
                      left: '50%',
                      top: '50%',
                      transform: 'translate(-50%, -50%)',
                      fontSize: '38px',
                      filter: 'drop-shadow(0 4px 12px rgba(0,0,0,0.6))',
                      zIndex: 2,
                      pointerEvents: 'none',
                      animation: 'bounce 2s infinite ease-in-out'
                    }}>
                      🐕
                    </div>

                    {/* Render Placed Obstacles */}
                    {placedObstacles.map((obs, idx) => (
                      <div
                        key={obs.id}
                        onClick={() => handleToggleObstacle(obs.id)}
                        style={{
                          position: 'absolute',
                          left: `${obs.x}%`,
                          top: `${obs.y}%`,
                          transform: 'translate(-50%, -50%)',
                          zIndex: 10,
                          cursor: 'pointer',
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          gap: '4px',
                          transition: 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)'
                        }}
                      >
                        <div
                          style={{
                            width: '54px',
                            height: '54px',
                            borderRadius: '16px',
                            background: obs.cleared ? 'linear-gradient(135deg, #10B981, #059669)' : 'rgba(30, 41, 59, 0.9)',
                            border: `2px solid ${obs.cleared ? '#34D399' : 'rgba(255,255,255,0.2)'}`,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '24px',
                            boxShadow: obs.cleared ? '0 0 20px rgba(16, 185, 129, 0.6)' : '0 4px 12px rgba(0,0,0,0.4)',
                            transform: obs.cleared ? 'scale(1.1)' : 'scale(1)'
                          }}
                        >
                          {obs.cleared ? '✓' : obs.icon}
                        </div>
                        <span style={{
                          background: 'rgba(0, 0, 0, 0.8)',
                          color: obs.cleared ? 'var(--color-primary)' : '#FFF',
                          fontSize: '10px',
                          fontWeight: 700,
                          padding: '2px 8px',
                          borderRadius: '8px',
                          whiteSpace: 'nowrap'
                        }}>
                          #{idx + 1} {obs.label}
                        </span>
                      </div>
                    ))}

                    {/* Hint overlay */}
                    <div style={{
                      position: 'absolute',
                      bottom: '16px',
                      left: '20px',
                      background: 'rgba(0, 0, 0, 0.6)',
                      padding: '8px 14px',
                      borderRadius: '10px',
                      fontSize: '12px',
                      color: 'var(--color-text-secondary)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}>
                      <span>💡 Click any obstacle to simulate pet leaping/clearing and trigger clicker feedback.</span>
                    </div>
                  </div>
                </div>
              )}

              {/* SUBTAB 2: CANINE CLICKER & TRICK ENGINE */}
              {agilitySubTab === 'tricks' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                  {/* Left: Active Trick Coach & Clicker Pad */}
                  <div className="glass-panel" style={{ padding: '28px', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
                    <div style={{ width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                      <span style={{ fontSize: '11px', fontWeight: 800, color: 'var(--color-warning)', letterSpacing: '1px', textTransform: 'uppercase' }}>
                        ACTIVE TRICK OBJECTIVE
                      </span>
                      <span style={{ background: 'rgba(14, 165, 233, 0.15)', color: 'var(--color-secondary)', padding: '2px 10px', borderRadius: '10px', fontSize: '12px', fontWeight: 700 }}>
                        {activeTrick.difficulty}
                      </span>
                    </div>

                    <h2 style={{ fontSize: '26px', margin: '0 0 6px 0', color: '#FFF' }}>{activeTrick.name}</h2>
                    <div style={{
                      background: 'rgba(255, 255, 255, 0.05)',
                      padding: '4px 14px',
                      borderRadius: '12px',
                      fontSize: '13px',
                      fontWeight: 700,
                      color: 'var(--color-primary)',
                      marginBottom: '16px'
                    }}>
                      Vocal Cue: &ldquo;{activeTrick.cueWord}&rdquo;
                    </div>

                    <p style={{ color: 'var(--color-text-secondary)', fontSize: '14px', lineHeight: 1.5, marginBottom: '24px', maxWidth: '420px' }}>
                      {activeTrick.description}
                    </p>

                    {/* Giant Clicker Button */}
                    <button
                      onClick={handleMarkClicker}
                      style={{
                        width: '180px',
                        height: '180px',
                        borderRadius: '50%',
                        background: isClickingAnim ? 'linear-gradient(135deg, #F59E0B, #D97706)' : 'linear-gradient(135deg, #10B981, #059669)',
                        border: '6px solid rgba(255,255,255,0.2)',
                        boxShadow: isClickingAnim ? '0 0 40px rgba(245, 158, 11, 0.8)' : '0 8px 32px rgba(16, 185, 129, 0.5)',
                        color: '#000',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: 'pointer',
                        transform: isClickingAnim ? 'scale(0.94)' : 'scale(1)',
                        transition: 'all 0.1s ease',
                        marginBottom: '20px'
                      }}
                    >
                      <Zap size={40} color="#000" fill="#000" />
                      <span style={{ fontWeight: 900, fontSize: '16px', marginTop: '6px', letterSpacing: '1px' }}>
                        CLICK!
                      </span>
                      <span style={{ fontSize: '10px', fontWeight: 700, opacity: 0.8 }}>MARK BEHAVIOR</span>
                    </button>

                    {/* Reps Progress */}
                    <div style={{ width: '100%', maxWidth: '320px', marginBottom: '16px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                        <span style={{ color: 'var(--color-text-secondary)' }}>Mastery Repetitions</span>
                        <strong style={{ color: '#FFF' }}>{activeTrick.repsCompleted} / {activeTrick.repsTarget} Reps</strong>
                      </div>
                      <div style={{ height: '8px', background: 'rgba(255,255,255,0.1)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ height: '100%', width: `${(activeTrick.repsCompleted / activeTrick.repsTarget) * 100}%`, background: 'var(--color-primary)', borderRadius: '4px', transition: 'width 0.3s ease' }} />
                      </div>
                    </div>

                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                      Total Acoustic Marks Today: <strong style={{ color: 'var(--color-primary)' }}>{clickerCount}</strong>
                    </div>
                  </div>

                  {/* Right: Trick Curriculum List */}
                  <div className="glass-panel" style={{ padding: '24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                      <h3 style={{ fontSize: '18px', margin: 0 }}>Canine Trick Curriculum</h3>
                      <span style={{ fontSize: '12px', color: 'var(--color-primary)', fontWeight: 700 }}>
                        {trickCurriculum.filter(t => t.mastered).length} / {trickCurriculum.length} Mastered
                      </span>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                      {trickCurriculum.map(trick => {
                        const isCurrentActive = activeTrick.id === trick.id;
                        return (
                          <div
                            key={trick.id}
                            onClick={() => {
                              setActiveTrick({
                                id: trick.id,
                                name: trick.name,
                                cueWord: trick.cue,
                                difficulty: trick.level,
                                description: trick.tip,
                                repsCompleted: trick.reps,
                                repsTarget: trick.targetReps,
                                xpReward: trick.xp
                              });
                            }}
                            style={{
                              padding: '14px 18px',
                              borderRadius: '14px',
                              background: isCurrentActive ? 'rgba(16, 185, 129, 0.12)' : 'rgba(255, 255, 255, 0.03)',
                              border: isCurrentActive ? '1px solid rgba(16, 185, 129, 0.5)' : '1px solid rgba(255, 255, 255, 0.06)',
                              cursor: 'pointer',
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              transition: 'all 0.2s ease'
                            }}
                          >
                            <div>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <span style={{ fontWeight: 700, fontSize: '15px', color: '#FFF' }}>{trick.name}</span>
                                {trick.mastered && (
                                  <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: 'var(--color-primary)', fontSize: '10px', padding: '1px 6px', borderRadius: '6px', fontWeight: 700 }}>
                                    ✓ MASTERED
                                  </span>
                                )}
                              </div>
                              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '3px' }}>
                                Cue: &ldquo;{trick.cue}&rdquo; • Level: {trick.level} • {trick.tip}
                              </div>
                            </div>

                            <div style={{ textAlign: 'right' }}>
                              <div style={{ fontSize: '13px', fontWeight: 800, color: 'var(--color-warning)' }}>
                                +{trick.xp} XP
                              </div>
                              <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                                {trick.reps}/{trick.targetReps} Reps
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              )}

              {/* SUBTAB 3: MASTERY LEVELS & TROPHY SHELF */}
              {agilitySubTab === 'mastery' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                  {/* Hero Level Card */}
                  <div
                    className="glass-panel"
                    style={{
                      padding: '32px',
                      background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.98))',
                      border: '2px solid rgba(245, 158, 11, 0.4)',
                      borderRadius: '24px',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <span style={{ fontSize: '11px', fontWeight: 800, color: 'var(--color-warning)', letterSpacing: '1.2px', textTransform: 'uppercase' }}>
                        AGILITY PROFICIENCY RANK
                      </span>
                      <h1 style={{ fontSize: '32px', margin: '4px 0 8px 0', color: '#FFF' }}>
                        🏆 {agilityMasteryLevel}
                      </h1>
                      <p style={{ color: 'var(--color-text-secondary)', fontSize: '14px', margin: 0 }}>
                        {activePet.name} has earned <strong>{agilityXp} Agility XP</strong> across indoor and outdoor training courses.
                      </p>
                    </div>

                    <div style={{ textAlign: 'right', minWidth: '240px' }}>
                      <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>
                        Progress to Grand Champion ({agilityXp}/1000 XP)
                      </div>
                      <div style={{ height: '10px', background: 'rgba(255,255,255,0.1)', borderRadius: '6px', overflow: 'hidden' }}>
                        <div style={{ height: '100%', width: `${Math.min(100, (agilityXp / 1000) * 100)}%`, background: 'linear-gradient(90deg, #F59E0B, #10B981)', borderRadius: '6px' }} />
                      </div>
                    </div>
                  </div>

                  {/* Trophy Badges Grid */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
                    {[
                      { icon: '🚧', title: 'Hurdle Master', desc: 'Cleared 20+ virtual jumps', unlocked: true },
                      { icon: '🌀', title: 'Tunnel Flash', desc: 'Under 5-second sprint through tunnel', unlocked: true },
                      { icon: '🔻', title: 'Slalom Wizard', desc: '5 consecutive cone weaves', unlocked: agilityXp >= 400 },
                      { icon: '👑', title: 'Grand Agility Champion', desc: 'Exceed 1,000 Agility XP', unlocked: agilityXp >= 1000 }
                    ].map((badge, i) => (
                      <div
                        key={i}
                        className="glass-panel"
                        style={{
                          padding: '20px',
                          textAlign: 'center',
                          opacity: badge.unlocked ? 1 : 0.45,
                          border: badge.unlocked ? '1px solid rgba(245, 158, 11, 0.4)' : '1px solid rgba(255,255,255,0.06)'
                        }}
                      >
                        <div style={{ fontSize: '36px', marginBottom: '8px' }}>{badge.icon}</div>
                        <div style={{ fontWeight: 700, fontSize: '15px', color: '#FFF' }}>{badge.title}</div>
                        <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>{badge.desc}</div>
                        <div style={{ marginTop: '10px', fontSize: '11px', fontWeight: 700, color: badge.unlocked ? 'var(--color-primary)' : 'var(--color-text-secondary)' }}>
                          {badge.unlocked ? '✓ UNLOCKED' : '🔒 LOCKED'}
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Agility Sessions History Table */}
                  <div className="glass-panel" style={{ padding: '24px' }}>
                    <h3 style={{ fontSize: '18px', margin: '0 0 16px 0' }}>Agility Training Session History</h3>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                      <thead>
                        <tr style={{ borderBottom: '1px solid var(--color-card-border)', color: 'var(--color-text-secondary)', fontSize: '13px' }}>
                          <th style={{ padding: '12px 16px' }}>Obstacle Course</th>
                          <th style={{ padding: '12px 16px' }}>Pet</th>
                          <th style={{ padding: '12px 16px' }}>Duration</th>
                          <th style={{ padding: '12px 16px' }}>Cleared</th>
                          <th style={{ padding: '12px 16px' }}>XP Earned</th>
                        </tr>
                      </thead>
                      <tbody>
                        {agilitySessions.map(session => (
                          <tr key={session.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                            <td style={{ padding: '14px 16px', fontWeight: 600, color: '#FFF' }}>{session.obstacleCourse}</td>
                            <td style={{ padding: '14px 16px', color: 'var(--color-text-secondary)' }}>{session.petName}</td>
                            <td style={{ padding: '14px 16px', color: 'var(--color-text-secondary)' }}>{Math.round(session.durationSec / 60)} mins</td>
                            <td style={{ padding: '14px 16px', color: 'var(--color-primary)', fontWeight: 600 }}>{session.obstaclesCleared} Obstacles</td>
                            <td style={{ padding: '14px 16px', color: 'var(--color-warning)', fontWeight: 700 }}>+{session.xpEarned} XP</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB: FEEDING TRACKER */}
          {activeTab === 'feeding' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <h1 style={{ fontSize: '28px', margin: 0 }}>Feeding & Meal Logs for {activePet.name}</h1>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '4px' }}>Track daily calorie intake, nutrition balance, and AI food vision scans synced with mobile.</p>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button 
                    onClick={() => {
                      handleRunAiFoodScan();
                      setShowAiFoodModal(true);
                    }}
                    className="btn-primary" 
                    style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'linear-gradient(135deg, #10B981, #059669)' }}
                  >
                    <Camera size={18} />
                    <span>AI Vision Food Scan</span>
                  </button>
                  <button 
                    className="btn-primary" 
                    onClick={() => setShowMealModal(true)}
                    style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                  >
                    + Manual Log
                  </button>
                </div>
              </div>

              <div className="glass-panel" style={{ padding: '24px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--color-card-border)', color: 'var(--color-text-secondary)', fontSize: '14px' }}>
                      <th style={{ padding: '14px' }}>Food Product</th>
                      <th style={{ padding: '14px' }}>Portion Size</th>
                      <th style={{ padding: '14px' }}>Calories</th>
                      <th style={{ padding: '14px' }}>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {petMeals.map(meal => (
                      <tr key={meal.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                        <td style={{ padding: '16px', fontWeight: 600 }}>{meal.name}</td>
                        <td style={{ padding: '16px' }}>{meal.portion}g</td>
                        <td style={{ padding: '16px', color: 'var(--color-primary)', fontWeight: 600 }}>{meal.calories} kcal</td>
                        <td style={{ padding: '16px', color: 'var(--color-text-secondary)' }}>{meal.time}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB: WEIGHT & GROWTH */}
          {activeTab === 'weight' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <h1 style={{ fontSize: '28px', margin: 0 }}>Weight Tracker & Growth Trends</h1>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '4px' }}>Monitor {activePet.name}'s weight history and sync with phone in real time.</p>
                </div>
                <button className="btn-primary" onClick={() => setShowWeightModal(true)}>
                  + Log New Weight
                </button>
              </div>

              {/* Weight Progression Card */}
              <div className="glass-panel" style={{ padding: '32px', marginBottom: '32px' }}>
                <h3 style={{ marginBottom: '20px' }}>Historical Weigh-Ins</h3>
                <div style={{ display: 'flex', alignItems: 'flex-end', gap: '32px', height: '180px', paddingBottom: '20px', borderBottom: '1px solid var(--color-card-border)' }}>
                  {petWeights.map(w => {
                    const heightPercent = Math.min(100, Math.max(30, (w.weight / 35) * 100));
                    return (
                      <div key={w.id} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--color-primary)' }}>{w.weight} kg</div>
                        <div style={{
                          width: '44px',
                          height: `${heightPercent}px`,
                          background: 'linear-gradient(to top, var(--color-primary), #34D399)',
                          borderRadius: '8px'
                        }} />
                        <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>{w.date}</div>
                      </div>
                    );
                  })}
                </div>
                <div style={{ marginTop: '20px', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                  {petWeights.map(w => (
                    <div key={w.id} style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                      • <strong>{w.date}:</strong> {w.notes} ({w.weight} kg)
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB: TELE-VET & APPOINTMENTS */}
          {activeTab === 'vet' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <h1 style={{ fontSize: '28px', margin: 0 }}>Tele-Vet Virtual Care & Appointments</h1>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '4px' }}>Direct consultations with certified veterinarians and appointment scheduling.</p>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button className="btn-primary" onClick={() => setShowVetModal(true)} style={{ background: 'transparent', border: '1px solid var(--color-card-border)', color: '#FFF' }}>
                    Schedule Visit
                  </button>
                  <button className="btn-primary" onClick={() => setShowVideoCall(true)}>
                    Join Video Consultation
                  </button>
                </div>
              </div>

              {/* Consultation Hero Card */}
              <div className="glass-panel" style={{ padding: '32px', marginBottom: '32px', background: 'linear-gradient(135deg, rgba(14, 165, 233, 0.15), rgba(16, 185, 129, 0.1))', border: '1px solid rgba(14, 165, 233, 0.3)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <span style={{ background: 'rgba(14, 165, 233, 0.2)', color: 'var(--color-secondary)', padding: '6px 14px', borderRadius: '20px', fontSize: '13px', fontWeight: 700 }}>
                      TELE-VET ON DEMAND
                    </span>
                    <h2 style={{ fontSize: '28px', marginTop: '12px', marginBottom: '8px' }}>Dr. Priya Sharma, BVSc is Available</h2>
                    <p style={{ color: 'var(--color-text-secondary)', maxWidth: '550px', lineHeight: 1.6 }}>
                      Need quick medical advice regarding {activePet.name}? Launch a secure, encrypted video consultation with certified doctors directly from your browser.
                    </p>
                  </div>
                  <button 
                    onClick={() => setShowVideoCall(true)}
                    style={{
                      background: 'var(--color-secondary)',
                      color: '#FFF',
                      padding: '16px 32px',
                      borderRadius: '16px',
                      border: 'none',
                      fontWeight: 700,
                      fontSize: '16px',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '10px'
                    }}
                  >
                    <Video size={20} />
                    <span>Start Video Call</span>
                  </button>
                </div>
              </div>

              {/* Upcoming Clinic Visits */}
              <div className="glass-panel" style={{ padding: '28px' }}>
                <h3 style={{ marginBottom: '20px' }}>Scheduled Clinic Appointments</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {petAppts.map(a => (
                    <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '18px 24px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '16px' }}>
                      <div>
                        <div style={{ fontSize: '18px', fontWeight: 700 }}>{a.clinic}</div>
                        <div style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginTop: '4px' }}>
                          Purpose: {a.purpose} • Doctor: {a.doctor}
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontWeight: 600, color: 'var(--color-primary)' }}>{a.date}</div>
                        <span style={{ fontSize: '12px', color: 'var(--color-success)', background: 'rgba(74, 222, 128, 0.1)', padding: '3px 10px', borderRadius: '12px' }}>
                          ✓ Calendar Synced
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB: LOST PET MODE */}
          {activeTab === 'lostpet' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <h1 style={{ fontSize: '28px', margin: 0, color: 'var(--color-warning)' }}>🚨 Emergency Lost Pet Hub</h1>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '4px' }}>Instantly generate broadcast flyers and print physical posters.</p>
                </div>
                <button 
                  onClick={() => window.print()}
                  className="btn-primary" 
                  style={{ background: 'var(--color-warning)', color: '#000' }}
                >
                  Print / Download Flyer
                </button>
              </div>

              {/* Flyer Preview Card */}
              <div style={{ display: 'flex', gap: '32px' }}>
                {/* Form Controls */}
                <div className="glass-panel" style={{ flex: 1, padding: '28px' }}>
                  <h3 style={{ marginBottom: '20px' }}>Flyer Information</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Contact Phone Number</label>
                      <input 
                        defaultValue="+91 98401 23456" 
                        style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }} 
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Reward Amount</label>
                      <input 
                        defaultValue="₹5,000" 
                        style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }} 
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Microchip Registry ID</label>
                      <input 
                        defaultValue="985141002348123" 
                        readOnly 
                        style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid var(--color-card-border)', color: 'var(--color-text-secondary)' }} 
                      />
                    </div>
                  </div>
                </div>

                {/* Poster Preview */}
                <div style={{
                  width: '380px',
                  background: '#FFF3E0',
                  color: '#000',
                  padding: '32px',
                  borderRadius: '16px',
                  textAlign: 'center',
                  boxShadow: '0 20px 40px rgba(0,0,0,0.5)'
                }}>
                  <div style={{ fontSize: '28px', fontWeight: 900, color: '#D32F2F', letterSpacing: '1px' }}>🚨 LOST PET 🚨</div>
                  <img 
                    src={activePet.photoUrl} 
                    alt={activePet.name} 
                    style={{ width: '160px', height: '160px', borderRadius: '50%', objectFit: 'cover', margin: '20px auto', border: '4px solid #D32F2F' }} 
                  />
                  <h2 style={{ fontSize: '32px', margin: '8px 0', color: '#000' }}>{activePet.name}</h2>
                  <p style={{ fontSize: '16px', color: '#4B5563', margin: 0 }}>{activePet.breed} • {activePet.species}</p>
                  <div style={{ background: '#E65100', color: '#FFF', padding: '8px 16px', borderRadius: '8px', fontWeight: 800, margin: '20px 0', fontSize: '18px' }}>
                    REWARD: ₹5,000
                  </div>
                  <div style={{ fontSize: '14px', fontWeight: 700, color: '#374151' }}>Please Contact Owner:</div>
                  <div style={{ fontSize: '20px', fontWeight: 900, color: '#000', marginTop: '4px' }}>📞 +91 98401 23456</div>
                </div>
              </div>
            </div>
          )}

          {/* TAB: COLLAR & WALK GPS */}
          {activeTab === 'collar' && (
            <div>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>Smart Collar Live Telemetry & GPS Walk Map</h1>
                    <span style={{ 
                      background: activeWalk.isActive ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.08)', 
                      color: activeWalk.isActive ? 'var(--color-primary)' : 'var(--color-text-secondary)', 
                      padding: '4px 12px', 
                      borderRadius: '20px', 
                      fontSize: '13px', 
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <div className="live-beacon" style={{ background: activeWalk.isActive ? 'var(--color-primary)' : 'var(--color-text-secondary)' }} />
                      {activeWalk.isActive ? 'Live Walk Active' : 'Collar Standby'}
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Real-time BLE sensor streaming & GPS location tracking from your pet's smart collar.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <button 
                    onClick={() => setSimulatingWalk(!simulatingWalk)}
                    style={{
                      background: simulatingWalk ? 'rgba(251, 113, 133, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                      color: simulatingWalk ? 'var(--color-danger)' : 'var(--color-primary)',
                      border: `1px solid ${simulatingWalk ? 'rgba(251, 113, 133, 0.4)' : 'rgba(16, 185, 129, 0.4)'}`,
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                  >
                    {simulatingWalk ? <Square size={16} /> : <Play size={16} />}
                    <span>{simulatingWalk ? 'Pause Live Stream' : 'Simulate Walk Demo'}</span>
                  </button>

                  <a
                    href={`https://www.google.com/maps/search/?api=1&query=${activeWalk.latitude || 13.0827},${activeWalk.longitude || 80.2707}`}
                    target="_blank"
                    rel="noreferrer"
                    style={{
                      textDecoration: 'none',
                      background: 'rgba(255,255,255,0.06)',
                      color: '#FFF',
                      border: '1px solid var(--color-card-border)',
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                  >
                    <ExternalLink size={16} />
                    <span>Open Google Maps</span>
                  </a>
                </div>
              </div>

              {/* Hardware Device Info Banner */}
              <div className="glass-panel" style={{ padding: '20px 24px', marginBottom: '28px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div style={{ width: '48px', height: '48px', borderRadius: '14px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                    <Bluetooth size={24} />
                  </div>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '16px' }}>{collarTelemetry.deviceName || "PawLife Collar BLE-89X"}</div>
                    <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                      UUID: 4A89-BLE-PAW • Protocol: BLE 5.2 Low Energy • Firmware: v2.4.1
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '32px' }}>
                  <div>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Signal Strength</div>
                    <div style={{ fontWeight: 700, color: 'var(--color-success)', marginTop: '2px' }}>-54 dBm (Strong)</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Battery Status</div>
                    <div style={{ fontWeight: 700, color: 'var(--color-primary)', marginTop: '2px' }}>{collarTelemetry.batteryLevel || 92}% Healthy</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Telemetry Sync</div>
                    <div style={{ fontWeight: 700, color: '#F8FAFC', marginTop: '2px' }}>
                      {new Date(collarTelemetry.updatedAt || Date.now()).toLocaleTimeString()}
                    </div>
                  </div>
                </div>
              </div>

              {/* Telemetry Metric HUD Cards */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '32px' }}>
                {/* 1. Live BPM */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>Heart Rate (BPM)</span>
                    <Heart size={20} className="heart-beat" color="#FB7185" fill="#FB7185" />
                  </div>
                  <div style={{ fontSize: '36px', fontWeight: 800, color: '#FB7185' }}>
                    {collarTelemetry.heartRateBpm || 84} <span style={{ fontSize: '16px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>bpm</span>
                  </div>
                  <div style={{ marginTop: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ 
                      fontSize: '11px', 
                      padding: '3px 8px', 
                      borderRadius: '6px', 
                      background: (collarTelemetry.heartRateBpm || 84) > 105 ? 'rgba(251, 191, 36, 0.2)' : 'rgba(74, 222, 128, 0.2)',
                      color: (collarTelemetry.heartRateBpm || 84) > 105 ? 'var(--color-warning)' : 'var(--color-success)',
                      fontWeight: 700
                    }}>
                      {(collarTelemetry.heartRateBpm || 84) > 105 ? 'Cardio Zone' : 'Resting Baseline'}
                    </span>
                    <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Min 74 / Max 128</span>
                  </div>
                </div>

                {/* 2. Steps */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>Daily Collar Steps</span>
                    <Zap size={20} color="var(--color-secondary)" />
                  </div>
                  <div style={{ fontSize: '36px', fontWeight: 800, color: 'var(--color-secondary)' }}>
                    {collarTelemetry.stepCount?.toLocaleString() || '1,420'}
                  </div>
                  <div style={{ marginTop: '12px' }}>
                    <div style={{ height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${Math.min(100, Math.round(((collarTelemetry.stepCount || 1420) / 10000) * 100))}%`, height: '100%', background: 'var(--color-secondary)' }} />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                      <span>Target: 10,000</span>
                      <span>{Math.min(100, Math.round(((collarTelemetry.stepCount || 1420) / 10000) * 100))}%</span>
                    </div>
                  </div>
                </div>

                {/* 3. Collar Battery */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>Battery Level</span>
                    <BatteryCharging size={20} color="var(--color-primary)" />
                  </div>
                  <div style={{ fontSize: '36px', fontWeight: 800, color: 'var(--color-primary)' }}>
                    {collarTelemetry.batteryLevel || 92}%
                  </div>
                  <div style={{ marginTop: '12px' }}>
                    <div style={{ height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${collarTelemetry.batteryLevel || 92}%`, height: '100%', background: 'var(--color-primary)' }} />
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                      Estimated ~96 hours remaining
                    </div>
                  </div>
                </div>

                {/* 4. Calories */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>Energy Burned</span>
                    <Flame size={20} color="var(--color-warning)" />
                  </div>
                  <div style={{ fontSize: '36px', fontWeight: 800, color: 'var(--color-warning)' }}>
                    {Math.round(((activeWalk.distanceMeters || 2450) * 0.05) + ((collarTelemetry.stepCount || 1420) * 0.03))} <span style={{ fontSize: '16px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>kcal</span>
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '12px' }}>
                    Active burn based on weight ({activePet.weight} kg)
                  </div>
                </div>
              </div>

              {/* Interactive GPS Walk Map Visualizer */}
              <div className="glass-panel" style={{ padding: '28px', marginBottom: '32px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <div>
                    <h3 style={{ fontSize: '20px', margin: 0 }}>Interactive GPS Walk Map</h3>
                    <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                      Coordinates: <span style={{ color: '#F8FAFC', fontWeight: 600 }}>{activeWalk.latitude?.toFixed(5) || '13.08270'}° N, {activeWalk.longitude?.toFixed(5) || '80.27070'}° E</span> • Accuracy: ±3m
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Walk Distance</div>
                      <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-primary)' }}>
                        {((activeWalk.distanceMeters || 2450) / 1000).toFixed(2)} km
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Walk Duration</div>
                      <div style={{ fontSize: '18px', fontWeight: 800 }}>
                        {Math.floor((activeWalk.durationSeconds || 1540) / 60)}m {((activeWalk.durationSeconds || 1540) % 60)}s
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Pace</div>
                      <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-secondary)' }}>
                        12'40" /km
                      </div>
                    </div>
                  </div>
                </div>

                {/* SVG Visual GPS Map View */}
                <div style={{
                  width: '100%',
                  height: '360px',
                  borderRadius: '16px',
                  background: 'linear-gradient(180deg, #0d1527 0%, #080c14 100%)',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  position: 'relative',
                  overflow: 'hidden',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  {/* Subtle Grid Lines */}
                  <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', opacity: 0.15 }}>
                    <defs>
                      <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                        <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#FFF" strokeWidth="0.5" />
                      </pattern>
                    </defs>
                    <rect width="100%" height="100%" fill="url(#grid)" />
                  </svg>

                  {/* Route Polyline Trail */}
                  <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%' }} viewBox="0 0 800 360">
                    {/* Road simulation curves */}
                    <path 
                      d="M 120 280 Q 240 180 340 220 T 520 140 T 680 110" 
                      fill="none" 
                      stroke="rgba(16, 185, 129, 0.25)" 
                      strokeWidth="14" 
                      strokeLinecap="round" 
                    />
                    <path 
                      d="M 120 280 Q 240 180 340 220 T 520 140 T 680 110" 
                      fill="none" 
                      stroke="var(--color-primary)" 
                      strokeWidth="4" 
                      strokeLinecap="round" 
                      strokeDasharray="6,4"
                    />

                    {/* Start Pin */}
                    <circle cx="120" cy="280" r="8" fill="#10B981" />
                    <text x="120" y="310" textAnchor="middle" fill="#94A3B8" fontSize="12" fontWeight="600">Start (Home)</text>

                    {/* Mid points */}
                    <circle cx="340" cy="220" r="5" fill="#0EA5E9" />
                    <circle cx="520" cy="140" r="5" fill="#0EA5E9" />

                    {/* Current Pet Location Pin */}
                    <circle cx="680" cy="110" r="24" fill="rgba(16, 185, 129, 0.25)" className="live-beacon" />
                    <circle cx="680" cy="110" r="12" fill="#10B981" stroke="#FFF" strokeWidth="3" />
                  </svg>

                  {/* Floating Map HUD Card */}
                  <div style={{
                    position: 'absolute',
                    top: '20px',
                    left: '20px',
                    background: 'rgba(15, 23, 42, 0.85)',
                    backdropFilter: 'blur(8px)',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    padding: '12px 18px',
                    borderRadius: '12px',
                    fontSize: '13px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Navigation size={16} color="var(--color-primary)" />
                      <span style={{ fontWeight: 700 }}>Heading: North-East (42°)</span>
                    </div>
                    <div style={{ color: 'var(--color-text-secondary)', marginTop: '4px', fontSize: '11px' }}>
                      Live GPS Stream from Galaxy Device
                    </div>
                  </div>

                  {/* Floating Pet Marker Label */}
                  <div style={{
                    position: 'absolute',
                    top: '55px',
                    right: '80px',
                    background: 'rgba(16, 185, 129, 0.95)',
                    color: '#000',
                    fontWeight: 700,
                    padding: '6px 14px',
                    borderRadius: '20px',
                    fontSize: '13px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    boxShadow: '0 8px 24px rgba(16, 185, 129, 0.4)'
                  }}>
                    <span>🐕</span>
                    <span>{activePet.name} is walking here</span>
                  </div>

                  {/* Satellite Watermark */}
                  <div style={{
                    position: 'absolute',
                    bottom: '16px',
                    right: '20px',
                    fontSize: '11px',
                    color: 'rgba(255,255,255,0.4)',
                    background: 'rgba(0,0,0,0.5)',
                    padding: '4px 10px',
                    borderRadius: '6px'
                  }}>
                    Satellite High-Precision Telemetry
                  </div>
                </div>
              </div>

              {/* Past Walk Logs History */}
              <div className="glass-panel" style={{ padding: '28px' }}>
                <h3 style={{ fontSize: '20px', marginBottom: '20px' }}>Walk History & Activity Timeline</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {walkHistory.map((walk) => (
                    <div 
                      key={walk.id} 
                      style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'center', 
                        padding: '16px 20px', 
                        background: 'rgba(255, 255, 255, 0.03)', 
                        borderRadius: '14px',
                        border: '1px solid rgba(255, 255, 255, 0.05)'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <div style={{ width: '42px', height: '42px', borderRadius: '50%', background: 'rgba(16, 185, 129, 0.15)', color: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          <Compass size={20} />
                        </div>
                        <div>
                          <div style={{ fontWeight: 600 }}>Walk Session • {walk.date}</div>
                          <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                            Duration: {Math.round((walk.durationSeconds || 1200) / 60)} mins • {walk.calories || 110} kcal burned
                          </div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-primary)' }}>
                            {((walk.distanceMeters || 2000) / 1000).toFixed(2)} km
                          </div>
                          <div style={{ fontSize: '11px', color: 'var(--color-success)' }}>✓ GPS Verified Trail</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB: AI HEALTH DIAGNOSTIC & TRIAGE */}
          {activeTab === 'ai_triage' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>AI Health Diagnostic & Triage</h1>
                    <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: 'var(--color-primary)', padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
                      Multi-Modal Clinical Model
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Automated veterinary symptom triage, severity categorization, and exportable clinical PDF reports.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <button 
                    onClick={() => handleOpenVetReport(aiTriageReports[0])}
                    className="btn-primary" 
                    style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                  >
                    <Printer size={18} />
                    <span>Export Clinical Vet PDF</span>
                  </button>
                  <button 
                    onClick={() => setActiveTab('vet')}
                    style={{
                      background: 'rgba(255,255,255,0.06)',
                      color: '#FFF',
                      border: '1px solid var(--color-card-border)',
                      padding: '10px 18px',
                      borderRadius: '12px',
                      fontWeight: 600,
                      cursor: 'pointer'
                    }}
                  >
                    Book Clinic Visit
                  </button>
                </div>
              </div>

              {/* Triage Scanner Form Card */}
              <div className="glass-panel" style={{ padding: '32px', marginBottom: '32px' }}>
                <h3 style={{ fontSize: '20px', marginBottom: '8px' }}>Scan New Symptoms for {activePet.name}</h3>
                <p style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginBottom: '20px' }}>
                  Select a common clinical indicator or describe observed behaviors, rash, cough, or appetite changes.
                </p>

                {/* Quick Symptom Pills */}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginBottom: '20px' }}>
                  {[
                    "Persistent dry cough after morning walk",
                    "Redness & scratching around left ear",
                    "Sudden limping on right hind leg",
                    "Lethargy & refused breakfast kibble",
                    "Acute vomiting or suspected toxic ingestion"
                  ].map((symptom, idx) => (
                    <button
                      key={idx}
                      onClick={() => {
                        setSelectedSymptom(symptom);
                        handleRunAiTriage(symptom);
                      }}
                      style={{
                        padding: '8px 16px',
                        borderRadius: '20px',
                        background: selectedSymptom === symptom ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                        border: selectedSymptom === symptom ? '1px solid var(--color-primary)' : '1px solid var(--color-card-border)',
                        color: selectedSymptom === symptom ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                        fontSize: '13px',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      {symptom}
                    </button>
                  ))}
                </div>

                <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    <textarea
                      rows={3}
                      value={selectedSymptom}
                      onChange={e => setSelectedSymptom(e.target.value)}
                      placeholder="Describe what you observed (e.g. coughing, head shaking, skin redness)..."
                      style={{
                        width: '100%',
                        padding: '14px',
                        borderRadius: '12px',
                        background: 'rgba(255, 255, 255, 0.04)',
                        border: '1px solid var(--color-card-border)',
                        color: '#FFF',
                        fontSize: '14px',
                        lineHeight: 1.5,
                        resize: 'none'
                      }}
                    />
                  </div>

                  <button
                    disabled={isAnalyzingAi}
                    onClick={() => handleRunAiTriage()}
                    className="btn-primary"
                    style={{
                      padding: '16px 28px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '10px',
                      opacity: isAnalyzingAi ? 0.7 : 1
                    }}
                  >
                    <Sparkles size={18} />
                    <span>{isAnalyzingAi ? 'Analyzing Clinical Vitals...' : 'Run Diagnostic Scan'}</span>
                  </button>
                </div>
              </div>

              {/* Latest Diagnostic Assessment Result */}
              {aiTriageReports.length > 0 && (
                <div className="glass-panel" style={{
                  padding: '32px',
                  marginBottom: '32px',
                  border: aiTriageReports[0].urgency === 'EMERGENCY' ? '1px solid rgba(239, 68, 68, 0.5)' : aiTriageReports[0].urgency === 'MODERATE' ? '1px solid rgba(245, 158, 11, 0.5)' : '1px solid rgba(16, 185, 129, 0.5)',
                  background: aiTriageReports[0].urgency === 'EMERGENCY' ? 'rgba(239, 68, 68, 0.05)' : aiTriageReports[0].urgency === 'MODERATE' ? 'rgba(245, 158, 11, 0.04)' : 'rgba(16, 185, 129, 0.04)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <span style={{
                          padding: '6px 14px',
                          borderRadius: '20px',
                          fontWeight: 800,
                          fontSize: '13px',
                          background: aiTriageReports[0].urgency === 'EMERGENCY' ? '#EF4444' : aiTriageReports[0].urgency === 'MODERATE' ? '#F59E0B' : 'var(--color-primary)',
                          color: '#000'
                        }}>
                          {aiTriageReports[0].urgency === 'EMERGENCY' ? '🚨 CRITICAL EMERGENCY' : aiTriageReports[0].urgency === 'MODERATE' ? '⚠️ SCHEDULE VET VISIT' : '💚 MILD OBSERVATION'}
                        </span>
                        <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                          Analyzed {new Date(aiTriageReports[0].timestamp || Date.now()).toLocaleTimeString()}
                        </span>
                      </div>
                      <h2 style={{ fontSize: '22px', marginTop: '12px', marginBottom: '4px' }}>
                        Reported Symptoms: "{aiTriageReports[0].symptoms}"
                      </h2>
                    </div>

                    <button 
                      onClick={() => handleOpenVetReport(aiTriageReports[0])}
                      className="btn-primary" 
                      style={{ padding: '10px 18px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      <Printer size={16} />
                      <span>Print PDF Report</span>
                    </button>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
                    <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '20px', borderRadius: '14px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <h4 style={{ fontSize: '14px', color: 'var(--color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '10px' }}>
                        Algorithmic Clinical Assessment & Findings
                      </h4>
                      <p style={{ fontSize: '15px', lineHeight: 1.7, margin: 0, color: '#F1F5F9' }}>
                        {aiTriageReports[0].findings}
                      </p>
                    </div>

                    <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '20px', borderRadius: '14px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <h4 style={{ fontSize: '14px', color: 'var(--color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '10px' }}>
                        Patient Summary Snapshot
                      </h4>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '13px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span style={{ color: 'var(--color-text-secondary)' }}>Patient:</span>
                          <strong>{activePet.name} ({activePet.species})</strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span style={{ color: 'var(--color-text-secondary)' }}>Current Weight:</span>
                          <strong>{activePet.weight} kg</strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span style={{ color: 'var(--color-text-secondary)' }}>Collar Vitals:</span>
                          <strong>{collarTelemetry.heartRateBpm} BPM (Stable)</strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span style={{ color: 'var(--color-text-secondary)' }}>Attending Vet:</span>
                          <strong>Dr. Priya Sharma</strong>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Historical Diagnostic Reports */}
              <div className="glass-panel" style={{ padding: '28px' }}>
                <h3 style={{ fontSize: '20px', marginBottom: '20px' }}>Prior Diagnostic Triage History</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {aiTriageReports.map(rep => (
                    <div 
                      key={rep.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '16px 20px',
                        background: 'rgba(255,255,255,0.03)',
                        borderRadius: '14px',
                        border: '1px solid rgba(255,255,255,0.05)'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <div style={{
                          width: '40px',
                          height: '40px',
                          borderRadius: '50%',
                          background: rep.urgency === 'EMERGENCY' ? 'rgba(239, 68, 68, 0.2)' : rep.urgency === 'MODERATE' ? 'rgba(245, 158, 11, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}>
                          <Sparkles size={20} color={rep.urgency === 'EMERGENCY' ? '#EF4444' : rep.urgency === 'MODERATE' ? '#F59E0B' : 'var(--color-primary)'} />
                        </div>
                        <div>
                          <div style={{ fontWeight: 600 }}>{rep.symptoms}</div>
                          <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                            {new Date(rep.timestamp || Date.now()).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })} • Assessment: {rep.findings.slice(0, 75)}...
                          </div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <span style={{
                          fontSize: '11px',
                          fontWeight: 700,
                          padding: '3px 10px',
                          borderRadius: '8px',
                          background: rep.urgency === 'EMERGENCY' ? 'rgba(239, 68, 68, 0.2)' : rep.urgency === 'MODERATE' ? 'rgba(245, 158, 11, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                          color: rep.urgency === 'EMERGENCY' ? '#EF4444' : rep.urgency === 'MODERATE' ? '#F59E0B' : 'var(--color-primary)'
                        }}>
                          {rep.urgency}
                        </span>
                        <button
                          onClick={() => handleOpenVetReport(rep)}
                          style={{
                            background: 'rgba(255,255,255,0.06)',
                            border: '1px solid var(--color-card-border)',
                            color: '#FFF',
                            borderRadius: '8px',
                            padding: '6px 12px',
                            fontSize: '12px',
                            cursor: 'pointer'
                          }}
                        >
                          View PDF
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB: FAMILY HUB & MULTI-USER CO-PARENTING */}
          {activeTab === 'family' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>Family Hub & Co-Parenting</h1>
                    <span style={{ background: 'rgba(14, 165, 233, 0.2)', color: 'var(--color-secondary)', padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
                      Live Cloud Sync Board
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Shared real-time chore checklist, feeding duties, and walking responsibilities between co-parents and caretakers.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '16px' }}>
                  <div style={{ background: 'rgba(255,255,255,0.05)', padding: '8px 16px', borderRadius: '12px', border: '1px solid var(--color-card-border)', textAlign: 'center' }}>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-primary)' }}>
                      {familyTasks.filter(t => t.isCompleted).length} / {familyTasks.length}
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>Completed Today</div>
                  </div>
                </div>
              </div>

              {/* Co-Parenting Team Roster */}
              <div className="glass-panel" style={{ padding: '24px', marginBottom: '32px' }}>
                <h3 style={{ fontSize: '18px', marginBottom: '16px' }}>Co-Parenting Care Team</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
                  {coParentTeam.map((member, idx) => (
                    <div 
                      key={idx}
                      style={{
                        padding: '16px',
                        background: 'rgba(255, 255, 255, 0.03)',
                        borderRadius: '14px',
                        border: '1px solid rgba(255, 255, 255, 0.06)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '12px'
                      }}
                    >
                      <div style={{ fontSize: '28px' }}>{member.avatar}</div>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '14px' }}>{member.name}</div>
                        <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>{member.role}</div>
                        <div style={{ fontSize: '11px', color: 'var(--color-success)', marginTop: '2px' }}>● {member.status}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Shared Live Chore Checklist */}
              <div className="glass-panel" style={{ padding: '28px', marginBottom: '32px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <h3 style={{ fontSize: '20px', margin: 0 }}>Daily Care Duties & Chores</h3>
                  <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                    Tap checkbox to sync real-time status across all devices
                  </span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
                  {familyTasks.map(task => (
                    <div
                      key={task.id}
                      onClick={() => handleToggleFamilyTask(task)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '16px 20px',
                        borderRadius: '14px',
                        background: task.isCompleted ? 'rgba(16, 185, 129, 0.08)' : 'rgba(255, 255, 255, 0.03)',
                        border: task.isCompleted ? '1px solid rgba(16, 185, 129, 0.3)' : '1px solid rgba(255, 255, 255, 0.06)',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <div style={{
                          width: '24px',
                          height: '24px',
                          borderRadius: '6px',
                          border: task.isCompleted ? '2px solid var(--color-primary)' : '2px solid rgba(255, 255, 255, 0.3)',
                          background: task.isCompleted ? 'var(--color-primary)' : 'transparent',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: '#000',
                          fontWeight: 900,
                          fontSize: '14px'
                        }}>
                          {task.isCompleted && '✓'}
                        </div>
                        <div>
                          <div style={{
                            fontWeight: 600,
                            fontSize: '15px',
                            textDecoration: task.isCompleted ? 'line-through' : 'none',
                            color: task.isCompleted ? 'var(--color-text-secondary)' : '#F8FAFC'
                          }}>
                            {task.title}
                          </div>
                          <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                            Scheduled for {task.time || 'Today'}
                          </div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <span style={{
                          padding: '4px 10px',
                          borderRadius: '8px',
                          fontSize: '12px',
                          background: 'rgba(255, 255, 255, 0.06)',
                          color: 'var(--color-text-secondary)'
                        }}>
                          👤 {task.assignee}
                        </span>
                        <span style={{
                          fontSize: '11px',
                          fontWeight: 700,
                          color: task.isCompleted ? 'var(--color-primary)' : 'var(--color-warning)'
                        }}>
                          {task.isCompleted ? 'COMPLETED' : 'PENDING'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Add New Chore Form */}
                <form onSubmit={handleAddFamilyTask} style={{ display: 'flex', gap: '12px', paddingTop: '16px', borderTop: '1px solid var(--color-card-border)' }}>
                  <input
                    required
                    placeholder="Add a new pet chore (e.g. Clean water fountain, Give joint chew)..."
                    value={newChoreTitle}
                    onChange={e => setNewChoreTitle(e.target.value)}
                    style={{
                      flex: 1,
                      padding: '12px 16px',
                      borderRadius: '12px',
                      background: 'rgba(255, 255, 255, 0.05)',
                      border: '1px solid var(--color-card-border)',
                      color: '#FFF',
                      fontSize: '14px'
                    }}
                  />
                  <select
                    value={newChoreAssignee}
                    onChange={e => setNewChoreAssignee(e.target.value)}
                    style={{
                      padding: '12px 16px',
                      borderRadius: '12px',
                      background: '#1E293B',
                      border: '1px solid var(--color-card-border)',
                      color: '#FFF',
                      fontSize: '14px'
                    }}
                  >
                    <option value="You">Assign: You</option>
                    <option value="Sarah (Co-Parent)">Assign: Sarah</option>
                    <option value="Alex (Walker)">Assign: Alex</option>
                    <option value="Dr. Priya Sharma">Assign: Dr. Sharma</option>
                  </select>
                  <button type="submit" className="btn-primary" style={{ padding: '12px 20px', whiteSpace: 'nowrap' }}>
                    + Add Chore
                  </button>
                </form>
              </div>
            </div>
          )}

          {/* TAB: STORE & SMART PANTRY */}
          {activeTab === 'marketplace' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>Smart Pantry & Marketplace</h1>
                    <span style={{ background: 'rgba(245, 158, 11, 0.2)', color: 'var(--color-warning)', padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
                      Auto-Refill & Delivery
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Smart kibble inventory level tracking, one-click automated refills, and veterinary pet products.
                  </p>
                </div>

                <button 
                  onClick={() => setShowCartModal(true)}
                  className="btn-primary" 
                  style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 20px' }}
                >
                  <ShoppingCart size={18} />
                  <span>View Cart ({cart.reduce((a, b) => a + b.qty, 0)})</span>
                </button>
              </div>

              {/* Smart Pantry Level Monitor Card */}
              <div className="glass-panel" style={{ padding: '32px', marginBottom: '32px', border: pantryLevel.remainingGrams < 1000 ? '1px solid rgba(251, 191, 36, 0.4)' : '1px solid var(--color-card-border)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div style={{ width: '52px', height: '52px', borderRadius: '16px', background: 'rgba(251, 191, 36, 0.15)', color: 'var(--color-warning)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <ShoppingBag size={28} />
                    </div>
                    <div>
                      <h2 style={{ fontSize: '20px', margin: 0 }}>Smart Pantry Scale: {pantryLevel.foodName}</h2>
                      <p style={{ color: 'var(--color-text-secondary)', margin: '4px 0 0', fontSize: '14px' }}>
                        IoT Pantry Scale sensor monitoring real-time remaining food
                      </p>
                    </div>
                  </div>

                  <button 
                    onClick={() => handleAddToCart(marketplaceItems[0])}
                    className="btn-primary" 
                    style={{ padding: '10px 18px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <ShoppingCart size={16} />
                    <span>1-Click Auto Refill</span>
                  </button>
                </div>

                {/* Gauge Bar */}
                <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '20px', borderRadius: '16px', border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>Inventory Fill Level:</span>
                    <span style={{ fontSize: '16px', fontWeight: 800, color: pantryLevel.remainingGrams < 1000 ? 'var(--color-warning)' : 'var(--color-primary)' }}>
                      {pantryLevel.remainingGrams}g remaining of {pantryLevel.totalCapacityGrams}g ({Math.round((pantryLevel.remainingGrams / pantryLevel.totalCapacityGrams) * 100)}%)
                    </span>
                  </div>
                  <div style={{ width: '100%', height: '14px', background: 'rgba(255, 255, 255, 0.08)', borderRadius: '7px', overflow: 'hidden' }}>
                    <div style={{
                      width: `${Math.round((pantryLevel.remainingGrams / pantryLevel.totalCapacityGrams) * 100)}%`,
                      height: '100%',
                      background: pantryLevel.remainingGrams < 1000 ? 'linear-gradient(90deg, #F59E0B, #EF4444)' : 'linear-gradient(90deg, #10B981, #059669)',
                      borderRadius: '7px',
                      transition: 'width 0.4s ease'
                    }} />
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '8px' }}>
                    <span>Estimated: ~{pantryLevel.daysLeft} days until empty at daily portion (240g/day)</span>
                    <span style={{ color: pantryLevel.remainingGrams < 1000 ? 'var(--color-warning)' : 'var(--color-primary)', fontWeight: 600 }}>
                      {pantryLevel.remainingGrams < 1000 ? '⚠️ LOW INVENTORY WARNING' : 'HEALTHY SUPPLY'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Product Catalog Grid */}
              <h3 style={{ fontSize: '20px', marginBottom: '20px' }}>Curated Veterinary Pet Essentials</h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
                {marketplaceItems.map(product => (
                  <div 
                    key={product.id}
                    className="glass-panel"
                    style={{
                      padding: '24px',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'space-between',
                      transition: 'transform 0.2s ease'
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                        <span style={{ fontSize: '36px' }}>{product.icon}</span>
                        <span style={{ background: 'rgba(255,255,255,0.06)', padding: '4px 10px', borderRadius: '8px', fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                          {product.category}
                        </span>
                      </div>
                      <h4 style={{ fontSize: '16px', margin: '0 0 6px' }}>{product.name}</h4>
                      <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.5, margin: 0 }}>
                        {product.desc}
                      </p>
                    </div>

                    <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid rgba(255, 255, 255, 0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-primary)' }}>
                          ₹{product.price}
                        </div>
                        <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>★ {product.rating} rating</div>
                      </div>

                      <button
                        onClick={() => handleAddToCart(product)}
                        className="btn-primary"
                        style={{ padding: '8px 16px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        <ShoppingCart size={14} />
                        <span>Add</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB: NEARBY PARKS & 24/7 VETS */}
          {activeTab === 'nearby_map' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>Nearby Dog Parks & 24/7 Emergency Vets</h1>
                    <span style={{ background: 'rgba(14, 165, 233, 0.2)', color: 'var(--color-secondary)', padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
                      Chennai Geolocation
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Verified pet recreation grounds, off-leash dog trails, and accredited 24/7 emergency veterinary trauma hospitals.
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '10px' }}>
                  {["All", "Emergency Vet", "Dog Park", "Pet Cafe"].map(cat => (
                    <button
                      key={cat}
                      onClick={() => setNearbyFilter(cat)}
                      style={{
                        padding: '8px 16px',
                        borderRadius: '12px',
                        background: nearbyFilter === cat ? 'var(--color-primary)' : 'rgba(255, 255, 255, 0.05)',
                        color: nearbyFilter === cat ? '#000' : 'var(--color-text-secondary)',
                        fontWeight: nearbyFilter === cat ? 700 : 500,
                        border: nearbyFilter === cat ? 'none' : '1px solid var(--color-card-border)',
                        cursor: 'pointer',
                        fontSize: '13px',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      {cat === 'Emergency Vet' ? '🚨 24/7 Vets' : cat === 'Dog Park' ? '🌳 Dog Parks' : cat === 'Pet Cafe' ? '☕ Pet Cafes' : 'All Places'}
                    </button>
                  ))}
                </div>
              </div>

              {/* Places Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px' }}>
                {nearbyPlaces
                  .filter(p => nearbyFilter === 'All' || p.category === nearbyFilter)
                  .map(place => (
                    <div 
                      key={place.id}
                      className="glass-panel"
                      style={{
                        padding: '28px',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'space-between',
                        border: place.is24Hours ? '1px solid rgba(239, 68, 68, 0.3)' : '1px solid var(--color-card-border)'
                      }}
                    >
                      <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                            <div style={{
                              width: '48px',
                              height: '48px',
                              borderRadius: '14px',
                              background: place.is24Hours ? 'rgba(239, 68, 68, 0.15)' : 'rgba(16, 185, 129, 0.15)',
                              color: place.is24Hours ? '#EF4444' : 'var(--color-primary)',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontSize: '22px'
                            }}>
                              {place.category === 'Emergency Vet' ? '🩺' : place.category === 'Dog Park' ? '🌳' : '☕'}
                            </div>
                            <div>
                              <h3 style={{ fontSize: '18px', margin: 0 }}>{place.name}</h3>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '4px', fontSize: '12px' }}>
                                <span style={{ color: place.is24Hours ? '#EF4444' : 'var(--color-secondary)', fontWeight: 700 }}>
                                  {place.category}
                                </span>
                                <span style={{ color: 'var(--color-text-secondary)' }}>•</span>
                                <span style={{ color: 'var(--color-text-secondary)' }}>{place.distanceKm} km away</span>
                              </div>
                            </div>
                          </div>

                          <span style={{ background: 'rgba(251, 191, 36, 0.15)', color: 'var(--color-warning)', padding: '4px 10px', borderRadius: '8px', fontSize: '12px', fontWeight: 700 }}>
                            ★ {place.rating}
                          </span>
                        </div>

                        <div style={{ fontSize: '13px', color: place.is24Hours ? '#FCA5A5' : 'var(--color-primary)', fontWeight: 600, marginBottom: '6px' }}>
                          {place.openTime}
                        </div>
                        <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', margin: '0 0 16px', lineHeight: 1.5 }}>
                          📍 {place.address}
                        </p>
                      </div>

                      <div style={{ display: 'flex', gap: '10px', paddingTop: '16px', borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
                        <a
                          href={`tel:${place.phone}`}
                          style={{
                            flex: 1,
                            textDecoration: 'none',
                            padding: '10px',
                            borderRadius: '12px',
                            background: 'rgba(255,255,255,0.06)',
                            border: '1px solid var(--color-card-border)',
                            color: '#FFF',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '6px',
                            fontSize: '13px',
                            fontWeight: 600
                          }}
                        >
                          <Phone size={15} />
                          <span>Call Clinic</span>
                        </a>

                        <a
                          href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(place.queryAddress)}`}
                          target="_blank"
                          rel="noreferrer"
                          className="btn-primary"
                          style={{
                            flex: 1.2,
                            textDecoration: 'none',
                            padding: '10px',
                            fontSize: '13px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '6px'
                          }}
                        >
                          <Navigation size={15} />
                          <span>Directions</span>
                        </a>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          )}

          {/* TAB: LONGITUDINAL WELLNESS & PREDICTIVE HEALTH ANALYTICS */}
          {activeTab === 'analytics' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h1 style={{ fontSize: '28px', margin: 0 }}>Longitudinal Health & Biometric Analytics</h1>
                    <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: 'var(--color-primary)', padding: '4px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: 600 }}>
                      Predictive Vitals Engine
                    </span>
                  </div>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: '6px' }}>
                    Multi-variable correlations linking physical activity, caloric nutrition balance, and resting heart rate trends.
                  </p>
                </div>

                <button 
                  onClick={() => setActiveTab('collar')}
                  className="btn-primary" 
                  style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                >
                  <Compass size={18} />
                  <span>View Live Collar</span>
                </button>
              </div>

              {/* 4 Metric Summary Cards */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '32px' }}>
                <div className="glass-panel" style={{ padding: '20px' }}>
                  <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>7-Day Walk Average</div>
                  <div style={{ fontSize: '28px', fontWeight: 800, color: 'var(--color-primary)', margin: '6px 0 2px' }}>2.84 km</div>
                  <div style={{ fontSize: '11px', color: 'var(--color-success)' }}>↑ 15% increase vs last week</div>
                </div>

                <div className="glass-panel" style={{ padding: '20px' }}>
                  <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Daily Caloric Burn</div>
                  <div style={{ fontSize: '28px', fontWeight: 800, color: 'var(--color-secondary)', margin: '6px 0 2px' }}>554 kcal</div>
                  <div style={{ fontSize: '11px', color: 'var(--color-success)' }}>✓ Ideal metabolic equilibrium</div>
                </div>

                <div className="glass-panel" style={{ padding: '20px' }}>
                  <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Resting Heart Rate</div>
                  <div style={{ fontSize: '28px', fontWeight: 800, color: '#FB7185', margin: '6px 0 2px' }}>81.5 BPM</div>
                  <div style={{ fontSize: '11px', color: 'var(--color-success)' }}>💚 Calm canine resting baseline</div>
                </div>

                <div className="glass-panel" style={{ padding: '20px' }}>
                  <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Health Index Forecast</div>
                  <div style={{ fontSize: '28px', fontWeight: 800, color: 'var(--color-warning)', margin: '6px 0 2px' }}>96%</div>
                  <div style={{ fontSize: '11px', color: 'var(--color-warning)' }}>Predicted +2% next month</div>
                </div>
              </div>

              {/* Multi-Series Visual Trend Chart */}
              <div className="glass-panel" style={{ padding: '32px', marginBottom: '32px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                  <div>
                    <h3 style={{ fontSize: '20px', margin: 0 }}>7-Day Exercise (km) vs Caloric Nutrition (kcal)</h3>
                    <p style={{ color: 'var(--color-text-secondary)', fontSize: '13px', margin: '4px 0 0' }}>
                      Daily correlation between distance covered and energy consumed
                    </p>
                  </div>

                  <div style={{ display: 'flex', gap: '16px', fontSize: '13px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <div style={{ width: '12px', height: '12px', background: 'var(--color-primary)', borderRadius: '3px' }} />
                      <span>Walk Distance (km)</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <div style={{ width: '12px', height: '12px', background: 'var(--color-secondary)', borderRadius: '3px' }} />
                      <span>Calories (kcal / 10)</span>
                    </div>
                  </div>
                </div>

                {/* Bar Graph */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', height: '200px', borderBottom: '1px solid var(--color-card-border)', paddingBottom: '16px' }}>
                  {wellnessTrends.map((t, idx) => (
                    <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px', flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px', height: '150px' }}>
                        {/* Walk bar */}
                        <div style={{
                          width: '18px',
                          height: `${Math.round((t.walkKm / 5) * 140)}px`,
                          background: 'linear-gradient(to top, var(--color-primary), #34D399)',
                          borderRadius: '6px'
                        }} title={`${t.walkKm} km`} />

                        {/* Calories bar */}
                        <div style={{
                          width: '18px',
                          height: `${Math.round((t.calories / 800) * 140)}px`,
                          background: 'linear-gradient(to top, var(--color-secondary), #38BDF8)',
                          borderRadius: '6px'
                        }} title={`${t.calories} kcal`} />
                      </div>
                      <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>{t.day}</div>
                    </div>
                  ))}
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-around', marginTop: '16px', fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                  {wellnessTrends.map((t, idx) => (
                    <div key={idx} style={{ textAlign: 'center' }}>
                      <div>{t.walkKm} km</div>
                      <div style={{ color: 'var(--color-secondary)' }}>{t.calories} kcal</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* AI Predictive Diagnosis & Anomaly Detection */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                <div className="glass-panel" style={{ padding: '24px', background: 'rgba(16, 185, 129, 0.05)', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                    <CheckCircle size={22} color="var(--color-primary)" />
                    <h3 style={{ margin: 0, fontSize: '18px' }}>AI Predictive Trajectory</h3>
                  </div>
                  <p style={{ fontSize: '14px', lineHeight: 1.6, color: 'var(--color-text-secondary)', margin: 0 }}>
                    Based on 14 days of telemetry, {activePet.name}'s daily 2.8 km average pace combined with 550 kcal balanced feeding maintains optimal canine cardiovascular tone. Projected health index remains steady at <strong>96%</strong> with minimal risk of joint degeneration.
                  </p>
                </div>

                <div className="glass-panel" style={{ padding: '24px', background: 'rgba(14, 165, 233, 0.05)', border: '1px solid rgba(14, 165, 233, 0.3)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                    <Shield size={22} color="var(--color-secondary)" />
                    <h3 style={{ margin: 0, fontSize: '18px' }}>Anomaly & Vital Guard</h3>
                  </div>
                  <p style={{ fontSize: '14px', lineHeight: 1.6, color: 'var(--color-text-secondary)', margin: 0 }}>
                    Continuous resting heart rate analysis shows zero tachycardia spikes (&gt;130 BPM while resting) and zero lethargy drops (&lt;60 BPM). Safe zone perimeter integrity: <strong>100% compliant</strong> today.
                  </p>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* MODAL: LOG MEAL */}
      {showMealModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="glass-panel" style={{ width: '440px', padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0 }}>Log Meal for {activePet.name}</h3>
              <button onClick={() => setShowMealModal(false)} style={{ background: 'none', border: 'none', color: '#FFF', cursor: 'pointer' }}><X size={20} /></button>
            </div>
            <form onSubmit={handleLogMeal} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Food Product Name</label>
                <input 
                  required
                  placeholder="e.g. Royal Canin Adult Kibble"
                  value={newMealName}
                  onChange={e => setNewMealName(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                />
              </div>
              <div style={{ display: 'flex', gap: '12px' }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Portion (grams)</label>
                  <input 
                    type="number"
                    value={newMealPortion}
                    onChange={e => setNewMealPortion(e.target.value)}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                  />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Calories (kcal)</label>
                  <input 
                    type="number"
                    value={newMealCalories}
                    onChange={e => setNewMealCalories(e.target.value)}
                    style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                  />
                </div>
              </div>
              <button type="submit" className="btn-primary" style={{ marginTop: '10px' }}>
                Save Meal Log & Sync Cloud
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: LOG WEIGHT */}
      {showWeightModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="glass-panel" style={{ width: '420px', padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0 }}>Log Weight for {activePet.name}</h3>
              <button onClick={() => setShowWeightModal(false)} style={{ background: 'none', border: 'none', color: '#FFF', cursor: 'pointer' }}><X size={20} /></button>
            </div>
            <form onSubmit={handleLogWeight} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Weight (kg)</label>
                <input 
                  type="number"
                  step="0.1"
                  required
                  placeholder="e.g. 25.8"
                  value={newWeight}
                  onChange={e => setNewWeight(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Notes</label>
                <input 
                  placeholder="e.g. Post-exercise morning weigh-in"
                  value={newWeightNotes}
                  onChange={e => setNewWeightNotes(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                />
              </div>
              <button type="submit" className="btn-primary" style={{ marginTop: '10px' }}>
                Save Weight & Sync Cloud
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: BOOK APPOINTMENT */}
      {showVetModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="glass-panel" style={{ width: '460px', padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0 }}>Book Vet Appointment</h3>
              <button onClick={() => setShowVetModal(false)} style={{ background: 'none', border: 'none', color: '#FFF', cursor: 'pointer' }}><X size={20} /></button>
            </div>
            <form onSubmit={handleBookAppointment} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Select Clinic</label>
                <select 
                  value={newClinic} 
                  onChange={e => setNewClinic(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: '#1E293B', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                >
                  <option value="Anna Nagar Pet Hospital">Anna Nagar Pet Hospital</option>
                  <option value="Happy Paws Clinic - T Nagar">Happy Paws Clinic - T Nagar</option>
                  <option value="Blue Cross Hospital - Guindy">Blue Cross Hospital - Guindy</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Visit Purpose</label>
                <input 
                  required
                  placeholder="e.g. Vaccination / Microchip check"
                  value={newPurpose}
                  onChange={e => setNewPurpose(e.target.value)}
                  style={{ width: '100%', padding: '12px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF' }}
                />
              </div>
              <button type="submit" className="btn-primary" style={{ marginTop: '10px' }}>
                Confirm Booking & Sync Calendar
              </button>
            </form>
          </div>
        </div>
      )}

      {/* FULLSCREEN MODAL: TELE-VET VIDEO CALL */}
      {showVideoCall && (
        <div style={{ position: 'fixed', inset: 0, background: '#000', zIndex: 200, display: 'flex', flexDirection: 'column' }}>
          {/* Call Header */}
          <div style={{ padding: '20px 32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(15, 23, 42, 0.8)', borderBottom: '1px solid var(--color-card-border)' }}>
            <div>
              <h3 style={{ margin: 0, fontSize: '18px' }}>PawLife Tele-Vet Video Session</h3>
              <p style={{ margin: 0, fontSize: '12px', color: 'var(--color-primary)' }}>● Encrypted HD • {Math.floor(callDuration / 60)}:{(callDuration % 60).toString().padStart(2, '0')}</p>
            </div>
            <button onClick={() => setShowVideoCall(false)} style={{ background: '#EF4444', color: '#FFF', border: 'none', borderRadius: '10px', padding: '8px 16px', fontWeight: 700, cursor: 'pointer' }}>
              End Call
            </button>
          </div>

          {/* Video Stream Stage */}
          <div style={{ flex: 1, position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'radial-gradient(circle, #1E293B, #0F172A)' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ width: '130px', height: '130px', borderRadius: '50%', background: 'rgba(16, 185, 129, 0.2)', border: '3px solid var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px', fontSize: '50px' }}>
                🩺
              </div>
              <h2 style={{ margin: '0 0 6px' }}>Dr. Priya Sharma, BVSc</h2>
              <p style={{ color: 'var(--color-text-secondary)', margin: 0 }}>Senior Veterinary Consultant • Connected</p>
            </div>

            {/* Self Camera Box */}
            <div style={{
              position: 'absolute',
              top: '24px',
              right: '24px',
              width: '180px',
              height: '130px',
              borderRadius: '16px',
              background: isVideoOff ? '#0F172A' : '#334155',
              border: '2px solid var(--color-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              overflow: 'hidden'
            }}>
              {isVideoOff ? (
                <VideoOff size={28} color="#94A3B8" />
              ) : (
                <div style={{ fontSize: '13px', fontWeight: 600, color: '#F8FAFC', textAlign: 'center' }}>
                  <span>{activePet.avatar}</span>
                  <div>You ({activePet.name})</div>
                </div>
              )}
            </div>
          </div>

          {/* Call Controls Bar */}
          <div style={{ padding: '24px', display: 'flex', justifyContent: 'center', gap: '20px', background: 'rgba(15, 23, 42, 0.95)' }}>
            <button 
              onClick={() => setIsMuted(!isMuted)}
              style={{ width: '56px', height: '56px', borderRadius: '50%', background: isMuted ? 'rgba(251, 191, 36, 0.2)' : 'rgba(255, 255, 255, 0.1)', color: isMuted ? 'var(--color-warning)' : '#FFF', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
            >
              {isMuted ? <MicOff size={24} /> : <Mic size={24} />}
            </button>
            <button 
              onClick={() => setIsVideoOff(!isVideoOff)}
              style={{ width: '56px', height: '56px', borderRadius: '50%', background: isVideoOff ? 'rgba(251, 113, 133, 0.2)' : 'rgba(255, 255, 255, 0.1)', color: isVideoOff ? 'var(--color-danger)' : '#FFF', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
            >
              {isVideoOff ? <VideoOff size={24} /> : <Video size={24} />}
            </button>
            <button 
              onClick={() => {
                alert("Prescription & Doctor Advice saved to Pet Profile!");
                setShowVideoCall(false);
              }}
              style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#EF4444', color: '#FFF', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
            >
              <Phone size={24} style={{ transform: 'rotate(135deg)' }} />
            </button>
          </div>
        </div>
      )}

      {/* MODAL: PRINTABLE CLINICAL VET PDF REPORT */}
      {showVetReportModal && currentAiReport && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.85)',
          backdropFilter: 'blur(10px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 300,
          padding: '20px',
          overflowY: 'auto'
        }}>
          <div style={{
            background: '#FFFFFF',
            color: '#0F172A',
            width: '740px',
            maxWidth: '100%',
            borderRadius: '16px',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column'
          }}>
            {/* Modal Screen-Only Controls Bar */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '16px 24px',
              background: '#0F172A',
              color: '#FFF'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', fontWeight: 600 }}>
                <Printer size={18} color="var(--color-primary)" />
                <span>Clinical Vet Summary Preview</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <button
                  onClick={() => window.print()}
                  className="btn-primary"
                  style={{
                    padding: '8px 16px',
                    fontSize: '13px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px'
                  }}
                >
                  <Download size={15} />
                  <span>Print / Save as PDF</span>
                </button>
                <button
                  onClick={() => setShowVetReportModal(false)}
                  style={{
                    background: 'rgba(255,255,255,0.1)',
                    border: 'none',
                    color: '#FFF',
                    borderRadius: '8px',
                    padding: '8px 12px',
                    cursor: 'pointer'
                  }}
                >
                  <X size={18} />
                </button>
              </div>
            </div>

            {/* Printable Document Area */}
            <div id="printable-vet-report" style={{ padding: '36px 40px', fontFamily: 'Inter, system-ui, sans-serif' }}>
              {/* Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '2px solid #0EA5E9', paddingBottom: '16px', marginBottom: '24px' }}>
                <div>
                  <h1 style={{ fontSize: '24px', fontWeight: 900, color: '#0F172A', margin: 0, letterSpacing: '-0.02em' }}>PAWLIFE CLINICAL VETERINARY SUMMARY</h1>
                  <p style={{ margin: '4px 0 0', color: '#64748B', fontSize: '13px' }}>
                    Generated by PawLife Health Platform • Cloud Synced & Cryptographically Signed
                  </p>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '12px', color: '#64748B' }}>Report Reference:</div>
                  <div style={{ fontSize: '14px', fontWeight: 700, color: '#0F172A' }}>PL-{Date.now().toString().slice(-6)}</div>
                  <div style={{ fontSize: '11px', color: '#64748B', marginTop: '2px' }}>Date: {new Date(currentAiReport.timestamp || Date.now()).toLocaleDateString([], { year: 'numeric', month: 'short', day: 'numeric' })}</div>
                </div>
              </div>

              {/* Patient Demographics */}
              <div style={{ background: '#F8FAFC', borderRadius: '12px', padding: '16px 20px', marginBottom: '24px', border: '1px solid #E2E8F0' }}>
                <h3 style={{ fontSize: '14px', textTransform: 'uppercase', color: '#475569', letterSpacing: '0.05em', margin: '0 0 12px' }}>Patient & Guardian Profile</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '14px', fontSize: '13px' }}>
                  <div>
                    <span style={{ color: '#64748B' }}>Patient Name: </span>
                    <strong style={{ color: '#0F172A' }}>{activePet.name}</strong>
                  </div>
                  <div>
                    <span style={{ color: '#64748B' }}>Species / Breed: </span>
                    <strong style={{ color: '#0F172A' }}>{activePet.species} ({activePet.breed})</strong>
                  </div>
                  <div>
                    <span style={{ color: '#64748B' }}>Age / Gender: </span>
                    <strong style={{ color: '#0F172A' }}>{activePet.age} • Intact</strong>
                  </div>
                  <div>
                    <span style={{ color: '#64748B' }}>Patient Weight: </span>
                    <strong style={{ color: '#0F172A' }}>{activePet.weight} kg</strong>
                  </div>
                  <div>
                    <span style={{ color: '#64748B' }}>Microchip ID: </span>
                    <strong style={{ color: '#0F172A' }}>985141002348123</strong>
                  </div>
                  <div>
                    <span style={{ color: '#64748B' }}>Primary Guardian: </span>
                    <strong style={{ color: '#0F172A' }}>Shoban (Co-Parent Team)</strong>
                  </div>
                </div>
              </div>

              {/* Clinical AI Triage Findings */}
              <div style={{ marginBottom: '24px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                  <h3 style={{ fontSize: '15px', color: '#0F172A', margin: 0, fontWeight: 800 }}>Chief Complaint & AI Triage Classification</h3>
                  <span style={{
                    padding: '4px 12px',
                    borderRadius: '8px',
                    fontSize: '12px',
                    fontWeight: 800,
                    background: currentAiReport.urgency === 'EMERGENCY' ? '#FEE2E2' : currentAiReport.urgency === 'MODERATE' ? '#FEF3C7' : '#DCFCE7',
                    color: currentAiReport.urgency === 'EMERGENCY' ? '#DC2626' : currentAiReport.urgency === 'MODERATE' ? '#D97706' : '#15803D'
                  }}>
                    {currentAiReport.urgency} URGENCY
                  </span>
                </div>

                <div style={{ padding: '16px', background: '#F1F5F9', borderRadius: '10px', marginBottom: '12px', fontSize: '13px' }}>
                  <span style={{ color: '#475569', fontWeight: 600 }}>Reported Symptoms: </span>
                  <span style={{ color: '#0F172A' }}>"{currentAiReport.symptoms}"</span>
                </div>

                <div style={{ padding: '16px', border: '1px solid #E2E8F0', borderRadius: '10px' }}>
                  <div style={{ fontSize: '12px', fontWeight: 700, color: '#475569', textTransform: 'uppercase', marginBottom: '6px' }}>Diagnostic Assessment & Recommendations</div>
                  <p style={{ margin: 0, fontSize: '13px', lineHeight: 1.6, color: '#1E293B' }}>
                    {currentAiReport.findings}
                  </p>
                </div>
              </div>

              {/* Collar Vitals & Nutrition Summary */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '28px' }}>
                <div style={{ padding: '16px', border: '1px solid #E2E8F0', borderRadius: '10px' }}>
                  <h4 style={{ margin: '0 0 10px', fontSize: '13px', color: '#475569' }}>Smart Collar Biometrics</h4>
                  <div style={{ fontSize: '12px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#64748B' }}>Resting Heart Rate:</span>
                      <strong>{collarTelemetry.heartRateBpm} BPM (Normal: 70-120)</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#64748B' }}>Daily Activity:</span>
                      <strong>{collarTelemetry.stepCount?.toLocaleString()} steps (~{((activeWalk.distanceMeters || 2450) / 1000).toFixed(2)} km)</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#64748B' }}>Geofence Perimeter:</span>
                      <strong style={{ color: '#16A34A' }}>Within Safe Zone (&lt; 150m)</strong>
                    </div>
                  </div>
                </div>

                <div style={{ padding: '16px', border: '1px solid #E2E8F0', borderRadius: '10px' }}>
                  <h4 style={{ margin: '0 0 10px', fontSize: '13px', color: '#475569' }}>Nutrition & Feeding History</h4>
                  <div style={{ fontSize: '12px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#64748B' }}>Current Food:</span>
                      <strong>{pantryLevel.foodName.slice(0, 24)}...</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#64748B' }}>Daily Caloric Intake:</span>
                      <strong>{petMeals.reduce((a, m) => a + (m.calories || 0), 0)} kcal</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#64748B' }}>Smart Pantry Supply:</span>
                      <strong>{pantryLevel.remainingGrams}g left (~{pantryLevel.daysLeft} days)</strong>
                    </div>
                  </div>
                </div>
              </div>

              {/* Doctor Sign-off & Footer */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', paddingTop: '16px', borderTop: '1px solid #E2E8F0', fontSize: '12px', color: '#64748B' }}>
                <div>
                  <div>Clinic: Anna Nagar Pet Hospital & Tele-Vet Network</div>
                  <div>Certified BVSc & AH Registration: VCI/TN/8492</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: '#0F172A' }}>Dr. Priya Sharma, BVSc</div>
                  <div style={{ fontStyle: 'italic', color: '#94A3B8' }}>Electronically Verified & Signed</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: SHOPPING CART & CHECKOUT DRAWER */}
      {showCartModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 250 }}>
          <div className="glass-panel" style={{ width: '480px', padding: '32px', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <ShoppingCart size={22} color="var(--color-primary)" />
                <h3 style={{ margin: 0 }}>PawLife Cart & Refill</h3>
              </div>
              <button onClick={() => setShowCartModal(false)} style={{ background: 'none', border: 'none', color: '#FFF', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {orderSuccess ? (
              <div style={{ textAlign: 'center', padding: '32px 16px' }}>
                <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: 'rgba(16, 185, 129, 0.2)', border: '2px solid var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px', color: 'var(--color-primary)' }}>
                  <CheckCircle size={36} />
                </div>
                <h2 style={{ fontSize: '22px', margin: '0 0 8px' }}>Order Placed Successfully!</h2>
                <p style={{ color: 'var(--color-text-secondary)', fontSize: '14px', lineHeight: 1.6 }}>
                  Smart Pantry auto-refill confirmed. Expected delivery in 2 business days. Inventory level restored to 100%.
                </p>
              </div>
            ) : (
              <div>
                {cart.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '40px 16px', color: 'var(--color-text-secondary)' }}>
                    Your cart is currently empty. Add products from the marketplace or tap 1-Click Auto Refill.
                  </div>
                ) : (
                  <div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '20px' }}>
                      {cart.map(item => (
                        <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 16px', background: 'rgba(255,255,255,0.03)', borderRadius: '12px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <span style={{ fontSize: '24px' }}>{item.icon}</span>
                            <div>
                              <div style={{ fontWeight: 600, fontSize: '14px' }}>{item.name}</div>
                              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Qty: {item.qty} • ₹{item.price} each</div>
                            </div>
                          </div>
                          <div style={{ fontWeight: 700, color: 'var(--color-primary)' }}>
                            ₹{item.price * item.qty}
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Promo Code Form */}
                    <form onSubmit={handleApplyPromo} style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
                      <input
                        placeholder="Enter Promo Code (e.g. PAWFIRST)"
                        value={promoCode}
                        onChange={e => setPromoCode(e.target.value)}
                        style={{
                          flex: 1,
                          padding: '10px 14px',
                          borderRadius: '10px',
                          background: 'rgba(255,255,255,0.05)',
                          border: '1px solid var(--color-card-border)',
                          color: '#FFF',
                          fontSize: '13px'
                        }}
                      />
                      <button type="submit" style={{ padding: '10px 16px', borderRadius: '10px', background: 'rgba(255,255,255,0.1)', color: '#FFF', border: '1px solid var(--color-card-border)', fontWeight: 600, fontSize: '13px', cursor: 'pointer' }}>
                        Apply
                      </button>
                    </form>

                    {discountPercent > 0 && (
                      <div style={{ fontSize: '12px', color: 'var(--color-success)', background: 'rgba(74, 222, 128, 0.1)', padding: '6px 12px', borderRadius: '8px', marginBottom: '16px' }}>
                        🎉 Promo Code '{promoCode.toUpperCase()}' applied: 20% discount activated!
                      </div>
                    )}

                    {/* Cost Calculation */}
                    <div style={{ borderTop: '1px solid var(--color-card-border)', paddingTop: '16px', marginBottom: '20px', fontSize: '14px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      {(() => {
                        const subtotal = cart.reduce((a, b) => a + (b.price * b.qty), 0);
                        const discount = Math.round(subtotal * (discountPercent / 100));
                        const total = subtotal - discount;
                        return (
                          <>
                            <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--color-text-secondary)' }}>
                              <span>Subtotal:</span>
                              <span>₹{subtotal}</span>
                            </div>
                            {discountPercent > 0 && (
                              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--color-success)' }}>
                                <span>Discount (20%):</span>
                                <span>-₹{discount}</span>
                              </div>
                            )}
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: '18px', color: '#FFF', marginTop: '4px' }}>
                              <span>Total Due:</span>
                              <span style={{ color: 'var(--color-primary)' }}>₹{total}</span>
                            </div>
                          </>
                        );
                      })()}
                    </div>

                    <button
                      onClick={handleCheckout}
                      className="btn-primary"
                      style={{ width: '100%', padding: '14px', fontSize: '15px' }}
                    >
                      Complete 1-Click Order & Auto-Refill
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      {/* MODAL: AI VISION FOOD SCANNER & NUTRITION BREAKDOWN */}
      {showAiFoodModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(10px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 300, padding: '20px' }}>
          <div className="glass-panel" style={{ width: '560px', padding: '32px', maxHeight: '92vh', overflowY: 'auto', border: '1px solid rgba(16, 185, 129, 0.4)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <Camera size={22} />
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '18px' }}>AI Vision Meal Scanner</h3>
                  <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                    CameraX & Multi-Modal Ingredient Classification
                  </div>
                </div>
              </div>
              <button onClick={() => setShowAiFoodModal(false)} style={{ background: 'none', border: 'none', color: '#FFF', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {/* Simulated Camera Viewfinder */}
            <div style={{
              position: 'relative',
              height: '190px',
              borderRadius: '16px',
              background: 'radial-gradient(circle at center, rgba(16, 185, 129, 0.15), rgba(10, 15, 29, 0.95))',
              border: '2px dashed rgba(16, 185, 129, 0.4)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '20px',
              overflow: 'hidden'
            }}>
              {/* Corner reticle guides */}
              <div style={{ position: 'absolute', top: '12px', left: '12px', width: '20px', height: '20px', borderTop: '2px solid var(--color-primary)', borderLeft: '2px solid var(--color-primary)' }} />
              <div style={{ position: 'absolute', top: '12px', right: '12px', width: '20px', height: '20px', borderTop: '2px solid var(--color-primary)', borderRight: '2px solid var(--color-primary)' }} />
              <div style={{ position: 'absolute', bottom: '12px', left: '12px', width: '20px', height: '20px', borderBottom: '2px solid var(--color-primary)', borderLeft: '2px solid var(--color-primary)' }} />
              <div style={{ position: 'absolute', bottom: '12px', right: '12px', width: '20px', height: '20px', borderBottom: '2px solid var(--color-primary)', borderRight: '2px solid var(--color-primary)' }} />

              <div style={{ fontSize: '48px', marginBottom: '8px' }}>
                {aiFoodState.icon || '🍲'}
              </div>
              <div style={{ fontWeight: 700, fontSize: '15px', color: '#FFF', textAlign: 'center', maxWidth: '85%' }}>
                {aiFoodState.name}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '6px' }}>
                <span style={{ fontSize: '11px', background: 'rgba(16, 185, 129, 0.25)', color: 'var(--color-primary)', padding: '2px 10px', borderRadius: '12px', fontWeight: 700 }}>
                  ✓ {aiFoodState.confidencePercent}% Vision Match
                </span>
                <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                  {aiFoodState.category}
                </span>
              </div>

              {isScanningFood && (
                <div style={{ position: 'absolute', inset: 0, background: 'rgba(11, 15, 25, 0.85)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '10px' }}>
                  <RefreshCw size={24} className="spin" color="var(--color-primary)" />
                  <div style={{ fontSize: '13px', color: 'var(--color-primary)', fontWeight: 600 }}>Analyzing Bowl Pixels & Density...</div>
                </div>
              )}
            </div>

            {/* Candidate Food Presets */}
            <div style={{ marginBottom: '20px' }}>
              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', fontWeight: 600, textTransform: 'uppercase', marginBottom: '8px' }}>
                Quick Food Presets
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '8px' }}>
                {[
                  { name: "Royal Canin Adult Kibble", category: "Dry Extruded Kibble", kcalPer100g: 365, proteinPercent: 26, fatPercent: 14, icon: "🍲" },
                  { name: "Pedigree Chicken Gravy", category: "Canned Gravy Loaf", kcalPer100g: 115, proteinPercent: 9.5, fatPercent: 5.0, icon: "🥣" },
                  { name: "Fresh Lean Beef Bowl", category: "Raw / Cooked Fresh Meal", kcalPer100g: 175, proteinPercent: 21, fatPercent: 8.5, icon: "🥩" },
                  { name: "Salmon & Pumpkin Topper", category: "Nutritional Digestive Topper", kcalPer100g: 95, proteinPercent: 8, fatPercent: 4, icon: "🐟" }
                ].map(item => (
                  <button
                    key={item.name}
                    onClick={() => handleRunAiFoodScan(item)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      padding: '8px 12px',
                      borderRadius: '10px',
                      background: aiFoodState.name.includes(item.name.slice(0, 10)) ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.04)',
                      border: aiFoodState.name.includes(item.name.slice(0, 10)) ? '1px solid var(--color-primary)' : '1px solid var(--color-card-border)',
                      color: '#FFF',
                      fontSize: '12px',
                      cursor: 'pointer',
                      textAlign: 'left'
                    }}
                  >
                    <span>{item.icon}</span>
                    <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.name}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Portion Slider */}
            <div style={{ background: 'rgba(255, 255, 255, 0.03)', padding: '16px', borderRadius: '12px', marginBottom: '20px', border: '1px solid var(--color-card-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 600 }}>Adjust Scanned Portion:</span>
                <span style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-primary)' }}>
                  {aiFoodState.portionGrams} <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>grams</span>
                </span>
              </div>
              <input
                type="range"
                min="20"
                max="400"
                step="10"
                value={aiFoodState.portionGrams}
                onChange={e => setAiFoodState(prev => ({ ...prev, portionGrams: Number(e.target.value) }))}
                style={{ width: '100%', accentColor: 'var(--color-primary)', cursor: 'pointer' }}
              />
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                <span>Light Snack (20g)</span>
                <span>Normal Bowl (120g)</span>
                <span>Full Feast (400g)</span>
              </div>
            </div>

            {/* Macros Breakdown HUD */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '24px' }}>
              <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '12px', borderRadius: '10px', textAlign: 'center' }}>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>Estimated Energy</div>
                <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-primary)', marginTop: '2px' }}>
                  {Math.round((aiFoodState.portionGrams * aiFoodState.kcalPer100g) / 100)} <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>kcal</span>
                </div>
              </div>
              <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '12px', borderRadius: '10px', textAlign: 'center' }}>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>Crude Protein</div>
                <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-secondary)', marginTop: '2px' }}>
                  {aiFoodState.proteinPercent}%
                </div>
              </div>
              <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '12px', borderRadius: '10px', textAlign: 'center' }}>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>Crude Fat</div>
                <div style={{ fontSize: '18px', fontWeight: 800, color: '#F59E0B', marginTop: '2px' }}>
                  {aiFoodState.fatPercent}%
                </div>
              </div>
            </div>

            {/* Actions */}
            <div style={{ display: 'flex', gap: '12px' }}>
              <button
                onClick={() => setShowAiFoodModal(false)}
                style={{ flex: 1, padding: '12px', borderRadius: '12px', background: 'rgba(255, 255, 255, 0.06)', border: '1px solid var(--color-card-border)', color: '#FFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                onClick={handleLogAiFood}
                className="btn-primary"
                style={{ flex: 2, padding: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', fontSize: '14px' }}
              >
                <Utensils size={16} />
                <span>Log Meal & Deduct Pantry ({aiFoodState.portionGrams}g)</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: HIGH-CONTRAST SCANNABLE QR PASSPORT PASS */}
      {showQrModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(10px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 350, padding: '20px' }}>
          <div style={{ width: '440px', background: '#FFFFFF', color: '#0F172A', borderRadius: '24px', padding: '32px', textAlign: 'center', boxShadow: '0 24px 60px rgba(0,0,0,0.6)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Shield size={20} color="#059669" />
                <span style={{ fontWeight: 800, fontSize: '15px' }}>PawLife Verifiable Pass</span>
              </div>
              <button onClick={() => setShowQrModal(false)} style={{ background: 'none', border: 'none', color: '#64748B', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {/* Simulated 21x21 QR Code Pattern */}
            <div style={{
              width: '210px',
              height: '210px',
              margin: '0 auto 16px',
              background: '#FFFFFF',
              border: '2px solid #E2E8F0',
              borderRadius: '16px',
              padding: '12px',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between'
            }}>
              {/* Top Row: Finders */}
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                {/* Top-Left Finder */}
                <div style={{ width: '48px', height: '48px', border: '5px solid #000', borderRadius: '8px', padding: '5px', boxSizing: 'border-box' }}>
                  <div style={{ width: '100%', height: '100%', background: '#000', borderRadius: '4px' }} />
                </div>
                {/* Center noise */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 6px)', gap: '4px', padding: '4px' }}>
                  <div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} /><div />
                  <div /><div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} />
                  <div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} /><div />
                </div>
                {/* Top-Right Finder */}
                <div style={{ width: '48px', height: '48px', border: '5px solid #000', borderRadius: '8px', padding: '5px', boxSizing: 'border-box' }}>
                  <div style={{ width: '100%', height: '100%', background: '#000', borderRadius: '4px' }} />
                </div>
              </div>

              {/* Middle Row */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8, 1fr)', gap: '4px', padding: '4px 8px' }}>
                <div style={{ height: '6px', background: '#000' }} /><div /><div style={{ height: '6px', background: '#000' }} /><div style={{ height: '6px', background: '#000' }} />
                <div /><div style={{ height: '6px', background: '#000' }} /><div /><div style={{ height: '6px', background: '#000' }} />
                <div style={{ height: '6px', background: '#000' }} /><div /><div /><div style={{ height: '6px', background: '#000' }} />
                <div style={{ height: '6px', background: '#000' }} /><div /><div style={{ height: '6px', background: '#000' }} /><div />
              </div>

              {/* Bottom Row */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                {/* Bottom-Left Finder */}
                <div style={{ width: '48px', height: '48px', border: '5px solid #000', borderRadius: '8px', padding: '5px', boxSizing: 'border-box' }}>
                  <div style={{ width: '100%', height: '100%', background: '#000', borderRadius: '4px' }} />
                </div>
                {/* Noise */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 6px)', gap: '4px', padding: '4px' }}>
                  <div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} />
                  <div /><div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} /><div />
                  <div style={{ background: '#000' }} /><div style={{ background: '#000' }} /><div /><div style={{ background: '#000' }} /><div style={{ background: '#000' }} />
                </div>
              </div>
            </div>

            <h3 style={{ margin: '0 0 2px', fontSize: '20px', color: '#0F172A' }}>{petPassport.petName}</h3>
            <div style={{ fontSize: '12px', color: '#64748B', fontFamily: 'monospace', fontWeight: 600 }}>
              Microchip: {petPassport.microchipId}
            </div>
            <p style={{ fontSize: '12px', color: '#64748B', lineHeight: 1.5, margin: '8px 0 20px' }}>
              Scan with any smartphone camera to open the read-only public immunization pass.
            </p>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                onClick={() => {
                  navigator.clipboard?.writeText(petPassport.publicVerificationUrl);
                  alert("Public QR Verification link copied to clipboard!");
                }}
                style={{ flex: 1, padding: '12px', borderRadius: '12px', background: '#F1F5F9', border: '1px solid #CBD5E1', color: '#0F172A', fontWeight: 700, fontSize: '13px', cursor: 'pointer' }}
              >
                Copy Link
              </button>
              <a
                href={petPassport.publicVerificationUrl}
                target="_blank"
                rel="noreferrer"
                style={{ flex: 1, padding: '12px', borderRadius: '12px', background: '#0F172A', color: '#FFF', fontWeight: 700, fontSize: '13px', textDecoration: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
              >
                <span>Open URL</span>
                <ExternalLink size={14} />
              </a>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: LOG NEW VACCINE BOOSTER */}
      {showAddVaccineModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(10px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 350, padding: '20px' }}>
          <div className="glass-panel" style={{ width: '480px', padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Shield size={22} color="var(--color-primary)" />
                <h3 style={{ margin: 0, fontSize: '18px' }}>Log Vaccine Booster</h3>
              </div>
              <button onClick={() => setShowAddVaccineModal(false)} style={{ background: 'none', border: 'none', color: '#FFF', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleAddVaccine} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Vaccine Name</label>
                <input
                  placeholder="e.g. Rabies 3-Year Inactivated Booster"
                  value={newVaccineName}
                  onChange={e => setNewVaccineName(e.target.value)}
                  required
                  style={{ width: '100%', padding: '10px 14px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Category</label>
                  <select
                    value={newVaccineCategory}
                    onChange={e => setNewVaccineCategory(e.target.value)}
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '10px', background: '#1E293B', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px' }}
                  >
                    <option value="Core Canine">Core Canine</option>
                    <option value="Lifestyle / Boarding">Lifestyle / Boarding</option>
                    <option value="Preventive Parasite">Preventive Parasite</option>
                    <option value="Feline Core">Feline Core</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Batch / Lot #</label>
                  <input
                    placeholder="e.g. LOT-2026-X8"
                    value={newVaccineLot}
                    onChange={e => setNewVaccineLot(e.target.value)}
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Veterinary Clinic</label>
                <input
                  placeholder="e.g. Anna Nagar Pet Hospital"
                  value={newVaccineClinic}
                  onChange={e => setNewVaccineClinic(e.target.value)}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>Expiry / Valid Until</label>
                <input
                  placeholder="e.g. 15 Oct 2027"
                  value={newVaccineExpiry}
                  onChange={e => setNewVaccineExpiry(e.target.value)}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: '10px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--color-card-border)', color: '#FFF', fontSize: '13px' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowAddVaccineModal(false)}
                  style={{ flex: 1, padding: '12px', borderRadius: '12px', background: 'rgba(255,255,255,0.06)', border: '1px solid var(--color-card-border)', color: '#FFF', fontWeight: 600, cursor: 'pointer' }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  style={{ flex: 2, padding: '12px', fontSize: '14px' }}
                >
                  Save & Sync to Passport
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: SHARE COMMUNITY / PARK UPDATE */}
      {showNewPostModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '520px', padding: '32px', background: '#111827', border: '1px solid rgba(255,255,255,0.1)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)' }}>
                  <MessageSquare size={18} />
                </div>
                <h3 style={{ fontSize: '18px', margin: 0, color: '#FFF' }}>Share Dog Park & Social Update</h3>
              </div>
              <button
                onClick={() => setShowNewPostModal(false)}
                style={{ background: 'transparent', border: 'none', color: 'var(--color-text-secondary)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateCommunityPost} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>
                  Chennai Park / Location
                </label>
                <select
                  value={newPostLocation}
                  onChange={e => setNewPostLocation(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: '10px',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid var(--color-card-border)',
                    color: '#FFF',
                    fontSize: '13px'
                  }}
                >
                  <option value="Semmozhi Poonga Dog Trail" style={{ background: '#1E293B', color: '#FFF' }}>Semmozhi Poonga Dog Trail & Arena</option>
                  <option value="Besant Nagar Beach" style={{ background: '#1E293B', color: '#FFF' }}>Besant Nagar Beach Coastal Dog Run</option>
                  <option value="Anna Nagar Tower Park" style={{ background: '#1E293B', color: '#FFF' }}>Anna Nagar Tower Park Canine Meadow</option>
                  <option value="Anna Nagar Pet Hospital" style={{ background: '#1E293B', color: '#FFF' }}>Anna Nagar Pet Hospital & Health Hub</option>
                  <option value="Barking Deer Cafe" style={{ background: '#1E293B', color: '#FFF' }}>Barking Deer Pet Cafe & Play Arena</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: '6px' }}>
                  What's happening with {activePet.name}?
                </label>
                <textarea
                  required
                  rows={4}
                  placeholder={`Share park conditions, playdate invitations, or how ${activePet.name} is enjoying the day...`}
                  value={newPostContent}
                  onChange={e => setNewPostContent(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    borderRadius: '10px',
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid var(--color-card-border)',
                    color: '#FFF',
                    fontSize: '13px',
                    resize: 'none',
                    lineHeight: 1.5
                  }}
                />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowNewPostModal(false)}
                  style={{
                    flex: 1,
                    padding: '12px',
                    borderRadius: '12px',
                    background: 'rgba(255,255,255,0.06)',
                    border: '1px solid var(--color-card-border)',
                    color: '#FFF',
                    fontWeight: 600,
                    cursor: 'pointer'
                  }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  style={{ flex: 2, padding: '12px', fontSize: '14px' }}
                >
                  Post to Community
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
