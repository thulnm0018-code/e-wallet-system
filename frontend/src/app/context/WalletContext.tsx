import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import api from '../../api';
import { useAuth } from './AuthContext';

export interface Transaction {
  id: string;
  referenceCode?: string;
  type: 'send' | 'receive' | 'deposit' | 'withdraw';
  amount: number;
  recipient?: string;
  sender?: string;
  date: string;
  status: 'completed' | 'pending' | 'failed';
  message?: string;
}

interface WalletContextType {
  balance: number;
  transactions: Transaction[];
  loading: boolean;
  error: string | null;
  refreshWallet: () => Promise<void>;
  transfer: (receiverPhone: string, amount: number, message: string, otpCode: string) => Promise<any>;
  deposit: (amount: number, message: string, paymentMethod: string) => Promise<any>;
  withdraw: (amount: number, message: string) => Promise<any>;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

const unwrapResponseData = (payload: any): any => {
  if (!payload || typeof payload !== 'object') return payload;
  if (Array.isArray(payload)) return payload;

  if (payload.data !== undefined && payload.data !== null) {
    return unwrapResponseData(payload.data);
  }

  if (Array.isArray(payload.content)) return payload.content;
  if (Array.isArray(payload.items)) return payload.items;
  if (Array.isArray(payload.result)) return payload.result;

  return payload;
};

const obfuscateTransactionReference = (rawId: string | number) => {
  const idString = String(rawId);
  let hash = 0;
  for (let i = 0; i < idString.length; i += 1) {
    hash = (hash << 5) - hash + idString.charCodeAt(i);
    hash |= 0;
  }
  const normalized = (hash >>> 0).toString(36).toUpperCase().padStart(8, '0');
  return `TXN-${normalized}`;
};

const mapBackendTransaction = (tx: any, userPhone: string): Transaction => {
  let type: 'send' | 'receive' | 'deposit' | 'withdraw' = 'send';
  let sender = tx.senderPhone;
  let recipient = tx.receiverPhone;

  if (tx.type === 'DEPOSIT') {
    type = 'receive';
    sender = 'Bank Deposit';
  } else if (tx.type === 'WITHDRAW') {
    type = 'send';
    recipient = 'Bank Withdrawal';
  } else if (tx.type === 'TRANSFER') {
    if (tx.senderPhone === userPhone) {
      type = 'send';
    } else {
      type = 'receive';
    }
  }

  let status: 'completed' | 'pending' | 'failed' = 'completed';
  if (tx.status === 'SUCCESS') status = 'completed';
  else if (tx.status === 'FAILED') status = 'failed';
  else if (tx.status === 'PENDING') status = 'pending';

  const referenceCode = tx.transactionCode?.toString() || obfuscateTransactionReference(tx.id);

  return {
    id: String(tx.id),
    referenceCode,
    type,
    amount: tx.amount,
    sender: sender === 'SYSTEM' ? 'External Deposit' : sender,
    recipient: recipient === 'SYSTEM' ? 'External System' : recipient,
    date: tx.createdAt,
    status,
    message: tx.message
  };
};

export function WalletProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [balance, setBalance] = useState<number>(0.00);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const refreshWallet = async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch wallet info
      const walletRes: any = await api.get('/wallets/me');
      const walletPayload = unwrapResponseData(walletRes);
      const balanceValue = walletPayload?.balance ?? walletPayload?.data?.balance ?? 0;
      setBalance(Number(balanceValue || 0));

      // 2. Fetch history
      const historyRes: any = await api.get('/wallets/history?page=0&size=20');
      const historyPayload = unwrapResponseData(historyRes);
      const historyItems = Array.isArray(historyPayload)
        ? historyPayload
        : historyPayload?.content || historyPayload?.items || [];

      if (Array.isArray(historyItems)) {
        const mapped = historyItems.map((tx: any) => mapBackendTransaction(tx, user.phone));
        // Sort descending by date/id
        mapped.sort((a: any, b: any) => new Date(b.date).getTime() - new Date(a.date).getTime());
        setTransactions(mapped);
      } else {
        setTransactions([]);
      }
    } catch (err: any) {
      console.error('Failed to load wallet data:', err);
      setError(err.response?.data?.message || 'Failed to refresh wallet');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      refreshWallet();
    } else {
      setBalance(0.00);
      setTransactions([]);
    }
  }, [user]);

  const transfer = async (receiverPhone: string, amount: number, message: string, otpCode: string) => {
    try {
      const res = await api.post('/wallets/transfer', {
        receiverPhone,
        amount,
        message,
        otpCode
      });
      await refreshWallet();
      return res;
    } catch (err: any) {
      throw err;
    }
  };

  const deposit = async (amount: number, message: string, paymentMethod: string) => {
    try {
      const res = await api.post('/wallets/deposit', {
        amount,
        message,
        paymentMethod
      });
      await refreshWallet();
      return res;
    } catch (err: any) {
      throw err;
    }
  };

  const withdraw = async (amount: number, message: string) => {
    try {
      const res = await api.post('/wallets/withdraw', {
        amount,
        message
      });
      await refreshWallet();
      return res;
    } catch (err: any) {
      throw err;
    }
  };

  return (
    <WalletContext.Provider value={{ balance, transactions, loading, error, refreshWallet, transfer, deposit, withdraw }}>
      {children}
    </WalletContext.Provider>
  );
}

export function useWallet() {
  const context = useContext(WalletContext);
  if (context === undefined) {
    throw new Error('useWallet must be used within a WalletProvider');
  }
  return context;
}
