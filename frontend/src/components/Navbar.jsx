import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { username, role, token, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!token) return null;

  return (
    <nav className="navbar">
      <div className="navbar-brand">💰 WalletBank</div>
      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/transfer">Transfer</Link>
        <span className="navbar-user">{username} ({role})</span>
        <button onClick={handleLogout} className="btn-logout">Logout</button>
      </div>
    </nav>
  );
}