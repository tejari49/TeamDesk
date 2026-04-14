import { useMemo, useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useLanguage } from '../contexts/LanguageContext';

export const Layout = () => {
  const { logout, profile } = useAuth();
  const { t } = useLanguage();
  const isAdmin = profile?.role === 'admin';
  const [showMore, setShowMore] = useState(false);

  const items = useMemo(
    () => [
      { to: '/', label: t('today') },
      { to: '/groups', label: t('groups') },
      { to: '/chat', label: t('chat') },
      { to: '/handovers', label: t('handovers') },
      { to: '/team', label: t('team') },
      { to: '/links', label: t('links') },
      { to: '/settings', label: t('settings') },
      ...(isAdmin ? [{ to: '/admin', label: t('admin') }] : [])
    ],
    [isAdmin, t]
  );
  const mobilePrimary = useMemo(() => items.slice(0, 3), [items]);
  const mobileMore = useMemo(() => items.slice(3), [items]);

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
      <nav className="bottom-nav">
        {mobilePrimary.map((item) => (
          <NavLink key={item.to} to={item.to} className="nav-link">
            {item.label}
          </NavLink>
        ))}
        <button className="nav-link nav-more" onClick={() => setShowMore((prev) => !prev)}>{showMore ? 'Schließen' : 'Mehr'}</button>
      </nav>
      {showMore && (
        <div className="mobile-more-sheet">
          {mobileMore.map((item) => (
            <NavLink key={item.to} to={item.to} className="nav-link" onClick={() => setShowMore(false)}>
              {item.label}
            </NavLink>
          ))}
        </div>
      )}
    </div>
  );
};
