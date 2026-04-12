import type { Timestamp } from 'firebase/firestore';

export const todayIso = () => new Date().toISOString().slice(0, 10);

export const formatRelativeTime = (value?: Timestamp) => {
  if (!value) return 'just now';
  const diffMs = Date.now() - value.toDate().getTime();
  const minutes = Math.round(diffMs / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  return `${days}d ago`;
};
