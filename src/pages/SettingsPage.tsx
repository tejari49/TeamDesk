import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const SettingsPage = () => {
  const { profile } = useAuth();
  const isAdmin = profile?.role === 'admin';

  return (
    <div className="grid-2">
      <section className="card bubble">
        <h2>Einstellungen</h2>
        <p>Dein Verknüpfungscode: <strong>{profile?.userCode}</strong></p>
        <p>Nutze diesen Code, damit dich Gruppenadmins hinzufügen können.</p>
      </section>

      <section className="card bubble">
        <h2>Wo kann ich Inhalte pflegen?</h2>
        <ul>
          <li>
            <strong>Aktuelle Ankündigungen:</strong> im <Link to="/admin">Admin-Bereich</Link> unter „Master Admin Panel“.
          </li>
          <li>
            <strong>Links:</strong> auf der <Link to="/links">Links-Seite</Link> (als Admin sichtbar inkl. Eingabeformular).
          </li>
        </ul>
      </section>

      <section className="card bubble full">
        <h2>Release Verlauf</h2>
        <p>
          Öffne den klickbaren Verlauf unten im Layout oder direkt hier: <Link to="/releases">Release Verlauf anzeigen</Link>.
        </p>
        {isAdmin && <p className="hint">Hinweis: Logs werden bei relevanten Änderungen automatisch im Hintergrund erzeugt.</p>}
      </section>
    </div>
  );
};
