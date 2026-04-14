import { type FormEvent, useEffect, useMemo, useState } from 'react';
import { createLink, subscribeToLinks } from '../firebase/api';
import { useAuth } from '../contexts/AuthContext';
import { useLanguage } from '../contexts/LanguageContext';
import type { LinkDoc } from '../types';

export const LinksPage = () => {
  const { profile } = useAuth();
  const { lang } = useLanguage();
  const isAdmin = profile?.role === 'admin';
  const [links, setLinks] = useState<LinkDoc[]>([]);
  const [title, setTitle] = useState('');
  const [url, setUrl] = useState('');
  const [category, setCategory] = useState('General');
  const [description, setDescription] = useState('');
  const [hovered, setHovered] = useState<LinkDoc | null>(null);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState('');

  const normalizeUrl = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) return '';
    return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
  };

  useEffect(() => subscribeToLinks(!isAdmin, setLinks), [isAdmin]);

  const grouped = useMemo(() => {
    return links.reduce<Record<string, LinkDoc[]>>((acc, cur) => {
      acc[cur.category] = [...(acc[cur.category] ?? []), cur];
      return acc;
    }, {});
  }, [links]);

  const add = async (event: FormEvent) => {
    event.preventDefault();
    if (!isAdmin) {
      setFeedback(lang === 'de' ? 'Nur Admins dürfen Links speichern.' : 'Only admins can save links.');
      return;
    }
    setSaving(true);
    setFeedback('');
    try {
      const normalizedUrl = normalizeUrl(url);
      await createLink({ title: title.trim(), url: normalizedUrl, category: category.trim(), description: description.trim(), sortOrder: 100, visible: true });
      setTitle('');
      setUrl('');
      setDescription('');
      setFeedback(lang === 'de' ? 'Link wurde gespeichert.' : 'Link saved.');
    } catch (error) {
      setFeedback(`${lang === 'de' ? 'Speichern fehlgeschlagen' : 'Save failed'}: ${(error as Error).message}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>{lang === 'de' ? 'Schnelllinks' : 'Quick links'}</h2>
        {feedback && <p className="hint">{feedback}</p>}
        {Object.keys(grouped).length === 0 && <p>{lang === 'de' ? 'Noch keine Links vorhanden. Als Admin den ersten Link anlegen.' : 'No links yet. Add the first from Admin.'}</p>}
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
            <p>{hovered.description || (lang === 'de' ? 'Keine Beschreibung vorhanden.' : 'No description available.')}</p>
            <small>{hovered.url}</small>
          </div>
        )}
      </section>

      {isAdmin && (
        <section className="card bubble">
          <h2>{lang === 'de' ? 'Link hinzufügen' : 'Add link'}</h2>
          <form className="stack" onSubmit={add}>
            <input placeholder="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            <input placeholder="https://..." value={url} onChange={(e) => setUrl(e.target.value)} required />
            <input placeholder="Category" value={category} onChange={(e) => setCategory(e.target.value)} required />
            <input placeholder={lang === 'de' ? 'Kurzbeschreibung (optional)' : 'Short description (optional)'} value={description} onChange={(e) => setDescription(e.target.value)} />
            <button className="btn" disabled={saving}>{saving ? (lang === 'de' ? 'Speichert...' : 'Saving...') : (lang === 'de' ? 'Speichern' : 'Save')}</button>
          </form>
        </section>
      )}
    </div>
  );
};
