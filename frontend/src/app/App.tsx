/// <reference types="react" />
import type { ReactNode } from 'react';
import { BrowserRouter, Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { WalletProvider } from './context/WalletContext';
import { Navigation } from './components/Navigation';
import { Dashboard } from './components/Dashboard';
import { Transactions } from './components/Transactions';
import { Send } from './components/Send';
import { Receive } from './components/Receive';
import { Login } from './components/Login';
import { Register } from './components/Register';
import { OTPVerification } from './components/OTPVerification';
import { ForgotPassword } from './components/ForgotPassword';
import { ResetPassword } from './components/ResetPassword';
import { Profile } from './components/Profile';
import { AdminDashboard } from './components/AdminDashboard';
import { AuthProvider, useAuth } from './context/AuthContext';

function AppContent() {
  const location = useLocation();
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register' || location.pathname === '/otp' || location.pathname === '/forgot-password' || location.pathname === '/reset-password';
  const hideNavbar = isAuthPage || location.pathname === '/admin';

  return (
    <div className="min-h-screen bg-stone-white">
      {!hideNavbar && <Navigation />}
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/send" element={<Send />} />
        <Route path="/receive" element={<Receive />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/otp" element={<OTPVerification />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/admin" element={<RequireAdmin><AdminDashboard /></RequireAdmin>} />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <WalletProvider>
        <BrowserRouter>
          <AppContent />
        </BrowserRouter>
      </WalletProvider>
    </AuthProvider>
  );
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (!user || user.role !== 'admin') {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

