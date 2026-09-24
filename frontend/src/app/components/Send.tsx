import { useState, useRef, useEffect } from 'react';
import { useWallet } from '../context/WalletContext';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, AlertCircle, ArrowLeft, ArrowRight, ShieldCheck, Lock, Download, Share2, Home } from 'lucide-react';
import api from '../../api';
import { useAuth } from '../context/AuthContext';

interface MockReceiver {
  phone: string;
  name: string;
  walletId: string;
  status: 'ACTIVE' | 'LOCKED';
}

const mockReceivers: MockReceiver[] = [
  { phone: '0987654321', name: 'KENZO TANGE', walletId: 'WL-7703-1289', status: 'ACTIVE' },
  { phone: '0123456789', name: 'KAZUYO SEJIMA', walletId: 'WL-3301-4491', status: 'LOCKED' },
];


type TransferStep = 'RECIPIENT' | 'AMOUNT' | 'CONFIRMATION' | 'SUCCESS';

export function Send() {
  const { balance, transfer } = useWallet();
  const { user } = useAuth();
  const navigate = useNavigate();

  // Step state
  const [step, setStep] = useState<TransferStep>('RECIPIENT');

  // Input states
  const [phone, setPhone] = useState('');
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');
  const [otp, setOtp] = useState<string[]>(Array(6).fill(''));
  const [displayedOtp, setDisplayedOtp] = useState<string>('');

  // Validation/Error states
  const [validatedReceiver, setValidatedReceiver] = useState<MockReceiver | null>(null);
  const [phoneError, setPhoneError] = useState<string | null>(null);
  const [amountError, setAmountError] = useState<string | null>(null);
  const [systemError, setSystemError] = useState<string | null>(null);
  
  // Simulated state flags
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [txTimestamp, setTxTimestamp] = useState('');
  const [txRef, setTxRef] = useState('');
  const [otpTimeLeft, setOtpTimeLeft] = useState(0);

  // OTP inputs ref for autofocus
  const otpInputRef = useRef<HTMLInputElement | null>(null);
  const profileComplete = Boolean(user?.address?.trim()) && Boolean(user?.dateOfBirth);
  const senderName = user?.name?.trim() || 'Current account';
  const receiptRef = useRef<HTMLDivElement>(null);
  const formatVnd = (value: number) => Math.round(value).toLocaleString('en-US');

  const getReceiptValues = () => {
    const transferAmount = Number(amount || 0);
    const fee = Math.round(transferAmount * 0.002 * 100) / 100;

    return {
      transferAmount,
      fee,
      total: transferAmount + fee,
    };
  };

  const escapeXml = (value: string) => value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');

  const handleSaveReceipt = () => {
    const { transferAmount, fee, total } = getReceiptValues();
    const receiptSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="900" height="780" viewBox="0 0 900 780">
      <rect width="900" height="780" fill="#f7f7f4"/>
      <rect x="50" y="40" width="800" height="700" fill="#ffffff" stroke="#d0d0ca"/>
      <text x="90" y="105" font-family="Arial, sans-serif" font-size="28" font-weight="700" fill="#202020">TRANSFER RECEIPT</text>
      <text x="90" y="145" font-family="Arial, sans-serif" font-size="16" fill="#777">SUCCESSFULLY TRANSFERRED</text>
      <text x="90" y="220" font-family="Arial, sans-serif" font-size="44" font-weight="700" fill="#202020">${escapeXml(formatVnd(transferAmount))} VND</text>
      <line x1="90" y1="260" x2="810" y2="260" stroke="#d0d0ca"/>
      <text x="90" y="315" font-family="Arial, sans-serif" font-size="16" fill="#777">SENDER</text>
      <text x="810" y="315" text-anchor="end" font-family="Arial, sans-serif" font-size="19" font-weight="700" fill="#202020">${escapeXml(senderName)}</text>
      <text x="90" y="365" font-family="Arial, sans-serif" font-size="16" fill="#777">RECEIVER</text>
      <text x="810" y="365" text-anchor="end" font-family="Arial, sans-serif" font-size="19" font-weight="700" fill="#202020">${escapeXml(validatedReceiver?.name || 'Unknown')}</text>
      <text x="90" y="415" font-family="Arial, sans-serif" font-size="16" fill="#777">SERVICE FEE</text>
      <text x="810" y="415" text-anchor="end" font-family="Arial, sans-serif" font-size="19" fill="#202020">${escapeXml(formatVnd(fee))} VND</text>
      <text x="90" y="465" font-family="Arial, sans-serif" font-size="16" fill="#777">TOTAL DEBITED</text>
      <text x="810" y="465" text-anchor="end" font-family="Arial, sans-serif" font-size="19" font-weight="700" fill="#202020">${escapeXml(formatVnd(total))} VND</text>
      <text x="90" y="530" font-family="Arial, sans-serif" font-size="16" fill="#777">REFERENCE</text>
      <text x="810" y="530" text-anchor="end" font-family="Arial, sans-serif" font-size="17" fill="#202020">${escapeXml(txRef)}</text>
      <text x="90" y="575" font-family="Arial, sans-serif" font-size="16" fill="#777">TIME</text>
      <text x="810" y="575" text-anchor="end" font-family="Arial, sans-serif" font-size="17" fill="#202020">${escapeXml(txTimestamp)}</text>
      <text x="90" y="665" font-family="Arial, sans-serif" font-size="15" fill="#777">${escapeXml(note.trim() ? `Message: ${note.trim()}` : 'No message')}</text>
    </svg>`;
    const blob = new Blob([receiptSvg], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${txRef || 'transfer-receipt'}.svg`;
    link.click();
    URL.revokeObjectURL(url);
  };

  const handleShareReceipt = async () => {
    const { transferAmount, total } = getReceiptValues();
    const shareText = `Transfer successful\n${formatVnd(transferAmount)} VND to ${validatedReceiver?.name || 'recipient'}\nTotal debited: ${formatVnd(total)} VND\nReference: ${txRef}`;

    if (navigator.share) {
      await navigator.share({ title: 'Transfer receipt', text: shareText });
      return;
    }

    await navigator.clipboard.writeText(shareText);
  };

  const normalizePhone = (phone?: string | null) => {
    if (!phone) return '';

    const digits = phone.replace(/\D/g, '');

    if (digits.startsWith('84')) {
      return `0${digits.slice(2)}`;
    }

    return digits;
  };

  useEffect(() => {
    if (!profileComplete) {
      navigate('/profile', {
        state: {
          profilePrompt: 'Complete your address and date of birth before sending money.'
        }
      });
    }
  }, [navigate, profileComplete]);

  // Auto-validate phone number as it is typed
  useEffect(() => {
    const cleanPhone = phone.replace(/\D/g, '');
    const currentUserPhone = normalizePhone(user?.phone);

    console.log('INPUT PHONE:', cleanPhone);
    console.log('USER PHONE:', user?.phone);
    console.log('NORMALIZED USER PHONE:', currentUserPhone);

    if (cleanPhone.length === 10) {
      if (cleanPhone === currentUserPhone) {
        setValidatedReceiver(null);
        setPhoneError('SELF_TRANSFER_NOT_ALLOWED');
        return;
      }

      const fetchReceiver = async () => {
        try {
          const response: any = await api.get(
  `/users/phone/${cleanPhone}`
);

const responsePayload = response?.data ?? response;
const userData = responsePayload?.data ?? responsePayload;
console.log('receiver lookup response', response, userData);

if (userData) {

    const receiverStatus =
        userData.walletStatus === 'LOCKED'
            ? 'LOCKED'
            : 'ACTIVE';

    if (receiverStatus === 'LOCKED') {

        setValidatedReceiver(null);
        setPhoneError('RECEIVER_LOCKED');

    } else {

        setValidatedReceiver({
            phone: cleanPhone,
            name: userData.name,
            walletId: userData.walletId ? String(userData.walletId) : 'PENDING',
            status: receiverStatus
        });

        setPhoneError(null);
    }

} else {

    setValidatedReceiver(null);
    setPhoneError('NOT_FOUND');

}
        } catch (err: any) {
          const status = err.response?.status;

          if (status === 404 || status === 400) {
            setValidatedReceiver(null);
            setPhoneError('NOT_FOUND');
          } else {
            setValidatedReceiver(null);
            setPhoneError('LOOKUP_FAILED');
          }
        }
      };

      const delayDebounce = setTimeout(() => {
        fetchReceiver();
      }, 500);
      return () => clearTimeout(delayDebounce);
    } else {
      setValidatedReceiver(null);
      setPhoneError(null);
    }
  }, [phone, user?.phone]);

  // OTP countdown timer
  useEffect(() => {
    if (otpTimeLeft <= 0) return;

    const timer = setTimeout(() => setOtpTimeLeft(otpTimeLeft - 1), 1000);
    return () => clearTimeout(timer);
  }, [otpTimeLeft]);

  // Handle OTP digit changes
  const handleOtpChange = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 6);
    setOtp(Array.from({ length: 6 }, (_, index) => digits[index] || ''));
    setSystemError(null);
  };

  const focusOtpInput = () => otpInputRef.current?.focus();

  // Step navigation helpers
  const handleRecipientNext = () => {
    if (validatedReceiver && phoneError !== 'SELF_TRANSFER_NOT_ALLOWED') {
      setStep('AMOUNT');
    }
  };

  const handleAmountNext = () => {
    const amountNum = parseFloat(amount);
    if (!amount || isNaN(amountNum) || amountNum <= 0) {
      setAmountError('INVALID_AMOUNT');
      return;
    }

    if (amountNum > balance) {
      setAmountError('INSUFFICIENT_BALANCE');
      return;
    }

    setAmountError(null);
    
    // Initiate transfer to generate OTP
    initiateTransfer();
  };

  const initiateTransfer = async () => {
    const cleanPhone = phone.replace(/\D/g, '');
    const amountNum = parseFloat(amount);

    if (phoneError === 'SELF_TRANSFER_NOT_ALLOWED') {
      return;
    }

    try {
      setIsSubmitting(true);
      const response: any = await api.post('/wallets/transfer/initiate', {
        receiverPhone: cleanPhone,
        amount: amountNum,
        message: note.trim() || undefined
      });

      const otpData = response?.data;
      
        // The backend must deliver this OTP through a configured secure channel.
      if (otpData) {
        setSystemError('OTP_DELIVERY_PENDING');
        setOtpTimeLeft(300);
        setStep('CONFIRMATION');
        setOtp(Array(6).fill(''));
        setIsSubmitting(false);
      }
    } catch (err: any) {
      console.error('Failed to initiate transfer:', err);
      setIsSubmitting(false);
      const errMsg = err?.response?.data?.message || '';
      if (typeof errMsg === 'string' && errMsg.startsWith('User status is')) {
        setSystemError(errMsg);
      } else {
        setSystemError('INITIATE_FAILED');
      }
    }
  };

  // Submit transfer action
  const handleConfirmSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const enteredOtp = otp.join('');
    
    if (enteredOtp.length < 6) {
      setSystemError('ENTER_FULL_OTP');
      return;
    }

    setIsSubmitting(true);
    setSystemError(null);

    try {
      const amountNum = parseFloat(amount);
      const cleanPhone = phone.replace(/\D/g, '');
      
      const response: any = await transfer(cleanPhone, amountNum, note.trim(), enteredOtp);
      const txn = response.data;
      
      const refCode = txn?.transactionCode || `TXN-${Math.floor(10000000 + Math.random() * 90000000)}`;
      const timestamp = txn?.createdAt 
        ? new Date(txn.createdAt).toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          })
        : new Date().toLocaleString('en-US');

      setTxRef(refCode);
      setTxTimestamp(timestamp);
      setIsSubmitting(false);
      setStep('SUCCESS');
    } catch (err: any) {
      console.error('Transfer failed:', err);
      setIsSubmitting(false);
      
      const errMsg = err.response?.data?.message || '';
      
      if (errMsg.toLowerCase().includes('insufficient') || errMsg.toLowerCase().includes('balance')) {
        setSystemError('INSUFFICIENT_BALANCE');
      } else if (errMsg.toLowerCase().includes('otp locked')) {
        setSystemError('OTP_LOCKED');
      } else if (typeof errMsg === 'string' && errMsg.startsWith('User status is')) {
        setSystemError(errMsg);
      } else if (errMsg.toLowerCase().includes('otp') || errMsg.toLowerCase().includes('invalid')) {
        setSystemError('INVALID_OTP');
      } else if (errMsg.toLowerCase().includes('conflict') || errMsg.toLowerCase().includes('processing')) {
        setSystemError('DUPLICATE_REQUEST');
      } else {
        setSystemError('DATABASE_FAILURE');
      }
    }
  };

  // --- RENDERING STEPS ---

  // STEP 1: RECIPIENT INPUT SCREEN
  if (step === 'RECIPIENT') {
    return (
      <main className="min-h-screen bg-stone-white text-charcoal-black flex items-center justify-center px-8 py-16">
        <div className="w-full max-w-[600px] space-y-12">
          
          <div className="space-y-4">
            <h1 className="text-[48px] tracking-tight font-black leading-none uppercase">
              Transfer Funds
            </h1>
            <div className="text-[11px] tracking-[0.2em] text-medium-concrete uppercase font-bold">
              Step 1 of 3: Validate Receiver
            </div>
          </div>

          <div className="space-y-10">
            {/* Phone Input Box */}
            <div className="flex flex-col gap-3">
              <label htmlFor="receiver-phone" className="uppercase tracking-[0.2em] text-[11px] font-bold text-charcoal-black/70">
                Receiver Phone Number
              </label>
              <input
                id="receiver-phone"
                type="text"
                maxLength={10}
                placeholder="Enter 10-digit number (e.g. 0987654321)"
                value={phone}
                onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
                className="bg-transparent border-0 border-b border-grid-line focus:border-charcoal-black focus:outline-none px-0 py-4 text-[20px] text-charcoal-black placeholder:text-medium-concrete font-mono tracking-widest transition-colors duration-150"
                autoFocus
              />
            </div>

            {/* Validation Outputs */}
            {validatedReceiver && (
              /* SOLID CONCRETE SLAB BLOCK */
              <div className="bg-concrete-gray border border-grid-line p-8 space-y-6 animate-fade-in">
                <div className="text-[10px] tracking-[0.25em] uppercase text-charcoal-black/60 font-bold flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-charcoal-black" />
                  Receiver Verified
                </div>
                <div className="grid grid-cols-[1fr_auto] gap-y-3 text-[14px]">
                  <div className="font-semibold text-charcoal-black/60 uppercase text-[12px] tracking-wider">Name</div>
                  <div className="font-black text-right uppercase text-black">{validatedReceiver.name}</div> 
                  <div className="font-semibold text-charcoal-black/60 uppercase text-[12px] tracking-wider">Status</div>
                  <div className="font-bold text-right text-[10px] tracking-widest bg-charcoal-black text-stone-white px-2 py-0.5 uppercase border border-charcoal-black w-fit justify-self-end">
                    {validatedReceiver.status}
                  </div>
                </div>
              </div>
            )}

            {phoneError === 'NOT_FOUND' && (
              /* MUTED ERROR LINE */
              <div className="border-b border-[#8B6B6B]/40 py-4 text-left animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-[#8B6B6B] font-bold flex items-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Receiver not found
                </div>
              </div>
            )}

            {phoneError === 'RECEIVER_LOCKED' && (
              /* LOCKED WARNING BLOCK */
              <div className="bg-concrete-gray border border-grid-line p-8 space-y-3 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-charcoal-black font-bold flex items-center gap-2">
                  <Lock className="w-4 h-4" />
                  Receiver wallet unavailable
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  The recipient's wallet has security restrictions active. Please check the address or status.
                </div>
              </div>
            )}

            {phoneError === 'SELF_TRANSFER_NOT_ALLOWED' && (
              <div className="bg-concrete-gray border border-grid-line p-8 space-y-3 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-charcoal-black font-bold flex items-center gap-2">
                  <Lock className="w-4 h-4" />
                  Self-transfer not allowed
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  You cannot send money to your own phone number.
                </div>
              </div>
            )}

            {phoneError === 'LOOKUP_FAILED' && (
              <div className="bg-concrete-gray border border-grid-line p-8 space-y-3 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-charcoal-black font-bold flex items-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Recipient lookup unavailable
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  We could not verify this recipient. Please check your connection and try again.
                </div>
              </div>
            )}

            {/* Navigation buttons */}
            <div className="pt-6 flex gap-4">
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="w-1/2 h-14 border border-grid-line text-charcoal-black hover:bg-concrete-gray text-[12px] uppercase tracking-[0.25em] font-bold transition-colors duration-100 cursor-pointer flex items-center justify-center gap-2"
              >
                <ArrowLeft className="w-4 h-4" /> Cancel
              </button>
              <button
                type="button"
                onClick={handleRecipientNext}
                disabled={!validatedReceiver}
                className={`w-1/2 h-14 text-[12px] uppercase tracking-[0.25em] font-bold transition-all duration-100 flex items-center justify-center gap-2 cursor-pointer ${
                  validatedReceiver 
                    ? 'bg-charcoal-black text-stone-white hover:bg-[#2A2A2A]' 
                    : 'bg-concrete-gray text-charcoal-black/30 border border-grid-line/50 cursor-not-allowed'
                }`}
              >
                Next Step <ArrowRight className="w-4 h-4" />
              </button>
            </div>

          </div>
        </div>
      </main>
    );
  }

  // STEP 2: AMOUNT INPUT SCREEN
  if (step === 'AMOUNT') {
    return (
      <main className="min-h-screen bg-stone-white text-charcoal-black flex items-center justify-center px-8 py-16">
        <div className="w-full max-w-[650px] space-y-10">
          
          <div className="text-center space-y-3">
            <h1 className="text-[12px] tracking-[0.25em] text-medium-concrete uppercase font-bold">
              Step 2 of 3: Transfer Amount
            </h1>
            <div className="text-[13px] tracking-[0.15em] text-charcoal-black font-semibold uppercase">
              Send to: <span className="font-black underline">{validatedReceiver?.name}</span>
            </div>
          </div>

          <div className="space-y-8">
            
            {/* Visual Centered Monolithic Amount input */}
            <div className="relative py-8 flex flex-col items-center justify-center border border-grid-line bg-concrete-gray/15 p-10 space-y-4">
              
              <div className="flex items-center justify-center w-full">
                <span className="text-[36px] md:text-[48px] font-black text-charcoal-black/40 mr-3 select-none font-mono">VND</span>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => {
                    setAmount(e.target.value);
                    setAmountError(null);
                  }}
                  className="w-full max-w-[320px] text-center text-[64px] md:text-[80px] font-black tracking-tighter bg-transparent border-0 outline-none text-charcoal-black font-mono leading-none focus:ring-0"
                  autoFocus
                />
              </div>

              <div className="text-[11px] tracking-[0.2em] text-medium-concrete font-bold uppercase select-none">
                Available balance: {formatVnd(balance)} VND
              </div>
            </div>

            {/* Note / Message input */}
            <div className="flex flex-col gap-2">
              <label htmlFor="transfer-note" className="uppercase tracking-[0.2em] text-[10px] font-bold text-charcoal-black/60">
                Add Description/Message (Optional)
              </label>
              <input
                id="transfer-note"
                type="text"
                placeholder="Enter transfer description message"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                className="bg-transparent border-0 border-b border-grid-line focus:border-charcoal-black focus:outline-none px-0 py-3 text-[14px] text-charcoal-black placeholder:text-medium-concrete transition-colors duration-150"
              />
            </div>

            {/* Monochromatic Insufficient Balance alert block */}
            {amountError === 'INSUFFICIENT_BALANCE' && (
              <div className="bg-concrete-gray border border-grid-line p-8 text-center space-y-2 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Insufficient balance
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  The requested amount of {formatVnd(Number(amount || 0))} VND exceeds your current wallet balance.
                </div>
              </div>
            )}

            {amountError === 'INVALID_AMOUNT' && (
              <div className="border-b border-[#8B6B6B]/40 py-4 text-center animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-[#8B6B6B] font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Enter a valid transfer amount
                </div>
              </div>
            )}

            {/* Navigation buttons */}
            <div className="pt-4 flex gap-4">
              <button
                type="button"
                onClick={() => setStep('RECIPIENT')}
                className="w-1/2 h-14 border border-grid-line text-charcoal-black hover:bg-concrete-gray text-[12px] uppercase tracking-[0.25em] font-bold transition-colors duration-100 cursor-pointer flex items-center justify-center gap-2"
              >
                <ArrowLeft className="w-4 h-4" /> Back
              </button>
              <button
                type="button"
                onClick={handleAmountNext}
                disabled={!amount || parseFloat(amount) <= 0}
                className={`w-1/2 h-14 text-[12px] uppercase tracking-[0.25em] font-bold transition-all duration-100 flex items-center justify-center gap-2 cursor-pointer ${
                  amount && parseFloat(amount) > 0
                    ? 'bg-charcoal-black text-stone-white hover:bg-[#2A2A2A]' 
                    : 'bg-concrete-gray text-charcoal-black/30 border border-grid-line/50 cursor-not-allowed'
                }`}
              >
                Continue <ArrowRight className="w-4 h-4" />
              </button>
            </div>

          </div>
        </div>
      </main>
    );
  }

  // STEP 3: TRANSFER CONFIRMATION SCREEN
  if (step === 'CONFIRMATION') {
    const amountVal = parseFloat(amount);
    const transferFee = Math.round(amountVal * 0.002 * 100) / 100;
    const totalDeduction = amountVal + transferFee;

    return (
      <main className="min-h-screen bg-stone-white text-charcoal-black flex items-center justify-center px-8 py-16">
        <div className="w-full max-w-[600px] space-y-10">
          
          <div className="space-y-3">
            <h1 className="text-[48px] tracking-tight font-black leading-none uppercase">
              Security Verification
            </h1>
            <div className="text-[11px] tracking-[0.2em] text-medium-concrete uppercase font-bold">
              Step 3 of 3: Confirm Transfer
            </div>
          </div>

          <form onSubmit={handleConfirmSubmit} className="space-y-10">
            {/* Clean summary block */}
            <div className="border border-grid-line bg-concrete-gray/15">
              <table className="w-full text-[12px] tracking-wide text-left border-collapse">
                <tbody>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 w-1/3 border-r border-grid-line">Sender</td>
                    <td className="px-6 py-4 font-bold text-charcoal-black uppercase">{senderName} (YOU)</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Receiver</td>
                    <td className="px-6 py-4 text-charcoal-black font-bold uppercase">{validatedReceiver?.name}</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Amount</td>
                    <td className="px-6 py-4 font-extrabold text-charcoal-black">{formatVnd(amountVal)} VND</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">System Fee</td>
                    <td className="px-6 py-4 text-charcoal-black font-medium">{formatVnd(transferFee)} VND</td>
                  </tr>
                  {note.trim() && (
                    <tr className="border-b border-grid-line">
                      <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Message</td>
                      <td className="px-6 py-4 text-charcoal-black font-medium italic">"{note.trim()}"</td>
                    </tr>
                  )}
                  <tr className="bg-concrete-gray/40">
                    <td className="px-6 py-4 font-black uppercase text-charcoal-black border-r border-grid-line">Total Cost</td>
                    <td className="px-6 py-4 font-black text-charcoal-black text-[14px]">{formatVnd(totalDeduction)} VND</td>
                  </tr>
                </tbody>
              </table>
            </div>

            {/* OTP Delivery Status */}
            {systemError === 'OTP_DELIVERY_PENDING' && (
              <div className="bg-concrete-gray border border-grid-line p-6 space-y-4 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-charcoal-black font-bold flex items-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Secure OTP Delivery Required
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  The transfer OTP was generated, but no secure delivery channel is configured for this environment.
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black uppercase tracking-wider font-bold">
                  OTP Expires In: <span className={otpTimeLeft > 60 ? 'text-charcoal-black' : 'text-red-600'}>{Math.floor(otpTimeLeft / 60)}:{String(otpTimeLeft % 60).padStart(2, '0')}</span>
                </div>
              </div>
            )}

            {/* OTP Passcode request */}
            <div className="space-y-4 text-center">
              <label className="uppercase tracking-[0.2em] text-[11px] font-bold text-charcoal-black/70 block">
                Enter 6-Digit OTP Code
              </label>
              
              <div className="relative flex justify-between gap-2 max-w-[360px] mx-auto" onClick={focusOtpInput}>
                <input
                  ref={otpInputRef}
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  value={otp.join('')}
                  onChange={(e) => handleOtpChange(e.target.value)}
                  disabled={isSubmitting}
                  aria-label="6-digit OTP code"
                  className="absolute inset-0 z-10 h-full w-full cursor-text opacity-0"
                />
                {otp.map((digit, idx) => (
                  <div
                    key={idx}
                    className="flex h-14 w-12 items-center justify-center border border-grid-line bg-transparent text-center font-mono text-[20px] font-bold leading-none text-charcoal-black select-none"
                  >{digit}</div>
                ))}
              </div>

            </div>

            {/* Monochromatic failure states (No bright alerts) */}
            {systemError === 'OTP_LOCKED' && (
              <div className="bg-concrete-gray border border-grid-line p-8 text-center space-y-2 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold flex items-center justify-center gap-2">
                  <Lock className="w-4 h-4" />
                  OTP Locked
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  Your OTP has been locked after multiple failed attempts. Please request a new OTP to continue.
                </div>
              </div>
            )}

            {systemError && typeof systemError === 'string' && systemError.startsWith('User status is') && (
              <div className="bg-concrete-gray border border-grid-line p-8 text-center space-y-2 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Account Status
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider font-mono">
                  {systemError}
                </div>
              </div>
            )}

            {systemError === 'DUPLICATE_REQUEST' && (
              <div className="bg-concrete-gray border border-grid-line p-8 text-center space-y-2 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Transaction already processing
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  A duplicate transfer request was detected. Please check your activity log before trying again.
                </div>
              </div>
            )}

            {systemError === 'DATABASE_FAILURE' && (
              <div className="bg-concrete-gray border border-grid-line p-8 text-center space-y-2 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Transaction failed unexpectedly
                </div>
                <div className="text-[11px] leading-relaxed text-charcoal-black/60 uppercase tracking-wider">
                  The host server returned a system network database fault. No funds were debited.
                </div>
              </div>
            )}

            {systemError === 'ENTER_FULL_OTP' && (
              <div className="border-b border-[#8B6B6B]/40 py-4 text-center animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-[#8B6B6B] font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Complete the 6-digit security code
                </div>
              </div>
            )}

            {systemError === 'INVALID_OTP' && (
              <div className="border-b border-[#8B6B6B]/40 py-4 text-center animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.18em] text-[#8B6B6B] font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  You entered the wrong password
                </div>
              </div>
            )}

            {systemError === 'INSUFFICIENT_BALANCE' && (
              <div className="bg-concrete-gray border border-grid-line p-8 text-center space-y-2 animate-fade-in">
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  Insufficient wallet balance
                </div>
              </div>
            )}

            {/* Form actions */}
            <div className="pt-4 flex gap-4">
              <button
                type="button"
                onClick={() => setStep('AMOUNT')}
                disabled={isSubmitting}
                className="w-1/2 h-14 border border-grid-line text-charcoal-black hover:bg-concrete-gray text-[12px] uppercase tracking-[0.25em] font-bold transition-colors duration-100 cursor-pointer flex items-center justify-center gap-2"
              >
                <ArrowLeft className="w-4 h-4" /> Back
              </button>
              <button
                type="submit"
                disabled={isSubmitting || otp.join('').length < 6}
                className={`w-1/2 h-14 text-[12px] uppercase tracking-[0.25em] font-bold transition-all duration-100 flex items-center justify-center gap-2 cursor-pointer ${
                  !isSubmitting && otp.join('').length === 6
                    ? 'bg-charcoal-black text-stone-white hover:bg-[#2A2A2A]' 
                    : 'bg-concrete-gray text-charcoal-black/30 border border-grid-line/50 cursor-not-allowed'
                }`}
              >
                {isSubmitting ? 'Processing...' : 'Authorize Send'}
              </button>
            </div>

          </form>
        </div>
      </main>
    );
  }

  // STEP 4: TRANSFER SUCCESS SCREEN
  if (step === 'SUCCESS') {
    const { transferAmount, fee, total } = getReceiptValues();

    return (
      <main className="min-h-screen bg-stone-white text-charcoal-black px-5 py-8 md:px-8 md:py-12 animate-fade-in overflow-y-auto">
        <div className="mx-auto w-full max-w-[680px] space-y-5">
          <div className="flex items-center justify-between border-b border-grid-line pb-4 text-[10px] tracking-[0.2em] uppercase font-bold text-charcoal-black/50">
            <span>Transfer receipt</span>
            <span className="flex items-center gap-2"><CheckCircle className="h-4 w-4" /> Success</span>
          </div>

          <div ref={receiptRef} className="overflow-hidden border border-charcoal-black bg-stone-white shadow-[0_20px_55px_rgba(0,0,0,0.12)]">
            <div className="flex items-start justify-between gap-6 bg-charcoal-black px-6 py-5 text-stone-white">
              <div>
                <div className="text-[10px] font-bold uppercase tracking-[0.28em] text-stone-white/60">E-Wallet</div>
                <div className="mt-2 text-[18px] font-black uppercase tracking-[0.12em]">Transfer receipt</div>
              </div>
              <div className="flex items-center gap-2 border border-stone-white/30 px-3 py-2 text-[10px] font-bold uppercase tracking-[0.16em]">
                <CheckCircle className="h-4 w-4" /> Completed
              </div>
            </div>

            <div className="border-b border-grid-line bg-concrete-gray/25 px-6 py-7">
              <div className="text-[10px] font-bold uppercase tracking-[0.22em] text-charcoal-black/55">Amount sent</div>
              <div className="mt-2 text-[36px] font-black leading-none tracking-tight md:text-[44px]">{formatVnd(transferAmount)} <span className="text-[18px] tracking-[0.12em] text-charcoal-black/55">VND</span></div>
              <div className="mt-3 text-[12px] uppercase tracking-[0.12em] text-charcoal-black/65">To <span className="font-black text-charcoal-black">{validatedReceiver?.name}</span></div>
            </div>

            <div className="grid grid-cols-1 gap-x-8 gap-y-5 px-6 py-6 text-[12px] sm:grid-cols-2">
              <div><div className="text-[10px] font-bold uppercase tracking-[0.16em] text-charcoal-black/50">From</div><div className="mt-1 font-bold uppercase break-words">{senderName}</div></div>
              <div><div className="text-[10px] font-bold uppercase tracking-[0.16em] text-charcoal-black/50">To</div><div className="mt-1 font-bold uppercase break-words">{validatedReceiver?.name}</div></div>
              <div><div className="text-[10px] font-bold uppercase tracking-[0.16em] text-charcoal-black/50">Service fee</div><div className="mt-1 font-semibold">{formatVnd(fee)} VND</div></div>
              <div><div className="text-[10px] font-bold uppercase tracking-[0.16em] text-charcoal-black/50">Total debited</div><div className="mt-1 font-black">{formatVnd(total)} VND</div></div>
              {note.trim() && <div className="sm:col-span-2"><div className="text-[10px] font-bold uppercase tracking-[0.16em] text-charcoal-black/50">Message</div><div className="mt-1 break-words">{note.trim()}</div></div>}
            </div>

            <div className="flex flex-col gap-2 border-t border-dashed border-grid-line bg-concrete-gray/15 px-6 py-4 text-[10px] uppercase tracking-[0.12em] text-charcoal-black/60 sm:flex-row sm:items-center sm:justify-between">
              <span>{txTimestamp}</span>
              <span className="font-mono font-bold text-charcoal-black break-all">{txRef}</span>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <button type="button" onClick={handleSaveReceipt} className="flex h-12 items-center justify-center gap-2 border border-charcoal-black bg-charcoal-black px-4 text-[11px] font-bold uppercase tracking-[0.15em] text-stone-white transition hover:bg-concrete-gray hover:text-charcoal-black"><Download className="h-4 w-4" /> Save image</button>
            <button type="button" onClick={handleShareReceipt} className="flex h-12 items-center justify-center gap-2 border border-grid-line px-4 text-[11px] font-bold uppercase tracking-[0.15em] transition hover:bg-concrete-gray"><Share2 className="h-4 w-4" /> Share</button>
            <button type="button" onClick={() => navigate('/dashboard')} className="flex h-12 items-center justify-center gap-2 border border-grid-line px-4 text-[11px] font-bold uppercase tracking-[0.15em] transition hover:bg-concrete-gray"><Home className="h-4 w-4" /> Dashboard</button>
          </div>
        </div>
      </main>
    );
  }

  return null;
}
