import {
  addDoc,
  arrayRemove,
  arrayUnion,
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
import type {
  AnnouncementDoc,
  GroupDoc,
  HandoverDoc,
  HandoverPriority,
  HandoverState,
  LinkDoc,
  StatusDoc,
  TeamStatus,
  UserProfile
} from '../types';

const mapDoc = <T>(id: string, data: object) => ({ id, ...(data as T) });

const avatarFromEmail = (email: string, displayName: string) =>
  `https://api.dicebear.com/9.x/glass/svg?seed=${encodeURIComponent(displayName || email)}`;

export const createOrUpdateUserProfile = async (input: {
  uid: string;
  email: string;
  displayName: string;
  photoURL?: string;
}) => {
  const userRef = doc(db, 'users', input.uid);
  const existing = await getDoc(userRef);
  const role = ADMIN_EMAIL_ALLOWLIST.includes(input.email) ? 'admin' : 'member';
  const photoURL = input.photoURL || avatarFromEmail(input.email, input.displayName);

  if (existing.exists()) {
    const current = existing.data() as Partial<UserProfile>;
    await updateDoc(userRef, {
      displayName: input.displayName,
      email: input.email,
      photoURL,
      role: current.role ?? role,
      lastActiveAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
    return;
  }

  await setDoc(userRef, {
    uid: input.uid,
    displayName: input.displayName,
    email: input.email,
    photoURL,
    role,
    groupIds: [],
    lastActiveAt: serverTimestamp(),
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const touchUserActivity = async (uid: string) => {
  await updateDoc(doc(db, 'users', uid), {
    lastActiveAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const updateOwnProfile = async (uid: string, payload: { displayName: string; photoURL: string }) => {
  await updateDoc(doc(db, 'users', uid), {
    displayName: payload.displayName,
    photoURL: payload.photoURL,
    updatedAt: serverTimestamp()
  });
};

export const subscribeToUsers = (cb: (users: UserProfile[]) => void) =>
  onSnapshot(query(collection(db, 'users'), orderBy('displayName', 'asc')), (snapshot) => {
    cb(snapshot.docs.map((d) => mapDoc<UserProfile>(d.id, d.data())));
  });

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
  const id = `${payload.uid}_${payload.date.split('-').join('_')}`;
  await setDoc(
    doc(db, 'statuses', id),
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
  return onSnapshot(query(collection(db, 'handovers'), ...constraints), (snapshot) =>
    cb(snapshot.docs.map((d) => mapDoc<HandoverDoc>(d.id, d.data())))
  );
};

export const createHandover = async (payload: Omit<HandoverDoc, 'id' | 'createdAt' | 'updatedAt'>) => {
  await addDoc(collection(db, 'handovers'), { ...payload, createdAt: serverTimestamp(), updatedAt: serverTimestamp() });
};

export const updateHandover = async (id: string, payload: Partial<HandoverDoc>) => {
  await updateDoc(doc(db, 'handovers', id), { ...payload, updatedAt: serverTimestamp() });
};

export const subscribeToAnnouncements = (publishedOnly: boolean, cb: (items: AnnouncementDoc[]) => void) => {
  const constraints: QueryConstraint[] = [orderBy('updatedAt', 'desc')];
  if (publishedOnly) constraints.push(where('published', '==', true));
  return onSnapshot(query(collection(db, 'announcements'), ...constraints), (snapshot) =>
    cb(snapshot.docs.map((d) => mapDoc<AnnouncementDoc>(d.id, d.data())))
  );
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

export const getOpenHandoverCount = async () => {
  const snapshot = await getDocs(query(collection(db, 'handovers'), where('status', '==', 'open')));
  return snapshot.size;
};

export const subscribeToGroups = (cb: (groups: GroupDoc[]) => void) =>
  onSnapshot(query(collection(db, 'groups'), orderBy('name', 'asc')), (snapshot) => {
    cb(snapshot.docs.map((d) => mapDoc<GroupDoc>(d.id, d.data())));
  });

export const createGroup = async (payload: {
  name: string;
  createdByUid: string;
  createdByName: string;
}) => {
  const groupRef = await addDoc(collection(db, 'groups'), {
    name: payload.name,
    createdByUid: payload.createdByUid,
    createdByName: payload.createdByName,
    adminUids: [payload.createdByUid],
    memberUids: [payload.createdByUid],
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });

  await updateDoc(doc(db, 'users', payload.createdByUid), {
    groupIds: arrayUnion(groupRef.id),
    updatedAt: serverTimestamp()
  });
};

export const addUserToGroup = async (groupId: string, uid: string) => {
  await updateDoc(doc(db, 'groups', groupId), {
    memberUids: arrayUnion(uid),
    updatedAt: serverTimestamp()
  });
  await updateDoc(doc(db, 'users', uid), {
    groupIds: arrayUnion(groupId),
    updatedAt: serverTimestamp()
  });
};

export const removeUserFromGroup = async (groupId: string, uid: string) => {
  await updateDoc(doc(db, 'groups', groupId), {
    memberUids: arrayRemove(uid),
    adminUids: arrayRemove(uid),
    updatedAt: serverTimestamp()
  });
  await updateDoc(doc(db, 'users', uid), {
    groupIds: arrayRemove(groupId),
    updatedAt: serverTimestamp()
  });
};

export const promoteGroupAdmin = async (groupId: string, uid: string) => {
  await updateDoc(doc(db, 'groups', groupId), {
    adminUids: arrayUnion(uid),
    memberUids: arrayUnion(uid),
    updatedAt: serverTimestamp()
  });
};
