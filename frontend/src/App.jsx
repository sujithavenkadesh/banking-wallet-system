import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import TransferForm from './components/TransferForm';
import Statement from './components/Statement';
import './index.css';
import TransactionHistory from './components/TransactionHistory';

function ProtectedRoute({ children }) {
  const { token } = useAuth();
  return token ? children : <Navigate to="/login" replace />;
}

function AppRoutes() {
  const { token } = useAuth();

  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/login" element={token ? <Navigate to="/dashboard" /> : <Login />} />
        <Route path="/register" element={token ? <Navigate to="/dashboard" /> : <Register />} />
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/transfer" element={<ProtectedRoute><TransferForm /></ProtectedRoute>} />
        <Route path="/statement/:accountNumber" element={<ProtectedRoute><Statement /></ProtectedRoute>} />
        <Route path="/" element={<Navigate to={token ? '/dashboard' : '/login'} />} />
        <Route path="/history" element={<ProtectedRoute><TransactionHistory /></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}