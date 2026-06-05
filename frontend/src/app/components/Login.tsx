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

    const input = phone.trim();
    const isEmailInput = input.includes('@');

    // 1. Validation check
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

    // 2. Admin Check
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

    // 3. Registered User Check (from localStorage)
    let foundUser: any = null;
    try {
      const regUsers = JSON.parse(localStorage.getItem('registered_users') || '[]');
      if (isEmailInput) {
        foundUser = regUsers.find((u: any) => u.email?.toLowerCase() === input.toLowerCase());
      } else {
        // Normalize phone numbers to compare (remove spaces, dashes, etc.)
        const normalizedInput = input.replace(/[\s-()]/g, '');
        foundUser = regUsers.find((u: any) => u.phone?.replace(/[\s-()]/g, '') === normalizedInput);
      }
    } catch (e) {
      console.error(e);
    }

    if (foundUser) {
      if (foundUser.password !== password) {
        setFormMessage('Incorrect password');
        setPasswordError(' ');
      } else if (foundUser.walletStatus === 'LOCKED') {
        setFormMessage('Wallet is locked');
        setPhoneError(' ');
        setPasswordError(' ');
      } else {
        login({
          name: foundUser.name || foundUser.fullName,
          phone: foundUser.phone,
          email: foundUser.email,
          role: foundUser.role || 'user',
          walletId: foundUser.id,
          accountType: foundUser.accountType || 'STANDARD',
          walletStatus: foundUser.walletStatus || 'ACTIVE',
          memberSince: foundUser.memberSince || 'JAN 2025'
        });
        setFormMessage('');
        setPhone('');
        setPassword('');
        navigate('/dashboard');
      }
      setLoading(false);
      return;
    }

    // 4. Default Fallback User Check
    const isDefaultUser = (isEmailInput && input.toLowerCase() === 'hello@wallet.com') ||
      (!isEmailInput && (input === '+84 123 456 789' || input === '123456789' || input.replace(/[\s-()]/g, '').endsWith('123456789')));

    if (input === PHONE_LOCKED_VALUE || (isEmailInput && input.toLowerCase() === 'locked@wallet.com')) {
      setFormMessage('Wallet is locked');
      setPhoneError(' ');
      setPasswordError(' ');
    } else if (isDefaultUser || (!isEmailInput && /^[+]?\d[\d\s-]{7,}$/i.test(input))) {
      // If matches default user, or if password matches fallback password 'wallet123'
      if (password !== 'wallet123') {
        setFormMessage('Incorrect password');
        setPasswordError(' ');
      } else {
        login({
          name: 'ANDO TADAO',
          phone: isEmailInput ? '+84 123 456 789' : input,
          email: isEmailInput ? input : 'hello@wallet.com',
          role: 'user',
          walletId: 'WL-8802-9901',
          memberSince: 'JAN 2025',
          walletStatus: 'ACTIVE'
        });
        setFormMessage('');
        setPhone('');
        setPassword('');
        navigate('/dashboard');
      }
    } else {
      setFormMessage(isEmailInput ? 'Email not found' : 'Phone number not found');
      setPhoneError(' ');
    }

    setLoading(false);
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
