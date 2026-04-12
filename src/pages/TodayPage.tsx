import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getOpenHandoverCount, subscribeToAnnouncements, subscribeToStatusesByDate } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import { formatRelativeTime, todayIso } from '../utils/date';
import { StatusBadge } from '../components/StatusBadge';
import type { AnnouncementDoc, StatusDoc } from '../types';

export const TodayPage = () => {
  const { user } = useAuth();
  const [statuses, setStatuses] = useState<StatusDoc[]>([]);
  const [announcements, setAnnouncements] = useState<AnnouncementDoc[]>([]);
  const [openCount, setOpenCount] = useState(0);

  useEffect(() => subscribeToStatusesByDate(todayIso(), setStatuses), []);
  useEffect(() => subscribeToAnnouncements(true, setAnnouncements), []);
  useEffect(() => {
    void getOpenHandoverCount().then(setOpenCount);
  }, []);

  const overview = useMemo(() => {
    return statuses.reduce<Record<string, number>>((acc, cur) => {
      acc[cur.status] = (acc[cur.status] ?? 0) + 1;
      return acc;
    }, {});
  }, [statuses]);

  return (
    <div>
      <h2>Good morning, {user?.displayName ?? 'teammate'} 👋</h2>
      <div className="grid-3">
        <section className="card">
          <h3>Today overview</h3>
          <ul>
            {Object.entries(overview).map(([status, count]) => (
              <li key={status}>
                <span className={`badge badge-${status}`}>{status}</span> {count}
              </li>
            ))}
            {statuses.length === 0 && <li>No status updates yet.</li>}
          </ul>
        </section>
        <section className="card">
          <h3>Open handovers</h3>
          <p className="big-number">{openCount}</p>
          <Link className="btn" to="/handovers">
            View handovers
          </Link>
        </section>
        <section className="card">
          <h3>Quick actions</h3>
          <div className="stack">
            <Link className="btn" to="/team">
              Set my status
            </Link>
            <Link className="btn" to="/handovers">
              Create handover
            </Link>
            <Link className="btn" to="/links">
              View links
            </Link>
          </div>
        </section>
      </div>

      <section className="card">
        <h3>Today team statuses</h3>
        {statuses.length === 0 ? (
          <p>No updates yet. Be first to post your status.</p>
        ) : (
          <ul className="list">
            {statuses.map((item) => (
              <li key={item.id}>
                <div>
                  <strong>{item.displayName}</strong> <StatusBadge status={item.status} />
                  {item.note && <p>{item.note}</p>}
                </div>
                <small>{formatRelativeTime(item.updatedAt)}</small>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <h3>Latest announcements</h3>
        {announcements.length === 0 ? (
          <p>No active announcements.</p>
        ) : (
          <ul className="list">
            {announcements.slice(0, 5).map((a) => (
              <li key={a.id}>
                <div>
                  <strong>{a.title}</strong>
                  <p>{a.message}</p>
                </div>
                <small>{formatRelativeTime(a.updatedAt)}</small>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
};
