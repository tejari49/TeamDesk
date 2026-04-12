import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { RequireAuth } from './components/RequireAuth';
import { useAuth } from './contexts/AuthContext';
import { AdminPage } from './pages/AdminPage';
import { GroupsPage } from './pages/GroupsPage';
import { HandoversPage } from './pages/HandoversPage';
import { LinksPage } from './pages/LinksPage';
import { LoginPage } from './pages/LoginPage';
import { TeamPage } from './pages/TeamPage';
import { TodayPage } from './pages/TodayPage';

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route path="/" element={<TodayPage />} />
        <Route path="/groups" element={<GroupsPage />} />
        <Route path="/handovers" element={<HandoversPage />} />
        <Route path="/team" element={<TeamPage />} />
        <Route path="/links" element={<LinksPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Route>
      <Route path="*" element={<Navigate to={user ? '/' : '/login'} replace />} />
    </Routes>
  );
}
