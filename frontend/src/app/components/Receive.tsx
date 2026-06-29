import { useState } from 'react';
import { Card } from './Card';
import { Button } from './Button';
import { useNavigate } from 'react-router-dom';
import { Copy, Check } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export function Receive() {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const { user } = useAuth();

  const walletAddress = user?.phone ?? 'Unknown';

  const handleCopy = () => {
    navigator.clipboard.writeText(walletAddress);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
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
              IMPORTANT NOTICE
            </div>
            <div className="space-y-4 text-[15px] leading-relaxed tracking-wide text-charcoal-black/80">
              <p>
                This is a demonstration wallet address. In a production environment, this would be your unique blockchain or payment network identifier.
              </p>
              <p>
                Share this address only with trusted parties who need to send you funds.
              </p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
