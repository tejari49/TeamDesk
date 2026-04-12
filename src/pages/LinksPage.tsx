import { type FormEvent, useEffect, useMemo, useState } from 'react';
import { createLink, subscribeToLinks } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import type { LinkDoc } from '../types';

export const LinksPage = () => {
  const { profile } = useAuth();
  const isAdmin = profile?.role === 'admin';
  const [links, setLinks] = useState<LinkDoc[]>([]);
  const [title, setTitle] = useState('');
  const [url, setUrl] = useState('');
  const [category, setCategory] = useState('General');
  const [description, setDescription] = useState('');
  const [hovered, setHovered] = useState<LinkDoc | null>(null);

  useEffect(() => subscribeToLinks(!isAdmin, setLinks), [isAdmin]);

  const grouped = useMemo(() => {
    return links.reduce<Record<string, LinkDoc[]>>((acc, cur) => {
      acc[cur.category] = [...(acc[cur.category] ?? []), cur];
      return acc;
    }, {});
  }, [links]);

  const add = async (event: FormEvent) => {
    event.preventDefault();
    if (!isAdmin) return;
    await createLink({ title, url, category, description, sortOrder: 100, visible: true });
    setTitle('');
    setUrl('');
    setDescription('');
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>Quick links</h2>
        {Object.keys(grouped).length === 0 && <p>No links yet. Add the first from Admin.</p>}
        {Object.entries(grouped).map(([group, items]) => (
          <div key={group}>
            <h3>{group}</h3>
            <ul>
              {items.map((item) => (
                <li key={item.id} onMouseEnter={() => setHovered(item)} onMouseLeave={() => setHovered(null)}>
                  <a href={item.url} target="_blank" rel="noreferrer" title={item.description || ''}>
                    {item.title}
                  </a>
                  {item.description && <small className="hint"> — {item.description}</small>}
                </li>
              ))}
            </ul>
          </div>
        ))}
        {hovered && (
          <div className="link-preview card">
            <strong>{hovered.title}</strong>
            <p>{hovered.description || 'Keine Beschreibung vorhanden.'}</p>
            <small>{hovered.url}</small>
          </div>
        )}
      </section>

      {isAdmin && (
        <section className="card bubble">
          <h2>Add link</h2>
          <form className="stack" onSubmit={add}>
            <input placeholder="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            <input placeholder="https://..." type="url" value={url} onChange={(e) => setUrl(e.target.value)} required />
            <input placeholder="Category" value={category} onChange={(e) => setCategory(e.target.value)} required />
            <input placeholder="Kurzbeschreibung (optional)" value={description} onChange={(e) => setDescription(e.target.value)} />
            <button className="btn">Save</button>
          </form>
        </section>
      )}
    </div>
  );
};
