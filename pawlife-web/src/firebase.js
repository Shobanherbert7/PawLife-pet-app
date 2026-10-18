import { initializeApp } from 'firebase/app';
import { 
  getFirestore, 
  collection, 
  doc, 
  setDoc, 
  addDoc, 
  onSnapshot, 
  query, 
  orderBy, 
  limit, 
  where 
} from 'firebase/firestore';
import { 
  getAuth, 
  signInAnonymously, 
  onAuthStateChanged,
  GoogleAuthProvider,
  signInWithPopup
} from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIzaSyCNgrcU2tWoTf2IjSaGiBtmPvReSNzAwcw",
  authDomain: "pawlife-ced47.firebaseapp.com",
  projectId: "pawlife-ced47",
  storageBucket: "pawlife-ced47.firebasestorage.app",
  messagingSenderId: "346326276764",
  appId: "1:346326276764:web:pawlife-web-app"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app);
export const GoogleAuthProviderClass = GoogleAuthProvider;
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: 'select_account' });

export {
  collection,
  doc,
  setDoc,
  addDoc,
  onSnapshot,
  query,
  orderBy,
  limit,
  where
};
