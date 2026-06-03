import { useMemo, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Button } from './Button';
import { Input } from './Input';

function evaluateStrength(password: string) {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score += 1;

  if (!password) return { label: 'Empty', width: '0%', color: 'bg-grid-line' };
  if (score <= 1) return { label: 'Weak', width: '25%', color: 'bg-[#8B6B6B]' };
  if (score === 2) return { label: 'Fair', width: '50%', color: 'bg-[#8B8371]' };
  if (score === 3) return { label: 'Strong', width: '75%', color: 'bg-[#6B6B5A]' };
  return { label: 'Very strong', width: '100%', color: 'bg-charcoal-black' };
}

export function ResetPassword() {
  const navigate = useNavigate();
  const location = useLocation();
  const identifier = location.state?.identifier || '';

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [confirmError, setConfirmError] = useState('');
  const [success, setSuccess] = useState(false);

  const strength = useMemo(() => evaluateStrength(password), [password]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordError('');
    setConfirmError('');

    if (password.length < 8) {
      setPasswordError('Password must be at least 8 characters');
      return;
    }

    if (confirm !== password) {
      setConfirmError('Passwords do not match');
      return;
    }

    setLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 1500));
    setLoading(false);
    setSuccess(true);
  };

  const handleRedirectLogin = () => {
    navigate('/login');
  };

  if (success) {
    return (
      <main className="min-h-screen overflow-hidden bg-[#e4e1dc] text-charcoal-black flex items-center justify-center">
        {/* Background gradients */}
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.18),transparent_18%),radial-gradient(circle_at_bottom_right,rgba(0,0,0,0.08),transparent_18%),linear-gradient(125deg,#ebe8e1,#d9d6cf)]" />
        <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.12),transparent_36%,rgba(0,0,0,0.05))]" />

        <div className="relative text-center px-6 max-w-lg w-full space-y-16 animate-fade-in">
          <div className="space-y-4">
            <div className="uppercase tracking-[0.4em] text-[11px] text-medium-concrete">Success State</div>
            <h1 className="text-[2.2rem] md:text-[2.6rem] leading-snug tracking-[0.2em] font-normal uppercase text-charcoal-black max-w-md mx-auto">
              Password Updated Successfully
            </h1>
          </div>

          <div className="pt-8">
            <button
              onClick={handleRedirectLogin}
              className="group relative inline-flex items-center gap-3 text-[12px] uppercase tracking-[0.35em] text-charcoal-black/70 hover:text-charcoal-black transition-colors duration-200"
            >
              <span>Back to Login</span>
              <span className="inline-block transition-transform duration-200 group-hover:translate-x-1">→</span>
              <span className="absolute bottom-[-6px] left-0 right-0 h-[1px] bg-charcoal-black/30 group-hover:bg-charcoal-black transition-colors duration-200" />
            </button>
          </div>
        </div>

        <style>{`
          .animate-fade-in {
            animation: fadeIn 0.8s ease-out forwards;
          }
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(12px); }
            to { opacity: 1; transform: translateY(0); }
          }
        `}</style>
      </main>
    );
  }

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
          <div className="uppercase tracking-[0.35em] text-[11px] text-medium-concrete">Set new password</div>
          <h1 className="text-[2.8rem] leading-[0.9] tracking-[0.15em] font-black uppercase">
            RESET
          </h1>
          {identifier && (
            <p className="text-[12px] tracking-[0.08em] text-medium-concrete font-normal pt-2">
              Create a new password credentials for <span className="text-charcoal-black font-medium break-all">{identifier}</span>.
            </p>
          )}
        </div>

        <form onSubmit={handleSubmit} className="space-y-10">
          <div className="space-y-8">
            <div className="space-y-2">
              <Input
                label="New Password"
                type="password"
                placeholder="Create a new password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                error={passwordError}
                className="text-[15px] tracking-wide"
              />

              {password && (
                <div className="pt-2 space-y-2 animate-fade-in-quick">
                  <div className="flex items-center justify-between text-[10px] uppercase tracking-[0.25em] text-medium-concrete">
                    <span>Password strength</span>
                    <span>{strength.label}</span>
                  </div>
                  <div className="h-[2px] w-full bg-[#E6E4DF]">
                    <div
                      className={`h-full ${strength.color} transition-all duration-300`}
                      style={{ width: strength.width }}
                    />
                  </div>
                </div>
              )}
            </div>

            <Input
              label="Confirm Password"
              type="password"
              placeholder="Confirm your new password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              disabled={loading}
              error={confirmError}
              className="text-[15px] tracking-wide"
            />
          </div>

          <div className="space-y-6 pt-4">
            <Button
              type="submit"
              variant="primary"
              className="h-14 w-full rounded-none uppercase tracking-[0.35em] transition-all duration-200 hover:tracking-[0.42em]"
              disabled={loading}
            >
              {loading ? 'Updating Password…' : 'Update Password'}
            </Button>

            <div className="text-center">
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="text-[11px] uppercase tracking-[0.3em] text-medium-concrete hover:text-charcoal-black transition-colors duration-150"
              >
                Cancel
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
        .animate-fade-in-quick {
          animation: fadeInQuick 0.3s ease-out forwards;
        }
        @keyframes fadeInQuick {
          from { opacity: 0; transform: translateY(4px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </main>
  );
}

