import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { onAuthStateChanged, signInAnonymously, signInWithPopup, signOut, type User } from 'firebase/auth';
import { doc, onSnapshot } from 'firebase/firestore';
import { auth, googleProvider, db } from '../firebase/client';
import { createOrUpdateUserProfile } from '../firebase/api';
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

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubAuth = onAuthStateChanged(auth, async (nextUser) => {
      setUser(nextUser);
      if (nextUser?.email) {
        await createOrUpdateUserProfile({
          uid: nextUser.uid,
          email: nextUser.email,
          displayName: nextUser.displayName ?? nextUser.email,
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
      if (!snap.exists()) {
        setProfile({
          uid: user.uid,
          displayName: user.displayName ?? 'Anonymous',
          email: user.email ?? 'anon@local',
          photoURL: user.photoURL ?? undefined,
          role: 'member',
          createdAt: undefined as never,
          updatedAt: undefined as never
        });
        return;
      }
      setProfile(snap.data() as UserProfile);
    });
    return unsubProfile;
  }, [user]);

  const value = useMemo(
    () => ({
      user,
      profile,
      loading,
      signInWithGoogle: async () => {
        await signInWithPopup(auth, googleProvider);
      },
      signInDevAnonymous: async () => {
        await signInAnonymously(auth);
      },
      logout: async () => {
        await signOut(auth);
      }
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
