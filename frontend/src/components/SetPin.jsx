import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function SetPin() {
  const [pin, setPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (pin !== confirmPin) {
      setError('PINs do not match');
      return;
    }
    if (!/^\d{4,6}$/.test(pin)) {
      setError('PIN must be 4 to 6 digits');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/set-pin', { pin });
      setSuccess(true);
      setTimeout(() => navigate('/dashboard'), 1200);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to set PIN');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Set Transaction PIN</h2>
        <p className="auth-subtitle">This PIN will be required every time you send money</p>

        {error && <div className="error-banner">{error}</div>}
        {success && <div className="result-banner success">PIN set successfully! Redirecting...</div>}

        <label>New PIN (4-6 digits)</label>
        <input
          type="password"
          inputMode="numeric"
          maxLength={6}
          value={pin}
          onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
          required
        />

        <label>Confirm PIN</label>
        <input
          type="password"
          inputMode="numeric"
          maxLength={6}
          value={confirmPin}
          onChange={(e) => setConfirmPin(e.target.value.replace(/\D/g, ''))}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Saving...' : 'Set PIN'}
        </button>
      </form>
    </div>
  );
}