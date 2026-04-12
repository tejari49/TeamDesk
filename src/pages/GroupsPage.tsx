import { type FormEvent, useEffect, useMemo, useState } from 'react';
import { addUserToGroup, createGroup, promoteGroupAdmin, removeUserFromGroup, subscribeToGroups, subscribeToUsers } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import type { GroupDoc, UserProfile } from '../types';
import { formatRelativeTime } from '../utils/date';

const isOnline = (date?: { toDate: () => Date }) => {
  if (!date) return false;
  return Date.now() - date.toDate().getTime() < 5 * 60 * 1000;
};

export const GroupsPage = () => {
  const { user } = useAuth();
  const [groups, setGroups] = useState<GroupDoc[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [groupName, setGroupName] = useState('');
  const [selectedGroup, setSelectedGroup] = useState('');
  const [selectedUser, setSelectedUser] = useState('');

  useEffect(() => subscribeToGroups(setGroups), []);
  useEffect(() => subscribeToUsers(setUsers), []);

  const myGroups = useMemo(
    () => groups.filter((g) => g.memberUids.includes(user?.uid ?? '')),
    [groups, user?.uid]
  );

  const activeGroup = groups.find((g) => g.id === selectedGroup) ?? myGroups[0];
  const members = users.filter((u) => activeGroup?.memberUids.includes(u.uid));
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
          <input value={groupName} onChange={(e) => setGroupName(e.target.value)} placeholder="Neue Gruppe" />
          <button className="btn">Gruppe erstellen</button>
        </form>

        <ul className="list">
          {myGroups.map((g) => (
            <li key={g.id} onClick={() => setSelectedGroup(g.id)} className={activeGroup?.id === g.id ? 'selected-row' : ''}>
              <div>
                <strong>{g.name}</strong>
                <p>{g.memberUids.length} Mitglieder</p>
              </div>
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
                <select value={selectedUser} onChange={(e) => setSelectedUser(e.target.value)}>
                  <option value="">Nutzer auswählen</option>
                  {users.map((u) => (
                    <option value={u.uid} key={u.uid}>
                      {u.displayName} ({u.email})
                    </option>
                  ))}
                </select>
                <div className="inline">
                  <button className="btn" onClick={() => selectedUser && void addUserToGroup(activeGroup.id, selectedUser)}>
                    Hinzufügen
                  </button>
                  <button className="btn btn-secondary" onClick={() => selectedUser && void promoteGroupAdmin(activeGroup.id, selectedUser)}>
                    Zum Gruppenadmin
                  </button>
                  <button className="btn btn-danger" onClick={() => selectedUser && void removeUserFromGroup(activeGroup.id, selectedUser)}>
                    Entfernen
                  </button>
                </div>
              </div>
            )}

            <ul className="list">
              {members.map((m) => (
                <li key={m.uid}>
                  <div className="profile-line">
                    <img src={m.photoURL} alt={m.displayName} className="avatar" />
                    <div>
                      <strong>{m.displayName}</strong>
                      <p>{m.email}</p>
                    </div>
                  </div>
                  <div>
                    <span className={`online-dot ${isOnline(m.lastActiveAt) ? 'on' : 'off'}`} />
                    <small>{isOnline(m.lastActiveAt) ? 'online' : 'offline'}</small>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}
      </section>
    </div>
  );
};
