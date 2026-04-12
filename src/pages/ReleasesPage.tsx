import { useEffect, useState } from 'react';
import { subscribeReleaseLogs } from '../firebase/api';
import type { ReleaseLogDoc } from '../types';
import { formatRelativeTime } from '../utils/date';

export const ReleasesPage = () => {
  const [logs, setLogs] = useState<ReleaseLogDoc[]>([]);

  useEffect(() => subscribeReleaseLogs(setLogs), []);

  return (
    <section className="card bubble">
      <h2>Release Verlauf</h2>
      <ul className="list">
        {logs.map((log) => (
          <li key={log.id}>
            <div>
              <strong>v{log.version} · {log.category}</strong>
              <p>{log.message}</p>
              <small>von {log.actorName}</small>
            </div>
            <small>{formatRelativeTime(log.createdAt)}</small>
          </li>
        ))}
      </ul>
      {logs.length === 0 && <p>Noch keine Einträge vorhanden.</p>}
    </section>
  );
};
