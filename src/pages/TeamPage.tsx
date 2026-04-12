import { type FormEvent, useEffect, useMemo, useState } from 'react';
import { subscribeToStatusesByDate, subscribeToUsers, upsertStatus } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import { formatRelativeTime, todayIso } from '../utils/date';
import { StatusBadge } from '../components/StatusBadge';
import type { StatusDoc, TeamStatus, UserProfile } from '../types';

const statuses: TeamStatus[] = ['office', 'remote', 'vacation', 'sick', 'unavailable'];

export const TeamPage = () => {
  const { user, profile } = useAuth();
  const [team, setTeam] = useState<UserProfile[]>([]);
  const [todayStatuses, setTodayStatuses] = useState<StatusDoc[]>([]);
  const [status, setStatus] = useState<TeamStatus>('office');
  const [note, setNote] = useState('');
  const [date, setDate] = useState(todayIso());
  const [saving, setSaving] = useState(false);

  useEffect(() => subscribeToUsers(setTeam), []);
  useEffect(() => subscribeToStatusesByDate(todayIso(), setTodayStatuses), []);

  const statusByUid = useMemo(() => new Map(todayStatuses.map((s) => [s.uid, s])), [todayStatuses]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) return;
    setSaving(true);
    await upsertStatus({
      uid: user.uid,
      displayName: user.displayName ?? user.email ?? 'Unknown',
      date,
      status,
      note
    });
    setSaving(false);
    setNote('');
  };

  return (
    <div className="grid-2">
      <section className="card">
        <h2>Set my status</h2>
        <form className="stack" onSubmit={submit}>
          <label>
            Date
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </label>
          <label>
            Status
            <select value={status} onChange={(e) => setStatus(e.target.value as TeamStatus)}>
              {statuses.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label>
            Note
            <textarea value={note} onChange={(e) => setNote(e.target.value)} placeholder="Optional context" />
          </label>
          <button className="btn" disabled={saving}>{saving ? 'Saving…' : 'Save status'}</button>
        </form>
      </section>

      <section className="card">
        <h2>Team directory</h2>
        <ul className="list">
          {team.map((member) => (
            <li key={member.uid}>
              <div>
                <strong>{member.displayName}</strong>
                <p>{member.email}</p>
                <small>{member.role}</small>
              </div>
              {statusByUid.get(member.uid) ? (
                <div>
                  <StatusBadge status={statusByUid.get(member.uid)?.status ?? 'unavailable'} />
                  <small>{formatRelativeTime(statusByUid.get(member.uid)?.updatedAt)}</small>
                </div>
              ) : (
                <small>No status</small>
              )}
            </li>
          ))}
        </ul>
        {profile?.role === 'admin' && <p className="hint">Admins may edit any status directly in Firestore if urgent.</p>}
      </section>
    </div>
  );
};
