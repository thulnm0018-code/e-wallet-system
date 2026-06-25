import api from '../../api';
import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Button } from './Button';
import { Input } from './Input';
import { useAuth } from '../context/AuthContext';
import { OTPModal } from './OTPModal';

interface ApiResponse<T> {
  message: string;
  data: T;
}

interface UserData {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  createdAt: string;
}

interface LoginData {
  expiresIn: number;
  user: UserData;
}

export function Login() {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const [phoneError, setPhoneError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [formMessage, setFormMessage] = useState('');

  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [isOtpOpen, setIsOtpOpen] = useState(false);
  const [pendingIdentifier, setPendingIdentifier] = useState('');

  useEffect(() => {
    if (location.state?.message) {
      setFormMessage(location.state.message);
    }
  }, [location.state]);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = (await api.get('/auth/me')) as ApiResponse<UserData>;
        const user = response.data;

        if (!user) {
          return;
        }

        login({
          name: user.name,
          phone: user.phone,
          email: user.email,
          role: user.role === 'ADMIN' ? 'admin' : 'user',
          walletId: user.id.toString(),
          accountType: 'STANDARD',
          walletStatus: 'ACTIVE',
          memberSince: user.createdAt
        });

        if (user.role === 'ADMIN') {
          navigate('/admin', { replace: true });
        } else {
          navigate('/dashboard', { replace: true });
        }
      } catch {
        // Chưa đăng nhập hoặc cookie hết hạn, giữ nguyên tại trang login
      }
    };

    checkAuth();
  }, [login, navigate]);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (loading) {
      return;
    }

    setPhoneError('');
    setPasswordError('');
    setFormMessage('');
    setLoading(true);

    const identifier = phone.trim();
    const isEmail = identifier.includes('@');

    if (isEmail) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(identifier)) {
        setPhoneError(' ');
        setFormMessage('Invalid email format');
        setLoading(false);
        return;
      }
    } else {
      const phoneRegex = /^[+]?\d[\d\s-]{7,}$/;
      if (!phoneRegex.test(identifier)) {
        setPhoneError(' ');
        setFormMessage('Invalid phone number');
        setLoading(false);
        return;
      }
    }

    try {
      const response = (await api.post('/auth/login', {
        identifier,
        password
      })) as ApiResponse<LoginData>;

      const authData = response.data;

      if (!authData?.user) {
        throw new Error('Invalid login response');
      }

      const user = authData.user;

      login({
        name: user.name,
        phone: user.phone,
        email: user.email,
        role: user.role === 'ADMIN' ? 'admin' : 'user',
        walletId: user.id.toString(),
        accountType: 'STANDARD',
        walletStatus: 'ACTIVE',
        memberSince: user.createdAt
      });

      setPhone('');
      setPassword('');
      setFormMessage('');

      if (user.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }

    } catch (error: any) {
      const errorResponse = error.response?.data;

      if (errorResponse) {
        if (errorResponse.data && typeof errorResponse.data === 'object') {
          const errorsMap = errorResponse.data;
          if (errorsMap.identifier) setPhoneError(errorsMap.identifier);
          if (errorsMap.password) setPasswordError(errorsMap.password);
          setFormMessage('Please correct the highlighted errors.');
        } else if (errorResponse.message) {
          setFormMessage(errorResponse.message);
          if (errorResponse.message.toLowerCase().includes('otp') || errorResponse.message.toLowerCase().includes('activated')) {
            setPendingIdentifier(identifier);
            setIsOtpOpen(true);
          }
        } else {
          setFormMessage('Login failed. Please try again.');
        }
      } else {
        setFormMessage('Cannot connect to server. Please check your network.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black">
      <div className={`relative flex justify-center px-6 py-20 transition-all duration-300 ${isOtpOpen ? 'blur-sm select-none pointer-events-none' : ''}`}>
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
      {isOtpOpen && (
        <OTPModal
          isOpen={isOtpOpen}
          identifier={pendingIdentifier}
          onSuccess={() => {
            setIsOtpOpen(false);
            setFormMessage('Xác thực tài khoản thành công! Vui lòng đăng nhập.');
          }}
          onClose={() => {
            setIsOtpOpen(false);
          }}
        />
      )}
    </main>
  );
}