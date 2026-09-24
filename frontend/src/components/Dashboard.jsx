import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [initialBalance, setInitialBalance] = useState('');

  const fetchAccounts = async () => {
    try {
      const res = await api.get('/accounts/my');
      setAccounts(res.data);
    } catch (err) {
      setError('Failed to load accounts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    try {
      await api.post('/accounts', { initialBalance: initialBalance || 0 });
      setInitialBalance('');
      setShowCreate(false);
      fetchAccounts();
    } catch (err) {
      setError('Failed to create account');
    }
  };

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

  if (loading) return <div className="page-loading">Loading...</div>;

  return (
    <div className="page-container">
      <div className="dashboard-header">
        <div>
          <h1>Your Accounts</h1>
          <p className="total-balance">Total Balance: ₹{totalBalance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</p>
        </div>
        <button className="btn-primary" onClick={() => setShowCreate(!showCreate)}>
          + New Account
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {showCreate && (
        <form className="create-account-card" onSubmit={handleCreateAccount}>
          <label>Initial Balance (₹)</label>
          <input
            type="number"
            step="0.01"
            min="0"
            value={initialBalance}
            onChange={(e) => setInitialBalance(e.target.value)}
            placeholder="0.00"
          />
          <button type="submit" className="btn-primary">Create</button>
        </form>
      )}

      <div className="accounts-grid">
        {accounts.length === 0 && <p className="empty-state">No accounts yet. Create one to get started.</p>}
        {accounts.map((acc) => (
          <div key={acc.id} className="account-card">
            <div className="account-card-header">
              <span className="account-number">{acc.accountNumber}</span>
            </div>
            <div className="account-balance">₹{acc.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
            <div className="account-card-footer">
              <Link to={`/statement/${acc.accountNumber}`} className="link-btn">View Statement</Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}