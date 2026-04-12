import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { onAuthStateChanged, signInAnonymously, signInWithPopup, signOut, type User } from 'firebase/auth';
import { doc, onSnapshot } from 'firebase/firestore';
import { auth, db, googleProvider } from '../firebase/client';
import { createOrUpdateUserProfile, touchUserActivity } from '../firebase/api';
import type { UserProfile } from '../types';

interface AuthContextValue {
  user: User | null;
  profile: UserProfile | null;
  loading: boolean;
  signInWithGoogle: () => Promise<void>;
  signInDevAnonymous: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubAuth = onAuthStateChanged(auth, async (nextUser) => {
      setUser(nextUser);
      if (nextUser) {
        const fallbackEmail = nextUser.email ?? `${nextUser.uid}@anon.teamdesk.local`;
        await createOrUpdateUserProfile({
          uid: nextUser.uid,
          email: fallbackEmail,
          displayName: nextUser.displayName ?? fallbackEmail,
          photoURL: nextUser.photoURL ?? undefined
        });
      }
      setLoading(false);
    });
    return unsubAuth;
  }, []);

  useEffect(() => {
    if (!user) {
      setProfile(null);
      return;
    }
    const unsubProfile = onSnapshot(doc(db, 'users', user.uid), (snap) => {
      if (!snap.exists()) return;
      setProfile(snap.data() as UserProfile);
    });
    return unsubProfile;
  }, [user]);

  useEffect(() => {
    if (!user) return;
    void touchUserActivity(user.uid);
    const timer = window.setInterval(() => {
      void touchUserActivity(user.uid);
    }, 60_000);
    return () => window.clearInterval(timer);
  }, [user]);

  const value = useMemo(
    () => ({
      user,
      profile,
      loading,
      signInWithGoogle: async () => signInWithPopup(auth, googleProvider).then(() => undefined),
      signInDevAnonymous: async () => signInAnonymously(auth).then(() => undefined),
      logout: async () => signOut(auth).then(() => undefined)
    }),
    [loading, profile, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
