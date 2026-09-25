import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function TransferForm() {
  const [accounts, setAccounts] = useState([]);
  const [step, setStep] = useState('details'); // 'details' | 'pin'
  const [form, setForm] = useState({ fromAccountNumber: '', toAccountNumber: '', amount: '', remarks: '' });
  const [pin, setPin] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/accounts/my').then((res) => setAccounts(res.data)).catch(() => {});
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleContinue = (e) => {
    e.preventDefault();
    setError('');
    if (!form.fromAccountNumber || !form.toAccountNumber || !form.amount) {
      setError('Please fill all required fields');
      return;
    }
    if (form.fromAccountNumber === form.toAccountNumber) {
      setError('Cannot transfer to the same account');
      return;
    }
    setStep('pin');
  };

  const handleBack = () => {
    setStep('details');
    setPin('');
    setError('');
  };

  const handleConfirm = async (e) => {
    e.preventDefault();
    setError('');
    setResult(null);
    setLoading(true);
    try {
      const res = await api.post('/transactions/transfer', {
        ...form,
        amount: parseFloat(form.amount),
        pin,
      });
      setResult(res.data);
      setForm({ fromAccountNumber: '', toAccountNumber: '', amount: '', remarks: '' });
      setPin('');
      setStep('details');
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed');
    } finally {
      setLoading(false);
    }
  };

  const selectedAccount = accounts.find((a) => a.accountNumber === form.fromAccountNumber);

  return (
    <div className="page-container">
      <h1>Transfer Funds</h1>

      {result && (
        <div className={`result-banner ${result.status === 'FLAGGED' ? 'warning' : 'success'}`}>
          {result.status === 'FLAGGED'
            ? '⚠️ Transaction flagged for review due to large amount. Funds not yet moved.'
            : '✅ Transfer completed successfully!'}
        </div>
      )}

      {step === 'details' && (
        <form className="transfer-card" onSubmit={handleContinue}>
          {error && <div className="error-banner">{error}</div>}

          <label>From Account</label>
          <select name="fromAccountNumber" value={form.fromAccountNumber} onChange={handleChange} required>
            <option value="">Select account</option>
            {accounts.map((acc) => (
              <option key={acc.id} value={acc.accountNumber}>
                {acc.accountNumber} (₹{acc.balance.toLocaleString('en-IN')})
              </option>
            ))}
          </select>

          <label>To Account Number</label>
          <input
            name="toAccountNumber"
            value={form.toAccountNumber}
            onChange={handleChange}
            placeholder="Recipient account number"
            required
          />

          <label>Amount (₹)</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            name="amount"
            value={form.amount}
            onChange={handleChange}
            required
          />

          <label>Remarks (optional)</label>
          <input name="remarks" value={form.remarks} onChange={handleChange} placeholder="e.g. rent payment" />

          <button type="submit" className="btn-primary">
            Send Money
          </button>
        </form>
      )}

      {step === 'pin' && (
        <form className="transfer-card" onSubmit={handleConfirm}>
          {error && <div className="error-banner">{error}</div>}

          <div className="transfer-summary">
            <div className="summary-row">
              <span>From</span>
              <strong>{form.fromAccountNumber}{selectedAccount ? ` (₹${selectedAccount.balance.toLocaleString('en-IN')})` : ''}</strong>
            </div>
            <div className="summary-row">
              <span>To</span>
              <strong>{form.toAccountNumber}</strong>
            </div>
            <div className="summary-row">
              <span>Amount</span>
              <strong>₹{parseFloat(form.amount || 0).toLocaleString('en-IN')}</strong>
            </div>
            {form.remarks && (
              <div className="summary-row">
                <span>Remarks</span>
                <strong>{form.remarks}</strong>
              </div>
            )}
          </div>

          <label>Enter Transaction PIN</label>
          <input
            type="password"
            inputMode="numeric"
            maxLength={6}
            value={pin}
            onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
            placeholder="4-6 digit PIN"
            autoFocus
            required
          />

          <div className="transfer-actions">
            <button type="button" className="btn-secondary" onClick={handleBack}>
              ← Back
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Processing...' : 'Confirm & Send Money'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}