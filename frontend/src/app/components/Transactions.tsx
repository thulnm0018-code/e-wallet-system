import { useWallet, Transaction } from '../context/WalletContext';
import { ArrowUpRight, ArrowDownLeft, Search, X, Printer, ArrowRight, SlidersHorizontal, RotateCcw } from 'lucide-react';
import { useState, useMemo } from 'react';

export function Transactions() {
  const { transactions } = useWallet();

  // Filters State
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<'all' | 'send' | 'receive'>('all');
  const [statusFilter, setStatusFilter] = useState<'all' | 'completed' | 'pending' | 'failed'>('all');
  const [datePreset, setDatePreset] = useState<'all' | 'today' | '7days' | '30days' | 'custom'>('all');
  const [customStartDate, setCustomStartDate] = useState('');
  const [customEndDate, setCustomEndDate] = useState('');

  // Selected Transaction for Drawer Detail Panel
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);

  // Toggle Filters Visibility
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(true);

  // Reset all filters to default
  const handleResetFilters = () => {
    setSearchTerm('');
    setTypeFilter('all');
    setStatusFilter('all');
    setDatePreset('all');
    setCustomStartDate('');
    setCustomEndDate('');
  };

  // Derive Category based on recipient/sender
  const getCategory = (t: Transaction) => {
    const desc = (t.type === 'send' ? t.recipient : t.sender) || '';
    const upper = desc.toUpperCase();
    if (upper.includes('SALARY') || upper.includes('PAYROLL')) return 'INCOME';
    if (upper.includes('GROCERY') || upper.includes('STORE') || upper.includes('SUPERMARKET')) return 'RETAIL & GROCERY';
    if (upper.includes('UTILITY') || upper.includes('POWER') || upper.includes('WATER') || upper.includes('ELECTRIC')) return 'UTILITIES';
    if (upper.includes('COFFEE') || upper.includes('CAFE') || upper.includes('RESTAURANT')) return 'DINING';
    if (upper.includes('TRANSFER') || upper.includes('FRIEND') || upper.includes('FAMILY')) return 'PEER TRANSFER';
    return 'GENERAL LEDGER';
  };

  const formatTransactionReference = (transaction: Transaction) => {
    if (transaction.referenceCode) {
      return transaction.referenceCode;
    }
    const parsedId = Number(transaction.id);
    if (!Number.isNaN(parsedId)) {
      return `TXN-${(parsedId % 9000 + 1000).toString().padStart(4, '0')}`;
    }
    return transaction.id;
  };

  // 1. Filter Transactions dynamically in real-time
  const filteredTransactions = useMemo(() => {
    return transactions.filter(t => {
      // Search term filter
      if (searchTerm.trim() !== '') {
        const query = searchTerm.toLowerCase();
        const matchesSender = t.sender?.toLowerCase().includes(query);
        const matchesRecipient = t.recipient?.toLowerCase().includes(query);
        const matchesMessage = t.message?.toLowerCase().includes(query);
        const matchesReference = t.referenceCode?.toLowerCase().includes(query);
        const matchesId = t.id.toLowerCase().includes(query);
        const matchesAmount = t.amount.toFixed(2).includes(query);
        
        if (!matchesSender && !matchesRecipient && !matchesMessage && !matchesReference && !matchesId && !matchesAmount) {
          return false;
        }
      }

      // Transaction Type filter
      if (typeFilter !== 'all' && t.type !== typeFilter) {
        return false;
      }

      // Status filter
      if (statusFilter !== 'all' && t.status !== statusFilter) {
        return false;
      }

      // Date Range filter
      const tDate = new Date(t.date);
      const now = new Date();
      if (datePreset === 'today') {
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        if (tDate < today) return false;
      } else if (datePreset === '7days') {
        const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        if (tDate < sevenDaysAgo) return false;
      } else if (datePreset === '30days') {
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        if (tDate < thirtyDaysAgo) return false;
      } else if (datePreset === 'custom') {
        if (customStartDate) {
          const start = new Date(customStartDate);
          start.setHours(0, 0, 0, 0);
          if (tDate < start) return false;
        }
        if (customEndDate) {
          const end = new Date(customEndDate);
          end.setHours(23, 59, 59, 999);
          if (tDate > end) return false;
        }
      }

      return true;
    });
  }, [transactions, searchTerm, typeFilter, statusFilter, datePreset, customStartDate, customEndDate]);

  // 2. Compute Ledger Summary Metrics dynamically
  const metrics = useMemo(() => {
    let totalVolume = 0;
    let netFlow = 0;
    filteredTransactions.forEach(t => {
      totalVolume += t.amount;
      if (t.type === 'receive') {
        netFlow += t.amount;
      } else {
        netFlow -= t.amount;
      }
    });
    return {
      totalVolume,
      netFlow,
      count: filteredTransactions.length
    };
  }, [filteredTransactions]);

  return (
    <div className="max-w-[1440px] mx-auto px-8 py-16 space-y-12 no-print relative">
      {/* Self-contained Print Styles */}
      <style dangerouslySetInnerHTML={{ __html: `
        @media print {
          body {
            background: #FFFFFF !important;
            color: #1A1A1A !important;
            font-family: monospace !important;
          }
          .no-print {
            display: none !important;
          }
          .print-only {
            display: block !important;
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: auto;
            padding: 40px !important;
            background: #FFFFFF !important;
            border: none !important;
          }
        }
      `}} />

      {/* PAGE HEADER: Monolithic typography */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 border-b border-grid-line pb-8">
        <div className="space-y-2">
          <div className="text-[12px] uppercase tracking-[0.25em] text-medium-concrete font-medium">FINANCIAL RECORD LEDGER</div>
          <h1 className="text-[48px] tracking-tight font-extrabold text-charcoal-black leading-none uppercase">
            TRANSACTION HISTORY
          </h1>
        </div>
        <div className="flex gap-4">
          <button
            onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
            className="flex items-center gap-2 border border-charcoal-black px-6 py-3 text-[12px] uppercase tracking-[0.15em] font-bold rounded-none hover:bg-concrete-gray transition-colors duration-100 cursor-pointer"
          >
            <SlidersHorizontal className="w-4 h-4" />
            {showAdvancedFilters ? "HIDE FILTERS" : "SHOW FILTERS"}
          </button>
          <button
            onClick={handleResetFilters}
            className="flex items-center gap-2 border border-grid-line px-6 py-3 text-[12px] uppercase tracking-[0.15em] font-bold rounded-none hover:bg-concrete-gray transition-colors duration-100 cursor-pointer text-medium-concrete hover:text-charcoal-black"
          >
            <RotateCcw className="w-4 h-4" />
            RESET
          </button>
        </div>
      </div>

      {/* LEDGER SUMMARY STATS BAR: Geometric block layouts, 0px border-radius, pure monochromatic */}
      <div className="grid grid-cols-1 md:grid-cols-3 border border-grid-line divide-y md:divide-y-0 md:divide-x divide-grid-line bg-stone-white">
        <div className="p-8 flex flex-col justify-between h-36">
          <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-medium">TOTAL DISPLAYED VOLUME</span>
          <div className="text-[32px] font-extrabold tracking-tight text-charcoal-black font-mono">
            ${metrics.totalVolume.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>
        <div className="p-8 flex flex-col justify-between h-36">
          <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-medium">NET FLOW DIRECTION</span>
          <div className={`text-[32px] font-extrabold tracking-tight font-mono ${metrics.netFlow >= 0 ? 'text-charcoal-black' : 'text-charcoal-black'}`}>
            {metrics.netFlow >= 0 ? '+' : '-'}${Math.abs(metrics.netFlow).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>
        <div className="p-8 flex flex-col justify-between h-36">
          <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-medium">LEDGER ITEM COUNT</span>
          <div className="text-[32px] font-extrabold tracking-tight text-charcoal-black font-mono">
            {metrics.count} <span className="text-[14px] font-bold text-medium-concrete uppercase tracking-widest ml-1">TXNS</span>
          </div>
        </div>
      </div>

      {/* ADVANCED FILTER SYSTEM (Ando Tadao / Zen Concrete style) */}
      {showAdvancedFilters && (
        <div className="border border-grid-line p-8 bg-stone-white space-y-8 animate-fade-in">
          {/* Top Row: Minimalist Search Box */}
          <div className="flex flex-col space-y-2 w-full">
            <label className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">Search Ledger Sheet</label>
            <div className="relative flex items-center">
              <Search className="absolute left-0 w-5 h-5 text-charcoal-black/50" strokeWidth={1.5} />
              <input
                type="text"
                placeholder="TYPE COUNTERPARTY NAME, LEDGER ID, MEMO KEYWORDS OR AMOUNT..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full border-b border-grid-line focus:border-charcoal-black bg-transparent pl-8 pr-8 py-4 text-[14px] uppercase tracking-[0.08em] outline-none text-charcoal-black transition-colors duration-150 placeholder-charcoal-black/30 font-medium"
              />
              {searchTerm && (
                <button
                  onClick={() => setSearchTerm('')}
                  className="absolute right-0 p-2 text-charcoal-black/40 hover:text-charcoal-black transition-colors cursor-pointer"
                >
                  <X className="w-5 h-5" />
                </button>
              )}
            </div>
          </div>

          {/* Middle Row: Date Range Preset & Custom Date Fields */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="flex flex-col space-y-2 w-full">
              <label className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">Date Window Preset</label>
              <div className="flex border border-grid-line divide-x divide-grid-line bg-stone-white">
                {(['all', 'today', '7days', '30days', 'custom'] as const).map((preset) => (
                  <button
                    key={preset}
                    onClick={() => setDatePreset(preset)}
                    className={`flex-1 py-3.5 text-[11px] uppercase tracking-[0.15em] font-bold transition-all duration-100 cursor-pointer rounded-none ${
                      datePreset === preset
                        ? 'bg-charcoal-black text-stone-white'
                        : 'text-charcoal-black hover:bg-concrete-gray'
                    }`}
                  >
                    {preset === 'all' ? 'All Time' : preset === '7days' ? '7 Days' : preset === '30days' ? '30 Days' : preset}
                  </button>
                ))}
              </div>
            </div>

            {/* Custom Range Fields (visible only when 'custom' is selected) */}
            {datePreset === 'custom' && (
              <div className="grid grid-cols-2 gap-6">
                <div className="flex flex-col space-y-2">
                  <label className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">Custom From</label>
                  <input
                    type="date"
                    value={customStartDate}
                    onChange={(e) => setCustomStartDate(e.target.value)}
                    className="w-full border-b border-grid-line focus:border-charcoal-black bg-transparent py-3 text-[13px] uppercase tracking-wider outline-none text-charcoal-black transition-colors duration-150 font-mono font-bold"
                  />
                </div>
                <div className="flex flex-col space-y-2">
                  <label className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">Custom To</label>
                  <input
                    type="date"
                    value={customEndDate}
                    onChange={(e) => setCustomEndDate(e.target.value)}
                    className="w-full border-b border-grid-line focus:border-charcoal-black bg-transparent py-3 text-[13px] uppercase tracking-wider outline-none text-charcoal-black transition-colors duration-150 font-mono font-bold"
                  />
                </div>
              </div>
            )}
          </div>

          {/* Bottom Row: Transaction Type & Status Selectors */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="flex flex-col space-y-2 w-full">
              <label className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">Transaction Type</label>
              <div className="flex border border-grid-line divide-x divide-grid-line bg-stone-white">
                {(['all', 'send', 'receive'] as const).map((type) => (
                  <button
                    key={type}
                    onClick={() => setTypeFilter(type)}
                    className={`flex-1 py-3.5 text-[11px] uppercase tracking-[0.15em] font-bold transition-all duration-100 cursor-pointer rounded-none ${
                      typeFilter === type
                        ? 'bg-charcoal-black text-stone-white'
                        : 'text-charcoal-black hover:bg-concrete-gray'
                    }`}
                  >
                    {type === 'all' ? 'All Ledger Items' : type === 'send' ? 'Debit (Sent)' : 'Credit (Received)'}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex flex-col space-y-2 w-full">
              <label className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">Settlement Status</label>
              <div className="flex border border-grid-line divide-x divide-grid-line bg-stone-white overflow-x-auto">
                {(['all', 'completed', 'pending', 'failed'] as const).map((status) => (
                  <button
                    key={status}
                    onClick={() => setStatusFilter(status)}
                    className={`flex-1 py-3.5 text-[11px] uppercase tracking-[0.15em] font-bold min-w-[80px] transition-all duration-100 cursor-pointer rounded-none ${
                      statusFilter === status
                        ? 'bg-charcoal-black text-stone-white'
                        : 'text-charcoal-black hover:bg-concrete-gray'
                    }`}
                  >
                    {status}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* GEOMETRIC LEDGER TABLE: Sharp corners, monolithic lines */}
      <div className="border border-grid-line bg-stone-white">
        {/* Table Header: Pure Grayscale Concrete */}
        <div className="hidden md:grid grid-cols-[80px_1fr_200px_150px_180px] gap-6 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
          <div>FLOW</div>
          <div>COUNTERPARTY / DETAILS</div>
          <div>TRANSACTION DATE</div>
          <div>STATUS</div>
          <div className="text-right">LEDGER AMOUNT</div>
        </div>

        {/* Table Body */}
        {filteredTransactions.length === 0 ? (
          <div className="py-24 text-center text-charcoal-black/40 tracking-[0.15em] text-[13px] uppercase font-bold bg-stone-white">
            NO RECORDED TRANSACTIONS MATCHED FILTERS
          </div>
        ) : (
          filteredTransactions.map((transaction, index) => {
            const category = getCategory(transaction);
            const isDebit = transaction.type === 'send';
            
            return (
              <div
                key={transaction.id}
                onClick={() => setSelectedTransaction(transaction)}
                className={`grid grid-cols-1 md:grid-cols-[80px_1fr_200px_150px_180px] gap-4 md:gap-6 p-6 hover:bg-[#EAEAEA]/60 transition-colors duration-100 cursor-pointer items-center text-[14px] ${
                  index < filteredTransactions.length - 1 ? 'border-b border-grid-line' : ''
                }`}
              >
                {/* 1. Geometric Flow Block */}
                <div className="flex md:block items-center justify-between">
                  <div className="w-12 h-12 bg-charcoal-black flex items-center justify-center rounded-none border border-charcoal-black">
                    {isDebit ? (
                      <ArrowUpRight className="w-5 h-5 text-stone-white" strokeWidth={1.5} />
                    ) : (
                      <ArrowDownLeft className="w-5 h-5 text-stone-white" strokeWidth={1.5} />
                    )}
                  </div>
                  <span className="md:hidden text-[11px] uppercase tracking-wider font-bold text-medium-concrete font-mono">
                    REF: {formatTransactionReference(transaction)}
                  </span>
                </div>

                {/* 2. Counterparty & Details */}
                <div className="space-y-1">
                  <div className="font-bold text-charcoal-black uppercase tracking-wide">
                    {isDebit ? transaction.recipient : transaction.sender}
                  </div>
                  <div className="flex flex-wrap gap-2 items-center text-[11px] uppercase tracking-wider text-charcoal-black/50">
                    <span className="font-semibold">{category}</span>
                    <span className="w-1.5 h-1.5 bg-grid-line rounded-none" />
                    <span className="font-mono">REF: {formatTransactionReference(transaction)}</span>
                  </div>
                </div>

                {/* 3. Transaction Date & Time */}
                <div className="flex flex-row md:flex-col justify-between md:justify-center gap-2 md:gap-0">
                  <span className="md:hidden text-[11px] uppercase tracking-wider text-medium-concrete">DATE:</span>
                  <div className="text-[13px] tracking-wide text-charcoal-black uppercase">
                    {new Date(transaction.date).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                      year: 'numeric'
                    })}
                  </div>
                  <div className="text-[11px] text-charcoal-black/40 font-mono hidden md:block">
                    {new Date(transaction.date).toLocaleTimeString('en-US', {
                      hour: '2-digit',
                      minute: '2-digit',
                      hour12: true
                    })}
                  </div>
                </div>

                {/* 4. Settlement Status badge */}
                <div className="flex flex-row md:flex-col justify-between md:justify-center gap-2 md:gap-0">
                  <span className="md:hidden text-[11px] uppercase tracking-wider text-medium-concrete">STATUS:</span>
                  <div className="flex items-center gap-2">
                    <span
                      className={`w-2.5 h-2.5 rounded-none ${
                        transaction.status === 'completed'
                          ? 'bg-success'
                          : transaction.status === 'pending'
                          ? 'bg-warning'
                          : 'bg-error'
                      }`}
                    />
                    <span
                      className={`text-[11px] uppercase tracking-[0.15em] font-bold ${
                        transaction.status === 'completed'
                          ? 'text-success'
                          : transaction.status === 'pending'
                          ? 'text-warning'
                          : 'text-error'
                      }`}
                    >
                      {transaction.status}
                    </span>
                  </div>
                </div>

                {/* 5. Ledger Amount */}
                <div className="flex flex-row md:flex-col justify-between md:justify-center md:items-end gap-2 md:gap-0 border-t border-grid-line/50 pt-4 md:pt-0 md:border-0">
                  <span className="md:hidden text-[11px] uppercase tracking-wider text-medium-concrete font-bold">AMOUNT:</span>
                  <div className="text-[18px] font-extrabold tracking-tight text-charcoal-black font-mono">
                    {isDebit ? '-' : '+'}${transaction.amount.toFixed(2)}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* TRANSACTION DETAIL SIDE PANEL (Drawer/Sheet - Slide-in) */}
      {selectedTransaction && (
        <>
          {/* Backdrop (Darkened Grayscale Overlay, minimal fade) */}
          <div
            onClick={() => setSelectedTransaction(null)}
            className="fixed inset-0 bg-charcoal-black/40 z-40 transition-opacity duration-150 ease-out"
          />

          {/* Right Side Sliding Drawer Panel */}
          <div className="fixed right-0 top-0 bottom-0 w-full md:w-[480px] bg-stone-white border-l border-grid-line z-50 p-10 flex flex-col justify-between shadow-none transition-transform duration-150 ease-out transform translate-x-0 animate-slide-in">
            
            {/* Top Bar inside Drawer */}
            <div className="space-y-8 flex-1 overflow-y-auto pr-2">
              <div className="flex justify-between items-center border-b border-grid-line pb-6">
                <div>
                  <div className="text-[10px] uppercase tracking-[0.25em] text-medium-concrete font-medium">ARCHIVAL DOCUMENT</div>
                  <div className="text-[12px] uppercase tracking-[0.15em] font-bold text-charcoal-black">LEDGER SHEET DETAIL</div>
                </div>
                <button
                  onClick={() => setSelectedTransaction(null)}
                  className="w-10 h-10 border border-charcoal-black hover:bg-concrete-gray flex items-center justify-center text-charcoal-black transition-colors duration-100 cursor-pointer rounded-none"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Monolithic Bold Amount Display */}
              <div className="bg-concrete-gray/30 p-8 border border-grid-line flex flex-col justify-center items-center space-y-4 rounded-none relative overflow-hidden">
                <span className="text-[11px] uppercase tracking-[0.25em] text-medium-concrete font-bold">TRANSACTED AMOUNT</span>
                <div className="text-[42px] font-black tracking-tight text-charcoal-black font-mono leading-none">
                  {selectedTransaction.type === 'send' ? '-' : '+'}${selectedTransaction.amount.toFixed(2)}
                </div>
                
                {/* Diagonal Brutalist Status Stamp */}
                <div className="mt-2 border-2 border-dashed px-4 py-1.5 uppercase text-[12px] tracking-[0.2em] font-black select-none rounded-none rotate-[-2deg]"
                  style={{
                    borderColor: selectedTransaction.status === 'completed' ? '#6B6B5A' : selectedTransaction.status === 'pending' ? '#8B8371' : '#8B6B6B',
                    color: selectedTransaction.status === 'completed' ? '#6B6B5A' : selectedTransaction.status === 'pending' ? '#8B8371' : '#8B6B6B',
                  }}
                >
                  STAMP: {selectedTransaction.status}
                </div>
              </div>

              {/* Architectural Flow Diagram */}
              <div className="space-y-3">
                <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold block">TRANSACTION FLOW DIRECTION</span>
                <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-4 border border-grid-line p-5 bg-stone-white rounded-none text-center">
                  <div className="space-y-1">
                    <div className="text-[10px] text-medium-concrete uppercase tracking-wider font-bold">SENDER</div>
                    <div className="text-[12px] font-bold text-charcoal-black uppercase truncate">
                      {selectedTransaction.sender || 'YOU'}
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-medium-concrete" />
                  <div className="space-y-1">
                    <div className="text-[10px] text-medium-concrete uppercase tracking-wider font-bold">RECIPIENT</div>
                    <div className="text-[12px] font-bold text-charcoal-black uppercase truncate">
                      {selectedTransaction.recipient || 'YOU'}
                    </div>
                  </div>
                </div>
              </div>

              {/* Structural Ledger Fields */}
              <div className="space-y-3">
                <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold block">TECHNICAL SPECIFICATION</span>
                <div className="border border-grid-line bg-stone-white divide-y divide-grid-line rounded-none text-[12px]">
                  <div className="grid grid-cols-[140px_1fr] p-3.5 gap-4">
                    <span className="uppercase text-medium-concrete tracking-wider font-medium">REFERENCE CODE</span>
                    <span className="font-mono text-charcoal-black font-bold uppercase truncate">{formatTransactionReference(selectedTransaction)}</span>
                  </div>
                  <div className="grid grid-cols-[140px_1fr] p-3.5 gap-4">
                    <span className="uppercase text-medium-concrete tracking-wider font-medium">SETTLEMENT TYPE</span>
                    <span className="text-charcoal-black font-bold uppercase">
                      {selectedTransaction.type === 'send' ? 'DEBIT (SENT FUNDS)' : 'CREDIT (RECEIVED FUNDS)'}
                    </span>
                  </div>
                  <div className="grid grid-cols-[140px_1fr] p-3.5 gap-4">
                    <span className="uppercase text-medium-concrete tracking-wider font-medium">LEDGER CATEGORY</span>
                    <span className="text-charcoal-black font-bold uppercase">{getCategory(selectedTransaction)}</span>
                  </div>
                  <div className="grid grid-cols-[140px_1fr] p-3.5 gap-4">
                    <span className="uppercase text-medium-concrete tracking-wider font-medium">TIMESTAMP</span>
                    <span className="text-charcoal-black font-medium font-mono uppercase">
                      {new Date(selectedTransaction.date).toLocaleString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                        hour12: true
                      })}
                    </span>
                  </div>
                  <div className="grid grid-cols-[140px_1fr] p-3.5 gap-4">
                    <span className="uppercase text-medium-concrete tracking-wider font-medium">MEMO MESSAGE</span>
                    <span className="text-charcoal-black font-bold uppercase italic">
                      {selectedTransaction.message || "NO EXTRA CORRESPONDENCE RECORDED"}
                    </span>
                  </div>
                  <div className="grid grid-cols-[140px_1fr] p-3.5 gap-4">
                    <span className="uppercase text-medium-concrete tracking-wider font-medium">REFERENCE CODE</span>
                    <span className="font-mono text-charcoal-black font-bold text-medium-concrete">
                      {formatTransactionReference(selectedTransaction)}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Bottom Actions footer (rectangular, monolithic buttons) */}
            <div className="flex gap-4 border-t border-grid-line pt-8 mt-6">
              <button
                onClick={() => window.print()}
                className="flex-1 flex items-center justify-center gap-2 bg-charcoal-black hover:bg-concrete-gray hover:text-charcoal-black border border-charcoal-black text-stone-white py-4 text-[12px] uppercase tracking-[0.2em] font-extrabold transition-colors duration-100 rounded-none cursor-pointer"
              >
                <Printer className="w-4 h-4" />
                PRINT RECORD
              </button>
              <button
                onClick={() => setSelectedTransaction(null)}
                className="flex-1 bg-concrete-gray hover:bg-charcoal-black hover:text-stone-white text-charcoal-black border border-grid-line py-4 text-[12px] uppercase tracking-[0.2em] font-extrabold transition-colors duration-100 rounded-none cursor-pointer"
              >
                CLOSE SHEET
              </button>
            </div>

          </div>

          {/* PRINT-ONLY RECEIPT SLIP CARD (Hidden on screen, visible only during print) */}
          <div className="hidden print-only space-y-12">
            <div className="border-4 border-double border-charcoal-black p-8 text-center space-y-8">
              <div className="space-y-2">
                <div className="text-[20px] font-black tracking-widest font-mono">E-WALLET ARCHIVAL LEDGER</div>
                <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black/60">WL-8802-9901 ── TOKYO DIVISION</div>
              </div>

              <div className="border-y border-dashed border-charcoal-black py-6 text-center space-y-2">
                <div className="text-[11px] uppercase tracking-[0.2em] text-charcoal-black/50">TRANSACTED AMOUNT</div>
                <div className="text-[36px] font-black tracking-tight font-mono">
                  {selectedTransaction.type === 'send' ? '-' : '+'}${selectedTransaction.amount.toFixed(2)}
                </div>
                <div className="text-[12px] font-bold uppercase tracking-wider font-mono">
                  STATUS: {selectedTransaction.status.toUpperCase()}
                </div>
              </div>

              <table className="w-full text-left font-mono text-[13px] border-collapse">
                <tbody>
                  <tr className="border-b border-dashed border-charcoal-black/30">
                    <td className="py-2.5 font-bold uppercase">REFERENCE CODE:</td>
                    <td className="py-2.5 text-right uppercase">{formatTransactionReference(selectedTransaction)}</td>
                  </tr>
                  <tr className="border-b border-dashed border-charcoal-black/30">
                    <td className="py-2.5 font-bold uppercase">TIMESTAMP:</td>
                    <td className="py-2.5 text-right uppercase">{new Date(selectedTransaction.date).toISOString().replace('T', ' ').substring(0, 19)}</td>
                  </tr>
                  <tr className="border-b border-dashed border-charcoal-black/30">
                    <td className="py-2.5 font-bold uppercase">FLOW TYPE:</td>
                    <td className="py-2.5 text-right uppercase">{selectedTransaction.type === 'send' ? 'DEBIT (SENT)' : 'CREDIT (RECEIVED)'}</td>
                  </tr>
                  <tr className="border-b border-dashed border-charcoal-black/30">
                    <td className="py-2.5 font-bold uppercase">SENDER:</td>
                    <td className="py-2.5 text-right uppercase">{selectedTransaction.sender || 'YOU'}</td>
                  </tr>
                  <tr className="border-b border-dashed border-charcoal-black/30">
                    <td className="py-2.5 font-bold uppercase">RECIPIENT:</td>
                    <td className="py-2.5 text-right uppercase">{selectedTransaction.recipient || 'YOU'}</td>
                  </tr>
                  <tr className="border-b border-dashed border-charcoal-black/30">
                    <td className="py-2.5 font-bold uppercase">MEMO:</td>
                    <td className="py-2.5 text-right uppercase">{selectedTransaction.message || "N/A"}</td>
                  </tr>
                  <tr>
                    <td className="py-2.5 font-bold uppercase">REF CODE:</td>
                    <td className="py-2.5 text-right uppercase">{formatTransactionReference(selectedTransaction)}</td>
                  </tr>
                </tbody>
              </table>

              <div className="border-t border-dashed border-charcoal-black pt-6 text-center text-[10px] uppercase tracking-[0.25em] text-charcoal-black/50">
                THANK YOU FOR TRANSACTING ON E-WALLET SYSTEM
              </div>
            </div>
          </div>
        </>
      )}

    </div>
  );
}

