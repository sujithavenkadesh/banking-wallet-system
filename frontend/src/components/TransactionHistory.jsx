import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

export default function TransactionHistory() {
  const { username } = useAuth();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/transactions/history')
      .then((res) => setTransactions(res.data))
      .catch(() => setError('Failed to load transaction history'))
      .finally(() => setLoading(false));
  }, []);

  const statusClass = (status) => {
    if (status === 'SUCCESS') return 'badge-success';
    if (status === 'FLAGGED') return 'badge-warning';
    if (status === 'FAILED') return 'badge-danger';
    return 'badge-pending';
  };

  if (loading) return <div className="page-loading">Loading...</div>;

  return (
    <div className="page-container">
      <h1>Transaction History</h1>
      <p className="total-balance" style={{ marginBottom: '20px' }}>All money sent and received across your accounts</p>

      {error && <div className="error-banner">{error}</div>}

      <div className="statement-table">
        <div className="statement-row statement-header">
          <span>Date</span>
          <span>From</span>
          <span>To</span>
          <span>Amount</span>
          <span>Status</span>
        </div>
        {transactions.length === 0 && <p className="empty-state">No transactions yet.</p>}
        {transactions.map((t) => {
          const isSender = t.fromAccountOwner === username;
          return (
            <div key={t.id} className="statement-row">
              <span>{new Date(t.timestamp).toLocaleString('en-IN')}</span>
              <span>
                {t.fromAccountOwner ? `${t.fromAccountOwner} (${t.fromAccountNumber})` : '—'}
              </span>
              <span>
                {t.toAccountOwner ? `${t.toAccountOwner} (${t.toAccountNumber})` : '—'}
              </span>
              <span className={isSender ? 'amount-debit' : 'amount-credit'}>
                {isSender ? '-' : '+'}₹{t.amount.toLocaleString('en-IN')}
              </span>
              <span className={`badge ${statusClass(t.status)}`}>{t.status}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}