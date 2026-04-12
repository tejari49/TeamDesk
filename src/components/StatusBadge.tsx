import type { TeamStatus } from '../types';

export const StatusBadge = ({ status }: { status: TeamStatus }) => {
  return <span className={`badge badge-${status}`}>{status}</span>;
};
