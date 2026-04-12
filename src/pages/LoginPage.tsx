import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';

export const LoginPage = () => {
  const { signInWithGoogle, signInDevAnonymous } = useAuth();
  const [error, setError] = useState('');
  const isLocalDev = window.location.hostname === 'localhost';

  const handleLogin = async () => {
    try {
      await signInWithGoogle();
    } catch {
      setError('Google Sign-in fehlgeschlagen.');
    }
  };

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <h1>TeamDesk</h1>
        <p>Internes Tages-Dashboard.</p>
        <button className="btn" onClick={() => void handleLogin()}>
          Mit Google anmelden
        </button>
        {isLocalDev && (
          <button className="btn btn-secondary" onClick={() => void signInDevAnonymous()}>
            Dev Anonymous Sign-in
          </button>
        )}
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
};
