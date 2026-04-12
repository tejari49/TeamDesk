import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

import type { ReactElement } from 'react';

export const RequireAuth = ({ children }: { children: ReactElement }) => {
  const { user, loading } = useAuth();
  if (loading) return <div className="centered">Loading…</div>;
  if (!user) return <Navigate to="/login" replace />;
  return children;
};
