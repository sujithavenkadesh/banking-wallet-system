import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';

export default function Statement() {
  const { accountNumber } = useParams();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(`/transactions/statement/${accountNumber}`)
      .then((res) => setTransactions(res.data))
      .catch(() => setError('Failed to load statement'))
      .finally(() => setLoading(false));
  }, [accountNumber]);

  const statusClass = (status) => {
    if (status === 'SUCCESS') return 'badge-success';
    if (status === 'FLAGGED') return 'badge-warning';
    if (status === 'FAILED') return 'badge-danger';
    return 'badge-pending';
  };

  if (loading) return <div className="page-loading">Loading...</div>;

  return (
    <div className="page-container">
      <Link to="/dashboard" className="back-link">← Back to Dashboard</Link>
      <h1>Statement: {accountNumber}</h1>

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
        {transactions.map((t) => (
          <div key={t.id} className="statement-row">
            <span>{new Date(t.timestamp).toLocaleString('en-IN')}</span>
            <span>{t.fromAccountOwner ? `${t.fromAccountOwner} (${t.fromAccountNumber})` : '—'}</span>
            <span>{t.toAccountOwner ? `${t.toAccountOwner} (${t.toAccountNumber})` : '—'}</span>
            <span className={t.fromAccountNumber === accountNumber ? 'amount-debit' : 'amount-credit'}>
              {t.fromAccountNumber === accountNumber ? '-' : '+'}₹{t.amount.toLocaleString('en-IN')}
            </span>
            <span className={`badge ${statusClass(t.status)}`}>{t.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
}