import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from './Button';
import { Input } from './Input';
import { useAuth } from '../context/AuthContext';

const PHONE_LOCKED_VALUE = '0000000000';
const ADMIN_PHONE = '9999999999';
const ADMIN_PASSWORD = 'admin123';

export function Login() {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [phoneError, setPhoneError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [formMessage, setFormMessage] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (loading) return;

    setPhoneError('');
    setPasswordError('');
    setFormMessage('');
    setLoading(true);

    const input = phone.trim();
    const isEmailInput = input.includes('@');

    if (isEmailInput) {
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input)) {
        setFormMessage('Invalid email format');
        setPhoneError(' ');
        setLoading(false);
        return;
      }
    } else {
      if (!/^[+]?\d[\d\s-]{7,}$/i.test(input)) {
        setFormMessage('Phone number not found');
        setPhoneError(' ');
        setLoading(false);
        return;
      }
    }

    const isAdmin = (isEmailInput && input.toLowerCase() === 'admin@wallet.com' && password === ADMIN_PASSWORD) ||
      (!isEmailInput && input === ADMIN_PHONE && password === ADMIN_PASSWORD);

    if (isAdmin) {
      login({
        name: 'ADMIN',
        phone: ADMIN_PHONE,
        email: 'admin@wallet.com',
        role: 'admin',
        walletId: 'WL-ADMIN-ROOT',
        memberSince: 'JAN 2025',
        walletStatus: 'ACTIVE'
      });
      setFormMessage('');
      setPhone('');
      setPassword('');
      navigate('/admin');
      setLoading(false);
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        identifier: input,
        password: password,
      });

      const authData = response.data.data;

      localStorage.setItem('accessToken', authData.accessToken);
      localStorage.setItem('refreshToken', authData.refreshToken);

      login({
        name: authData.user.name,
        phone: authData.user.phone,
        email: authData.user.email,
        role: 'user',
        walletId: authData.user.id.toString(),
        accountType: 'STANDARD',
        walletStatus: 'ACTIVE',
        memberSince: authData.user.createdAt || 'JUN 2026'
      });

      setFormMessage('');
      setPhone('');
      setPassword('');
      navigate('/dashboard');
    } catch (error: any) {
      if (error.response && error.response.data) {
        const serverResponse = error.response.data;
        if (serverResponse.data && typeof serverResponse.data === 'object') {
          const errorsMap = serverResponse.data;
          if (errorsMap.identifier) setPhoneError(errorsMap.identifier);
          if (errorsMap.password) setPasswordError(errorsMap.password);
          setFormMessage('Please correct the highlighted errors.');
        } else if (serverResponse.message) {
          setFormMessage(serverResponse.message);
        } else {
          setFormMessage('Login failed. Please try again.');
        }
      } else {
        setFormMessage('Cannot connect to Java Server. Please check your network.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black">
      <div className="relative flex justify-center px-6 py-20">
        <div
          className={`w-full max-w-md border border-grid-line bg-stone-white/96 p-8 shadow-[0_24px_80px_rgba(0,0,0,0.08)] transition-opacity duration-200 ${loading ? 'opacity-80' : 'opacity-100'
            }`}
        >
          <div className="mb-10 space-y-2">
            <div className="uppercase tracking-[0.35em] text-[11px] text-medium-concrete">Secure access</div>
            <h1 className="text-[3.4rem] leading-[0.9] tracking-[0.15em] font-black">LOGIN</h1>
          </div>

          <form onSubmit={handleSubmit} className="grid gap-6">
            <Input
              label="Phone Number or Email"
              type="text"
              placeholder="hello@wallet.com or +84 123 456 789"
              value={phone}
              onChange={(event) => setPhone(event.target.value)}
              disabled={loading}
              error={phoneError}
            />
            <Input
              label="Password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              disabled={loading}
              error={passwordError}
            />

            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => navigate('/forgot-password')}
                disabled={loading}
                className="text-[12px] uppercase tracking-[0.35em] text-medium-concrete transition-colors duration-150 hover:text-charcoal-black"
              >
                Forgot password
              </button>
            </div>

            <Button
              type="submit"
              variant="primary"
              className={`h-14 w-full rounded-none overflow-hidden ${loading ? 'pointer-events-none px-0' : ''}`}
              disabled={loading}
            >
              {loading ? (
                <span className="block h-full w-full bg-charcoal-black animate-pulse" aria-hidden="true" />
              ) : (
                <span className="uppercase tracking-[0.35em]">Login</span>
              )}
            </Button>

            {formMessage ? (
              <div className="pt-2 text-[13px] uppercase tracking-[0.3em] text-[#6B6B5A]">
                {formMessage}
              </div>
            ) : null}
          </form>

          <div className="mt-4 text-center">
            <button
              type="button"
              onClick={() => navigate('/register')}
              className="text-[12px] uppercase tracking-[0.35em] text-charcoal-black transition-colors duration-150 hover:text-medium-concrete"
            >
              Create account
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}
