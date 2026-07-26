import { useState, useEffect } from 'react';
import { Card } from './Card';
import { Button } from './Button';
import { useNavigate } from 'react-router-dom';
import { Copy, Check, DollarSign, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useWallet } from '../context/WalletContext';
import api from '../../api';

const formatMobilePhone = (value?: string | null) => {
  if (!value) return 'Unknown';

  const digits = value.replace(/\D/g, '');
  if (!digits) return 'Unknown';

  if (digits.startsWith('84')) {
    return `0${digits.slice(2)}`;
  }

  return digits.startsWith('0') ? digits : digits;
};

const maskPhoneNumber = (value: string) => {
  if (!value || value === 'Unknown') return 'Unknown';
  const digits = value.replace(/\D/g, '');
  if (digits.length <= 4) return value;

  const visiblePrefix = digits.slice(0, 4);
  const visibleSuffix = digits.slice(-3);
  return `${visiblePrefix} *** ${visibleSuffix}`;
};

export function Receive() {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const [depositAmount, setDepositAmount] = useState('');
  const [depositMessage, setDepositMessage] = useState('');
  const [depositMethod, setDepositMethod] = useState('BANK_TRANSFER');
  const [depositStatus, setDepositStatus] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [showPhone, setShowPhone] = useState(false);
  const { user } = useAuth();
  const { deposit, refreshWallet } = useWallet();
  const profileComplete = Boolean(user?.address?.trim()) && Boolean(user?.dateOfBirth);

  const walletAddress = formatMobilePhone(user?.phone);
  const maskedWalletAddress = maskPhoneNumber(walletAddress);

  const handleCopy = () => {
    navigator.clipboard.writeText(walletAddress);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  useEffect(() => {
    if (!profileComplete) {
      navigate('/profile', {
        state: {
          profilePrompt: 'Complete your address and date of birth before receiving funds.'
        }
      });
    }
  }, [profileComplete, navigate]);

  const handleDeposit = async (e: React.FormEvent) => {
    if (!profileComplete) {
      navigate('/profile', {
        state: {
          profilePrompt: 'Complete your address and date of birth before receiving funds.'
        }
      });
      return;
    }
    e.preventDefault();
    const amount = Number(depositAmount);
    if (!amount || amount <= 0) {
      setDepositStatus('Please enter a valid amount');
      return;
    }

    try {
      setSubmitting(true);
      setDepositStatus(null);
      await deposit(amount, depositMessage.trim() || 'Demo deposit', depositMethod);
      setDepositAmount('');
      setDepositMessage('');
      setDepositStatus('Deposit request submitted and pending admin approval');
      await refreshWallet();
    } catch (error: any) {
      setDepositStatus(error?.response?.data?.message || 'Deposit failed');
    } finally {
      setSubmitting(false);
    }
  };

  if (!profileComplete) {
    return null;
  }

  return (
    <div className="max-w-[1440px] mx-auto px-8 py-16">
      <div className="max-w-[600px] mx-auto space-y-12">
        <div className="space-y-4">
          <h1 className="text-[48px] tracking-tight" style={{ fontWeight: 800 }}>
            RECEIVE FUNDS
          </h1>
        </div>

        <Card className="p-12 bg-stone-white border-grid-line overflow-visible">
          <div className="space-y-6">
            <div className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">
              DEPOSIT FUNDS
            </div>
            <form onSubmit={handleDeposit} className="space-y-4">
              <div className="grid gap-4 md:grid-cols-[1fr_1fr] relative z-30">
                <input
                  type="number"
                  min="1"
                  step="0.01"
                  placeholder="Amount"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value)}
                  className="w-full border border-grid-line px-4 py-3 text-charcoal-black"
                />
                <div className="relative z-40">
                  <select
                    value={depositMethod}
                    onChange={(e) => setDepositMethod(e.target.value)}
                    className="w-full border border-grid-line px-4 py-3 text-charcoal-black bg-white"
                  >
                    <option value="Linked Bank">Linked Bank</option>
                    <option value="Domestic Card / Napas">Domestic Card / Napas</option>
                    <option value="VNPAY-QR">VNPAY-QR</option>
                    <option value="Virtual Account">Virtual Account</option>
                  </select>
                </div>
              </div>
              <input
                type="text"
                placeholder="Message (optional)"
                value={depositMessage}
                onChange={(e) => setDepositMessage(e.target.value)}
                className="w-full border border-grid-line px-4 py-3 text-charcoal-black"
              />
              <div className="pt-3 relative z-10">
                <Button variant="primary" type="submit" className="w-full flex items-center justify-center gap-2" disabled={submitting}>
                  <DollarSign className="w-4 h-4" />
                  {submitting ? 'PROCESSING...' : 'REQUEST DEPOSIT'}
                </Button>
              </div>
            </form>
            {depositStatus && (
              <div className="text-sm text-charcoal-black/80">{depositStatus}</div>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
