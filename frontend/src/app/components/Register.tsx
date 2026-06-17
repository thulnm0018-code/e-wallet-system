import axios from 'axios';
import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from './Button';
import { Input } from './Input';

const EXISTING_EMAIL = 'hello@wallet.com';
const EXISTING_PHONE = '+84 123 456 789';

function evaluateStrength(password: string) {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score += 1;

  if (!password) return { label: 'Empty', width: '0%', color: 'bg-grid-line' };
  if (score <= 1) return { label: 'Weak', width: '22%', color: 'bg-[#8B6B6B]' };
  if (score === 2) return { label: 'Fair', width: '48%', color: 'bg-[#8B8371]' };
  if (score === 3) return { label: 'Strong', width: '72%', color: 'bg-[#6B6B5A]' };
  return { label: 'Very strong', width: '100%', color: 'bg-charcoal-black' };
}

export function Register() {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [nameError, setNameError] = useState('');
  const [emailError, setEmailError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [confirmError, setConfirmError] = useState('');
  const [formWarning, setFormWarning] = useState('');

  const strength = useMemo(() => evaluateStrength(password), [password]);

  const validateEmail = (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
  const validatePhone = (value: string) => /^[+]?\d[\d\s-]{7,}$/.test(value.trim());

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (loading) return;

    setNameError('');
    setEmailError('');
    setPhoneError('');
    setPasswordError('');
    setConfirmError('');
    setFormWarning('');

    const trimmedEmail = email.trim();
    const trimmedPhone = phone.trim();
    const trimmedName = fullName.trim();

    if (!trimmedName) {
      setNameError(' ');
    }
    if (!validateEmail(trimmedEmail)) {
      setEmailError('Enter a valid email');
    }
    if (!validatePhone(trimmedPhone)) {
      setPhoneError('Enter a valid phone number');
    }
    if (password.length < 8) {
      setPasswordError('Use 8+ characters');
    }
    if (confirm !== password) {
      setConfirmError('Passwords do not match');
    }

    if (
      !trimmedName ||
      !validateEmail(trimmedEmail) ||
      !validatePhone(trimmedPhone) ||
      password.length < 8 ||
      confirm !== password
    ) {
      return;
    }

    if (trimmedEmail.toLowerCase() === EXISTING_EMAIL || trimmedPhone === EXISTING_PHONE) {
      setFormWarning('Phone number or email already exists');
      return;
    }

    setLoading(true);
    try {
      await axios.post('http://localhost:8080/api/auth/register', {
        name: trimmedName,
        email: trimmedEmail,
        phone: trimmedPhone,
        password,
      });

      navigate('/login');
    } catch (error: any) {
      setFormWarning(
        error.response?.data?.message || 'Failed to create account. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.18),transparent_16%),radial-gradient(circle_at_bottom_right,rgba(0,0,0,0.08),transparent_20%),linear-gradient(140deg,#ebe8e1,#d9d6cf)]" />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.12),transparent_36%,rgba(0,0,0,0.05))]" />
      <div className="relative flex min-h-screen items-center justify-center px-6 py-20">
        <div
          className={`w-full max-w-md border border-grid-line bg-stone-white/96 p-8 shadow-[0_28px_90px_rgba(0,0,0,0.08)] transition-opacity duration-200 ${
            loading ? 'opacity-80' : 'opacity-100'
          }`}
        >
          <div className="mb-10 space-y-2">
            <div className="uppercase tracking-[0.35em] text-[11px] text-medium-concrete">Register</div>
            <h1 className="text-[3rem] leading-[0.95] tracking-[0.15em] font-black">CREATE ACCOUNT</h1>
          </div>

          <form onSubmit={handleSubmit} className="grid gap-6">
            <Input
              label="Full Name"
              type="text"
              placeholder="Jane Doe"
              value={fullName}
              onChange={(event) => setFullName(event.target.value)}
              disabled={loading}
              error={nameError}
            />
            <Input
              label="Email"
              type="email"
              placeholder="hello@wallet.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              disabled={loading}
              error={emailError}
            />
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
              placeholder="Create a password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              disabled={loading}
              error={passwordError}
            />
            {password ? (
              <div className="space-y-2">
                <div className="flex items-center justify-between text-[11px] uppercase tracking-[0.28em] text-medium-concrete">
                  <span>Password strength</span>
                  <span>{strength.label}</span>
                </div>
                <div className="h-[5px] w-full overflow-hidden rounded-full bg-[#E6E4DF]">
                  <div
                    className={`h-full ${strength.color} transition-all duration-200`}
                    style={{ width: strength.width }}
                  />
                </div>
              </div>
            ) : null}
            <Input
              label="Confirm Password"
              type="password"
              placeholder="Repeat password"
              value={confirm}
              onChange={(event) => setConfirm(event.target.value)}
              disabled={loading}
              error={confirmError}
            />

            {formWarning ? (
              <div className="rounded-none border border-[#8B6B6B]/30 bg-[#F5F4F0] px-4 py-3 text-center text-[13px] uppercase tracking-[0.28em] text-[#8B6B6B]">
                {formWarning}
              </div>
            ) : null}

            <Button type="submit" variant="primary" className="h-14 w-full rounded-none uppercase tracking-[0.35em]" disabled={loading}>
              {loading ? 'Creating account…' : 'Create Account'}
            </Button>
          </form>
        </div>
      </div>
    </main>
  );
}
