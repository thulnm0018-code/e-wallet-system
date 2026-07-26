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
  dateOfBirth?: string | null;
  avatarUrl?: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (user: User) => void;
  logout: () => void;
  updateUser: (updates: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const unwrapApiData = <T,>(payload: any): T | null => {
  if (!payload) return null;
  return payload?.data?.data ?? payload?.data ?? payload ?? null;
};

const normalizeRole = (role?: string): Role => {
  const normalized = role?.toUpperCase();
  if (normalized === 'ADMIN' || normalized === 'ROLE_ADMIN') {
    return 'admin';
  }
  return 'user';
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const login = (u: User) => setUser(u);

  const syncUserFromSession = async () => {
    try {
      const response: any = await api.get('/auth/me');
      const userData = unwrapApiData<any>(response);

      if (userData) {
        setUser({
          name: userData.name,
          phone: userData.phone,
          email: userData.email,
          role: normalizeRole(userData.role),
          walletId: userData.walletId?.toString(),
          memberSince: userData.createdAt || undefined,
          accountType: 'STANDARD',
          walletStatus: 'ACTIVE',
          address: userData.address || undefined,
          dateOfBirth: userData.dateOfBirth || null,
          avatarUrl: userData.avatarUrl || undefined,
        });
        return true;
      }
    } catch (error) {
      setUser(null);
    }

    return false;
  };

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
      await syncUserFromSession();
      setLoading(false);
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