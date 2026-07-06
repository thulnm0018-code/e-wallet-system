import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api';
import { useWallet } from '../context/WalletContext';
import {
  Users, Activity, Database, Terminal, Bell, Lock, Unlock, Trash2,
  Check, X, AlertTriangle, Cpu, Layers, HardDrive, ShieldAlert,
  RotateCcw, Play, Power, AlertCircle
} from 'lucide-react';

interface SimulatedUser {
  id: string;
  name: string;
  phone: string;
  status: 'ACTIVE' | 'LOCKED';
  balance: number;
  lastActivity: string;
}

interface SuspiciousTransaction {
  id: string;
  type: 'send' | 'receive';
  amount: number;
  initiator: string;
  counterparty: string;
  reason: string;
  status: 'PENDING REVIEW' | 'APPROVED' | 'REJECTED' | 'FLAGGED';
  timestamp: string;
}

interface SlideNotification {
  id: string;
  message: string;
  timestamp: string;
}

interface AdminUserPayload {
  id: number;
  name: string;
  phone: string;
  userStatus: 'ACTIVE' | 'LOCKED' | 'BANNED' | 'PENDING_VERIFICATION' | string;
  balance: number;
  createdAt?: string;
}

interface AdminTransactionPayload {
  id: number;
  transactionCode: string;
  sender: string | null;
  receiver: string | null;
  amount: number;
  type: string;
  status: string;
  createdAt: string;
}

export function AdminDashboard() {
  const navigate = useNavigate();
  const { transactions } = useWallet();

  // Active Sidebar Tab State
  const [activeTab, setActiveTab] = useState<'dashboard' | 'users' | 'transactions' | 'monitoring' | 'logs' | 'notifications'>('dashboard');

  // Helper functions to sync with localStorage
  const syncLocalStorageUserStatus = (userId: string, status: 'ACTIVE' | 'LOCKED') => {
    try {
      const reg = localStorage.getItem('registered_users');
      if (reg) {
        const parsed = JSON.parse(reg);
        const next = parsed.map((u: any) => u.id === userId ? { ...u, walletStatus: status } : u);
        localStorage.setItem('registered_users', JSON.stringify(next));
      }
    } catch (e) {
      console.error(e);
    }
  };

  const deleteLocalStorageUser = (userId: string) => {
    try {
      const reg = localStorage.getItem('registered_users');
      if (reg) {
        const parsed = JSON.parse(reg);
        const next = parsed.filter((u: any) => u.id !== userId);
        localStorage.setItem('registered_users', JSON.stringify(next));
      }
    } catch (e) {
      console.error(e);
    }
  };

  // Simulated State Data
  const [users, setUsers] = useState<SimulatedUser[]>([]);
  const [dashboardStats, setDashboardStats] = useState({ totalUsers: 0, activeWallets: 0, totalVolume: 0, pendingReviews: 0 });

  const [suspiciousTxns, setSuspiciousTxns] = useState<SuspiciousTransaction[]>([
    { id: 'TXN-101', type: 'send', amount: 12500.00, initiator: 'TOYO ITO', counterparty: 'UNKNOWN OFFSHORE BANK', reason: 'DEBIT TRANSFER TO UNREGISTERED LEDGER', status: 'PENDING REVIEW', timestamp: '12 mins ago' },
    { id: 'TXN-102', type: 'send', amount: 4850.00, initiator: 'SHIGERU BAN', counterparty: 'COFFEE CASINO', reason: 'RAPID MICRO-WITHDRAWALS (15x IN 1 MIN)', status: 'PENDING REVIEW', timestamp: '24 mins ago' },
    { id: 'TXN-103', type: 'send', amount: 0.00, initiator: 'ARATA ISOZAKI', counterparty: 'AUTH SERVICE GATEWAY', reason: 'CONCURRENT LOGINS FROM TOKYO & REYKJAVIK', status: 'FLAGGED', timestamp: '2 hours ago' },
    { id: 'TXN-104', type: 'receive', amount: 150000.00, initiator: 'SANAA STUDIO', counterparty: 'STRUCTURAL CORP DEPOSIT', reason: 'SINGLE INPUT LARGE ACCUMULATION', status: 'APPROVED', timestamp: '4 hours ago' },
  ]);

  const [auditLogs, setAuditLogs] = useState<string[]>([
    'LOG-801: JWT Access Token generated for operator WL-8802-9901',
    'LOG-802: User ANDO TADAO balance update request completed',
    'LOG-803: Route GET /api/v1/wallets called from proxy 192.168.1.124 (Flagged)',
    'LOG-804: Webhook delivery failure on endpoint bank-broker-prod (retry scheduled)',
    'LOG-805: Session initialization for user SANAA STUDIO (status ACTIVE)',
  ]);

  // Notifications slide toasts state
  const [notifications, setNotifications] = useState<SlideNotification[]>([]);

  useEffect(() => {
    const loadAdminData = async () => {
      try {
        const [usersResponse, transactionsResponse, dashboardResponse]: any = await Promise.all([
          api.get('/users/admin'),
          api.get('/users/admin/transactions'),
          api.get('/users/admin/dashboard'),
        ]);

        const adminUsers = (usersResponse?.data || []).map((user: AdminUserPayload) => ({
          id: String(user.id),
          name: user.name,
          phone: user.phone,
          status: user.userStatus === 'LOCKED' ? 'LOCKED' : 'ACTIVE',
          balance: Number(user.balance || 0),
          lastActivity: user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Just registered',
        }));

        const adminTransactions = (transactionsResponse?.data || []).map((txn: AdminTransactionPayload) => ({
          id: `TXN-${txn.id}`,
          type: txn.type === 'TRANSFER' ? 'send' : 'receive',
          amount: Number(txn.amount || 0),
          initiator: txn.sender || 'SYSTEM',
          counterparty: txn.receiver || 'SYSTEM',
          reason: txn.transactionCode,
          status: txn.status === 'PENDING' ? 'PENDING REVIEW' : txn.status === 'SUCCESS' ? 'APPROVED' : 'REJECTED',
          timestamp: new Date(txn.createdAt).toLocaleDateString(),
        }));

        setUsers(adminUsers);
        setSuspiciousTxns(adminTransactions.length > 0 ? adminTransactions : suspiciousTxns);
        setDashboardStats({
          totalUsers: Number(dashboardResponse?.data?.totalUsers || adminUsers.length),
          activeWallets: Number(dashboardResponse?.data?.activeWallets || adminUsers.filter((u: SimulatedUser) => u.status === 'ACTIVE').length),
          totalVolume: Number(dashboardResponse?.data?.totalVolume || 0),
          pendingReviews: Number(dashboardResponse?.data?.pendingReviews || 0),
        });
      } catch (error) {
        console.error('Unable to load admin data', error);
      }
    };

    loadAdminData();
  }, []);

  // Lock Account Flow Modal State
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [userToLock, setUserToLock] = useState<SimulatedUser | null>(null);

  // Expiry JWT Overlay State
  const [sessionExpired, setSessionExpired] = useState(false);
  const [expiryCountdown, setExpiryCountdown] = useState(100);

  // Trigger Monochromatic Toast notification
  const triggerNotification = (message: string) => {
    const id = Date.now().toString();
    const newToast: SlideNotification = { id, message, timestamp: 'JUST NOW' };
    setNotifications(prev => [newToast, ...prev]);

    // Auto-remove after 4 seconds
    setTimeout(() => {
      setNotifications(prev => prev.filter(t => t.id !== id));
    }, 4000);
  };

  // JWT expiry interval countdown & redirection
  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    if (sessionExpired) {
      triggerNotification("Session expired - JWT invalidated");
      interval = setInterval(() => {
        setExpiryCountdown(prev => {
          if (prev <= 4) {
            clearInterval(interval);
            navigate('/login');
            return 0;
          }
          return prev - 4;
        });
      }, 100);
    }
    return () => clearInterval(interval);
  }, [sessionExpired, navigate]);

  // Handle Account locking
  const openLockConfirmation = (user: SimulatedUser) => {
    setUserToLock(user);
    setConfirmModalOpen(true);
  };

  const executeLockUser = () => {
    if (userToLock) {
      setUsers(prev => prev.map(u => u.id === userToLock.id ? { ...u, status: 'LOCKED' } : u));
      syncLocalStorageUserStatus(userToLock.id, 'LOCKED');
      triggerNotification(`Wallet locked: ${userToLock.name}`);
      setAuditLogs(prev => [`LOG-899: Operator revoked credentials & blacklisted token for user ${userToLock.id}`, ...prev]);
    }
    setConfirmModalOpen(false);
    setUserToLock(null);
  };

  const handleUnlockUser = (user: SimulatedUser) => {
    setUsers(prev => prev.map(u => u.id === user.id ? { ...u, status: 'ACTIVE' } : u));
    syncLocalStorageUserStatus(user.id, 'ACTIVE');
    triggerNotification(`Wallet unlocked: ${user.name}`);
    setAuditLogs(prev => [`LOG-900: Operator restored credentials for user ${user.id}`, ...prev]);
  };

  const handleDeleteUser = (userId: string, userName: string) => {
    setUsers(prev => prev.filter(u => u.id !== userId));
    deleteLocalStorageUser(userId);
    triggerNotification(`Record deleted: ${userName}`);
    setAuditLogs(prev => [`LOG-901: Operator purged user record for index ID ${userId}`, ...prev]);
  };

  // Handle Suspicious Transactions Actions
  const handleApproveTxn = (id: string) => {
    setSuspiciousTxns(prev => prev.map(t => t.id === id ? { ...t, status: 'APPROVED' } : t));
    triggerNotification(`Transaction ${id} Approved`);
    setAuditLogs(prev => [`LOG-910: Suspicious txn ${id} audited & manual state marked APPROVED`, ...prev]);
  };

  const handleRejectTxn = (id: string) => {
    setSuspiciousTxns(prev => prev.map(t => t.id === id ? { ...t, status: 'REJECTED' } : t));
    triggerNotification(`Transaction ${id} Rejected`);
    setAuditLogs(prev => [`LOG-911: Suspicious txn ${id} flagged fraudulent & marked REJECTED`, ...prev]);
  };

  const handleFlagTxn = (id: string) => {
    setSuspiciousTxns(prev => prev.map(t => t.id === id ? { ...t, status: 'FLAGGED' } : t));
    triggerNotification(`Transaction ${id} Flagged`);
    setAuditLogs(prev => [`LOG-912: Suspicious txn ${id} escalation flow triggered`, ...prev]);
  };

  // Compute Metrics dynamically
  const metrics = useMemo(() => {
    const totalBalance = users.reduce((acc, u) => acc + u.balance, 0);
    const suspiciousCount = suspiciousTxns.filter(t => t.status === 'PENDING REVIEW' || t.status === 'FLAGGED').length;
    return {
      totalUsers: dashboardStats.totalUsers || users.length,
      activeWallets: dashboardStats.activeWallets || users.filter(u => u.status === 'ACTIVE').length,
      txnVolume: dashboardStats.totalVolume || totalBalance,
      suspicious: dashboardStats.pendingReviews || suspiciousCount,
    };
  }, [users, suspiciousTxns, dashboardStats]);

  return (
    <div className="w-full max-w-[1440px] mx-auto min-h-[1024px] bg-stone-white flex flex-col md:flex-row border-x border-grid-line font-sans relative select-none">

      {/* MONOLITHIC SIDEBAR: Deep Charcoal, sharp 0px corners, high-contrast, minimalist */}
      <aside className="w-full md:w-[280px] bg-charcoal-black border-r border-grid-line text-stone-white flex flex-col justify-between p-8 shrink-0 rounded-none z-10">
        <div className="space-y-12">
          {/* Logo / Monolithic title */}
          <div className="space-y-1">
            <div className="text-[10px] uppercase tracking-[0.3em] text-medium-concrete font-medium">ADMINISTRATIVE COCKPIT</div>
            <div className="text-[20px] font-black tracking-[0.1em] text-stone-white font-mono">
              E-WALLET // CONTROL
            </div>
            <div className="h-px bg-stone-white/20 w-full mt-4" />
          </div>

          {/* Navigation links: Pure minimal typography */}
          <nav className="flex flex-col space-y-4">
            {(['dashboard', 'users', 'transactions', 'monitoring', 'logs', 'notifications'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`text-[12px] font-bold tracking-[0.2em] uppercase py-3 transition-all duration-100 flex items-center gap-3 cursor-pointer text-left w-full rounded-none ${activeTab === tab
                    ? 'text-stone-white border-l-2 border-stone-white pl-4 -ml-4 font-black'
                    : 'text-medium-concrete hover:text-stone-white pl-0'
                  }`}
              >
                {tab === 'dashboard' && <Activity className="w-4 h-4" />}
                {tab === 'users' && <Users className="w-4 h-4" />}
                {tab === 'transactions' && <Layers className="w-4 h-4" />}
                {tab === 'monitoring' && <ShieldAlert className="w-4 h-4" />}
                {tab === 'logs' && <Terminal className="w-4 h-4" />}
                {tab === 'notifications' && <Bell className="w-4 h-4" />}
                {tab.replace('-', ' ')}
              </button>
            ))}
          </nav>
        </div>

        {/* Sidebar Footer: Expiry simulation and Operator identification */}
        <div className="space-y-6 pt-8 border-t border-stone-white/10 mt-12">
          <div className="space-y-1">
            <div className="text-[10px] uppercase tracking-wider text-medium-concrete font-semibold">LOGGED AS OPERATOR</div>
            <div className="text-[12px] font-bold uppercase tracking-wider font-mono">SYSTEM ADMINISTRATOR [ROOT]</div>
          </div>

          <button
            onClick={() => setSessionExpired(true)}
            className="w-full bg-stone-white hover:bg-concrete-gray text-charcoal-black py-3 text-[11px] font-extrabold uppercase tracking-[0.2em] rounded-none transition-colors duration-100 cursor-pointer text-center"
          >
            SIMULATE EXPIRE (JWT)
          </button>
        </div>
      </aside>

      {/* ADMIN RIGHT SIDEBAR/CONTENT AREA: Resolution targeted 1440x1024 */}
      <main className="flex-1 p-10 flex flex-col justify-between overflow-x-hidden min-h-[1024px]">
        <div className="space-y-10">

          {/* Header Submenu Indicator */}
          <div className="flex justify-between items-end border-b border-grid-line pb-6">
            <div className="space-y-1">
              <div className="text-[11px] uppercase tracking-[0.25em] text-medium-concrete font-medium">ADMIN SHEET DIRECTORY</div>
              <div className="text-[28px] font-extrabold tracking-tight text-charcoal-black uppercase font-mono">
                {activeTab.replace('-', ' ')} PANEL
              </div>
            </div>
            <div className="text-right text-[11px] uppercase tracking-widest text-medium-concrete font-mono">
              ADMIN
              <div className="text-[12px] font-bold text-charcoal-black uppercase">System Administrator</div>
            </div>
          </div>

          {/* ========================================================
              TAB 1: DASHBOARD (Metrics + Queue Monitoring Placeholders)
              ======================================================== */}
          {activeTab === 'dashboard' && (
            <div className="space-y-10">

              {/* ADMIN METRICS SLABS: Oversized, raw, brutalist architectural layout */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">

                {/* Slab 1 */}
                <div className="border border-grid-line p-8 bg-stone-white flex flex-col justify-between h-40 rounded-none relative">
                  <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">TOTAL CONTROL USERS</span>
                  <div className="text-[44px] font-black tracking-tight text-charcoal-black font-mono leading-none">
                    {metrics.totalUsers} <span className="text-[12px] font-bold text-medium-concrete uppercase tracking-widest">REG</span>
                  </div>
                </div>

                {/* Slab 2 */}
                <div className="border border-grid-line p-8 bg-stone-white flex flex-col justify-between h-40 rounded-none relative">
                  <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">ACTIVE DEPLOYED WALLETS</span>
                  <div className="text-[44px] font-black tracking-tight text-charcoal-black font-mono leading-none">
                    {metrics.activeWallets} <span className="text-[12px] font-bold text-medium-concrete uppercase tracking-widest">LIVE</span>
                  </div>
                </div>

                {/* Slab 3 */}
                <div className="border border-grid-line p-8 bg-stone-white flex flex-col justify-between h-40 rounded-none relative">
                  <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">TRANSACTION BALANCE VOL</span>
                  <div className="text-[44px] font-black tracking-tight text-charcoal-black font-mono leading-none whitespace-normal break-words overflow-hidden">
                    ${metrics.txnVolume.toLocaleString('en-US', { maximumFractionDigits: 0 })}
                  </div>
                </div>

              </div>

              {/* FUTURE EXTENSION VISUAL PLACEHOLDERS: Redis, RabbitMQ, Docker containers */}
              <div className="space-y-6">
                <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">INFRASTRUCTURE STATUS (FUTURE EXTENSIONS)</div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

                  {/* Redis Monitor */}
                  <div className="border border-grid-line p-6 bg-stone-white space-y-4 rounded-none">
                    <div className="flex justify-between items-center border-b border-grid-line pb-3">
                      <span className="text-[12px] font-extrabold tracking-wider uppercase font-mono flex items-center gap-2">
                        <Database className="w-4 h-4" /> REDIS CACHE TELEMETRY
                      </span>
                      <span className="text-[10px] bg-success text-stone-white px-2 py-0.5 font-bold uppercase tracking-wider">ACTIVE</span>
                    </div>
                    <div className="space-y-3 font-mono text-[12px]">
                      <div className="flex justify-between">
                        <span className="text-medium-concrete uppercase">HIT RATE</span>
                        <span className="font-bold">99.41%</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-medium-concrete uppercase">MEMORY UTILIZATION</span>
                        <span className="font-bold">1.24 GB / 8.00 GB</span>
                      </div>
                      {/* Brutalist Memory percentage bar */}
                      <div className="w-full bg-concrete-gray h-2.5 rounded-none border border-grid-line">
                        <div className="bg-charcoal-black h-2 rounded-none" style={{ width: '15.5%' }} />
                      </div>
                      <div className="flex justify-between text-[11px] pt-1">
                        <span className="text-medium-concrete uppercase">CONNECTED CLIENTS</span>
                        <span className="font-bold">244</span>
                      </div>
                    </div>
                  </div>

                  {/* RabbitMQ Queue Monitor */}
                  <div className="border border-grid-line p-6 bg-stone-white space-y-4 rounded-none">
                    <div className="flex justify-between items-center border-b border-grid-line pb-3">
                      <span className="text-[12px] font-extrabold tracking-wider uppercase font-mono flex items-center gap-2">
                        <HardDrive className="w-4 h-4" /> RABBITMQ AMQP QUEUES
                      </span>
                      <span className="text-[10px] bg-success text-stone-white px-2 py-0.5 font-bold uppercase tracking-wider">ONLINE</span>
                    </div>
                    <div className="space-y-3 font-mono text-[12px]">
                      <div className="flex justify-between">
                        <span className="text-medium-concrete uppercase">INCOMING RATE</span>
                        <span className="font-bold">1,248 MSG/S</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-medium-concrete uppercase">READY / UNACKED</span>
                        <span className="font-bold">0 / 0</span>
                      </div>
                      {/* Queue graphic */}
                      <div className="w-full bg-concrete-gray h-2.5 rounded-none border border-grid-line">
                        <div className="bg-charcoal-black h-2 rounded-none" style={{ width: '0%' }} />
                      </div>
                      <div className="flex justify-between text-[11px] pt-1">
                        <span className="text-medium-concrete uppercase">CONSUMER CHAN</span>
                        <span className="font-bold">48 ACTIVE</span>
                      </div>
                    </div>
                  </div>

                  {/* Docker Deployments */}
                  <div className="border border-grid-line p-6 bg-stone-white space-y-4 rounded-none">
                    <div className="flex justify-between items-center border-b border-grid-line pb-3">
                      <span className="text-[12px] font-extrabold tracking-wider uppercase font-mono flex items-center gap-2">
                        <Cpu className="w-4 h-4" /> DOCKER DEPLOYMENTS
                      </span>
                      <span className="text-[10px] bg-success text-stone-white px-2 py-0.5 font-bold uppercase tracking-wider">HEALTHY</span>
                    </div>
                    <div className="space-y-2 font-mono text-[11px]">
                      <div className="flex justify-between items-center border-b border-dashed border-grid-line/50 pb-1">
                        <span className="text-charcoal-black font-bold uppercase truncate">WALLET-API-GATEWAY</span>
                        <span className="text-success font-bold text-[10px] uppercase">UP (14D)</span>
                      </div>
                      <div className="flex justify-between items-center border-b border-dashed border-grid-line/50 pb-1">
                        <span className="text-charcoal-black font-bold uppercase truncate">WALLET-AUTH-SERVICE</span>
                        <span className="text-success font-bold text-[10px] uppercase">UP (14D)</span>
                      </div>
                      <div className="flex justify-between items-center border-b border-dashed border-grid-line/50 pb-1">
                        <span className="text-charcoal-black font-bold uppercase truncate">POSTGRES-LEDGER-DB</span>
                        <span className="text-success font-bold text-[10px] uppercase">UP (14D)</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-charcoal-black font-bold uppercase truncate">RABBITMQ-AMQP-BROKER</span>
                        <span className="text-success font-bold text-[10px] uppercase">UP (14D)</span>
                      </div>
                    </div>
                  </div>

                </div>
              </div>

            </div>
          )}

          {/* ========================================================
              TAB 2: USER MANAGEMENT (Geometric Enterprise Table)
              ======================================================== */}
          {activeTab === 'users' && (
            <div className="space-y-6">

              {/* User management container */}
              <div className="border border-grid-line bg-stone-white">
                <div className="hidden md:grid grid-cols-[140px_1fr_150px_110px_140px_110px_200px] gap-4 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  <div>USER INDEX ID</div>
                  <div>NAME / ENTITY</div>
                  <div>PHONE NUMBER</div>
                  <div>STATUS</div>
                  <div>BALANCE</div>
                  <div>LAST ACTIVE</div>
                  <div className="text-right">ADMIN CONTROL</div>
                </div>

                {users.map((user, index) => (
                  <div
                    key={user.id}
                    className={`grid grid-cols-1 md:grid-cols-[140px_1fr_150px_110px_140px_110px_200px] gap-4 p-6 hover:bg-[#EAEAEA]/40 items-center text-[13px] ${index < users.length - 1 ? 'border-b border-grid-line' : ''
                      }`}
                  >
                    {/* User ID */}
                    <div className="font-mono text-medium-concrete font-bold text-[12px]">
                      {user.id}
                    </div>

                    {/* Name */}
                    <div className="font-bold text-charcoal-black uppercase tracking-wide">
                      {user.name}
                    </div>

                    {/* Phone */}
                    <div className="font-mono text-charcoal-black">
                      {user.phone}
                    </div>

                    {/* Status */}
                    <div>
                      <span
                        className={`text-[10px] font-bold uppercase px-3.5 py-1.5 tracking-wider border rounded-none ${user.status === 'ACTIVE'
                            ? 'border-success text-success bg-success/5'
                            : 'border-error text-error bg-error/5'
                          }`}
                      >
                        {user.status}
                      </span>
                    </div>

                    {/* Balance */}
                    <div className="font-mono font-bold text-charcoal-black">
                      ${user.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </div>

                    {/* Last activity */}
                    <div className="text-charcoal-black/60 uppercase text-[11px]">
                      {user.lastActivity}
                    </div>

                    {/* Administrative Controls */}
                    <div className="flex gap-2.5 justify-end">
                      {user.status === 'ACTIVE' ? (
                        <button
                          onClick={() => openLockConfirmation(user)}
                          className="px-4 py-2 border border-charcoal-black hover:bg-charcoal-black hover:text-stone-white text-charcoal-black font-extrabold text-[10px] tracking-widest uppercase rounded-none transition-colors duration-100 flex items-center gap-1 cursor-pointer"
                        >
                          <Lock className="w-3.5 h-3.5" /> LOCK
                        </button>
                      ) : (
                        <button
                          onClick={() => handleUnlockUser(user)}
                          className="px-4 py-2 bg-charcoal-black hover:bg-concrete-gray text-stone-white border border-charcoal-black hover:text-charcoal-black font-extrabold text-[10px] tracking-widest uppercase rounded-none transition-colors duration-100 flex items-center gap-1 cursor-pointer"
                        >
                          <Unlock className="w-3.5 h-3.5" /> UNLOCK
                        </button>
                      )}

                      <button
                        onClick={() => handleDeleteUser(user.id, user.name)}
                        className="px-3.5 py-2 border border-grid-line hover:border-error hover:text-error text-medium-concrete font-extrabold text-[10px] tracking-widest uppercase rounded-none transition-colors duration-100 cursor-pointer"
                        title="Delete User"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>

            </div>
          )}

          {/* ========================================================
              TAB 3: TRANSACTIONS (General Ledger Audit Flow)
              ======================================================== */}
          {activeTab === 'transactions' && (
            <div className="space-y-6">

              {/* Transactions grid */}
              <div className="border border-grid-line bg-stone-white">
                <div className="hidden md:grid grid-cols-[110px_1fr_200px_150px_140px] gap-6 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  <div>LEDGER ID</div>
                  <div>{"SENDER ──> RECIPIENT"}</div>
                  <div>DATE & TIMESTAMP</div>
                  <div>STATUS</div>
                  <div className="text-right">AMOUNT</div>
                </div>

                {transactions.length === 0 ? (
                  <div className="py-20 text-center font-mono text-[13px] uppercase text-medium-concrete">
                    NO RECORDED LEDGER TRANSACTIONS FOUND
                  </div>
                ) : (
                  transactions.map((t, index) => (
                    <div
                      key={t.id}
                      className={`grid grid-cols-1 md:grid-cols-[110px_1fr_200px_150px_140px] gap-4 p-6 hover:bg-[#EAEAEA]/40 items-center text-[13px] ${index < transactions.length - 1 ? 'border-b border-grid-line' : ''
                        }`}
                    >
                      {/* ID */}
                      <span className="font-mono text-medium-concrete font-bold text-[12px]">#{t.id}</span>

                      {/* Flow */}
                      <div className="flex items-center gap-4">
                        <span className="font-bold text-charcoal-black truncate uppercase max-w-[150px]">
                          {t.type === 'receive' ? t.sender : 'SYSTEM'}
                        </span>
                        <span className="text-medium-concrete font-mono">{"──>"}</span>
                        <span className="font-bold text-charcoal-black truncate uppercase max-w-[150px]">
                          {t.type === 'send' ? t.recipient : 'SYSTEM'}
                        </span>
                      </div>

                      {/* Date */}
                      <div className="font-mono text-[12px] uppercase">
                        {new Date(t.date).toISOString().replace('T', ' ').substring(0, 19)}
                      </div>

                      {/* Status */}
                      <div>
                        <span className={`text-[10px] font-bold uppercase ${t.status === 'completed' ? 'text-success' : t.status === 'pending' ? 'text-warning' : 'text-error'}`}>
                          {t.status}
                        </span>
                      </div>

                      {/* Amount */}
                      <div className="text-right font-mono font-bold text-[15px]">
                        {t.type === 'send' ? '-' : '+'}${t.amount.toFixed(2)}
                      </div>
                    </div>
                  ))
                )}
              </div>

            </div>
          )}

          {/* ========================================================
              TAB 4: TRANSACTION MONITORING (Suspicious Feeds & Actions)
              ======================================================== */}
          {activeTab === 'monitoring' && (
            <div className="space-y-6">

              <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">SUSPICIOUS TRANSACTION AUDIT QUEUE</div>

              {/* Geometric queue */}
              <div className="border border-grid-line bg-stone-white">
                <div className="hidden md:grid grid-cols-[100px_1fr_220px_150px_130px_240px] gap-4 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  <div>TXN REF</div>
                  <div>INITIATOR / DETAILS</div>
                  <div>TRIGGER VIOLATION REASON</div>
                  <div>STATUS</div>
                  <div>AMOUNT</div>
                  <div className="text-right">DECISION CONTROL</div>
                </div>

                {suspiciousTxns.map((txn, index) => {
                  const isSuspicious = txn.status === 'PENDING REVIEW' || txn.status === 'FLAGGED';

                  return (
                    <div
                      key={txn.id}
                      className={`grid grid-cols-1 md:grid-cols-[100px_1fr_220px_150px_130px_240px] gap-4 p-6 items-center text-[13px] transition-colors duration-100 ${index < suspiciousTxns.length - 1 ? 'border-b border-grid-line' : ''
                        } ${isSuspicious ? 'bg-[#8B8371]/10 hover:bg-[#8B8371]/15' : 'hover:bg-[#EAEAEA]/30'}`}
                    >
                      {/* TXN Ref */}
                      <span className="font-mono text-medium-concrete font-bold text-[12px]">{txn.id}</span>

                      {/* Initiator */}
                      <div className="space-y-1">
                        <div className="font-bold text-charcoal-black uppercase">{txn.initiator}</div>
                        <div className="text-[10px] text-medium-concrete uppercase font-mono">{"──>"} {txn.counterparty}</div>
                      </div>

                      {/* Reason */}
                      <div className="text-[11px] font-bold text-error uppercase font-mono leading-relaxed">
                        {txn.reason}
                      </div>

                      {/* Status */}
                      <div>
                        <span className={`text-[10px] font-bold uppercase px-3 py-1 border rounded-none ${txn.status === 'APPROVED' ? 'border-success text-success bg-success/5' :
                            txn.status === 'REJECTED' ? 'border-error text-error bg-error/5' :
                              txn.status === 'FLAGGED' ? 'border-[#8B8371] text-[#8B8371] bg-[#8B8371]/5' :
                                'border-charcoal-black text-charcoal-black bg-stone-white'
                          }`}>
                          {txn.status}
                        </span>
                      </div>

                      {/* Amount */}
                      <div className="font-mono font-bold text-[15px] text-charcoal-black">
                        ${txn.amount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </div>

                      {/* Control Actions */}
                      <div className="flex gap-2 justify-end">
                        {txn.status === 'PENDING REVIEW' || txn.status === 'FLAGGED' ? (
                          <>
                            <button
                              onClick={() => handleApproveTxn(txn.id)}
                              className="px-3 py-1.5 border border-success hover:bg-success hover:text-stone-white text-success font-extrabold text-[10px] tracking-wider uppercase rounded-none transition-colors duration-100 cursor-pointer"
                            >
                              APPROVE
                            </button>
                            <button
                              onClick={() => handleRejectTxn(txn.id)}
                              className="px-3 py-1.5 border border-error hover:bg-error hover:text-stone-white text-error font-extrabold text-[10px] tracking-wider uppercase rounded-none transition-colors duration-100 cursor-pointer"
                            >
                              REJECT
                            </button>
                            {txn.status !== 'FLAGGED' && (
                              <button
                                onClick={() => handleFlagTxn(txn.id)}
                                className="px-3 py-1.5 border border-[#8B8371] hover:bg-[#8B8371] hover:text-stone-white text-[#8B8371] font-extrabold text-[10px] tracking-wider uppercase rounded-none transition-colors duration-100 cursor-pointer"
                              >
                                ESCALATE
                              </button>
                            )}
                          </>
                        ) : (
                          <span className="text-[11px] uppercase tracking-wider text-medium-concrete italic font-medium pr-4">
                            SETTLED & LOGGED
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

            </div>
          )}

          {/* ========================================================
              TAB 5: SYSTEM LOGS (Live Audit Logs Console)
              ======================================================== */}
          {activeTab === 'logs' && (
            <div className="space-y-6">

              <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">SYSTEM AUDIT TERMINAL FEED (LIVE)</div>

              <div className="border border-grid-line bg-charcoal-black text-stone-white p-8 font-mono text-[12px] space-y-3 leading-relaxed rounded-none min-h-[400px]">
                <div className="text-medium-concrete border-b border-stone-white/10 pb-2 mb-4 uppercase tracking-widest text-[10px]">
                  ARCHIVAL AUDIT FEED ──── OUTPUT TERMINAL
                </div>
                {auditLogs.map((log, index) => (
                  <div key={index} className="flex gap-4">
                    <span className="text-medium-concrete">[{new Date().toISOString().substring(11, 19)}]</span>
                    <span className={log.includes('(Flagged)') ? 'text-error font-bold' : log.includes('purged') || log.includes('revoked') ? 'text-warning font-bold' : 'text-stone-white/90'}>
                      {log}
                    </span>
                  </div>
                ))}
                <div className="text-[#6B6B5A] pt-4 flex items-center gap-2">
                  <span className="inline-block w-2.5 h-2.5 bg-[#6B6B5A] animate-pulse rounded-none" />
                  <span>AWAITING SUB-ROUTINE INCOMING LOGS...</span>
                </div>
              </div>

            </div>
          )}

          {/* ========================================================
              TAB 6: NOTIFICATIONS TOAST CENTER & TESTS
              ======================================================== */}
          {activeTab === 'notifications' && (
            <div className="space-y-6">

              <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">SYSTEM TOAST DISPATCH BOARD</div>

              <div className="border border-grid-line p-8 bg-stone-white rounded-none max-w-xl space-y-6">
                <div className="text-[12px] uppercase text-charcoal-black font-extrabold tracking-widest border-b border-grid-line pb-3">
                  MANUAL NOTIFICATION EMULATOR
                </div>
                <p className="text-[13px] text-charcoal-black/70 leading-relaxed uppercase">
                  Click below to dispatch desaturated monochrome slide panels. These reflect standard administrative alerts.
                </p>

                <div className="grid grid-cols-2 gap-4">
                  <button
                    onClick={() => triggerNotification("Transfer completed - Reference TXN-8902")}
                    className="py-3 border border-charcoal-black hover:bg-concrete-gray text-charcoal-black text-[11px] uppercase tracking-wider font-extrabold rounded-none cursor-pointer text-center"
                  >
                    "Transfer completed"
                  </button>
                  <button
                    onClick={() => triggerNotification("User session blacklisted - Token purged")}
                    className="py-3 border border-charcoal-black hover:bg-concrete-gray text-charcoal-black text-[11px] uppercase tracking-wider font-extrabold rounded-none cursor-pointer text-center"
                  >
                    "Wallet locked"
                  </button>
                  <button
                    onClick={() => triggerNotification("JWT token expired - Redirect sequence initiated")}
                    className="py-3 border border-charcoal-black hover:bg-concrete-gray text-charcoal-black text-[11px] uppercase tracking-wider font-extrabold rounded-none cursor-pointer text-center"
                  >
                    "Session expired"
                  </button>
                  <button
                    onClick={() => triggerNotification("OTP verification token sent successfully")}
                    className="py-3 border border-charcoal-black hover:bg-concrete-gray text-charcoal-black text-[11px] uppercase tracking-wider font-extrabold rounded-none cursor-pointer text-center"
                  >
                    "OTP sent successfully"
                  </button>
                </div>
              </div>

            </div>
          )}

        </div>

        {/* Console status footer */}
        <div className="border-t border-grid-line pt-6 mt-12 flex justify-between items-center text-[10px] uppercase tracking-[0.25em] text-medium-concrete font-bold font-mono">
          <span>OPERATOR HOST: WALLET-MGMT-NODE-Tokyo</span>
          <span>SYSTEM TIME: {new Date().toISOString().substring(0, 10)} {new Date().toLocaleTimeString()}</span>
        </div>
      </main>

      {/* ========================================================
          JWT SESSION EXPIRED OVERLAY: Full screen architectural overlay
          ======================================================== */}
      {sessionExpired && (
        <div className="fixed inset-0 bg-charcoal-black bg-opacity-95 z-[9999] flex flex-col items-center justify-center space-y-8 text-stone-white p-6 no-print transition-all duration-150">
          <div className="space-y-3 text-center">
            <AlertCircle className="w-16 h-16 text-error mx-auto animate-pulse" />
            <h2 className="text-[36px] font-black tracking-[0.25em] text-stone-white uppercase font-mono">
              SESSION EXPIRED
            </h2>
            <p className="text-[12px] uppercase tracking-[0.2em] text-medium-concrete font-bold max-w-md mx-auto leading-relaxed">
              JWT AUTHORIZATION KEY EXPIRED OR BLACKLISTED BY OPERATOR SECURITY REGULATION. REDIRECTING TO LOGIN INTERFACE...
            </p>
          </div>

          {/* Minimalist Linear Loading Bar */}
          <div className="w-80 h-1.5 bg-stone-white/20 border border-stone-white/10 relative rounded-none overflow-hidden">
            <div
              className="bg-stone-white h-full transition-all duration-100 ease-out"
              style={{ width: `${100 - expiryCountdown}%` }}
            />
          </div>

          <div className="text-[10px] tracking-[0.3em] text-medium-concrete font-mono">
            SECURE RE-ROUTING IN PROGRESS...
          </div>
        </div>
      )}

      {/* ========================================================
          LOCK USER CONFIRMATION MODAL: Harsh, geometric, flat black modal
          ======================================================== */}
      {confirmModalOpen && userToLock && (
        <>
          {/* Flat black overlay */}
          <div
            onClick={() => setConfirmModalOpen(false)}
            className="fixed inset-0 bg-charcoal-black/75 z-40 transition-opacity duration-100"
          />

          {/* Geometric Modal */}
          <div className="fixed left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full max-w-[420px] bg-charcoal-black border border-grid-line p-10 z-50 text-stone-white rounded-none shadow-none space-y-8 animate-fade-in">
            <div className="space-y-4">
              <div className="flex items-center gap-3 text-error border-b border-stone-white/10 pb-4">
                <AlertTriangle className="w-6 h-6" />
                <span className="text-[12px] font-black uppercase tracking-[0.25em]">CRITICAL REVOCATION ALERT</span>
              </div>

              <div className="space-y-2">
                <h3 className="text-[18px] font-extrabold uppercase tracking-wide">
                  LOCK USER ACCOUNT?
                </h3>
                <div className="text-[12px] text-medium-concrete font-medium uppercase font-mono tracking-wider">
                  TARGET ID: #{userToLock.id}
                </div>
                <div className="text-[14px] font-bold text-stone-white uppercase">
                  NAME: {userToLock.name}
                </div>
              </div>
            </div>

            {/* Warning Message Box */}
            <div className="border border-error/50 bg-error/5 p-4 text-[12px] text-error font-extrabold uppercase tracking-[0.12em] leading-relaxed rounded-none text-center">
              “User access will be revoked immediately”
            </div>

            {/* Flat 90-degree Buttons */}
            <div className="flex gap-4">
              <button
                onClick={executeLockUser}
                className="flex-1 bg-stone-white hover:bg-concrete-gray text-charcoal-black py-4 text-[11px] uppercase tracking-[0.2em] font-black rounded-none cursor-pointer text-center"
              >
                CONFIRM LOCK
              </button>
              <button
                onClick={() => setConfirmModalOpen(false)}
                className="flex-1 bg-transparent hover:bg-stone-white/10 border border-stone-white/30 text-stone-white py-4 text-[11px] uppercase tracking-[0.2em] font-black rounded-none cursor-pointer text-center"
              >
                CANCEL
              </button>
            </div>
          </div>
        </>
      )}

      {/* ========================================================
          SLIDE-OUT SYSTEM NOTIFICATIONS PANELS: Monochrome only, desaturated
          ======================================================== */}
      <div className="fixed bottom-10 right-10 z-50 flex flex-col gap-3 max-w-[380px] w-full no-print">
        {notifications.map((toast) => (
          <div
            key={toast.id}
            className="w-full bg-charcoal-black border border-grid-line text-stone-white p-5 flex justify-between items-start gap-4 shadow-none rounded-none animate-slide-in relative overflow-hidden"
          >
            <div className="space-y-1">
              <div className="text-[9px] uppercase tracking-[0.3em] text-medium-concrete font-bold font-mono">SYSTEM INTERCEPT ALERT</div>
              <div className="text-[12px] font-bold uppercase tracking-wider text-stone-white leading-normal">
                {toast.message}
              </div>
            </div>
            <button
              onClick={() => setNotifications(prev => prev.filter(t => t.id !== toast.id))}
              className="p-1 text-medium-concrete hover:text-stone-white transition-colors cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ))}
      </div>

    </div>
  );
}
