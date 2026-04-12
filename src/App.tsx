import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { RequireAuth } from './components/RequireAuth';
import { useAuth } from './contexts/AuthContext';
import { AdminPage } from './pages/AdminPage';
import { ChatPage } from './pages/ChatPage';
import { GroupsPage } from './pages/GroupsPage';
import { HandoversPage } from './pages/HandoversPage';
import { LinksPage } from './pages/LinksPage';
import { LoginPage } from './pages/LoginPage';
import { ReleasesPage } from './pages/ReleasesPage';
import { SettingsPage } from './pages/SettingsPage';
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
        <Route path="/chat" element={<ChatPage />} />
        <Route path="/handovers" element={<HandoversPage />} />
        <Route path="/team" element={<TeamPage />} />
        <Route path="/links" element={<LinksPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="/releases" element={<ReleasesPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Route>
      <Route path="*" element={<Navigate to={user ? '/' : '/login'} replace />} />
    </Routes>
  );
}
