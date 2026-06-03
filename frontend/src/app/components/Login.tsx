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

    await new Promise((resolve) => setTimeout(resolve, 1100));

    if (phone.trim() === PHONE_LOCKED_VALUE) {
      setFormMessage('Wallet is locked');
      setPhoneError(' ');
      setPasswordError(' ');
    } else if (!/^[+]?\d[\d\s-]{7,}$/i.test(phone.trim())) {
      setFormMessage('Phone number not found');
      setPhoneError(' ');
    } else if (password !== 'wallet123') {
      setFormMessage('Incorrect password');
      setPasswordError(' ');
    } else {
      // Determine role: admin vs normal user
      const isAdmin = phone.trim() === ADMIN_PHONE && password === ADMIN_PASSWORD;
      const role = isAdmin ? 'admin' : 'user';
      const name = isAdmin ? 'ADMIN' : 'ANDO TADAO';

      login({ name, phone: phone.trim(), role });

      setFormMessage('');
      setPhone('');
      setPassword('');

      // Redirect based on role
      if (isAdmin) {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    }

    setLoading(false);
  };

  return (
    <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.18),transparent_18%),radial-gradient(circle_at_bottom_right,rgba(0,0,0,0.08),transparent_18%),linear-gradient(125deg,#ebe8e1,#d9d6cf)]" />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.12),transparent_36%,rgba(0,0,0,0.05))]" />
      <div className="relative flex min-h-screen items-center justify-center px-6 py-20">
        <div
          className={`w-full max-w-md border border-grid-line bg-stone-white/96 p-8 shadow-[0_24px_80px_rgba(0,0,0,0.08)] transition-opacity duration-200 ${
            loading ? 'opacity-80' : 'opacity-100'
          }`}
        >
          <div className="mb-10 space-y-2">
            <div className="uppercase tracking-[0.35em] text-[11px] text-medium-concrete">Secure access</div>
            <h1 className="text-[3.4rem] leading-[0.9] tracking-[0.15em] font-black">LOGIN</h1>
          </div>

          <form onSubmit={handleSubmit} className="grid gap-6">
            <Input
              label="Phone Number"
              type="tel"
              placeholder="+84 123 456 789"
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

          <div className="relative py-6 mt-2">
            <div className="absolute inset-x-0 top-1/2 h-px bg-grid-line" />
            <div className="relative flex justify-center">
              <button
                type="button"
                onClick={() => navigate('/register')}
                className="bg-stone-white/96 px-4 text-[12px] uppercase tracking-[0.35em] text-charcoal-black transition-colors duration-150 hover:text-medium-concrete"
              >
                Create account
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
