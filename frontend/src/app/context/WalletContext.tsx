import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

export interface Transaction {
  id: string;
  type: 'send' | 'receive';
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
  addTransaction: (transaction: Omit<Transaction, 'id' | 'date'>) => void;
  updateBalance: (amount: number) => void;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

export function WalletProvider({ children }: { children: ReactNode }) {
  const [balance, setBalance] = useState<number>(() => {
    const saved = localStorage.getItem('wallet_balance');
    return saved ? parseFloat(saved) : 10000.00;
  });

  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    const saved = localStorage.getItem('wallet_transactions');
    if (saved) {
      return JSON.parse(saved);
    }
    return [
      {
        id: '1',
        type: 'receive',
        amount: 5000.00,
        sender: 'SALARY DEPOSIT',
        date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'completed',
        message: 'Monthly payroll transfer'
      },
      {
        id: '2',
        type: 'send',
        amount: 150.00,
        recipient: 'GROCERY STORE',
        date: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'completed',
        message: 'Weekly grocery supplies'
      },
      {
        id: '3',
        type: 'send',
        amount: 75.50,
        recipient: 'UTILITY PAYMENT',
        date: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
        status: 'completed',
        message: 'Electricity bill payment'
      }
    ];
  });

  useEffect(() => {
    localStorage.setItem('wallet_balance', balance.toString());
  }, [balance]);

  useEffect(() => {
    localStorage.setItem('wallet_transactions', JSON.stringify(transactions));
  }, [transactions]);

  const addTransaction = (transaction: Omit<Transaction, 'id' | 'date'>) => {
    const newTransaction: Transaction = {
      ...transaction,
      id: Date.now().toString(),
      date: new Date().toISOString()
    };

    setTransactions(prev => [newTransaction, ...prev]);

    if (transaction.status === 'completed') {
      if (transaction.type === 'send') {
        setBalance(prev => prev - transaction.amount);
      } else {
        setBalance(prev => prev + transaction.amount);
      }
    }
  };

  const updateBalance = (amount: number) => {
    setBalance(amount);
  };

  return (
    <WalletContext.Provider value={{ balance, transactions, addTransaction, updateBalance }}>
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
