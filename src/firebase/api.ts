import {
  addDoc,
  arrayRemove,
  arrayUnion,
  collection,
  doc,
  getDoc,
  getDocs,
  limit,
  onSnapshot,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
  type QueryConstraint,
  type Timestamp
} from 'firebase/firestore';
import { db } from './client';
import { ADMIN_EMAIL_ALLOWLIST, APP_VERSION } from './config';
import type {
  AnnouncementDoc,
  DirectMessageDoc,
  GroupDoc,
  GroupMessageDoc,
  HandoverDoc,
  HandoverPriority,
  HandoverState,
  LinkDoc,
  StatusDoc,
  TeamStatus,
  UserProfile,
  ReleaseLogDoc
} from '../types';

const mapDoc = <T>(id: string, data: object) => ({ id, ...(data as T) });
const avatarFromEmail = (email: string, displayName: string) =>
  `https://api.dicebear.com/9.x/glass/svg?seed=${encodeURIComponent(displayName || email)}`;
const makeUserCode = (uid: string) => uid.slice(0, 3).toUpperCase() + uid.slice(-5).toUpperCase();

export const createReleaseLog = async (payload: {
  category: 'new' | 'fix' | 'change';
  message: string;
  actorUid: string;
  actorName: string;
}) =>
  addDoc(collection(db, 'release_logs'), {
    version: APP_VERSION,
    category: payload.category,
    message: payload.message,
    actorUid: payload.actorUid,
    actorName: payload.actorName,
    createdAt: serverTimestamp()
  });

export const subscribeReleaseLogs = (cb: (items: ReleaseLogDoc[]) => void) =>
  onSnapshot(query(collection(db, 'release_logs'), orderBy('createdAt', 'desc'), limit(200)), (snapshot) =>
    cb(snapshot.docs.map((d) => mapDoc<ReleaseLogDoc>(d.id, d.data())))
  );

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
      role: role === 'admin' ? 'admin' : current.role ?? role,
      userCode: current.userCode ?? makeUserCode(input.uid),
      language: current.language ?? 'de',
      lastActiveAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
    return;
  }

  await setDoc(userRef, {
    uid: input.uid,
    userCode: makeUserCode(input.uid),
    displayName: input.displayName,
    email: input.email,
    photoURL,
    role,
    language: 'de',
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


export const updateUserLanguage = async (uid: string, language: 'de' | 'en') => {
  await updateDoc(doc(db, 'users', uid), {
    language,
    updatedAt: serverTimestamp()
  });
  await createReleaseLog({ category: 'change', message: `Sprache geändert: ${language}`, actorUid: uid, actorName: uid });
};

export const updateOwnProfile = async (uid: string, payload: { displayName: string; photoURL: string }) => {
  await updateDoc(doc(db, 'users', uid), {
    displayName: payload.displayName,
    photoURL: payload.photoURL,
    updatedAt: serverTimestamp()
  });
  await createReleaseLog({ category: 'change', message: 'Profil aktualisiert', actorUid: uid, actorName: payload.displayName });
};

export const subscribeToUsers = (cb: (users: UserProfile[]) => void) =>
  onSnapshot(query(collection(db, 'users'), orderBy('displayName', 'asc')), (snapshot) => {
    cb(snapshot.docs.map((d) => mapDoc<UserProfile>(d.id, d.data())));
  });

export const subscribeUsersByUids = (uids: string[], cb: (users: UserProfile[]) => void) => {
  if (!uids.length) {
    cb([]);
    return () => undefined;
  }
  const chunks = [] as string[][];
  for (let i = 0; i < uids.length; i += 10) chunks.push(uids.slice(i, i + 10));
  const unsubs = chunks.map((chunk) =>
    onSnapshot(query(collection(db, 'users'), where('uid', 'in', chunk)), () => {
      void getUsersByUids(uids).then(cb);
    })
  );
  void getUsersByUids(uids).then(cb);
  return () => unsubs.forEach((u) => u());
};

export const getUsersByUids = async (uids: string[]) => {
  if (!uids.length) return [] as UserProfile[];
  const chunks = [] as string[][];
  for (let i = 0; i < uids.length; i += 10) chunks.push(uids.slice(i, i + 10));
  const docs = await Promise.all(
    chunks.map((chunk) => getDocs(query(collection(db, 'users'), where('uid', 'in', chunk))))
  );
  return docs.flatMap((snap) => snap.docs.map((d) => mapDoc<UserProfile>(d.id, d.data())));
};

export const getUserByCode = async (code: string) => {
  const snap = await getDocs(query(collection(db, 'users'), where('userCode', '==', code), limit(1)));
  if (snap.empty) return null;
  const docSnap = snap.docs[0];
  return mapDoc<UserProfile>(docSnap.id, docSnap.data());
};

export const subscribeToStatusesByDate = (date: string, cb: (statuses: StatusDoc[]) => void) =>
  onSnapshot(
    query(collection(db, 'statuses'), where('date', '==', date), orderBy('updatedAt', 'desc')),
    (snapshot) => cb(snapshot.docs.map((d) => mapDoc<StatusDoc>(d.id, d.data())))
  );

export const upsertStatus = async (payload: {
  uid: string;
  displayName: string;
  date: string;
  status: TeamStatus;
  note: string;
}) => {
  const id = `${payload.uid}_${payload.date.split('-').join('_')}`;
  await setDoc(doc(db, 'statuses', id), { ...payload, updatedAt: serverTimestamp() }, { merge: true });
  await createReleaseLog({ category: 'change', message: `Status gesetzt: ${payload.status}`, actorUid: payload.uid, actorName: payload.displayName });
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
  await createReleaseLog({ category: 'new', message: `Handover erstellt: ${payload.title}`, actorUid: payload.createdByUid, actorName: payload.createdBy });
};

export const updateHandover = async (id: string, payload: Partial<HandoverDoc>) =>
  updateDoc(doc(db, 'handovers', id), { ...payload, updatedAt: serverTimestamp() });

export const subscribeToAnnouncements = (publishedOnly: boolean, cb: (items: AnnouncementDoc[]) => void) => {
  const constraints: QueryConstraint[] = [orderBy('updatedAt', 'desc')];
  if (publishedOnly) constraints.push(where('published', '==', true));
  return onSnapshot(query(collection(db, 'announcements'), ...constraints), (snapshot) =>
    cb(snapshot.docs.map((d) => mapDoc<AnnouncementDoc>(d.id, d.data())))
  );
};

export const createAnnouncement = async (payload: Omit<AnnouncementDoc, 'id' | 'createdAt' | 'updatedAt'>) => {
  await addDoc(collection(db, 'announcements'), { ...payload, createdAt: serverTimestamp(), updatedAt: serverTimestamp() });
  await createReleaseLog({ category: 'new', message: `Ankündigung: ${payload.title}`, actorUid: payload.createdByUid, actorName: payload.createdBy });
};

export const updateAnnouncement = async (id: string, payload: Partial<AnnouncementDoc>) =>
  updateDoc(doc(db, 'announcements', id), { ...payload, updatedAt: serverTimestamp() });

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

export const subscribeToGroups = (
  uid: string,
  isAdmin: boolean,
  cb: (groups: GroupDoc[]) => void
) => {
  const base = collection(db, 'groups');
  const q = isAdmin
    ? query(base, orderBy('name', 'asc'))
    : query(base, where('memberUids', 'array-contains', uid), orderBy('name', 'asc'));
  return onSnapshot(
    q,
    (snapshot) => cb(snapshot.docs.map((d) => mapDoc<GroupDoc>(d.id, d.data()))),
    () => cb([])
  );
};

export const createGroup = async (payload: { name: string; createdByUid: string; createdByName: string }) => {
  const groupRef = await addDoc(collection(db, 'groups'), {
    name: payload.name,
    createdByUid: payload.createdByUid,
    createdByName: payload.createdByName,
    adminUids: [payload.createdByUid],
    memberUids: [payload.createdByUid],
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });

  await updateDoc(doc(db, 'users', payload.createdByUid), { groupIds: arrayUnion(groupRef.id), updatedAt: serverTimestamp() });
  await createReleaseLog({ category: 'new', message: `Gruppe erstellt: ${payload.name}`, actorUid: payload.createdByUid, actorName: payload.createdByName });
};

export const addUserToGroup = async (groupId: string, uid: string) => {
  await updateDoc(doc(db, 'groups', groupId), { memberUids: arrayUnion(uid), updatedAt: serverTimestamp() });
  await updateDoc(doc(db, 'users', uid), { groupIds: arrayUnion(groupId), updatedAt: serverTimestamp() });
};

export const addUserToGroupByCode = async (groupId: string, userCode: string) => {
  const user = await getUserByCode(userCode.toUpperCase());
  if (!user) throw new Error('Code nicht gefunden');
  await addUserToGroup(groupId, user.uid);
};

export const removeUserFromGroup = async (groupId: string, uid: string) => {
  await updateDoc(doc(db, 'groups', groupId), {
    memberUids: arrayRemove(uid),
    adminUids: arrayRemove(uid),
    updatedAt: serverTimestamp()
  });
  await updateDoc(doc(db, 'users', uid), { groupIds: arrayRemove(groupId), updatedAt: serverTimestamp() });
};

export const promoteGroupAdmin = async (groupId: string, uid: string) =>
  updateDoc(doc(db, 'groups', groupId), {
    adminUids: arrayUnion(uid),
    memberUids: arrayUnion(uid),
    updatedAt: serverTimestamp()
  });

export const conversationIdFromUids = (uidA: string, uidB: string) => [uidA, uidB].sort().join('__');

export const subscribeToGroupMessages = (groupId: string, cb: (items: GroupMessageDoc[]) => void) =>
  onSnapshot(
    query(collection(db, 'groupMessages'), where('groupId', '==', groupId), orderBy('createdAt', 'asc')),
    (snapshot) => cb(snapshot.docs.map((d) => mapDoc<GroupMessageDoc>(d.id, d.data())))
  );

export const sendGroupMessage = async (groupId: string, senderUid: string, senderName: string, content: string) =>
  addDoc(collection(db, 'groupMessages'), {
    groupId,
    senderUid,
    senderName,
    content,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });

const editableWithinHour = (createdAt?: Timestamp) => {
  if (!createdAt) return false;
  return Date.now() - createdAt.toDate().getTime() <= 3_600_000;
};

export const canEditMessage = editableWithinHour;

export const updateGroupMessage = async (id: string, content: string) =>
  updateDoc(doc(db, 'groupMessages', id), { content, updatedAt: serverTimestamp() });

export const deleteGroupMessage = async (id: string) => updateDoc(doc(db, 'groupMessages', id), { content: '[gelöscht]', updatedAt: serverTimestamp() });

export const subscribeToDirectMessages = (conversationId: string, cb: (items: DirectMessageDoc[]) => void) =>
  onSnapshot(
    query(collection(db, 'directMessages'), where('conversationId', '==', conversationId), orderBy('createdAt', 'asc')),
    (snapshot) => cb(snapshot.docs.map((d) => mapDoc<DirectMessageDoc>(d.id, d.data())))
  );

export const sendDirectMessage = async (
  conversationId: string,
  senderUid: string,
  receiverUid: string,
  senderName: string,
  content: string
) =>
  addDoc(collection(db, 'directMessages'), {
    conversationId,
    senderUid,
    receiverUid,
    senderName,
    content,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });

export const updateDirectMessage = async (id: string, content: string) =>
  updateDoc(doc(db, 'directMessages', id), { content, updatedAt: serverTimestamp() });

export const deleteDirectMessage = async (id: string) =>
  updateDoc(doc(db, 'directMessages', id), { content: '[gelöscht]', updatedAt: serverTimestamp() });
