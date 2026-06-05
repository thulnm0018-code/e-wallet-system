import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from './Button';

export function OTPVerification() {
  const navigate = useNavigate();
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [shaking, setShaking] = useState(false);
  const [timeLeft, setTimeLeft] = useState(300);
  const [expired, setExpired] = useState(false);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const maskedEmail = '***lo@w****t.com';

  useEffect(() => {
    if (timeLeft <= 0) {
      setExpired(true);
      return;
    }

    const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
    return () => clearTimeout(timer);
  }, [timeLeft]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const handleChange = (index: number, value: string) => {
    if (!/^\d?$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);
    setError('');
    setShaking(false);

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (expired) return;

    const otpCode = otp.join('');
    if (otpCode.length !== 6) {
      setError('Enter all 6 digits');
      return;
    }

    if (loading) return;
    setLoading(true);

    await new Promise((resolve) => setTimeout(resolve, 1000));

    if (otpCode === '123456') {
      setOtp(['', '', '', '', '', '']);
      setError('');
      setLoading(false);
      navigate('/dashboard');
    } else {
      setShaking(true);
      setError('Verification code invalid');
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
      setLoading(false);
      setTimeout(() => setShaking(false), 500);
    }
  };

  const handleResend = () => {
    setTimeLeft(300);
    setExpired(false);
    setOtp(['', '', '', '', '', '']);
    setError('');
    setShaking(false);
    inputRefs.current[0]?.focus();
  };

  const handleRequestNew = () => {
    setTimeLeft(300);
    setExpired(false);
    setOtp(['', '', '', '', '', '']);
    setError('');
    setShaking(false);
    inputRefs.current[0]?.focus();
  };

  return (
    <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.18),transparent_18%),radial-gradient(circle_at_bottom_right,rgba(0,0,0,0.08),transparent_18%),linear-gradient(125deg,#ebe8e1,#d9d6cf)]" />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.12),transparent_36%,rgba(0,0,0,0.05))]" />
      <div className="relative flex min-h-screen items-center justify-center px-6 py-20">
        <div className={`w-full max-w-md border border-grid-line bg-stone-white/96 p-8 shadow-[0_24px_80px_rgba(0,0,0,0.08)] transition-opacity duration-200 ${loading ? 'opacity-80' : 'opacity-100'}`}>
          <div className="mb-12 space-y-2">
            <div className="uppercase tracking-[0.35em] text-[11px] text-medium-concrete">Verify code</div>
            <h1 className="text-[3rem] leading-[0.9] tracking-[0.15em] font-black">VERIFY</h1>
          </div>

          <form onSubmit={handleSubmit} className="grid gap-8">
            <div className="text-center space-y-1">
              <p className="text-[12px] uppercase tracking-[0.28em] text-medium-concrete">Code sent to</p>
              <p className="text-[14px] tracking-[0.15em] font-medium text-charcoal-black">{maskedEmail}</p>
            </div>

            {!expired ? (
              <>
                <div className={`flex gap-2 justify-center ${shaking ? 'animate-bounce' : ''}`} style={shaking ? { animation: 'shake 0.3s ease-in-out' } : {}}>
                  {otp.map((digit, index) => (
                    <input
                      key={index}
                      ref={(el) => { inputRefs.current[index] = el; }}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      value={digit}
                      onChange={(e) => handleChange(index, e.target.value)}
                      onKeyDown={(e) => handleKeyDown(index, e)}
                      disabled={loading || expired}
                      className={`w-12 h-14 text-center text-lg font-bold border-2 transition-colors duration-150 ${
                        error
                          ? 'border-[#8B6B6B] text-charcoal-black'
                          : 'border-grid-line text-charcoal-black focus:border-charcoal-black focus:outline-none'
                      }`}
                    />
                  ))}
                </div>

                {error && <div className="text-center text-[12px] uppercase tracking-[0.28em] text-[#8B6B6B]">{error}</div>}

                <div className="text-center space-y-1">
                  <p className="text-[12px] uppercase tracking-[0.28em] text-medium-concrete">Code expires in</p>
                  <p className="text-[16px] tracking-[0.15em] font-bold text-charcoal-black">{formatTime(timeLeft)}</p>
                </div>

                <Button type="submit" variant="primary" className="h-12 w-full rounded-none uppercase tracking-[0.35em]" disabled={loading}>
                  {loading ? 'Verifying…' : 'Verify'}
                </Button>

                <div className="mt-6 text-center">
                  <button
                    type="button"
                    onClick={handleResend}
                    disabled={timeLeft > 0}
                    className="text-[11px] uppercase tracking-[0.35em] text-charcoal-black transition-colors duration-150 disabled:text-medium-concrete disabled:cursor-not-allowed hover:text-medium-concrete py-2"
                  >
                    Resend OTP
                  </button>
                </div>
              </>
            ) : (
              <>
                <div className="text-center space-y-4">
                  <div className="text-[14px] uppercase tracking-[0.28em] font-medium text-[#8B6B6B]">OTP expired</div>
                  <p className="text-[12px] uppercase tracking-[0.28em] text-medium-concrete">Request a new verification code</p>
                </div>

                <Button type="button" onClick={handleRequestNew} variant="primary" className="h-12 w-full rounded-none uppercase tracking-[0.35em]">
                  Request new code
                </Button>
              </>
            )}
          </form>
        </div>
      </div>

      <style>{`
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          25% { transform: translateX(-4px); }
          75% { transform: translateX(4px); }
        }
      `}</style>
    </main>
  );
}
