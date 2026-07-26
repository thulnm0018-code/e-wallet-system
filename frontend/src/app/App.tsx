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
import { ForgotPassword } from './components/ForgotPassword';
import { ResetPassword } from './components/ResetPassword';
import { Profile } from './components/Profile';
import { AdminDashboard } from './components/AdminDashboard';
import { Notifications } from './components/Notifications';
import { AuthProvider, useAuth } from './context/AuthContext';

function AppContent() {
  const location = useLocation();
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register' || location.pathname === '/forgot-password' || location.pathname === '/reset-password';
  const hideNavbar = isAuthPage || location.pathname === '/admin';

  return (
    <div className="min-h-screen bg-stone-white">
      {!hideNavbar && <Navigation />}
      <Routes>
        {/* Đường dẫn gốc / sẽ tự động điều hướng dựa theo Role của tài khoản */}
        <Route path="/" element={<RootIndexRoute />} />
        
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/transactions" element={<ProtectedRoute><Transactions /></ProtectedRoute>} />
        <Route path="/send" element={<ProtectedRoute><Send /></ProtectedRoute>} />
        <Route path="/receive" element={<ProtectedRoute><Receive /></ProtectedRoute>} />
        
        <Route path="/login" element={<GuestRoute><Login /></GuestRoute>} />
        <Route path="/register" element={<GuestRoute><Register /></GuestRoute>} />
        <Route path="/forgot-password" element={<GuestRoute><ForgotPassword /></GuestRoute>} />
        <Route path="/reset-password" element={<GuestRoute><ResetPassword /></GuestRoute>} />
        
        <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
        <Route path="/notification" element={<Navigate to="/notifications" replace />} />
        <Route path="/notificaiton" element={<Navigate to="/notifications" replace />} />
        <Route path="/admin" element={<RequireAdmin><AdminDashboard /></RequireAdmin>} />
      </Routes>
    </div>
  );
}

// Bộ điều hướng gốc thông minh tại đường dẫn "/"
function RootIndexRoute() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading…</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return user.role === 'admin' 
    ? <Navigate to="/admin" replace /> 
    : <Navigate to="/dashboard" replace />;
}

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading…</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Nếu là ADMIN nhưng cố tình vào các trang của USER (/dashboard, /send...) -> Đá ngược về /admin
  if (user.role === 'admin') {
    return <Navigate to="/admin" replace />;
  }

  return <>{children}</>;
}

function GuestRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading…</div>;
  }

  // Nếu đã đăng nhập rồi mà cố quay lại trang Login/Register -> Đá về đúng trang theo Role
  if (user) {
    return user.role === 'admin' 
      ? <Navigate to="/admin" replace /> 
      : <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading…</div>;
  }

  if (!user || user.role !== 'admin') {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
}

import { Toaster } from 'sonner';

export default function App() {
  return (
    <AuthProvider>
      <WalletProvider>
        <BrowserRouter>
          <AppContent />
          <Toaster richColors position="top-right" />
        </BrowserRouter>
      </WalletProvider>
    </AuthProvider>
  );
}