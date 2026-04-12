import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const navItems = [
  { to: '/', label: 'Heute' },
  { to: '/groups', label: 'Gruppen' },
  { to: '/handovers', label: 'Handovers' },
  { to: '/team', label: 'Team' },
  { to: '/links', label: 'Links' },
  { to: '/settings', label: 'Einstellungen' }
];

export const Layout = () => {
  const { logout, profile } = useAuth();
  const isAdmin = profile?.role === 'admin';

  const items = [...navItems, ...(isAdmin ? [{ to: '/admin', label: 'Master Admin' }] : [])];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>TeamDesk</h1>
        <p className="hint-light">{profile?.displayName}</p>
        <nav>
          {items.map((item) => (
            <NavLink key={item.to} to={item.to} className="nav-link">
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className="btn btn-secondary" onClick={() => void logout()}>
          Abmelden
        </button>
      </aside>
      <main className="content">
        <Outlet />
        <div className="release-footer">
          <NavLink to="/releases" className="release-link">Release Verlauf</NavLink>
        </div>
      </main>
      <nav className="bottom-nav">
        {items.map((item) => (
          <NavLink key={item.to} to={item.to} className="nav-link">
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
};
