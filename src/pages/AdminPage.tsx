import { type FormEvent, useEffect, useState } from 'react';
import { createAnnouncement, subscribeToAnnouncements, subscribeToGroups, subscribeToUsers, updateAnnouncement } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import type { AnnouncementDoc, GroupDoc, UserProfile } from '../types';

export const AdminPage = () => {
  const { user, profile } = useAuth();
  const [announcements, setAnnouncements] = useState<AnnouncementDoc[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [groups, setGroups] = useState<GroupDoc[]>([]);
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => subscribeToAnnouncements(false, setAnnouncements), []);
  useEffect(() => subscribeToUsers(setUsers), []);
  useEffect(() => subscribeToGroups(setGroups), []);

  if (profile?.role !== 'admin') return <p>Nicht berechtigt.</p>;

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) return;
    await createAnnouncement({
      title,
      message,
      published: true,
      createdBy: user.displayName ?? user.email ?? 'Unknown',
      createdByUid: user.uid
    });
    setTitle('');
    setMessage('');
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>Master Admin Panel</h2>
        <p>Du bist als globaler Admin eingeloggt und siehst alle Nutzer + Gruppen.</p>
        <form className="stack" onSubmit={submit}>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Ankündigung Titel" required />
          <textarea value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Nachricht" required />
          <button className="btn">Publizieren</button>
        </form>
        <ul className="list">
          {announcements.map((item) => (
            <li key={item.id}>
              <div>
                <strong>{item.title}</strong>
                <p>{item.message}</p>
              </div>
              <button className="btn btn-secondary" onClick={() => void updateAnnouncement(item.id, { published: !item.published })}>
                {item.published ? 'Archivieren' : 'Veröffentlichen'}
              </button>
            </li>
          ))}
        </ul>
      </section>

      <section className="card bubble">
        <h2>Alle Nutzer</h2>
        <ul className="list">
          {users.map((entry) => (
            <li key={entry.uid}>
              <div className="profile-line">
                <img src={entry.photoURL} alt={entry.displayName} className="avatar" />
                <div>
                  <strong>{entry.displayName}</strong>
                  <p>{entry.email}</p>
                </div>
              </div>
              <span className="pill">{entry.role}</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="card bubble full">
        <h2>Gruppenübersicht</h2>
        <ul className="list">
          {groups.map((group) => (
            <li key={group.id}>
              <div>
                <strong>{group.name}</strong>
                <p>Admins: {group.adminUids.length} · Mitglieder: {group.memberUids.length}</p>
              </div>
              <small>{group.createdByName}</small>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
};
