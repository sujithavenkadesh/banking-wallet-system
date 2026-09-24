import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function TransferForm() {
  const [accounts, setAccounts] = useState([]);
  const [form, setForm] = useState({ fromAccountNumber: '', toAccountNumber: '', amount: '', remarks: '' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/accounts/my').then((res) => setAccounts(res.data)).catch(() => {});
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setResult(null);
    setLoading(true);
    try {
      const res = await api.post('/transactions/transfer', {
        ...form,
        amount: parseFloat(form.amount),
      });
      setResult(res.data);
      setForm({ fromAccountNumber: '', toAccountNumber: '', amount: '', remarks: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>Transfer Funds</h1>

      <form className="transfer-card" onSubmit={handleSubmit}>
        {error && <div className="error-banner">{error}</div>}
        {result && (
          <div className={`result-banner ${result.status === 'FLAGGED' ? 'warning' : 'success'}`}>
            {result.status === 'FLAGGED'
              ? '⚠️ Transaction flagged for review due to large amount. Funds not yet moved.'
              : '✅ Transfer completed successfully!'}
          </div>
        )}

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

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Processing...' : 'Send Money'}
        </button>
      </form>
    </div>
  );
}