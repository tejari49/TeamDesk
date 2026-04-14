import { KeyboardEvent, useEffect, useMemo, useState } from 'react';
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

const localKey = (kind: 'group' | 'direct', id: string) => `teamdesk:${kind}:${id}`;

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
    setGroupMessages([]);
    const key = localKey('group', activeGroup.id);
    const cached = localStorage.getItem(key);
    if (cached) setGroupMessages(JSON.parse(cached));
    return subscribeToGroupMessages(activeGroup.id, (items) => {
      setGroupMessages(items);
      localStorage.setItem(key, JSON.stringify(items));
    });
  }, [activeGroup]);

  const conversationId = useMemo(() => {
    if (!user || !activeContactUid) return '';
    return conversationIdFromUids(user.uid, activeContactUid);
  }, [activeContactUid, user]);

  useEffect(() => {
    setDirectMessages([]);
    if (!conversationId) return;
    const key = localKey('direct', conversationId);
    const cached = localStorage.getItem(key);
    if (cached) setDirectMessages(JSON.parse(cached));
    return subscribeToDirectMessages(conversationId, (items) => {
      setDirectMessages(items);
      localStorage.setItem(key, JSON.stringify(items));
    });
  }, [conversationId]);

  const mode: 'group' | 'direct' = activeContactUid ? 'direct' : 'group';
  const messages = mode === 'group' ? groupMessages : directMessages;

  const send = async () => {
    if (!text.trim() || !user) return;
    try {
      if (mode === 'group' && activeGroup) {
        const optimistic = {
          id: `tmp-${Date.now()}`,
          groupId: activeGroup.id,
          senderUid: user.uid,
          senderName: user.displayName ?? user.email ?? 'User',
          content: text,
          quotedText: quote,
          reactions: {},
          createdAt: { toDate: () => new Date() } as never,
          updatedAt: { toDate: () => new Date() } as never
        };
        setGroupMessages((prev) => [...prev, optimistic as never]);
        await sendGroupMessage(activeGroup.id, user.uid, user.displayName ?? user.email ?? 'User', text, quote);
      }
      if (mode === 'direct' && activeContactUid && conversationId) {
        const optimistic = {
          id: `tmp-${Date.now()}`,
          conversationId,
          senderUid: user.uid,
          receiverUid: activeContactUid,
          senderName: user.displayName ?? user.email ?? 'User',
          content: text,
          quotedText: quote,
          reactions: {},
          createdAt: { toDate: () => new Date() } as never,
          updatedAt: { toDate: () => new Date() } as never
        };
        setDirectMessages((prev) => [...prev, optimistic as never]);
        await sendDirectMessage(conversationId, user.uid, activeContactUid, user.displayName ?? user.email ?? 'User', text, quote);
      }
      setText('');
      setQuote('');
    } catch (error) {
      alert(`Speichern fehlgeschlagen: ${(error as Error).message}`);
    }
  };

  const onEnterSend = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      void send();
    }
  };

  return (
    <div className="chat-layout card bubble">
      <aside className="chat-sidebar">
        <h3>Gruppen</h3>
        {groups.map((g) => (
          <button key={g.id} className={`chat-item ${activeGroup?.id === g.id ? 'active' : ''}`} onClick={() => { setActiveGroupId(g.id); setActiveContactUid(''); setText(''); setQuote(''); setMenuId(''); }}>
            #{g.name}
          </button>
        ))}
        <h3>Kontakte</h3>
        {contacts.filter((c) => c.uid !== user?.uid).map((c) => (
          <button key={c.uid} className={`chat-item ${activeContactUid === c.uid ? 'active' : ''}`} onClick={() => { setActiveContactUid(c.uid); setText(''); setQuote(''); setMenuId(''); }}>
            {c.displayName}
          </button>
        ))}
      </aside>

      <section className="chat-main">
        <div className="chat-header">
          <strong>{mode === 'group' ? `#${activeGroup?.name ?? 'Gruppe'}` : contacts.find((c) => c.uid === activeContactUid)?.displayName}</strong>
        </div>
        <div className="chat-messages">
          {messages.length === 0 && <p className="hint">Noch keine Nachrichten in diesem Chat.</p>}
          {messages.map((m) => {
            const own = m.senderUid === user?.uid;
            const createdAtSafe = (m as { createdAt?: { toDate?: () => Date } }).createdAt;
            const editable = own && typeof createdAtSafe?.toDate === 'function' ? canEditMessage(createdAtSafe as never) : false;
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
          <input value={text} onChange={(e) => setText(e.target.value)} onKeyDown={onEnterSend} placeholder="Nachricht schreiben..." />
          <button className="btn" onClick={() => void send()}>Senden</button>
        </div>
      </section>
    </div>
  );
};
