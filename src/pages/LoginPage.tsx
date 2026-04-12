import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';

export const LoginPage = () => {
  const { signInWithGoogle, signInDevAnonymous } = useAuth();
  const [error, setError] = useState('');

  const handleLogin = async () => {
    try {
      await signInWithGoogle();
    } catch {
      setError('Google sign-in failed. Use development sign-in if needed.');
    }
  };

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <h1>TeamDesk</h1>
        <p>Your internal dashboard for today.</p>
        <button className="btn" onClick={() => void handleLogin()}>
          Sign in with Google
        </button>
        <button className="btn btn-secondary" onClick={() => void signInDevAnonymous()}>
          Dev anonymous sign-in
        </button>
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
};
