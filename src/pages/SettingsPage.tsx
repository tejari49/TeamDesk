import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { updateUserLanguage } from '../firebase/api';

export const SettingsPage = () => {
  const { profile, user } = useAuth();
  const isAdmin = profile?.role === 'admin';
  const [language, setLanguage] = useState<'de' | 'en'>((profile?.language ?? 'de') as 'de' | 'en');

  const saveLanguage = async () => {
    if (!user) return;
    await updateUserLanguage(user.uid, language);
  };

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>Einstellungen</h2>
        <p>Dein Verknüpfungscode: <strong>{profile?.userCode}</strong></p>
        <p>Nutze diesen Code, damit dich Gruppenadmins hinzufügen können.</p>
      </section>

      <section className="card bubble">
        <h2>Sprache</h2>
        <select value={language} onChange={(e) => setLanguage(e.target.value as 'de' | 'en')}>
          <option value="de">Deutsch</option>
          <option value="en">English</option>
        </select>
        <button className="btn" onClick={() => void saveLanguage()}>Speichern</button>
        <p className="hint">Wird serverseitig im Nutzerprofil gespeichert.</p>
      </section>

      <section className="card bubble">
        <h2>Wo kann ich Inhalte pflegen?</h2>
        <ul>
          <li><strong>Aktuelle Ankündigungen:</strong> im <Link to="/admin">Admin-Bereich</Link>.</li>
          <li><strong>Links:</strong> auf der <Link to="/links">Links-Seite</Link> (als Admin sichtbar inkl. Formular).</li>
        </ul>
      </section>

      <section className="card bubble full">
        <h2>Release Verlauf</h2>
        <p>Unten im Layout anklicken oder direkt: <Link to="/releases">Release Verlauf anzeigen</Link>.</p>
        {isAdmin && <p className="hint">Logs werden bei relevanten Änderungen automatisch erzeugt.</p>}
      </section>
    </div>
  );
};
