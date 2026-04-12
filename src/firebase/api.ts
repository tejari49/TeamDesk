import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  onSnapshot,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
  type QueryConstraint
} from 'firebase/firestore';
import { db } from './client';
import { ADMIN_EMAIL_ALLOWLIST } from './config';
import type { AnnouncementDoc, HandoverDoc, HandoverPriority, HandoverState, LinkDoc, StatusDoc, TeamStatus, UserProfile } from '../types';

const mapDoc = <T>(id: string, data: object) => ({ id, ...(data as T) });

export const createOrUpdateUserProfile = async (input: {
  uid: string;
  email: string;
  displayName: string;
  photoURL?: string;
}) => {
  const userRef = doc(db, 'users', input.uid);
  const existing = await getDoc(userRef);
  const role = ADMIN_EMAIL_ALLOWLIST.includes(input.email) ? 'admin' : 'member';

  if (existing.exists()) {
    const current = existing.data() as Partial<UserProfile>;
    await updateDoc(userRef, {
      displayName: input.displayName,
      email: input.email,
      photoURL: input.photoURL ?? '',
      role: current.role ?? role,
      updatedAt: serverTimestamp()
    });
    return;
  }

  await setDoc(userRef, {
    uid: input.uid,
    displayName: input.displayName,
    email: input.email,
    photoURL: input.photoURL ?? '',
    role,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const subscribeToUsers = (cb: (users: UserProfile[]) => void) => {
  return onSnapshot(query(collection(db, 'users'), orderBy('displayName', 'asc')), (snapshot) => {
    cb(snapshot.docs.map((d) => mapDoc<UserProfile>(d.id, d.data())));
  });
};

export const subscribeToStatusesByDate = (date: string, cb: (statuses: StatusDoc[]) => void) => {
  const q = query(collection(db, 'statuses'), where('date', '==', date), orderBy('updatedAt', 'desc'));
  return onSnapshot(q, (snapshot) => cb(snapshot.docs.map((d) => mapDoc<StatusDoc>(d.id, d.data()))));
};

export const upsertStatus = async (payload: {
  uid: string;
  displayName: string;
  date: string;
  status: TeamStatus;
  note: string;
}) => {
  const id = `${payload.uid}_${payload.date.replaceAll('-', '_')}`;
  const ref = doc(db, 'statuses', id);
  await setDoc(
    ref,
    {
      ...payload,
      updatedAt: serverTimestamp()
    },
    { merge: true }
  );
};

export const subscribeToHandovers = (
  filters: { state?: HandoverState; priority?: HandoverPriority | 'all' },
  cb: (items: HandoverDoc[]) => void
) => {
  const constraints: QueryConstraint[] = [orderBy('createdAt', 'desc')];
  if (filters.state && filters.state !== 'open') constraints.push(where('status', '==', filters.state));
  if (filters.priority && filters.priority !== 'all') constraints.push(where('priority', '==', filters.priority));
  if (filters.state === 'open') constraints.push(where('status', '==', 'open'));
  const q = query(collection(db, 'handovers'), ...constraints);
  return onSnapshot(q, (snapshot) => cb(snapshot.docs.map((d) => mapDoc<HandoverDoc>(d.id, d.data()))));
};

export const createHandover = async (payload: Omit<HandoverDoc, 'id' | 'createdAt' | 'updatedAt'>) => {
  await addDoc(collection(db, 'handovers'), {
    ...payload,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const updateHandover = async (id: string, payload: Partial<HandoverDoc>) => {
  await updateDoc(doc(db, 'handovers', id), { ...payload, updatedAt: serverTimestamp() });
};

export const subscribeToAnnouncements = (publishedOnly: boolean, cb: (items: AnnouncementDoc[]) => void) => {
  const constraints: QueryConstraint[] = [orderBy('updatedAt', 'desc')];
  if (publishedOnly) constraints.push(where('published', '==', true));
  const q = query(collection(db, 'announcements'), ...constraints);
  return onSnapshot(q, (snapshot) => cb(snapshot.docs.map((d) => mapDoc<AnnouncementDoc>(d.id, d.data()))));
};

export const createAnnouncement = async (payload: Omit<AnnouncementDoc, 'id' | 'createdAt' | 'updatedAt'>) => {
  await addDoc(collection(db, 'announcements'), {
    ...payload,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const updateAnnouncement = async (id: string, payload: Partial<AnnouncementDoc>) => {
  await updateDoc(doc(db, 'announcements', id), { ...payload, updatedAt: serverTimestamp() });
};

export const subscribeToLinks = (visibleOnly: boolean, cb: (items: LinkDoc[]) => void) => {
  const constraints: QueryConstraint[] = [orderBy('category', 'asc'), orderBy('sortOrder', 'asc')];
  if (visibleOnly) constraints.push(where('visible', '==', true));
  return onSnapshot(query(collection(db, 'links'), ...constraints), (snapshot) =>
    cb(snapshot.docs.map((d) => mapDoc<LinkDoc>(d.id, d.data())))
  );
};

export const createLink = async (payload: Omit<LinkDoc, 'id' | 'createdAt' | 'updatedAt'>) => {
  await addDoc(collection(db, 'links'), { ...payload, createdAt: serverTimestamp(), updatedAt: serverTimestamp() });
};

export const updateLink = async (id: string, payload: Partial<LinkDoc>) => {
  await updateDoc(doc(db, 'links', id), { ...payload, updatedAt: serverTimestamp() });
};

export const deleteLink = async (id: string) => {
  await setDoc(doc(db, 'links', id), { visible: false, updatedAt: serverTimestamp() }, { merge: true });
};

export const getOpenHandoverCount = async () => {
  const snapshot = await getDocs(query(collection(db, 'handovers'), where('status', '==', 'open')));
  return snapshot.size;
};
