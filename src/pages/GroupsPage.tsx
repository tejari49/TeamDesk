import { type FormEvent, useEffect, useMemo, useState } from 'react';
import {
  addUserToGroupByCode,
  createGroup,
  promoteGroupAdmin,
  removeUserFromGroup,
  subscribeToGroups,
  subscribeUsersByUids
} from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import type { GroupDoc, UserProfile } from '../types';
import { formatRelativeTime } from '../utils/date';

const isOnline = (date?: { toDate: () => Date }) => !!date && Date.now() - date.toDate().getTime() < 5 * 60 * 1000;

export const GroupsPage = () => {
  const { user, profile } = useAuth();
  const [groups, setGroups] = useState<GroupDoc[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [groupName, setGroupName] = useState('');
  const [selectedGroup, setSelectedGroup] = useState('');
  const [inviteCode, setInviteCode] = useState('');

  useEffect(() => {
    if (!user) return;
    return subscribeToGroups(user.uid, profile?.role === 'admin', setGroups);
  }, [profile?.role, user]);

  const myGroups = useMemo(() => groups.filter((g) => g.memberUids.includes(user?.uid ?? '')), [groups, user?.uid]);
  const activeGroup = groups.find((g) => g.id === selectedGroup) ?? myGroups[0];
  const memberUids = useMemo(() => activeGroup?.memberUids ?? [], [activeGroup]);

  useEffect(() => subscribeUsersByUids(memberUids, setUsers), [memberUids]);

  const canManage = activeGroup?.adminUids.includes(user?.uid ?? '');

  const submitGroup = async (event: FormEvent) => {
    event.preventDefault();
    if (!user || !groupName.trim()) return;
    await createGroup({
      name: groupName,
      createdByUid: user.uid,
      createdByName: user.displayName ?? user.email ?? 'Unknown'
    });
    setGroupName('');
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>Gruppen</h2>
        <form className="stack" onSubmit={submitGroup}>
        <label>Gruppe auswählen</label>
        <select value={activeGroup?.id ?? ''} onChange={(e) => setSelectedGroup(e.target.value)}>
          <option value="">-- bitte wählen --</option>
          {myGroups.map((g) => (
            <option key={g.id} value={g.id}>{g.name}</option>
          ))}
        </select>

          <input value={groupName} onChange={(e) => setGroupName(e.target.value)} placeholder="Neue Gruppe" />
          <button className="btn">Gruppe erstellen</button>
        </form>

        <ul className="list">
          {myGroups.map((g) => (
            <li key={g.id} onClick={() => setSelectedGroup(g.id)} className={activeGroup?.id === g.id ? 'selected-row' : ''}>
              <div><strong>{g.name}</strong><p>{g.memberUids.length} Mitglieder</p></div>
              <small>{formatRelativeTime(g.updatedAt)}</small>
            </li>
          ))}
        </ul>
      </section>

      <section className="card bubble">
        <h2>{activeGroup ? `Mitglieder: ${activeGroup.name}` : 'Wähle eine Gruppe'}</h2>
        {activeGroup && (
          <>
            {canManage && (
              <div className="stack">
                <label>Mit Code hinzufügen</label>
                <input value={inviteCode} onChange={(e) => setInviteCode(e.target.value.toUpperCase())} placeholder="Nutzercode" />
                <div className="inline">
                  <button className="btn" onClick={() => inviteCode && void addUserToGroupByCode(activeGroup.id, inviteCode)}>Hinzufügen</button>
                  <button className="btn btn-secondary" onClick={() => inviteCode && users.find((u) => u.userCode === inviteCode) && void promoteGroupAdmin(activeGroup.id, users.find((u) => u.userCode === inviteCode)!.uid)}>Zum Gruppenadmin</button>
                  <button className="btn btn-danger" onClick={() => inviteCode && users.find((u) => u.userCode === inviteCode) && void removeUserFromGroup(activeGroup.id, users.find((u) => u.userCode === inviteCode)!.uid)}>Entfernen</button>
                </div>
              </div>
            )}
            <ul className="list">
              {users.map((m) => (
                <li key={m.uid}>
                  <div className="profile-line">
                    <img src={m.photoURL} alt={m.displayName} className="avatar" />
                    <div><strong>{m.displayName}</strong><p>Code: {m.userCode}</p></div>
                  </div>
                  <div><span className={`online-dot ${isOnline(m.lastActiveAt) ? 'on' : 'off'}`} /><small>{isOnline(m.lastActiveAt) ? 'online' : 'offline'}</small></div>
                </li>
              ))}
            </ul>
          </>
        )}
      </section>
    </div>
  );
};
