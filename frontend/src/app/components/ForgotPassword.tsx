import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from './Button';
import { Input } from './Input';

export function ForgotPassword() {
  const navigate = useNavigate();
  const [identifier, setIdentifier] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const validateIdentifier = (value: string) => {
    const email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
    const phone = /^[+]?\d[\d\s-]{7,}$/.test(value.trim());
    return email || phone;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const trimmed = identifier.trim();
    if (!trimmed) {
      setError('Enter email or phone number');
      return;
    }

    if (!validateIdentifier(trimmed)) {
      setError('Enter a valid email or phone number');
      return;
    }

    setLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 1500));
    setLoading(false);

    navigate('/reset-password', { state: { identifier: trimmed } });
  };

  return (
    <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black flex items-center justify-center">
      {/* Background gradients */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.18),transparent_18%),radial-gradient(circle_at_bottom_right,rgba(0,0,0,0.08),transparent_18%),linear-gradient(125deg,#ebe8e1,#d9d6cf)]" />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.12),transparent_36%,rgba(0,0,0,0.05))]" />

      <div className="relative w-full max-w-lg border border-grid-line bg-stone-white/96 p-12 md:p-16 shadow-[0_24px_80px_rgba(0,0,0,0.05)] transition-all duration-300">
        
        {/* Calm loading indicator at top of card */}
        {loading && (
          <div className="absolute top-0 left-0 right-0 h-[2px] bg-grid-line overflow-hidden">
            <div className="h-full bg-charcoal-black animate-pulse-line w-1/2" />
          </div>
        )}

        <div className="mb-14 space-y-2">
          <div className="uppercase tracking-[0.35em] text-[11px] text-medium-concrete">Recovery</div>
          <h1 className="text-[2.8rem] leading-[0.9] tracking-[0.15em] font-black uppercase">
            RECOVER
          </h1>
          <p className="text-[12px] tracking-[0.08em] text-medium-concrete font-normal pt-2">
            Enter your credentials. We'll verify them and guide you through setting up a new secure password.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-12">
          <Input
            label="Email Address or Phone"
            type="text"
            placeholder="e.g. name@wallet.com or +84 123 456 789"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            disabled={loading}
            error={error}
            className="text-[15px] tracking-wide"
          />

          <div className="space-y-6">
            <Button
              type="submit"
              variant="primary"
              className="h-14 w-full rounded-none uppercase tracking-[0.35em] transition-all duration-200 hover:tracking-[0.42em]"
              disabled={loading}
            >
              {loading ? 'Verifying Credentials…' : 'Continue'}
            </Button>

            <div className="text-center">
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="text-[11px] uppercase tracking-[0.3em] text-medium-concrete hover:text-charcoal-black transition-colors duration-150"
              >
                Back to Login
              </button>
            </div>
          </div>
        </form>
      </div>

      <style>{`
        @keyframes pulse-line {
          0% { transform: translateX(-100%); }
          50% { transform: translateX(100%); }
          100% { transform: translateX(200%); }
        }
        .animate-pulse-line {
          animation: pulse-line 1.8s infinite linear;
        }
      `}</style>
    </main>
  );
}

