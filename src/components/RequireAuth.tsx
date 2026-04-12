import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const RequireAuth = ({ children }: { children: JSX.Element }) => {
  const { user, loading } = useAuth();
  if (loading) return <div className="centered">Loading…</div>;
  if (!user) return <Navigate to="/login" replace />;
  return children;
};
