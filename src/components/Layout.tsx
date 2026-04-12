import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const navItems = [
  { to: '/', label: 'Today' },
  { to: '/handovers', label: 'Handovers' },
  { to: '/team', label: 'Team' },
  { to: '/links', label: 'Links' }
];

export const Layout = () => {
  const { logout, profile } = useAuth();
  const isAdmin = profile?.role === 'admin';

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>TeamDesk</h1>
        <nav>
          {[...navItems, ...(isAdmin ? [{ to: '/admin', label: 'Admin' }] : [])].map((item) => (
            <NavLink key={item.to} to={item.to} className="nav-link">
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className="btn btn-secondary" onClick={() => void logout()}>
          Sign out
        </button>
      </aside>
      <main className="content">
        <Outlet />
      </main>
      <nav className="bottom-nav">
        {[...navItems, ...(isAdmin ? [{ to: '/admin', label: 'Admin' }] : [])].map((item) => (
          <NavLink key={item.to} to={item.to} className="nav-link">
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
};
