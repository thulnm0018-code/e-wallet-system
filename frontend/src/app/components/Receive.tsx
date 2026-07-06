import { useState } from 'react';
import { Card } from './Card';
import { Button } from './Button';
import { useNavigate } from 'react-router-dom';
import { Copy, Check, DollarSign } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useWallet } from '../context/WalletContext';
import api from '../../api';

export function Receive() {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const [depositAmount, setDepositAmount] = useState('');
  const [depositMessage, setDepositMessage] = useState('');
  const [depositStatus, setDepositStatus] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { user } = useAuth();
  const { deposit, refreshWallet } = useWallet();

  const walletAddress = user?.phone ?? 'Unknown';

  const handleCopy = () => {
    navigator.clipboard.writeText(walletAddress);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDeposit = async (e: React.FormEvent) => {
    e.preventDefault();
    const amount = Number(depositAmount);
    if (!amount || amount <= 0) {
      setDepositStatus('Please enter a valid amount');
      return;
    }

    try {
      setSubmitting(true);
      setDepositStatus(null);
      await deposit(amount, depositMessage.trim() || 'Demo deposit');
      setDepositAmount('');
      setDepositMessage('');
      setDepositStatus('Deposit successful');
      await refreshWallet();
    } catch (error: any) {
      setDepositStatus(error?.response?.data?.message || 'Deposit failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-[1440px] mx-auto px-8 py-16">
      <div className="max-w-[600px] mx-auto space-y-12">
        <div className="space-y-4">
          <h1 className="text-[48px] tracking-tight" style={{ fontWeight: 800 }}>
            RECEIVE FUNDS
          </h1>
          <div className="text-[13px] tracking-[0.15em] text-charcoal-black/60">
            SHARE YOUR WALLET ADDRESS TO RECEIVE PAYMENTS
          </div>
        </div>

        <Card className="p-12 space-y-10">
          <div className="space-y-4">
            <div className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">
              YOUR WALLET ADDRESS
            </div>
            <div className="bg-stone-white border border-grid-line p-8">
              <div className="text-[20px] tracking-wider break-all" style={{ fontWeight: 700 }}>
                {walletAddress}
              </div>
            </div>
          </div>

          <div className="space-y-4">
            <Button
              variant="primary"
              className="w-full flex items-center justify-center gap-3"
              onClick={handleCopy}
            >
              {copied ? (
                <>
                  <Check className="w-5 h-5" strokeWidth={2} />
                  COPIED
                </>
              ) : (
                <>
                  <Copy className="w-5 h-5" strokeWidth={2} />
                  COPY ADDRESS
                </>
              )}
            </Button>
            <Button
              variant="ghost"
              className="w-full"
              onClick={() => navigate('/')}
            >
              BACK TO DASHBOARD
            </Button>
          </div>
        </Card>

        <Card className="p-12 bg-stone-white border-grid-line">
          <div className="space-y-6">
            <div className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">
              DEMO DEPOSIT
            </div>
            <form onSubmit={handleDeposit} className="space-y-4">
              <input
                type="number"
                min="1"
                step="0.01"
                placeholder="Amount"
                value={depositAmount}
                onChange={(e) => setDepositAmount(e.target.value)}
                className="w-full border border-grid-line px-4 py-3 text-charcoal-black"
              />
              <input
                type="text"
                placeholder="Message (optional)"
                value={depositMessage}
                onChange={(e) => setDepositMessage(e.target.value)}
                className="w-full border border-grid-line px-4 py-3 text-charcoal-black"
              />
              <Button variant="primary" type="submit" className="w-full flex items-center justify-center gap-2" disabled={submitting}>
                <DollarSign className="w-4 h-4" />
                {submitting ? 'PROCESSING...' : 'DEPOSIT FUNDS'}
              </Button>
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
