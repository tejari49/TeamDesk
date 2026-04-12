import type { Timestamp } from 'firebase/firestore';

export type UserRole = 'admin' | 'member';
export type TeamStatus = 'office' | 'remote' | 'vacation' | 'sick' | 'unavailable';
export type HandoverPriority = 'low' | 'medium' | 'high';
export type HandoverState = 'open' | 'done';

export interface UserProfile {
  uid: string;
  userCode: string;
  displayName: string;
  email: string;
  photoURL?: string;
  role: UserRole;
  language?: 'de' | 'en';
  groupIds?: string[];
  lastActiveAt?: Timestamp;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface StatusDoc {
  id: string;
  uid: string;
  date: string;
  status: TeamStatus;
  note: string;
  displayName: string;
  updatedAt: Timestamp;
}

export interface HandoverDoc {
  id: string;
  title: string;
  description: string;
  priority: HandoverPriority;
  status: HandoverState;
  assignedTo?: string;
  assignedToUid?: string;
  createdBy: string;
  createdByUid: string;
  dueDate?: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface AnnouncementDoc {
  id: string;
  title: string;
  message: string;
  published: boolean;
  createdBy: string;
  createdByUid: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface LinkDoc {
  id: string;
  title: string;
  url: string;
  category: string;
  sortOrder: number;
  visible: boolean;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface GroupDoc {
  id: string;
  name: string;
  createdByUid: string;
  createdByName: string;
  adminUids: string[];
  memberUids: string[];
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface GroupMessageDoc {
  id: string;
  groupId: string;
  senderUid: string;
  senderName: string;
  content: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface DirectMessageDoc {
  id: string;
  conversationId: string;
  senderUid: string;
  receiverUid: string;
  senderName: string;
  content: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface ReleaseLogDoc {
  id: string;
  version: string;
  category: "new" | "fix" | "change";
  message: string;
  actorUid: string;
  actorName: string;
  createdAt: Timestamp;
}
