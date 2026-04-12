import { type FormEvent, useEffect, useState } from 'react';
import { createHandover, subscribeToHandovers, updateHandover } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import { useLanguage } from '../contexts/LanguageContext';
import type { HandoverDoc, HandoverPriority, HandoverState } from '../types';
import { formatRelativeTime } from '../utils/date';

export const HandoversPage = () => {
  const { user } = useAuth();
  const { lang } = useLanguage();
  const [items, setItems] = useState<HandoverDoc[]>([]);
  const [state, setState] = useState<HandoverState | 'all'>('open');
  const [priority, setPriority] = useState<HandoverPriority | 'all'>('all');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [dueDate, setDueDate] = useState('');

  useEffect(() => subscribeToHandovers({ state: state === 'all' ? undefined : state, priority }, setItems), [state, priority]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!user || !title.trim()) return;
    await createHandover({
      title,
      description,
      priority: 'medium',
      status: 'open',
      assignedTo: '',
      assignedToUid: '',
      createdBy: user.displayName ?? user.email ?? 'Unknown',
      createdByUid: user.uid,
      dueDate: dueDate || undefined
    });
    setTitle('');
    setDescription('');
    setDueDate('');
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>{lang === 'de' ? 'Übergabe erstellen' : 'Create handover'}</h2>
        <form className="stack" onSubmit={submit}>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder={lang === 'de' ? 'Titel' : 'Title'} required />
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder={lang === 'de' ? 'Beschreibung' : 'Description'} />
          <label>
            {lang === 'de' ? 'Fälligkeitsdatum' : 'Due date'}
            <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </label>
          <button className="btn">{lang === 'de' ? 'Erstellen' : 'Create'}</button>
        </form>
      </section>

      <section className="card bubble">
        <h2>{lang === 'de' ? 'Übergaben' : 'Handovers'}</h2>
        <div className="filters">
          <select value={state} onChange={(e) => setState(e.target.value as HandoverState | 'all')}>
            <option value="all">{lang === 'de' ? 'Alle' : 'All'}</option>
            <option value="open">Open</option>
            <option value="done">{lang === 'de' ? 'Erledigt' : 'Done'}</option>
          </select>
          <select value={priority} onChange={(e) => setPriority(e.target.value as HandoverPriority | 'all')}>
            <option value="all">{lang === 'de' ? 'Alle Prioritäten' : 'All priorities'}</option>
            <option value="low">Low</option>
            <option value="medium">Medium</option>
            <option value="high">High</option>
          </select>
        </div>

        <ul className="list">
          {items.map((item) => (
            <li key={item.id}>
              <div>
                <strong>{item.title}</strong> <span className={`pill ${item.priority}`}>{item.priority}</span>
                <p>{item.description}</p>
                <small>{item.createdBy} · {formatRelativeTime(item.createdAt)}</small>
              </div>
              {item.status === 'open' && (
                <button className="btn btn-secondary" onClick={() => void updateHandover(item.id, { status: 'done' })}>
                  {lang === 'de' ? 'Als erledigt markieren' : 'Mark done'}
                </button>
              )}
            </li>
          ))}
        </ul>
        {items.length === 0 && <p>{lang === 'de' ? 'Keine Übergaben gefunden.' : 'No handovers found.'}</p>}
      </section>
    </div>
  );
};
