import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import api from '../../api';
import { useAuth } from './AuthContext';

export interface Transaction {
  id: string;
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
  deposit: (amount: number, message: string) => Promise<any>;
  withdraw: (amount: number, message: string) => Promise<any>;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

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

  return {
    id: String(tx.id),
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
      // 1. Fetch balance
      const balanceRes: any = await api.get('/wallets/balance');
      if (balanceRes && balanceRes.data !== undefined) {
        setBalance(Number(balanceRes.data));
      }

      // 2. Fetch history
      const historyRes: any = await api.get('/wallets/history');
      if (historyRes && historyRes.data) {
        const mapped = historyRes.data.map((tx: any) => mapBackendTransaction(tx, user.phone));
        // Sort descending by date/id
        mapped.sort((a: any, b: any) => new Date(b.date).getTime() - new Date(a.date).getTime());
        setTransactions(mapped);
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

  const deposit = async (amount: number, message: string) => {
    try {
      const res = await api.post('/wallets/deposit', {
        amount,
        message
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
