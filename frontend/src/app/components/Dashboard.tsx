import { useState, useEffect } from 'react';
import { useWallet, Transaction } from '../context/WalletContext';
import { useNavigate } from 'react-router-dom';
import { ArrowDownRight, ArrowUpRight, Clock3, Copy, Check, Eye, EyeOff, QrCode } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

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

  return `${digits.slice(0, 4)} *** ${digits.slice(-3)}`;
};

export function Dashboard() {
  const { balance, transactions, refreshWallet } = useWallet();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);
  const [walletModalOpen, setWalletModalOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const [showPhone, setShowPhone] = useState(false);
  const profileComplete = Boolean(user?.address?.trim()) && Boolean(user?.dateOfBirth);

  const handleFinancialNavigation = (path: string) => {
    if ((path === '/receive' || path === '/send') && !profileComplete) {
      navigate('/profile', {
        state: {
          profilePrompt: 'Complete your address and date of birth to unlock deposits and withdrawals.'
        }
      });
      return;
    }
    navigate(path);
  };

  // Close modal on escape keypress and trigger refresh wallet on mount
  useEffect(() => {
    refreshWallet();
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setSelectedTransaction(null);
        setWalletModalOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [refreshWallet]);

  const recentTransactions = transactions.slice(0, 8);
  const walletAddress = formatMobilePhone(user?.phone);
  const maskedWalletAddress = maskPhoneNumber(walletAddress);

  const handleCopyWallet = () => {
    navigator.clipboard.writeText(walletAddress);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <main className="min-h-screen bg-stone-white text-charcoal-black">
      <div className="max-w-[1440px] mx-auto px-8 py-16 space-y-16">
        
        {!profileComplete && (
          <div className="bg-[#8B6B6B]/10 border border-[#8B6B6B]/30 p-6 flex flex-col sm:flex-row justify-between items-center gap-4 rounded-none">
            <span className="text-[13px] uppercase tracking-[0.15em] font-bold text-[#8B6B6B]">
              Complete your profile to unlock wallet features.
            </span>
            <button
              onClick={() => navigate('/profile')}
              className="bg-charcoal-black hover:bg-concrete-gray text-stone-white hover:text-charcoal-black border border-charcoal-black px-6 py-2.5 text-[11px] font-extrabold uppercase tracking-widest rounded-none transition-colors duration-150 cursor-pointer"
            >
              Go to Profile
            </button>
          </div>
        )}

        {/* BALANCE SECTION */}
        <section className="bg-concrete-gray border border-grid-line p-12 md:p-16 space-y-8">
          <div className="flex items-center justify-between text-[11px] uppercase tracking-[0.25em] text-charcoal-black/60">
            <span>Total Balance</span>
            <div className="flex items-center gap-2">
              <span className="w-[6px] h-[6px] bg-[#6B6B5A] inline-block" aria-hidden="true" />
              <span>Wallet Secured</span>
            </div>
          </div>
          <div className="flex items-baseline gap-3">
            <span className="text-[72px] leading-none tracking-tight font-black text-charcoal-black">
              ${balance.toFixed(2)}
            </span>
            <span className="text-[20px] font-black tracking-widest text-charcoal-black/60">
              USD
            </span>
          </div>
        </section>

        {/* QUICK ACTION GRID */}
        <section className="space-y-6">
          <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-medium px-1">
            System operations
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-[1px] bg-grid-line border border-grid-line">
            <button
              onClick={() => handleFinancialNavigation('/receive')}
              className="bg-stone-white hover:bg-concrete-gray p-12 transition-colors duration-100 text-left focus:outline-none focus:ring-1 focus:ring-charcoal-black cursor-pointer"
            >
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <ArrowDownRight className="w-8 h-8 text-charcoal-black" />
                  <span className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">DEPOSIT</span>
                </div>
                <div className="text-[20px] tracking-wide font-black text-charcoal-black uppercase">Add Funds</div>
              </div>
            </button>

            <button
              onClick={() => handleFinancialNavigation('/send')}
              className="bg-stone-white hover:bg-concrete-gray p-12 transition-colors duration-100 text-left focus:outline-none focus:ring-1 focus:ring-charcoal-black cursor-pointer"
            >
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <ArrowUpRight className="w-8 h-8 text-charcoal-black" />
                  <span className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">WITHDRAW</span>
                </div>
                <div className="text-[20px] tracking-wide font-black text-charcoal-black uppercase">Cash Out</div>
              </div>
            </button>

            <button
              onClick={() => navigate('/transactions')}
              className="bg-stone-white hover:bg-concrete-gray p-12 transition-colors duration-100 text-left focus:outline-none focus:ring-1 focus:ring-charcoal-black cursor-pointer"
            >
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <Clock3 className="w-8 h-8 text-charcoal-black" />
                  <span className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">TRANSACTIONS</span>
                </div>
                <div className="text-[20px] tracking-wide font-black text-charcoal-black uppercase">View History</div>
              </div>
            </button>

            <button
              onClick={() => setWalletModalOpen(true)}
              className="bg-stone-white hover:bg-concrete-gray p-12 transition-colors duration-100 text-left focus:outline-none focus:ring-1 focus:ring-charcoal-black cursor-pointer"
            >
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <QrCode className="w-8 h-8 text-charcoal-black" />
                  <span className="text-[13px] uppercase tracking-[0.15em] text-charcoal-black/60">MY QR CODE</span>
                </div>
                <div className="text-[20px] tracking-wide font-black text-charcoal-black uppercase">Wallet Address</div>
              </div>
            </button>
          </div>
        </section>

        {/* RECENT TRANSACTIONS SECTION */}
        <section className="space-y-6">
          <div className="flex items-baseline justify-between px-1">
            <h2 className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-medium">
              Recent Activity log
            </h2>
            <button
              onClick={() => navigate('/transactions')}
              className="text-[11px] uppercase tracking-[0.15em] text-charcoal-black hover:text-medium-concrete transition-colors duration-100 cursor-pointer"
            >
              View Full Archive
            </button>
          </div>

          <div className="border border-grid-line bg-grid-line divide-y divide-grid-line">
            {recentTransactions.length === 0 ? (
              /* EMPTY TRANSACTION STATE */
              <div className="bg-stone-white py-24 flex flex-col items-center justify-center text-center space-y-4">
                <div className="text-[12px] uppercase tracking-[0.25em] text-medium-concrete">
                  No transaction history available
                </div>
                <div className="w-12 h-[1px] bg-grid-line" aria-hidden="true" />
              </div>
            ) : (
              recentTransactions.map((tx) => (
                <button
                  key={tx.id}
                  onClick={() => setSelectedTransaction(tx)}
                  className="w-full text-left bg-stone-white hover:bg-concrete-gray/60 py-6 px-6 md:px-8 transition-colors duration-100 flex items-center justify-between focus:outline-none cursor-pointer"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-3">
                      {tx.type === 'receive' ? (
                        <ArrowDownRight className="w-6 h-6 text-charcoal-black" />
                      ) : (
                        <ArrowUpRight className="w-6 h-6 text-charcoal-black" />
                      )}
                      <div className="text-[14px] font-bold tracking-wide uppercase text-charcoal-black">
                        {tx.type === 'receive' ? tx.sender || 'External Deposit' : tx.recipient || 'Transfer Sent'}
                      </div>
                      <span className="text-[9px] font-extrabold tracking-[0.15em] bg-concrete-gray text-charcoal-black px-2 py-0.5 border border-grid-line uppercase">
                        {tx.type === 'receive' ? 'INCOMING' : 'OUTGOING'}
                      </span>
                    </div>
                    <div className="text-[11px] text-medium-concrete uppercase tracking-wider font-normal">
                      {new Date(tx.date).toLocaleDateString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric'
                      })} — {new Date(tx.date).toLocaleTimeString('en-US', {
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </div>
                  </div>

                  <div className="flex items-center gap-8 md:gap-12">
                    <div className="text-[16px] font-extrabold tracking-tight text-charcoal-black">
                      {tx.type === 'send' ? '-' : '+'}${tx.amount.toFixed(2)}
                    </div>
                    
                    <div className="w-24 flex justify-end">
                      {tx.status === 'completed' ? (
                        <span className="text-[9px] font-bold tracking-[0.18em] uppercase bg-charcoal-black text-stone-white px-3 py-1.5 border border-charcoal-black">
                          Success
                        </span>
                      ) : tx.status === 'failed' ? (
                        <span className="text-[9px] font-bold tracking-[0.18em] uppercase border border-[#8B6B6B] text-[#8B6B6B] px-3 py-1.5 bg-[#8B6B6B]/5">
                          Failed
                        </span>
                      ) : (
                        <span className="text-[9px] font-bold tracking-[0.18em] uppercase bg-medium-concrete text-charcoal-black px-3 py-1.5">
                          Pending
                        </span>
                      )}
                    </div>
                  </div>
                </button>
              ))
            )}
          </div>
        </section>
      </div>

      {walletModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-charcoal-black/35 backdrop-blur-[1px] animate-fade-in">
          <div className="relative w-full max-w-lg bg-stone-white border border-grid-line p-10 md:p-12 shadow-[0_32px_120px_rgba(0,0,0,0.12)] space-y-8 animate-slide-up">
            <div className="flex items-center justify-between border-b border-grid-line pb-6">
              <div className="space-y-1">
                <div className="text-[10px] uppercase tracking-[0.3em] text-medium-concrete font-medium">Payment QR</div>
                <h2 className="text-[20px] font-bold tracking-[0.1em] text-charcoal-black uppercase">Wallet Address</h2>
              </div>
              <button
                onClick={() => setWalletModalOpen(false)}
                className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete hover:text-charcoal-black transition-colors duration-100 focus:outline-none cursor-pointer"
              >
                Close
              </button>
            </div>

            <div className="space-y-6">
              <div className="rounded-none border border-grid-line bg-stone-white p-5">
                <div className="mb-3 text-[11px] uppercase tracking-[0.2em] text-charcoal-black/60">
                  Receiver number
                </div>
                <div className="flex items-center justify-between gap-4">
                  <div className="text-[18px] font-bold tracking-wide text-charcoal-black">
                    {showPhone ? walletAddress : maskedWalletAddress}
                  </div>
                  <button
                    type="button"
                    onClick={() => setShowPhone((prev) => !prev)}
                    className="flex h-10 w-10 items-center justify-center rounded-full border border-grid-line bg-white text-charcoal-black/80 transition hover:bg-stone-white"
                    aria-label={showPhone ? 'Hide phone number' : 'Show phone number'}
                  >
                    {showPhone ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-center rounded-2xl border border-dashed border-grid-line bg-concrete-gray/40 p-6">
                <div className="flex h-40 w-40 items-center justify-center rounded-xl border border-charcoal-black/15 bg-white shadow-inner">
                  <div className="grid h-28 w-28 grid-cols-4 gap-2">
                    {Array.from({ length: 16 }).map((_, index) => (
                      <div
                        key={index}
                        className={`aspect-square rounded-sm ${[0, 1, 4, 5, 8, 9, 12, 13].includes(index) ? 'bg-charcoal-black' : 'bg-charcoal-black/10'}`}
                      />
                    ))}
                  </div>
                </div>
              </div>

              <button
                type="button"
                onClick={handleCopyWallet}
                className="flex w-full items-center justify-center gap-3 border border-charcoal-black bg-charcoal-black px-5 py-3 text-[11px] font-extrabold uppercase tracking-[0.2em] text-stone-white transition hover:bg-concrete-gray hover:text-charcoal-black"
              >
                {copied ? (
                  <>
                    <Check className="h-4 w-4" />
                    Copied
                  </>
                ) : (
                  <>
                    <Copy className="h-4 w-4" />
                    Copy address
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* TRANSACTION DETAIL MODAL */}
      {selectedTransaction && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-charcoal-black/35 backdrop-blur-[1px] animate-fade-in">
          <div className="relative w-full max-w-lg bg-stone-white border border-grid-line p-10 md:p-12 shadow-[0_32px_120px_rgba(0,0,0,0.12)] space-y-10 animate-slide-up">
            
            <div className="flex items-center justify-between border-b border-grid-line pb-6">
              <div className="space-y-1">
                <div className="text-[10px] uppercase tracking-[0.3em] text-medium-concrete font-medium">Record file</div>
                <h2 className="text-[20px] font-bold tracking-[0.1em] text-charcoal-black uppercase">Transaction Details</h2>
              </div>
              <button
                onClick={() => setSelectedTransaction(null)}
                className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete hover:text-charcoal-black transition-colors duration-100 focus:outline-none cursor-pointer"
              >
                Close [esc]
              </button>
            </div>

            <div className="border border-grid-line bg-concrete-gray/15">
              <table className="w-full text-[12px] tracking-wide text-left border-collapse">
                <tbody>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 w-1/3 border-r border-grid-line">Reference code</td>
                    <td className="px-6 py-4 font-mono text-charcoal-black break-all select-all font-medium">{selectedTransaction.referenceCode || selectedTransaction.id}</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Type</td>
                    <td className="px-6 py-4 font-bold uppercase text-charcoal-black">{selectedTransaction.type}</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Sender</td>
                    <td className="px-6 py-4 text-charcoal-black">{selectedTransaction.sender || 'Unknown'}</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Receiver</td>
                    <td className="px-6 py-4 text-charcoal-black">{selectedTransaction.recipient || 'Unknown'}</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Amount</td>
                    <td className="px-6 py-4 font-extrabold text-charcoal-black">${selectedTransaction.amount.toFixed(2)} USD</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Timestamp</td>
                    <td className="px-6 py-4 text-charcoal-black">
                      {new Date(selectedTransaction.date).toLocaleString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit'
                      })}
                    </td>
                  </tr>
                  <tr className={selectedTransaction.message ? "border-b border-grid-line" : ""}>
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Status</td>
                    <td className="px-6 py-4">
                      {selectedTransaction.status === 'completed' ? (
                        <span className="text-[9px] font-bold tracking-[0.18em] uppercase bg-charcoal-black text-stone-white px-3 py-1.5 border border-charcoal-black">Success</span>
                      ) : selectedTransaction.status === 'failed' ? (
                        <span className="text-[9px] font-bold tracking-[0.18em] uppercase border border-[#8B6B6B] text-[#8B6B6B] px-3 py-1.5">Failed</span>
                      ) : (
                        <span className="text-[9px] font-bold tracking-[0.18em] uppercase bg-medium-concrete text-charcoal-black px-3 py-1.5">Pending</span>
                      )}
                    </td>
                  </tr>
                  {selectedTransaction.message && (
                    <tr>
                      <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Message</td>
                      <td className="px-6 py-4 text-charcoal-black italic font-medium">"{selectedTransaction.message}"</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <div className="pt-2">
              <button
                onClick={() => setSelectedTransaction(null)}
                className="w-full h-14 bg-charcoal-black text-stone-white hover:bg-[#2A2A2A] text-[12px] uppercase tracking-[0.3em] font-medium transition-colors duration-100 cursor-pointer"
              >
                Dismiss Details
              </button>
            </div>
          </div>
        </div>
      )}

      <style>{`
        .animate-fade-in {
          animation: fadeIn 0.3s ease-out forwards;
        }
        .animate-slide-up {
          animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        }
        @keyframes fadeIn {
          from { opacity: 0; }
          to { opacity: 1; }
        }
        @keyframes slideUp {
          from { opacity: 0; transform: translateY(16px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </main>
  );
}

