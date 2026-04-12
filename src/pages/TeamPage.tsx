import { type FormEvent, useEffect, useMemo, useState } from 'react';
import { subscribeToStatusesByDate, subscribeToUsers, updateOwnProfile, upsertStatus } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import { formatRelativeTime, todayIso } from '../utils/date';
import { StatusBadge } from '../components/StatusBadge';
import type { StatusDoc, TeamStatus, UserProfile } from '../types';

const statuses: TeamStatus[] = ['office', 'remote', 'vacation', 'sick', 'unavailable'];

const avatarUrl = (seed: string) => `https://api.dicebear.com/9.x/glass/svg?seed=${encodeURIComponent(seed)}`;

export const TeamPage = () => {
  const { user, profile } = useAuth();
  const [team, setTeam] = useState<UserProfile[]>([]);
  const [todayStatuses, setTodayStatuses] = useState<StatusDoc[]>([]);
  const [status, setStatus] = useState<TeamStatus>('office');
  const [note, setNote] = useState('');
  const [date, setDate] = useState(todayIso());
  const [displayName, setDisplayName] = useState(profile?.displayName ?? '');
  const [photoSeed, setPhotoSeed] = useState(profile?.displayName ?? 'teamdesk');

  useEffect(() => subscribeToUsers(setTeam), []);
  useEffect(() => subscribeToStatusesByDate(todayIso(), setTodayStatuses), []);
  useEffect(() => {
    if (profile) {
      setDisplayName(profile.displayName);
      setPhotoSeed(profile.displayName || 'teamdesk');
    }
  }, [profile]);

  const statusByUid = useMemo(() => new Map(todayStatuses.map((s) => [s.uid, s])), [todayStatuses]);

  const submitStatus = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) return;
    await upsertStatus({
      uid: user.uid,
      displayName: displayName || user.displayName || user.email || 'Unknown',
      date,
      status,
      note
    });
    setNote('');
  };

  const submitProfile = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) return;
    await updateOwnProfile(user.uid, { displayName, photoURL: avatarUrl(photoSeed) });
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>Mein Profil</h2>
        <form className="stack" onSubmit={submitProfile}>
          <div className="profile-line">
            <img className="avatar lg" src={avatarUrl(photoSeed)} alt="Avatar Vorschau" />
            <p className="hint">Avatar über DiceBear API (keine echten Fotos).</p>
          </div>
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} placeholder="Anzeigename" />
          <input value={photoSeed} onChange={(e) => setPhotoSeed(e.target.value)} placeholder="Avatar Seed" />
          <button className="btn">Profil speichern</button>
        </form>
      </section>

      <section className="card bubble">
        <h2>Status setzen</h2>
        <form className="stack" onSubmit={submitStatus}>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          <select value={status} onChange={(e) => setStatus(e.target.value as TeamStatus)}>
            {statuses.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
          <textarea value={note} onChange={(e) => setNote(e.target.value)} placeholder="Optionaler Hinweis" />
          <button className="btn">Status speichern</button>
        </form>
      </section>

      <section className="card bubble full">
        <h2>Team Verzeichnis & Status heute</h2>
        <ul className="list">
          {team.map((member) => (
            <li key={member.uid}>
              <div className="profile-line">
                <img src={member.photoURL} alt={member.displayName} className="avatar" />
                <div>
                  <strong>{member.displayName}</strong>
                  <p>{member.email}</p>
                </div>
              </div>
              {statusByUid.get(member.uid) ? (
                <div>
                  <StatusBadge status={statusByUid.get(member.uid)?.status ?? 'unavailable'} />
                  <small>{formatRelativeTime(statusByUid.get(member.uid)?.updatedAt)}</small>
                </div>
              ) : (
                <small>Kein Status</small>
              )}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
};
