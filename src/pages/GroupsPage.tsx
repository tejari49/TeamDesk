import { type FormEvent, useEffect, useMemo, useState } from 'react';
import {
  addUserToGroupByCode,
  canEditMessage,
  conversationIdFromUids,
  createGroup,
  deleteDirectMessage,
  deleteGroupMessage,
  promoteGroupAdmin,
  removeUserFromGroup,
  sendDirectMessage,
  sendGroupMessage,
  subscribeToDirectMessages,
  subscribeToGroupMessages,
  subscribeToGroups,
  subscribeUsersByUids,
  updateDirectMessage,
  updateGroupMessage
} from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import type { DirectMessageDoc, GroupDoc, GroupMessageDoc, UserProfile } from '../types';
import { formatRelativeTime } from '../utils/date';

const isOnline = (date?: { toDate: () => Date }) => !!date && Date.now() - date.toDate().getTime() < 5 * 60 * 1000;

export const GroupsPage = () => {
  const { user } = useAuth();
  const [groups, setGroups] = useState<GroupDoc[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [groupName, setGroupName] = useState('');
  const [selectedGroup, setSelectedGroup] = useState('');
  const [inviteCode, setInviteCode] = useState('');
  const [groupMessage, setGroupMessage] = useState('');
  const [groupMessages, setGroupMessages] = useState<GroupMessageDoc[]>([]);
  const [contactUid, setContactUid] = useState('');
  const [directText, setDirectText] = useState('');
  const [directMessages, setDirectMessages] = useState<DirectMessageDoc[]>([]);

  useEffect(() => subscribeToGroups(setGroups), []);

  const myGroups = useMemo(() => groups.filter((g) => g.memberUids.includes(user?.uid ?? '')), [groups, user?.uid]);
  const activeGroup = groups.find((g) => g.id === selectedGroup) ?? myGroups[0];
  const memberUids = useMemo(() => activeGroup?.memberUids ?? [], [activeGroup]);

  useEffect(() => subscribeUsersByUids(memberUids, setUsers), [memberUids]);
  useEffect(() => {
    if (!activeGroup) return;
    return subscribeToGroupMessages(activeGroup.id, setGroupMessages);
  }, [activeGroup]);

  const conversationId = useMemo(
    () => (user?.uid && contactUid ? conversationIdFromUids(user.uid, contactUid) : ''),
    [contactUid, user?.uid]
  );

  useEffect(() => {
    if (!conversationId) {
      setDirectMessages([]);
      return;
    }
    return subscribeToDirectMessages(conversationId, setDirectMessages);
  }, [conversationId]);

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

  const sendToGroup = async (event: FormEvent) => {
    event.preventDefault();
    if (!activeGroup || !user || !groupMessage.trim()) return;
    await sendGroupMessage(activeGroup.id, user.uid, user.displayName ?? user.email ?? 'User', groupMessage);
    setGroupMessage('');
  };

  const sendToContact = async (event: FormEvent) => {
    event.preventDefault();
    if (!user || !contactUid || !directText.trim() || !conversationId) return;
    await sendDirectMessage(conversationId, user.uid, contactUid, user.displayName ?? user.email ?? 'User', directText);
    setDirectText('');
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

      <section className="card bubble full">
        <h2>Gruppenchat</h2>
        <form className="inline" onSubmit={sendToGroup}>
          <input value={groupMessage} onChange={(e) => setGroupMessage(e.target.value)} placeholder="Nachricht an Gruppe" />
          <button className="btn">Senden</button>
        </form>
        <ul className="list">
          {groupMessages.map((msg) => {
            const editable = msg.senderUid === user?.uid && canEditMessage(msg.createdAt);
            return (
              <li key={msg.id}>
                <div>
                  <strong>{msg.senderName}</strong>
                  <p>{msg.content}</p>
                  <small>{formatRelativeTime(msg.createdAt)}</small>
                </div>
                {editable && (
                  <div className="inline">
                    <button className="btn btn-secondary" onClick={() => { const next = window.prompt('Bearbeiten', msg.content); if (next) void updateGroupMessage(msg.id, next); }}>Bearbeiten</button>
                    <button className="btn btn-danger" onClick={() => void deleteGroupMessage(msg.id)}>Löschen</button>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </section>

      <section className="card bubble full">
        <h2>Direktnachrichten</h2>
        <select value={contactUid} onChange={(e) => setContactUid(e.target.value)}>
          <option value="">Kontakt wählen</option>
          {users.filter((u) => u.uid !== user?.uid).map((u) => <option key={u.uid} value={u.uid}>{u.displayName}</option>)}
        </select>
        <form className="inline" onSubmit={sendToContact}>
          <input value={directText} onChange={(e) => setDirectText(e.target.value)} placeholder="Nachricht an Kontakt" />
          <button className="btn">Senden</button>
        </form>
        <ul className="list">
          {directMessages.map((msg) => {
            const editable = msg.senderUid === user?.uid && canEditMessage(msg.createdAt);
            return (
              <li key={msg.id}>
                <div>
                  <strong>{msg.senderName}</strong>
                  <p>{msg.content}</p>
                  <small>{formatRelativeTime(msg.createdAt)}</small>
                </div>
                {editable && (
                  <div className="inline">
                    <button className="btn btn-secondary" onClick={() => { const next = window.prompt('Bearbeiten', msg.content); if (next) void updateDirectMessage(msg.id, next); }}>Bearbeiten</button>
                    <button className="btn btn-danger" onClick={() => void deleteDirectMessage(msg.id)}>Löschen</button>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </section>
    </div>
  );
};
