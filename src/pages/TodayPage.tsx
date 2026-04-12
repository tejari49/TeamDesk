import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getOpenHandoverCount, subscribeToAnnouncements, subscribeToGroups, subscribeToStatusesByDate, subscribeUsersByUids } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import { formatRelativeTime, todayIso } from '../utils/date';
import type { AnnouncementDoc, GroupDoc, StatusDoc, UserProfile } from '../types';

const online = (u: UserProfile) => !!u.lastActiveAt && Date.now() - u.lastActiveAt.toDate().getTime() < 5 * 60 * 1000;

export const TodayPage = () => {
  const { user, profile } = useAuth();
  const [statuses, setStatuses] = useState<StatusDoc[]>([]);
  const [announcements, setAnnouncements] = useState<AnnouncementDoc[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [groups, setGroups] = useState<GroupDoc[]>([]);
  const [openCount, setOpenCount] = useState(0);

  useEffect(() => subscribeToStatusesByDate(todayIso(), setStatuses), []);
  useEffect(() => subscribeToAnnouncements(true, setAnnouncements), []);
  useEffect(() => {
    if (!user) return;
    return subscribeToGroups(user.uid, profile?.role === 'admin', setGroups);
  }, [profile?.role, user]);
  useEffect(() => {
    void getOpenHandoverCount().then(setOpenCount);
  }, []);

  const myGroups = useMemo(() => groups.filter((g) => g.memberUids.includes(user?.uid ?? '')), [groups, user?.uid]);
  const memberUids = useMemo(() => Array.from(new Set(myGroups.flatMap((g) => g.memberUids))), [myGroups]);

  useEffect(() => subscribeUsersByUids(memberUids, setUsers), [memberUids]);

  const onlineCount = useMemo(() => users.filter(online).length, [users]);
  const statusByUid = useMemo(() => new Map(statuses.map((s) => [s.uid, s])), [statuses]);

  return (
    <div>
      <h2>Guten Morgen, {user?.displayName ?? 'Kollege'} 👋</h2>
      <div className="grid-3">
        <section className="card bubble">
          <h3>Mein Verknüpfungscode</h3>
          <p className="big-number">{profile?.userCode ?? '-'}</p>
          <small>Diesen Code teilst du mit Gruppenadmins.</small>
        </section>

        <section className="card bubble">
          <h3>Online jetzt</h3>
          <p className="big-number">{onlineCount}</p>
          <small>{users.length} Nutzer in deinen Gruppen</small>
        </section>
        <section className="card bubble">
          <h3>Offene Handovers</h3>
          <p className="big-number">{openCount}</p>
        </section>
        <section className="card bubble">
          <h3>Meine Gruppen</h3>
          <p className="big-number">{myGroups.length}</p>
          <Link className="btn" to="/groups">Gruppen öffnen</Link>
        </section>
      </div>

      <section className="card bubble">
        <h3>Status heute (nur Gruppenmitglieder)</h3>
        <ul className="list">
          {users.map((member) => {
            const statusItem = statusByUid.get(member.uid);
            return (
              <li key={member.uid}>
                <div>
                  <strong>{member.displayName}</strong>
                  <p>{statusItem?.status ?? 'kein Status'}</p>
                  {statusItem?.note && <p className="note-italic">{statusItem.note}</p>}
                </div>
                <small>{statusItem ? formatRelativeTime(statusItem.updatedAt) : ''}</small>
              </li>
            );
          })}
        </ul>
      </section>

      <section className="card bubble">
        <h3>Aktuelle Ankündigungen</h3>
        <ul className="list">
          {announcements.map((a) => (
            <li key={a.id}>
              <div><strong>{a.title}</strong><p>{a.message}</p></div>
              <small>{formatRelativeTime(a.updatedAt)}</small>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
};
