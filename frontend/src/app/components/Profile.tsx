import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, CircleDot, Lock, Monitor } from 'lucide-react';
import api from '../../api';
import { useAuth } from '../context/AuthContext';

interface SessionRow {
  id: string;
  device: string;
  browser: string;
  ip: string;
  lastActive: string;
}

const sessions: SessionRow[] = [
  { id: 's-001', device: 'MacBook Pro 16"', browser: 'Safari', ip: '192.168.1.14', lastActive: '14 mins ago' },
  { id: 's-002', device: 'iPhone 15 Pro', browser: 'Mobile Safari', ip: '192.168.1.88', lastActive: '1 hour ago' },
  { id: 's-003', device: 'Windows PC', browser: 'Edge', ip: '203.0.113.21', lastActive: 'Yesterday' }
];

export function Profile() {
  const navigate = useNavigate();
  const { user, updateUser, logout } = useAuth();

  const activeUser = user ?? {
    name: 'ANDO TADAO',
    phone: '+81 80 1234 5678',
    email: 'ando@example.com',
    role: 'user' as const,
    address: '13 Kiyomizu Dori, Kyoto'
  };

  const [name, setName] = useState(activeUser.name);
  const [email, setEmail] = useState(activeUser.email ?? '');
  const [phone, setPhone] = useState(activeUser.phone);
  const [dob, setDob] = useState('1992-06-18');
  const [address, setAddress] = useState(activeUser.address ?? '13 Kiyomizu Dori, Kyoto');
  const [saving, setSaving] = useState(false);
  const [profileMessage, setProfileMessage] = useState('');

  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');

  const walletId = user?.walletId ?? 'WL-8819-2204';
  const accountType = user?.accountType ?? 'STANDARD';
  const memberSince = user?.memberSince ?? 'JAN 2025';
  const walletStatus = user?.walletStatus ?? 'ACTIVE';
  const statusLabel = useMemo(() => {
    switch (walletStatus) {
      case 'LOCKED':
        return 'LOCKED';
      case 'PENDING':
        return 'PENDING';
      default:
        return 'ACTIVE';
    }
  }, [walletStatus]);
  const isLocked = walletStatus === 'LOCKED';

  useEffect(() => {
    if (!user) return;
    setName(user.name);
    setEmail(user.email ?? '');
    setPhone(user.phone);
    setAddress(user.address ?? '13 Kiyomizu Dori, Kyoto');
  }, [user]);

  const handleSave = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isLocked) return;

    setSaving(true);
    setProfileMessage('');
    try {
      const response: any = await api.put('/users/profile', {
        name,
        email,
        phone,
        address,
      });
      updateUser({ name, email, phone, address });
      setProfileMessage(response?.message || 'Profile updated successfully');
      window.setTimeout(() => setProfileMessage(''), 3200);
    } catch (error: any) {
      setProfileMessage(error?.response?.data?.message || 'Unable to update profile');
    } finally {
      setSaving(false);
    }
  };

  const resetPasswordModal = () => {
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setPasswordError('');
    setPasswordSuccess('');
  };

  const handlePasswordUpdate = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');

    if (newPassword.length < 8 || !/[A-Z]/.test(newPassword) || !/[0-9]/.test(newPassword)) {
      setPasswordError('Weak password — use at least 8 characters, a number, and an uppercase letter');
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError('Passwords do not match');
      return;
    }

    try {
      await api.put('/users/change-password', {
        currentPassword,
        newPassword,
      });
      setPasswordSuccess('Password updated successfully');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setTimeout(() => setPasswordSuccess(''), 3200);
    } catch (error: any) {
      setPasswordError(error?.response?.data?.message || 'Unable to update password');
    }
  };

  return (
    <main className="min-h-screen bg-stone-white text-charcoal-black px-4 py-8 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-[1400px] space-y-8">
        {saving && (
          <div className="h-0.5 w-full overflow-hidden rounded-none bg-transparent">
            <div className="h-full w-full bg-charcoal-black animate-pulse" />
          </div>
        )}
        <section className="grid gap-4 border border-grid-line bg-stone-white p-6 sm:p-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="space-y-2">
              <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">PROFILE / ACCOUNT</div>
              <h1 className="text-[34px] md:text-[42px] font-black uppercase tracking-[0.04em]">{activeUser.name}</h1>
            </div>
            <div className="flex flex-wrap items-center gap-3 text-[12px] uppercase tracking-[0.25em] text-charcoal-black/75">
              <span>ID: {walletId}</span>
            </div>
          </div>
          <div className="h-px bg-grid-line" aria-hidden="true" />
          <div className="grid gap-3 text-[12px] uppercase tracking-[0.25em] text-medium-concrete sm:grid-cols-[1fr_auto_1fr]">
            <div className="flex items-center gap-2">
              <CircleDot className="w-3 h-3 text-charcoal-black" />
              <span>{accountType} ACCOUNT</span>
            </div>
            <div className="hidden sm:flex items-center justify-center gap-2">
              <Bell className="w-3.5 h-3.5" />
              <span>2 notifications</span>
            </div>
            <div className="flex items-center justify-end gap-2">
              <span>Member since</span>
              <span className="font-bold text-charcoal-black">{memberSince}</span>
            </div>
          </div>
        </section>

        {isLocked && (
          <section className="border border-grid-line bg-[#e2e1dc] p-6 text-charcoal-black/85">
            <div className="font-bold uppercase tracking-[0.25em] text-[11px] text-medium-concrete mb-3">Account status</div>
            <div className="text-[14px] leading-7">This wallet is currently restricted. Financial actions are disabled until the account is restored.</div>
          </section>
        )}

        <div className="grid gap-8 xl:grid-cols-[380px_minmax(0,1fr)]">
          <aside className="space-y-6">
            <section className="border border-grid-line bg-stone-white p-8 space-y-8">
              <div className="grid gap-6">
                <div className="w-28 h-28 bg-grid-line text-charcoal-black grid place-items-center uppercase tracking-[0.35em] text-[11px] font-bold">PHOTO</div>
                <div className="space-y-3">
                  <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">Full name</div>
                  <div className="text-[24px] md:text-[30px] font-black uppercase tracking-[0.03em]">{activeUser.name}</div>
                </div>
                <div className="grid gap-4 text-[12px] uppercase tracking-[0.2em] text-medium-concrete">
                  <div className="flex justify-between gap-4">
                    <span>Wallet status</span>
                    <span className="font-bold text-charcoal-black">{statusLabel}</span>
                  </div>
                  <div className="flex justify-between gap-4">
                    <span>Account type</span>
                    <span className="font-bold text-charcoal-black">{accountType}</span>
                  </div>
                  <div className="flex justify-between gap-4">
                    <span>Member since</span>
                    <span className="font-bold text-charcoal-black">{memberSince}</span>
                  </div>
                </div>
              </div>
            </section>

            <section className="border border-grid-line bg-charcoal-black p-8 text-stone-white">
              <div className="uppercase tracking-[0.35em] text-[11px] text-stone-white/70 mb-4">Current balance</div>
              <div className="bg-[#2a2a2a] p-6">
                <div className="text-[40px] md:text-[48px] font-black tracking-tight leading-none">$12,980</div>
                <div className="mt-3 text-[11px] uppercase tracking-[0.35em] text-stone-white/70">USD</div>
              </div>
              <div className="mt-6 flex items-center justify-between border-t border-grid-line pt-5 text-[11px] uppercase tracking-[0.25em] text-stone-white/70">
                <span>Wallet status</span>
                <span className="font-bold uppercase">{statusLabel}</span>
              </div>
            </section>
          </aside>

          <div className="space-y-8">
            <form onSubmit={handleSave} className="border border-grid-line bg-stone-white p-8 space-y-6">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">Personal information</div>
                  <h2 className="mt-3 text-[26px] md:text-[30px] font-black uppercase tracking-[0.03em]">Account details</h2>
                </div>
                <div className="text-[11px] uppercase tracking-[0.25em] text-charcoal-black/60">Editable</div>
              </div>

              <div className={`grid gap-6 ${saving ? 'opacity-80' : ''}`}>
                {['Full Name', 'Email', 'Phone Number', 'Date of Birth', 'Address'].map((label, index) => {
                  const value =
                    label === 'Full Name' ? name :
                    label === 'Email' ? email :
                    label === 'Phone Number' ? phone :
                    label === 'Date of Birth' ? dob :
                    address;
                  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
                    if (label === 'Full Name') setName(event.target.value);
                    if (label === 'Email') setEmail(event.target.value);
                    if (label === 'Phone Number') setPhone(event.target.value);
                    if (label === 'Date of Birth') setDob(event.target.value);
                    if (label === 'Address') setAddress(event.target.value);
                  };

                  return (
                    <label key={label} className="block text-[12px] uppercase tracking-[0.28em] text-medium-concrete">
                      {label}
                      <input
                        type={label === 'Date of Birth' ? 'date' : 'text'}
                        value={value}
                        onChange={onChange}
                        className="mt-3 w-full bg-transparent text-charcoal-black text-[15px] leading-7 placeholder:text-medium-concrete focus:outline-none focus:ring-0 border-b border-grid-line py-4"
                        aria-label={label}
                        disabled={saving || isLocked}
                      />
                    </label>
                  );
                })}
              </div>

              {profileMessage && (
                <div className="border border-grid-line bg-concrete-gray/60 px-4 py-3 text-[13px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  {profileMessage}
                </div>
              )}

              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <button
                  type="submit"
                  disabled={saving || isLocked}
                  className="inline-flex items-center justify-center bg-charcoal-black text-stone-white px-6 py-3 uppercase tracking-[0.25em] text-[12px] font-bold focus:outline-none focus:ring-1 focus:ring-charcoal-black disabled:cursor-not-allowed disabled:bg-medium-concrete"
                >
                  Save Changes
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setName(activeUser.name);
                    setEmail(activeUser.email ?? '');
                    setPhone(activeUser.phone);
                    setAddress(activeUser.address ?? '13 Kiyomizu Dori, Kyoto');
                  }}
                  className="inline-flex items-center justify-center bg-concrete-gray text-charcoal-black px-6 py-3 uppercase tracking-[0.25em] text-[12px] font-bold focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                >
                  Cancel
                </button>
              </div>
            </form>

            <section className="border border-grid-line bg-stone-white p-8 space-y-6">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">Security settings</div>
                  <h2 className="mt-3 text-[24px] font-black uppercase tracking-[0.03em]">Protected access</h2>
                </div>
                <Lock className="w-6 h-6 text-charcoal-black" />
              </div>

              <div className="space-y-4 text-[13px] text-charcoal-black">
                <div className="flex items-center justify-between gap-4 border-b border-grid-line pb-4">
                  <div>
                    <div className="uppercase tracking-[0.25em] text-medium-concrete text-[11px]">Change Password</div>
                    <div className="mt-2 font-bold uppercase tracking-[0.08em]">Update your login key</div>
                  </div>
                  <button
                    type="button"
                    onClick={() => { setPasswordModalOpen(true); resetPasswordModal(); }}
                    className="text-[12px] uppercase tracking-[0.28em] text-charcoal-black hover:text-medium-concrete transition-colors duration-150 focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                  >
                    Change
                  </button>
                </div>

                <div className="flex items-center justify-between gap-4 border-b border-grid-line pb-4">
                  <div>
                    <div className="uppercase tracking-[0.25em] text-medium-concrete text-[11px]">Enable OTP Verification</div>
                    <div className="mt-2 font-bold uppercase tracking-[0.08em]">One-time validation</div>
                  </div>
                  <button
                    type="button"
                    className="text-[12px] uppercase tracking-[0.28em] text-charcoal-black hover:text-medium-concrete transition-colors duration-150 focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                  >
                    Enable
                  </button>
                </div>

                <div className="flex items-center justify-between gap-4 border-b border-grid-line pb-4">
                  <div>
                    <div className="uppercase tracking-[0.25em] text-medium-concrete text-[11px]">Two-Factor Authentication</div>
                    <div className="mt-2 font-bold uppercase tracking-[0.08em]">Status</div>
                  </div>
                  <span className="font-bold uppercase">Enabled</span>
                </div>

                <div className="flex items-center justify-between gap-4 border-b border-grid-line pb-4">
                  <div>
                    <div className="uppercase tracking-[0.25em] text-medium-concrete text-[11px]">Active sessions</div>
                    <div className="mt-2 font-bold uppercase tracking-[0.08em]">{sessions.length} devices</div>
                  </div>
                  <span className="text-[12px] uppercase tracking-[0.28em] text-charcoal-black/80">View below</span>
                </div>

                <div className="flex items-center justify-between gap-4">
                  <div>
                    <div className="uppercase tracking-[0.25em] text-medium-concrete text-[11px]">Logout all devices</div>
                    <div className="mt-2 font-bold uppercase tracking-[0.08em]">End every session</div>
                  </div>
                  <button
                    type="button"
                    className="text-[12px] uppercase tracking-[0.28em] text-charcoal-black hover:text-medium-concrete transition-colors duration-150 focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                  >
                    Terminate
                  </button>
                </div>
              </div>
            </section>

            <section className="border border-grid-line bg-stone-white p-8 space-y-6">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">Active session panel</div>
                  <h2 className="mt-3 text-[24px] font-black uppercase tracking-[0.03em]">Session details</h2>
                </div>
                <Monitor className="w-6 h-6 text-charcoal-black" />
              </div>

              <div className="space-y-4">
                {sessions.map((session, index) => (
                  <div key={session.id} className={`flex flex-col gap-3 py-4 ${index < sessions.length - 1 ? 'border-b border-grid-line' : ''}`}>
                    <div className="flex flex-wrap items-center justify-between gap-4">
                      <div className="text-[13px] uppercase tracking-[0.22em] text-medium-concrete">{session.device}</div>
                      <button
                        type="button"
                        className="text-[11px] uppercase tracking-[0.3em] text-charcoal-black hover:text-medium-concrete transition-colors duration-150 focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                      >
                        Terminate
                      </button>
                    </div>
                    <div className="grid gap-2 text-[13px] text-charcoal-black/80 sm:grid-cols-3">
                      <span className="uppercase tracking-[0.2em]">{session.browser}</span>
                      <span className="uppercase tracking-[0.2em]">{session.ip}</span>
                      <span className="uppercase tracking-[0.2em]">{session.lastActive}</span>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>
        </div>
      </div>

      {passwordModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-charcoal-black/35 p-4">
          <div className="w-full max-w-xl border border-grid-line bg-stone-white p-8 shadow-none focus:outline-none">
            <div className="flex items-start justify-between gap-4">
              <div>
                <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">Change password</div>
                <h2 className="mt-3 text-[26px] font-black uppercase tracking-[0.03em]">Brutalist update</h2>
              </div>
              <button
                onClick={() => setPasswordModalOpen(false)}
                className="text-[12px] uppercase tracking-[0.25em] text-charcoal-black hover:text-medium-concrete focus:outline-none focus:ring-1 focus:ring-charcoal-black"
              >
                Close
              </button>
            </div>
            <form onSubmit={handlePasswordUpdate} className="mt-8 space-y-6">
              {passwordError && (
                <div className="border border-[#8B6B6B] bg-[#f1efe9] px-4 py-3 text-[13px] uppercase tracking-[0.18em] text-[#8B6B6B] font-bold">{passwordError}</div>
              )}
              {passwordSuccess && (
                <div className="border border-grid-line bg-concrete-gray/60 px-4 py-3 text-[13px] uppercase tracking-[0.18em] font-bold text-charcoal-black">{passwordSuccess}</div>
              )}

              <label className="block text-[12px] uppercase tracking-[0.28em] text-medium-concrete">
                Current Password
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(event) => setCurrentPassword(event.target.value)}
                  className="mt-3 w-full bg-transparent text-charcoal-black border-b border-grid-line py-4 focus:outline-none focus:ring-0"
                  required
                />
              </label>
              <label className="block text-[12px] uppercase tracking-[0.28em] text-medium-concrete">
                New Password
                <input
                  type="password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                  className="mt-3 w-full bg-transparent text-charcoal-black border-b border-grid-line py-4 focus:outline-none focus:ring-0"
                  required
                />
              </label>
              <label className="block text-[12px] uppercase tracking-[0.28em] text-medium-concrete">
                Confirm Password
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  className="mt-3 w-full bg-transparent text-charcoal-black border-b border-grid-line py-4 focus:outline-none focus:ring-0"
                  required
                />
              </label>

              <div className="flex flex-col gap-4 sm:flex-row sm:justify-end">
                <button
                  type="button"
                  onClick={() => setPasswordModalOpen(false)}
                  className="inline-flex items-center justify-center bg-concrete-gray text-charcoal-black px-6 py-3 uppercase tracking-[0.25em] text-[12px] font-bold focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="inline-flex items-center justify-center bg-charcoal-black text-stone-white px-6 py-3 uppercase tracking-[0.25em] text-[12px] font-bold focus:outline-none focus:ring-1 focus:ring-charcoal-black"
                >
                  Update Password
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {profileMessage && (
        <div className="fixed bottom-6 right-6 z-50 w-[calc(100%-2rem)] max-w-[360px] border border-grid-line bg-stone-white px-5 py-4 text-[12px] uppercase tracking-[0.25em] font-bold text-charcoal-black shadow-none transition-transform duration-150 ease-out sm:w-auto">
          {profileMessage}
        </div>
      )}
    </main>
  );
}
