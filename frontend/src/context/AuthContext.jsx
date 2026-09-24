import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState({
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('role'),
  });

  const login = (token, username, role) => {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
    localStorage.setItem('role', role);
    setAuth({ token, username, role });
  };

  const logout = () => {
    localStorage.clear();
    setAuth({ token: null, username: null, role: null });
  };

  return (
    <AuthContext.Provider value={{ ...auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);