import { useEffect, useMemo, useState } from 'react';
import {
  canEditMessage,
  conversationIdFromUids,
  deleteDirectMessage,
  deleteGroupMessage,
  reactToDirectMessage,
  reactToGroupMessage,
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

export const ChatPage = () => {
  const { user, profile } = useAuth();
  const [groups, setGroups] = useState<GroupDoc[]>([]);
  const [contacts, setContacts] = useState<UserProfile[]>([]);
  const [activeGroupId, setActiveGroupId] = useState('');
  const [activeContactUid, setActiveContactUid] = useState('');
  const [groupMessages, setGroupMessages] = useState<GroupMessageDoc[]>([]);
  const [directMessages, setDirectMessages] = useState<DirectMessageDoc[]>([]);
  const [text, setText] = useState('');
  const [quote, setQuote] = useState('');
  const [menuId, setMenuId] = useState('');

  useEffect(() => {
    if (!user) return;
    return subscribeToGroups(user.uid, profile?.role === 'admin', setGroups);
  }, [profile?.role, user]);

  const activeGroup = groups.find((g) => g.id === activeGroupId) ?? groups[0];
  const memberUids = useMemo(() => activeGroup?.memberUids ?? [], [activeGroup]);
  useEffect(() => subscribeUsersByUids(memberUids, setContacts), [memberUids]);

  useEffect(() => {
    if (!activeGroup) return;
    return subscribeToGroupMessages(activeGroup.id, setGroupMessages);
  }, [activeGroup]);

  const conversationId = useMemo(() => {
    if (!user || !activeContactUid) return '';
    return conversationIdFromUids(user.uid, activeContactUid);
  }, [activeContactUid, user]);

  useEffect(() => {
    if (!conversationId) return setDirectMessages([]);
    return subscribeToDirectMessages(conversationId, setDirectMessages);
  }, [conversationId]);

  const mode: 'group' | 'direct' = activeContactUid ? 'direct' : 'group';
  const messages = mode === 'group' ? groupMessages : directMessages;

  const send = async () => {
    if (!text.trim() || !user) return;
    if (mode === 'group' && activeGroup) {
      await sendGroupMessage(activeGroup.id, user.uid, user.displayName ?? user.email ?? 'User', text, quote);
    }
    if (mode === 'direct' && activeContactUid && conversationId) {
      await sendDirectMessage(conversationId, user.uid, activeContactUid, user.displayName ?? user.email ?? 'User', text, quote);
    }
    setText('');
    setQuote('');
  };

  return (
    <div className="chat-layout card bubble">
      <aside className="chat-sidebar">
        <h3>Gruppen</h3>
        {groups.map((g) => (
          <button key={g.id} className={`chat-item ${activeGroup?.id === g.id ? 'active' : ''}`} onClick={() => { setActiveGroupId(g.id); setActiveContactUid(''); }}>
            #{g.name}
          </button>
        ))}
        <h3>Kontakte</h3>
        {contacts.filter((c) => c.uid !== user?.uid).map((c) => (
          <button key={c.uid} className={`chat-item ${activeContactUid === c.uid ? 'active' : ''}`} onClick={() => setActiveContactUid(c.uid)}>
            {c.displayName}
          </button>
        ))}
      </aside>

      <section className="chat-main">
        <div className="chat-header">
          <strong>{mode === 'group' ? `#${activeGroup?.name ?? 'Gruppe'}` : contacts.find((c) => c.uid === activeContactUid)?.displayName}</strong>
        </div>
        <div className="chat-messages">
          {messages.map((m) => {
            const own = m.senderUid === user?.uid;
            const editable = own && canEditMessage(m.createdAt);
            return (
              <div key={m.id} className={`msg ${own ? 'own' : ''}`}>
                <div>
                  <strong>{m.senderName}</strong>
                  {m.quotedText && <p className="quoted">↪ {m.quotedText}</p>}
                  <p>{m.content}</p>
                  <div className="reactions">{Object.values(m.reactions ?? {}).join(' ')}</div>
                </div>
                <div className="msg-actions">
                  <button className="icon-btn" onClick={() => setQuote(m.content)}>↩</button>
                  <button className="icon-btn" onClick={() => { if (mode === 'group') void reactToGroupMessage(m.id, user?.uid ?? '', '👍'); else void reactToDirectMessage(m.id, user?.uid ?? '', '👍'); }}>👍</button>
                  {editable && (
                    <>
                      <button className="icon-btn" onClick={() => setMenuId(menuId === m.id ? '' : m.id)}>⋯</button>
                      {menuId === m.id && (
                        <div className="msg-menu">
                          <button onClick={() => { const next = window.prompt('Bearbeiten', m.content); if (next) { if (mode === 'group') void updateGroupMessage(m.id, next); else void updateDirectMessage(m.id, next); } setMenuId(''); }}>Bearbeiten</button>
                          <button onClick={() => { if (mode === 'group') void deleteGroupMessage(m.id); else void deleteDirectMessage(m.id); setMenuId(''); }}>Löschen</button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>
        {quote && <p className="quoted">Antwort auf: {quote}</p>}
        <div className="chat-input">
          <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Nachricht schreiben..." />
          <button className="btn" onClick={() => void send()}>Senden</button>
        </div>
      </section>
    </div>
  );
};
