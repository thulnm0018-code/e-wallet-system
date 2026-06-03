import { useState, useRef, useEffect } from 'react';
import { useWallet } from '../context/WalletContext';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, AlertCircle, ArrowLeft, ArrowRight, ShieldCheck, Lock } from 'lucide-react';

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
  const { balance, addTransaction } = useWallet();
  const navigate = useNavigate();

  // Step state
  const [step, setStep] = useState<TransferStep>('RECIPIENT');

  // Input states
  const [phone, setPhone] = useState('');
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');
  const [otp, setOtp] = useState<string[]>(Array(6).fill(''));

  // Validation/Error states
  const [validatedReceiver, setValidatedReceiver] = useState<MockReceiver | null>(null);
  const [phoneError, setPhoneError] = useState<string | null>(null);
  const [amountError, setAmountError] = useState<string | null>(null);
  const [systemError, setSystemError] = useState<string | null>(null);
  
  // Simulated state flags
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [txTimestamp, setTxTimestamp] = useState('');
  const [txRef, setTxRef] = useState('');

  // OTP inputs ref for autofocus
  const otpRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Auto-validate phone number as it is typed
  useEffect(() => {
    const cleanPhone = phone.replace(/\D/g, '');
    
    if (cleanPhone.length === 10) {
      const match = mockReceivers.find(r => r.phone === cleanPhone);
      if (match) {
        if (match.status === 'LOCKED') {
          setValidatedReceiver(null);
          setPhoneError('RECEIVER_LOCKED');
        } else {
          setValidatedReceiver(match);
          setPhoneError(null);
        }
      } else {
        setValidatedReceiver(null);
        setPhoneError('NOT_FOUND');
      }
    } else {
      setValidatedReceiver(null);
      setPhoneError(null);
    }
  }, [phone]);

  // Handle OTP digit changes
  const handleOtpChange = (value: string, index: number) => {
    if (isNaN(Number(value))) return;
    
    const newOtp = [...otp];
    newOtp[index] = value.substring(value.length - 1);
    setOtp(newOtp);
    setSystemError(null);

    // Auto-focus next input
    if (value && index < 5) {
      otpRefs.current[index + 1]?.focus();
    }
  };

  // Handle OTP key down (for backspace navigation)
  const handleOtpKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, index: number) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      otpRefs.current[index - 1]?.focus();
    }
  };

  // Step navigation helpers
  const handleRecipientNext = () => {
    if (validatedReceiver) {
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
    setStep('CONFIRMATION');
  };

  // Submit transfer action
  const handleConfirmSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const enteredOtp = otp.join('');
    
    if (enteredOtp.length < 6) {
      setSystemError('ENTER_FULL_OTP');
      return;
    }

    setIsSubmitting(true);
    setSystemError(null);

    // Simulate network delay
    setTimeout(() => {
      // 1. Insufficient balance safety check
      const amountNum = parseFloat(amount);
      if (amountNum > balance) {
        setSystemError('INSUFFICIENT_BALANCE');
        setIsSubmitting(false);
        return;
      }

      // 2. Duplicate Request simulation (OTP: 111111)
      if (enteredOtp === '111111') {
        setSystemError('DUPLICATE_REQUEST');
        setIsSubmitting(false);
        return;
      }

      // 3. Database Failure simulation (OTP: 999999)
      if (enteredOtp === '999999') {
        setSystemError('DATABASE_FAILURE');
        setIsSubmitting(false);
        return;
      }

      // 4. Success Flow
      const refCode = `TXN-${Math.floor(10000000 + Math.random() * 90000000)}`;
      const timestamp = new Date().toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });

      addTransaction({
        type: 'send',
        amount: amountNum,
        recipient: validatedReceiver?.name || 'KENZO TANGE',
        status: 'completed',
        message: note.trim() || undefined
      });

      setTxRef(refCode);
      setTxTimestamp(timestamp);
      setIsSubmitting(false);
      setStep('SUCCESS');
    }, 1500);
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
                  <div className="font-black text-right uppercase text-charcoal-black">{validatedReceiver.name}</div>
                  
                  <div className="font-semibold text-charcoal-black/60 uppercase text-[12px] tracking-wider">Wallet ID</div>
                  <div className="font-mono font-medium text-right text-charcoal-black">{validatedReceiver.walletId}</div>
                  
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
              Send to: <span className="font-black underline">{validatedReceiver?.name}</span> ({validatedReceiver?.walletId})
            </div>
          </div>

          <div className="space-y-8">
            
            {/* Visual Centered Monolithic Amount input */}
            <div className="relative py-8 flex flex-col items-center justify-center border border-grid-line bg-concrete-gray/15 p-10 space-y-4">
              
              <div className="flex items-center justify-center w-full">
                <span className="text-[64px] md:text-[80px] font-black text-charcoal-black/40 mr-1 select-none font-mono">$</span>
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
                <span className="text-[18px] md:text-[24px] font-black text-charcoal-black/60 ml-2 select-none tracking-widest">USD</span>
              </div>

              <div className="text-[11px] tracking-[0.2em] text-medium-concrete font-bold uppercase select-none">
                Available balance: ${balance.toFixed(2)} USD
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
                  The requested amount of ${parseFloat(amount).toFixed(2)} USD exceeds your current wallet balance.
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
    const transferFee = 1.00;
    const amountVal = parseFloat(amount);
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
                    <td className="px-6 py-4 font-bold text-charcoal-black uppercase">ANDO TADAO (YOU)</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Receiver</td>
                    <td className="px-6 py-4 text-charcoal-black font-bold uppercase">
                      {validatedReceiver?.name} ({validatedReceiver?.walletId})
                    </td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Amount</td>
                    <td className="px-6 py-4 font-extrabold text-charcoal-black">${amountVal.toFixed(2)} USD</td>
                  </tr>
                  <tr className="border-b border-grid-line">
                    <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">System Fee</td>
                    <td className="px-6 py-4 text-charcoal-black font-medium">${transferFee.toFixed(2)} USD</td>
                  </tr>
                  {note.trim() && (
                    <tr className="border-b border-grid-line">
                      <td className="px-6 py-4 font-semibold uppercase text-charcoal-black/60 border-r border-grid-line">Message</td>
                      <td className="px-6 py-4 text-charcoal-black font-medium italic">"{note.trim()}"</td>
                    </tr>
                  )}
                  <tr className="bg-concrete-gray/40">
                    <td className="px-6 py-4 font-black uppercase text-charcoal-black border-r border-grid-line">Total Cost</td>
                    <td className="px-6 py-4 font-black text-charcoal-black text-[14px]">${totalDeduction.toFixed(2)} USD</td>
                  </tr>
                </tbody>
              </table>
            </div>

            {/* OTP Passcode request */}
            <div className="space-y-4 text-center">
              <label className="uppercase tracking-[0.2em] text-[11px] font-bold text-charcoal-black/70 block">
                Enter 6-Digit Wallet PIN or OTP Code
              </label>
              
              <div className="flex justify-between gap-2 max-w-[360px] mx-auto">
                {otp.map((digit, idx) => (
                  <input
                    key={idx}
                    type="text"
                    maxLength={1}
                    value={digit}
                    ref={(el) => { otpRefs.current[idx] = el; }}
                    onChange={(e) => handleOtpChange(e.target.value, idx)}
                    onKeyDown={(e) => handleOtpKeyDown(e, idx)}
                    className="w-12 h-14 bg-transparent border border-grid-line focus:border-charcoal-black focus:outline-none text-center text-[20px] font-bold font-mono text-charcoal-black select-all"
                  />
                ))}
              </div>

              <div className="text-[10px] tracking-widest text-medium-concrete uppercase font-medium mt-1">
                Pin hints: any code completes. Use <span className="font-bold font-mono">111111</span> or <span className="font-bold font-mono">999999</span> for failure demos
              </div>
            </div>

            {/* Monochromatic failure states (No bright alerts) */}
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

  // STEP 4: TRANSFER SUCCESS SCREEN (Full-Screen Composition)
  if (step === 'SUCCESS') {
    return (
      <main className="fixed inset-0 z-50 bg-stone-white text-charcoal-black flex flex-col justify-between p-8 md:p-16 animate-fade-in">
        
        {/* Header decoration */}
        <div className="flex justify-between items-center text-[10px] tracking-[0.25em] uppercase font-bold text-charcoal-black/50 border-b border-grid-line pb-6">
          <span>Receipt reference file</span>
          <span>System verified secure</span>
        </div>

        {/* Core receipt visual composition */}
        <div className="flex-1 flex flex-col items-center justify-center space-y-12 max-w-[650px] mx-auto w-full py-12">
          
          <div className="w-20 h-20 bg-charcoal-black flex items-center justify-center">
            <CheckCircle className="w-10 h-10 text-stone-white" strokeWidth={1.5} />
          </div>

          <div className="text-center space-y-4">
            <div className="text-[12px] uppercase tracking-[0.3em] font-extrabold text-charcoal-black/50">
              Successfully Transferred
            </div>
            
            {/* Extremely bold amount display */}
            <div className="text-[64px] md:text-[88px] font-black text-charcoal-black tracking-tighter leading-none font-mono">
              -${parseFloat(amount).toFixed(2)}
            </div>
            
            <div className="text-[20px] font-bold text-charcoal-black tracking-wide uppercase">
              To <span className="underline">{validatedReceiver?.name}</span>
            </div>
          </div>

          {/* Receipt details list */}
          <div className="w-full border border-grid-line bg-concrete-gray/15">
            <table className="w-full text-[11px] uppercase tracking-[0.1em] text-left border-collapse">
              <tbody>
                <tr className="border-b border-grid-line">
                  <td className="px-6 py-4 font-semibold text-charcoal-black/60 w-1/3 border-r border-grid-line">Receiver Wallet</td>
                  <td className="px-6 py-4 font-mono font-medium text-charcoal-black">{validatedReceiver?.walletId}</td>
                </tr>
                <tr className="border-b border-grid-line">
                  <td className="px-6 py-4 font-semibold text-charcoal-black/60 border-r border-grid-line">Timestamp</td>
                  <td className="px-6 py-4 text-charcoal-black font-medium">{txTimestamp}</td>
                </tr>
                <tr>
                  <td className="px-6 py-4 font-semibold text-charcoal-black/60 border-r border-grid-line">Reference Code</td>
                  <td className="px-6 py-4 font-mono text-charcoal-black break-all font-semibold select-all">{txRef}</td>
                </tr>
              </tbody>
            </table>
          </div>

        </div>

        {/* Big centered dashboard return button */}
        <div className="w-full max-w-[650px] mx-auto border-t border-grid-line pt-6">
          <button
            onClick={() => navigate('/dashboard')}
            className="w-full h-16 bg-charcoal-black text-stone-white hover:bg-[#2A2A2A] text-[12px] uppercase tracking-[0.3em] font-bold transition-colors duration-100 cursor-pointer flex items-center justify-center"
          >
            Return Dashboard
          </button>
        </div>

      </main>
    );
  }

  return null;
}
