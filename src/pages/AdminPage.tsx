import { type FormEvent, useEffect, useState } from 'react';
import { createAnnouncement, subscribeToAnnouncements, subscribeToUsers, updateAnnouncement } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import type { AnnouncementDoc, UserProfile } from '../types';

export const AdminPage = () => {
  const { user, profile } = useAuth();
  const [announcements, setAnnouncements] = useState<AnnouncementDoc[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => subscribeToAnnouncements(false, setAnnouncements), []);
  useEffect(() => subscribeToUsers(setUsers), []);

  if (profile?.role !== 'admin') return <p>Not authorized.</p>;

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
      <section className="card">
        <h2>Manage announcements</h2>
        <form className="stack" onSubmit={submit}>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Title" required />
          <textarea value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Message" required />
          <button className="btn">Publish</button>
        </form>
        <ul className="list">
          {announcements.map((item) => (
            <li key={item.id}>
              <div>
                <strong>{item.title}</strong>
                <p>{item.message}</p>
              </div>
              <button
                className="btn btn-secondary"
                onClick={() => void updateAnnouncement(item.id, { published: !item.published })}
              >
                {item.published ? 'Archive' : 'Publish'}
              </button>
            </li>
          ))}
        </ul>
      </section>

      <section className="card">
        <h2>Team roles</h2>
        <ul className="list">
          {users.map((entry) => (
            <li key={entry.uid}>
              <div>
                <strong>{entry.displayName}</strong>
                <p>{entry.email}</p>
              </div>
              <span className="pill">{entry.role}</span>
            </li>
          ))}
        </ul>
        <p className="hint">Seed: add links and one announcement to avoid empty first-run screens.</p>
      </section>
    </div>
  );
};
