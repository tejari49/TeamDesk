import { NavLink, Outlet } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useLanguage } from '../contexts/LanguageContext';

type NavItem = { to: string; label: string };

export const Layout = () => {
  const { logout, profile } = useAuth();
  const { t } = useLanguage();
  const isAdmin = profile?.role === 'admin';
  const [showQuickMenu, setShowQuickMenu] = useState(false);

  const items: NavItem[] = [
    { to: '/', label: t('today') },
    { to: '/groups', label: t('groups') },
    { to: '/chat', label: t('chat') },
    { to: '/handovers', label: t('handovers') },
    { to: '/team', label: t('team') },
    { to: '/links', label: t('links') },
    { to: '/settings', label: t('settings') },
    ...(isAdmin ? [{ to: '/admin', label: t('admin') }] : [])
  ];
  const mobileItems: NavItem[] = items.filter((item: NavItem) => ['/', '/chat', '/groups', '/handovers', '/settings'].includes(item.to));

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
          <NavLink to="/releases" className="release-link">{t('releases')}</NavLink>
        </div>
      </main>
      <div className="mobile-fab-wrap">
        {showQuickMenu && (
          <div className="mobile-fab-menu">
            <NavLink to="/team" className="nav-link" onClick={() => setShowQuickMenu(false)}>Status</NavLink>
            <NavLink to="/chat" className="nav-link" onClick={() => setShowQuickMenu(false)}>Chat</NavLink>
            <NavLink to="/handovers" className="nav-link" onClick={() => setShowQuickMenu(false)}>Handover</NavLink>
          </div>
        )}
        <button className="mobile-fab btn" onClick={() => setShowQuickMenu((prev) => !prev)}>+</button>
      </div>
      <nav className="bottom-nav">
        {mobileItems.map((item) => (
          <NavLink key={item.to} to={item.to} className="nav-link">
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
};
