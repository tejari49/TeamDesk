import type { Timestamp } from 'firebase/firestore';

export type UserRole = 'admin' | 'member';
export type TeamStatus = 'office' | 'remote' | 'vacation' | 'sick' | 'unavailable';
export type HandoverPriority = 'low' | 'medium' | 'high';
export type HandoverState = 'open' | 'done';

export interface UserProfile {
  uid: string;
  displayName: string;
  email: string;
  photoURL?: string;
  role: UserRole;
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
