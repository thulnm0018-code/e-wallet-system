import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import api from '../../api';

type Role = 'admin' | 'user' | null;
export type WalletStatus = 'ACTIVE' | 'LOCKED' | 'PENDING';

interface User {
  name: string;
  phone: string;
  role: Role;
  email?: string;
  walletId?: string;
  accountType?: string;
  memberSince?: string;
  walletStatus?: WalletStatus;
  address?: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (user: User) => void;
  logout: () => void;
  updateUser: (updates: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const login = (u: User) => setUser(u);

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
    }
  };

  const updateUser = (updates: Partial<User>) => {
    setUser((prev) => (prev ? { ...prev, ...updates } : prev));
  };

  useEffect(() => {
    async function restoreSession() {
      try {
        const response: any = await api.get('/auth/me');
        const userData = response.data;

        if (userData) {
          setUser({
            name: userData.name,
            phone: userData.phone,
            email: userData.email,
            role: userData.role === 'ADMIN' ? 'admin' : 'user',
            walletId: userData.id?.toString(),
            memberSince: userData.createdAt || undefined,
            accountType: 'STANDARD',
            walletStatus: 'ACTIVE',
          });
        } else {
          setUser(null);
        }
      } catch (error) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    }

    restoreSession();
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}