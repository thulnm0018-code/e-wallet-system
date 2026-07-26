import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';
import {
  Users,
  Activity,
  Database,
  Terminal,
  Bell,
  Lock,
  Unlock,
  Trash2,
  Cpu,
  Layers,
  HardDrive,
  ShieldAlert,
  AlertCircle,
  LogOut,
  ArrowRight,
  AlertTriangle,
  Check,
  X,
} from 'lucide-react';
import {
  fetchAdminDashboard,
  fetchAdminDashboardStats,
} from '../services/adminDashboardService';
import {
  fetchAdminUsers,
  deleteAdminUser,
  lockAdminUser,
  unlockAdminUser,
} from '../services/adminUserService';
import {
  fetchAdminTransactions,
  approveAdminDepositRequest,
  rejectAdminDepositRequest,
} from '../services/adminTransactionService';
import {
  fetchSuspiciousActivities,
  approveSuspiciousTransaction,
  rejectSuspiciousTransaction,
} from '../services/adminMonitoringService';
import { fetchAuditLogs } from '../services/adminAuditService';
import { fetchNotifications } from '../services/adminNotificationService';
import type {
  AdminDashboardStatsResponse,
  AdminDashboardSummary,
  AdminTransaction,
  AdminUser,
  AuditLogEntry,
  NotificationItem,
} from '../services/adminTypes';

function isBackendNotImplemented(error: unknown) {
  const status = (error as any)?.response?.status;
  return status === 404 || status === 501;
}

function formatDate(value?: string) {
  if (!value) return 'N/A';

  return new Date(value).toLocaleString('en-GB', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function getErrorMessage(error: unknown, fallback: string) {
  const message = (error as any)?.response?.data?.message;
  if (typeof message === 'string' && message.trim()) {
    return message;
  }

  const genericMessage = (error as Error | undefined)?.message;
  if (typeof genericMessage === 'string' && genericMessage.trim()) {
    return genericMessage;
  }

  return fallback;
}

export function AdminDashboard() {
  const navigate = useNavigate();
  const { user, logout: authLogout } = useAuth();

  const [activeTab, setActiveTab] = useState<'dashboard' | 'users' | 'transactions' | 'monitoring' | 'logs' | 'notifications'>('dashboard');

  const [dashboardSummary, setDashboardSummary] = useState<AdminDashboardSummary | null>(null);
  const [dashboardStats, setDashboardStats] = useState<AdminDashboardStatsResponse | null>(null);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [transactions, setTransactions] = useState<AdminTransaction[]>([]);
  const [suspiciousTxns, setSuspiciousTxns] = useState<AdminTransaction[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([]);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);

  const [transactionStatusFilter, setTransactionStatusFilter] = useState<'all' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUCCESS' | 'FAILED'>('all');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [userActionInProgress, setUserActionInProgress] = useState<number | null>(null);
  const [transactionActionInProgress, setTransactionActionInProgress] = useState<number | null>(null);
  const [suspiciousActionInProgress, setSuspiciousActionInProgress] = useState<string | null>(null);
  const [notificationLoadError, setNotificationLoadError] = useState<string | null>(null);

  const loadDashboard = async () => {
    setError(null);
    try {
      const [summary, stats] = await Promise.all([fetchAdminDashboard(), fetchAdminDashboardStats()]);
      setDashboardSummary(summary);
      setDashboardStats(stats);
    } catch (err) {
      setError(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : getErrorMessage(err, 'Unable to load dashboard metrics'));
    }
  };

  const loadUsers = async () => {
    setError(null);
    setLoading(true);
    try {
      const page = await fetchAdminUsers(0, 50);
      setUsers(page.content || []);
    } catch (err) {
      setError(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : getErrorMessage(err, 'Unable to load admin users'));
    } finally {
      setLoading(false);
    }
  };

  const loadTransactions = async () => {
    setError(null);
    setLoading(true);
    try {
      const page = await fetchAdminTransactions(0, 50, transactionStatusFilter);
      setTransactions(page.content || []);
    } catch (err) {
      setError(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : getErrorMessage(err, 'Unable to load transactions'));
    } finally {
      setLoading(false);
    }
  };

  const loadSuspicious = async () => {
    setError(null);
    setLoading(true);
    try {
      const items = await fetchSuspiciousActivities();
      setSuspiciousTxns(items || []);
    } catch (err) {
      setError(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : getErrorMessage(err, 'Unable to load suspicious transactions'));
    } finally {
      setLoading(false);
    }
  };

  const loadAudit = async () => {
    setError(null);
    setLoading(true);
    try {
      const logs = await fetchAuditLogs();
      setAuditLogs(logs || []);
    } catch (err) {
      setError(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : getErrorMessage(err, 'Unable to load audit logs'));
    } finally {
      setLoading(false);
    }
  };

  const loadNotifications = async () => {
    setNotificationLoadError(null);
    try {
      const list = await fetchNotifications();
      setNotifications(list || []);
    } catch (err) {
      setNotificationLoadError(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : getErrorMessage(err, 'Unable to load notifications'));
    }
  };

  useEffect(() => {
    void loadDashboard();
    void loadNotifications();
  }, []);

  useEffect(() => {
    void (async () => {
      if (activeTab === 'users') {
        await loadUsers();
      }
      if (activeTab === 'transactions') {
        await loadTransactions();
      }
      if (activeTab === 'monitoring') {
        await loadSuspicious();
      }
      if (activeTab === 'logs') {
        await loadAudit();
      }
      if (activeTab === 'notifications') {
        await loadNotifications();
      }
    })();
  }, [activeTab, transactionStatusFilter]);

  const handleLogout = async () => {
    await authLogout();
    navigate('/login', { replace: true });
  };

  const handleLockUser = async (userItem: AdminUser) => {
    setUserActionInProgress(userItem.id ?? null);
    try {
      await lockAdminUser(userItem.id);
      toast.success(`Locked ${userItem.name}`);
      await loadUsers();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Unable to lock user');
    } finally {
      setUserActionInProgress(null);
    }
  };

  const handleUnlockUser = async (userItem: AdminUser) => {
    setUserActionInProgress(userItem.id ?? null);
    try {
      await unlockAdminUser(userItem.id);
      toast.success(`Unlocked ${userItem.name}`);
      await loadUsers();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Unable to unlock user');
    } finally {
      setUserActionInProgress(null);
    }
  };

  const handleDeleteUser = async (userItem: AdminUser) => {
    setUserActionInProgress(userItem.id ?? null);
    try {
      await deleteAdminUser(userItem.id);
      toast.success(`Deleted ${userItem.name}`);
      await loadUsers();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Unable to delete user');
    } finally {
      setUserActionInProgress(null);
    }
  };

  const approveDepositRequest = async (transactionId: number) => {
    setTransactionActionInProgress(transactionId);
    try {
      await approveAdminDepositRequest(transactionId);
      toast.success(`Deposit request #${transactionId} approved`);
      await loadTransactions();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Failed to approve request');
    } finally {
      setTransactionActionInProgress(null);
    }
  };

  const rejectDepositRequest = async (transactionId: number) => {
    setTransactionActionInProgress(transactionId);
    try {
      await rejectAdminDepositRequest(transactionId);
      toast.success(`Deposit request #${transactionId} rejected`);
      await loadTransactions();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Failed to reject request');
    } finally {
      setTransactionActionInProgress(null);
    }
  };

  const approveSuspicious = async (txn: AdminTransaction) => {
    if (!txn.transactionCode) return;
    setSuspiciousActionInProgress(txn.transactionCode);
    try {
      await approveSuspiciousTransaction(txn.transactionCode);
      toast.success(`Transaction ${txn.transactionCode} approved`);
      await loadSuspicious();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Unable to approve suspicious transaction');
    } finally {
      setSuspiciousActionInProgress(null);
    }
  };

  const rejectSuspicious = async (txn: AdminTransaction) => {
    if (!txn.transactionCode) return;
    setSuspiciousActionInProgress(txn.transactionCode);
    try {
      await rejectSuspiciousTransaction(txn.transactionCode);
      toast.success(`Transaction ${txn.transactionCode} rejected`);
      await loadSuspicious();
    } catch (err) {
      toast.error(isBackendNotImplemented(err) ? 'Backend API not implemented yet' : 'Unable to reject suspicious transaction');
    } finally {
      setSuspiciousActionInProgress(null);
    }
  };

  const metrics = useMemo(() => {
    return {
      totalUsers: dashboardSummary?.totalUsers ?? users.length,
      activeWallets: dashboardSummary?.activeWallets ?? users.filter((u) => u.userStatus === 'ACTIVE').length,
      totalVolume: dashboardSummary?.totalVolume ?? '0',
      pendingReviews: dashboardSummary?.pendingReviews ?? suspiciousTxns.length,
      totalTransactions: dashboardSummary?.totalTransactions ?? transactions.length,
    };
  }, [dashboardSummary, users, transactions, suspiciousTxns]);

  const currentUserName = user?.name ?? 'SYSTEM ADMINISTRATOR';
  const currentUserRole = user?.role?.toUpperCase() ?? 'ADMIN';

  return (
    <div className="w-full max-w-[1440px] mx-auto min-h-[1024px] bg-stone-white flex flex-col md:flex-row border-x border-grid-line font-sans relative select-none">
      <aside className="w-full md:w-[280px] bg-charcoal-black border-r border-grid-line text-stone-white flex flex-col justify-between p-8 shrink-0 rounded-none z-10">
        <div className="space-y-12">
          <div className="space-y-1">
            <div className="text-[10px] uppercase tracking-[0.3em] text-medium-concrete font-medium">ADMINISTRATIVE COCKPIT</div>
            <div className="text-[20px] font-black tracking-[0.1em] text-stone-white font-mono">E-WALLET // CONTROL</div>
            <div className="h-px bg-stone-white/20 w-full mt-4" />
          </div>

          <nav className="flex flex-col space-y-4">
            {(['dashboard', 'users', 'transactions', 'monitoring', 'logs', 'notifications'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`text-[12px] font-bold tracking-[0.2em] uppercase py-3 transition-all duration-100 flex items-center gap-3 cursor-pointer text-left w-full rounded-none ${activeTab === tab ? 'text-stone-white border-l-2 border-stone-white pl-4 -ml-4 font-black' : 'text-medium-concrete hover:text-stone-white pl-0'}`}
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

        <div className="space-y-6 pt-8 border-t border-stone-white/10 mt-12">
          <div className="space-y-1">
            <div className="text-[10px] uppercase tracking-wider text-medium-concrete font-semibold">LOGGED AS OPERATOR</div>
            <div className="text-[12px] font-bold uppercase tracking-wider font-mono">{currentUserName}</div>
            <div className="text-[10px] uppercase tracking-wider text-medium-concrete">{currentUserRole}</div>
          </div>

          <button
            onClick={() => void handleLogout()}
            className="w-full flex items-center justify-center gap-2 bg-transparent border border-stone-white/30 hover:bg-stone-white/10 text-stone-white py-3 text-[11px] font-extrabold uppercase tracking-[0.2em] rounded-none transition-colors duration-100 cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
            LOG OUT
          </button>
        </div>
      </aside>

      <main className="flex-1 p-10 flex flex-col justify-between overflow-x-hidden min-h-[1024px]">
        <div className="space-y-10">
          <div className="flex justify-between items-end border-b border-grid-line pb-6">
            <div className="space-y-1">
              <div className="text-[11px] uppercase tracking-[0.25em] text-medium-concrete font-medium">ADMIN SHEET DIRECTORY</div>
              <div className="text-[28px] font-extrabold tracking-tight text-charcoal-black uppercase font-mono">{activeTab.replace('-', ' ')} PANEL</div>
            </div>
            <div className="text-right text-[11px] uppercase tracking-widest text-medium-concrete font-mono">
              ADMIN
              <div className="text-[12px] font-bold text-charcoal-black uppercase">{currentUserRole}</div>
            </div>
          </div>

          {activeTab === 'dashboard' && (
            <div className="space-y-10">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="border border-grid-line p-8 bg-stone-white flex flex-col justify-between h-40 rounded-none relative">
                  <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">TOTAL CONTROL USERS</span>
                  <div className="text-[44px] font-black tracking-tight text-charcoal-black font-mono leading-none">{metrics.totalUsers} <span className="text-[12px] font-bold text-medium-concrete uppercase tracking-widest">REG</span></div>
                </div>

                <div className="border border-grid-line p-8 bg-stone-white flex flex-col justify-between h-40 rounded-none relative">
                  <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">ACTIVE DEPLOYED WALLETS</span>
                  <div className="text-[44px] font-black tracking-tight text-charcoal-black font-mono leading-none">{metrics.activeWallets} <span className="text-[12px] font-bold text-medium-concrete uppercase tracking-widest">LIVE</span></div>
                </div>

                <div className="border border-grid-line p-8 bg-stone-white flex flex-col justify-between h-40 rounded-none relative">
                  <span className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">TRANSACTION BALANCE VOL</span>
                  <div className="text-[44px] font-black tracking-tight text-charcoal-black font-mono leading-none whitespace-normal break-words overflow-hidden">${Number(metrics.totalVolume).toLocaleString('en-US', { maximumFractionDigits: 0 })}</div>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="border border-grid-line p-6 bg-stone-white space-y-4 rounded-none">
                  <div className="flex justify-between items-center border-b border-grid-line pb-3">
                    <span className="text-[12px] font-extrabold tracking-wider uppercase font-mono flex items-center gap-2"><Database className="w-4 h-4" /> REDIS CACHE TELEMETRY</span>
                    <span className="text-[10px] bg-success text-stone-white px-2 py-0.5 font-bold uppercase tracking-wider">{dashboardStats?.redisTelemetry ? 'ACTIVE' : 'UNKNOWN'}</span>
                  </div>
                  <div className="space-y-3 font-mono text-[12px]">
                    <div className="flex justify-between"><span className="text-medium-concrete uppercase">HIT RATE</span><span className="font-bold">{dashboardStats?.redisTelemetry?.hitRate ?? '--'}</span></div>
                    <div className="flex justify-between"><span className="text-medium-concrete uppercase">MEMORY UTILIZATION</span><span className="font-bold">{dashboardStats?.redisTelemetry ? `${dashboardStats.redisTelemetry.memoryMb} MB` : '--'}</span></div>
                    <div className="w-full bg-concrete-gray h-2.5 rounded-none border border-grid-line"><div className="bg-charcoal-black h-2 rounded-none" style={{ width: dashboardStats?.redisTelemetry ? `${(dashboardStats.redisTelemetry.memoryMb / 1024) * 100}%` : '0%' }} /></div>
                    <div className="flex justify-between text-[11px] pt-1"><span className="text-medium-concrete uppercase">CONNECTED CLIENTS</span><span className="font-bold">{dashboardStats?.redisTelemetry?.connectedClients ?? '--'}</span></div>
                  </div>
                </div>

                <div className="border border-grid-line p-6 bg-stone-white space-y-4 rounded-none">
                  <div className="flex justify-between items-center border-b border-grid-line pb-3">
                    <span className="text-[12px] font-extrabold tracking-wider uppercase font-mono flex items-center gap-2"><HardDrive className="w-4 h-4" /> DEPLOYMENT HEALTH</span>
                    <span className="text-[10px] bg-success text-stone-white px-2 py-0.5 font-bold uppercase tracking-wider">{dashboardStats?.deployments ? 'HEALTHY' : 'UNKNOWN'}</span>
                  </div>
                  <div className="space-y-2 font-mono text-[11px]">
                    <div className="flex justify-between items-center border-b border-dashed border-grid-line/50 pb-1"><span className="text-charcoal-black font-bold uppercase truncate">API GATEWAY</span><span className="text-success font-bold text-[10px] uppercase">{dashboardStats?.deployments?.apiGateway ?? '--'}</span></div>
                    <div className="flex justify-between items-center border-b border-dashed border-grid-line/50 pb-1"><span className="text-charcoal-black font-bold uppercase truncate">AUTH SERVICE</span><span className="text-success font-bold text-[10px] uppercase">{dashboardStats?.deployments?.authService ?? '--'}</span></div>
                    <div className="flex justify-between items-center border-b border-dashed border-grid-line/50 pb-1"><span className="text-charcoal-black font-bold uppercase truncate">DATABASE</span><span className="text-success font-bold text-[10px] uppercase">{dashboardStats?.deployments?.database ?? '--'}</span></div>
                    <div className="flex justify-between items-center"><span className="text-charcoal-black font-bold uppercase truncate">RABBITMQ</span><span className="text-success font-bold text-[10px] uppercase">{dashboardStats?.deployments?.rabbitmq ?? '--'}</span></div>
                  </div>
                </div>
              </div>

              {error && (
                <div className="border border-error/50 bg-error/5 text-error p-6 uppercase tracking-[0.2em] font-bold">{error}</div>
              )}
            </div>
          )}

          {activeTab === 'users' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between gap-4">
                <div className="text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">USER MANAGEMENT</div>
                <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete">Loaded {users.length} users</div>
              </div>

              <div className="border border-grid-line bg-stone-white">
               <div className="hidden md:grid grid-cols-[220px_160px_100px_120px_140px_140px] gap-4 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  <div>NAME / EMAIL</div>
                  <div>PHONE NUMBER</div>
                  <div>STATUS</div>
                  <div>BALANCE</div>
                  <div>CREATED</div>
                  <div className="text-center">ACTIONS</div>
                </div>

                {loading ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">LOADING USERS…</div>
                ) : error ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-error">{error}</div>
                ) : users.length === 0 ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">NO ADMIN USERS FOUND</div>
                ) : (
                  users.map((userItem, index) => (
                      <div
                            key={userItem.id}
                           className={`grid grid-cols-1 md:grid-cols-[220px_160px_100px_120px_140px_140px] gap-4 p-6 hover:bg-[#EAEAEA]/40 items-center text-[13px]
                            ${index < users.length - 1 ? 'border-b border-grid-line' : ''}`}
                          >
                    <div className="space-y-1 min-w-0">
                      <div className="font-bold text-charcoal-black uppercase tracking-wide">
                        {userItem.name}
                      </div>

                      <div className="text-[11px] text-medium-concrete lowercase truncate max-w-[150px]">
                        {userItem.email}
                      </div>
                    </div>
                      <div className="font-mono text-charcoal-black">{userItem.phone}</div>
                      <div>
                        <span className={`text-[10px] font-bold uppercase px-3.5 py-1.5 tracking-wider border rounded-none ${userItem.userStatus === 'LOCKED' ? 'border-error text-error bg-error/5' : 'border-success text-success bg-success/5'}`}>
                          {userItem.userStatus}
                        </span>
                      </div>
                      <div className="font-mono font-bold text-charcoal-black">${Number(userItem.balance || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>
                      <div className="text-charcoal-black/60 uppercase text-[11px]">{formatDate(userItem.createdAt)}</div>
                      <div className="flex items-center justify-center gap-2">
                        {userItem.userStatus === 'ACTIVE' ? (
                          <button
                            onClick={() => void handleLockUser(userItem)}
                            disabled={userActionInProgress === userItem.id}
                            className="px-3 py-2 border border-charcoal-black hover:bg-charcoal-black hover:text-stone-white text-charcoal-black font-extrabold text-[10px] uppercase rounded-none transition-colors duration-100 flex items-center gap-1"
                          >
                            <Lock className="w-3.5 h-3.5" /> LOCK
                          </button>
                        ) : (
                          <button
                            onClick={() => void handleUnlockUser(userItem)}
                            disabled={userActionInProgress === userItem.id}
                            className="px-4 py-2 bg-charcoal-black hover:bg-concrete-gray text-stone-white border border-charcoal-black hover:text-charcoal-black font-extrabold text-[10px] tracking-widest uppercase rounded-none transition-colors duration-100 flex items-center gap-1 cursor-pointer disabled:opacity-40"
                          >
                            <Unlock className="w-3.5 h-3.5" /> UNLOCK
                          </button>
                        )}
                        <button
                        onClick={() => void handleDeleteUser(userItem)}
                        disabled={userActionInProgress === userItem.id}
                        className="w-10 h-10 border border-grid-line hover:border-error hover:text-error text-medium-concrete flex items-center justify-center rounded-none transition-colors duration-100 cursor-pointer disabled:opacity-40"
                        title="Delete User"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
          
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === 'transactions' && (
            <div className="space-y-6">
              <div className="border border-grid-line bg-stone-white">
                <div className="flex flex-col gap-3 p-6 border-b border-grid-line bg-stone-white">
                  <div className="flex flex-wrap items-center gap-3 justify-between">
                    <div className="text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">TRANSACTION LEDGER</div>
                    <div className="flex flex-wrap gap-2">
                      {['all', 'PENDING', 'APPROVED', 'REJECTED', 'SUCCESS', 'FAILED'].map((status) => (
                        <button
                          key={status}
                          onClick={() => setTransactionStatusFilter(status as any)}
                          className={`px-3 py-2 text-[11px] uppercase tracking-[0.2em] font-semibold rounded-none border ${transactionStatusFilter === status ? 'border-charcoal-black bg-charcoal-black text-stone-white' : 'border-grid-line text-charcoal-black bg-stone-white hover:bg-[#F3F3F3]'}`}
                        >
                          {status === 'all' ? 'ALL' : status}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
                <div className="hidden md:grid grid-cols-[110px_1fr_200px_150px_140px_160px] gap-6 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  <div>LEDGER ID</div>
                  <div>{'SENDER ──> RECIPIENT'}</div>
                  <div>DATE & TIMESTAMP</div>
                  <div>STATUS</div>
                  <div>AMOUNT</div>
                  <div className="text-right">ACTION</div>
                </div>

                {loading ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">LOADING TRANSACTIONS…</div>
                ) : error ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-error">{error}</div>
                ) : transactions.length === 0 ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">NO RECORDED LEDGER TRANSACTIONS FOUND</div>
                ) : (
                  transactions.map((txn, index) => (
                    <div key={txn.id} className={`grid grid-cols-1 md:grid-cols-[110px_1fr_200px_150px_140px_160px] gap-4 p-6 hover:bg-[#EAEAEA]/40 items-center text-[13px] ${index < transactions.length - 1 ? 'border-b border-grid-line' : ''}`}>
                      <span className="font-mono text-medium-concrete font-bold text-[12px]">#{txn.id}</span>
                      <div className="space-y-1">
                        <div className="font-bold text-charcoal-black truncate uppercase max-w-[220px]">{txn.type}</div>
                        <div className="text-[11px] uppercase tracking-[0.1em] text-medium-concrete font-mono">{txn.sender || 'SYSTEM'} → {txn.receiver || 'SYSTEM'}</div>
                        {txn.paymentMethod && <div className="text-[11px] text-charcoal-black/70 uppercase tracking-[0.08em] font-semibold">Method: {txn.paymentMethod}</div>}
                      </div>
                      <div className="font-mono text-[12px] uppercase">{formatDate(txn.createdAt)}</div>
                      <div><span className={`text-[10px] font-bold uppercase ${txn.status === 'APPROVED' ? 'text-success' : txn.status === 'PENDING' ? 'text-warning' : txn.status === 'REJECTED' ? 'text-error' : 'text-medium-concrete'}`}>{txn.status}</span></div>
                      <div className="text-right font-mono font-bold text-[15px]">{txn.type === 'TRANSFER' || txn.type === 'WITHDRAW' ? '-' : '+'}${Number(txn.amount || 0).toFixed(2)}</div>
                      <div className="flex justify-end gap-2">
                        {txn.type === 'DEPOSIT_REQUEST' && txn.status === 'PENDING' ? (
                          <>
                            <button
                              onClick={() => void approveDepositRequest(txn.id)}
                              disabled={transactionActionInProgress === txn.id}
                              className="px-3 py-2 border border-success text-success hover:bg-success hover:text-stone-white uppercase tracking-[0.12em] text-[10px] font-bold rounded-none transition-colors duration-100 disabled:opacity-40"
                            >
                              {transactionActionInProgress === txn.id ? 'PROCESSING' : 'APPROVE'}
                            </button>
                            <button
                              onClick={() => void rejectDepositRequest(txn.id)}
                              disabled={transactionActionInProgress === txn.id}
                              className="px-3 py-2 border border-error text-error hover:bg-error hover:text-stone-white uppercase tracking-[0.12em] text-[10px] font-bold rounded-none transition-colors duration-100 disabled:opacity-40"
                            >
                              {transactionActionInProgress === txn.id ? 'PROCESSING' : 'REJECT'}
                            </button>
                          </>
                        ) : (
                          <span className="text-[11px] uppercase tracking-wider text-medium-concrete italic font-medium">NO ACTIONS</span>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === 'monitoring' && (
            <div className="space-y-6">
              <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">SUSPICIOUS TRANSACTION AUDIT QUEUE</div>

              <div className="border border-grid-line bg-stone-white">
                <div className="hidden md:grid grid-cols-[100px_1fr_220px_150px_130px_240px] gap-4 p-6 bg-concrete-gray border-b border-grid-line text-[11px] uppercase tracking-[0.2em] font-bold text-charcoal-black">
                  <div>TXN REF</div>
                  <div>INITIATOR / DETAILS</div>
                  <div>TRIGGER VIOLATION REASON</div>
                  <div>STATUS</div>
                  <div>AMOUNT</div>
                  <div className="text-right">DECISION CONTROL</div>
                </div>

                {loading ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">LOADING SUSPICIOUS TRANSACTIONS…</div>
                ) : error ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-error">{error}</div>
                ) : suspiciousTxns.length === 0 ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">No suspicious activities found</div>
                ) : (
                  suspiciousTxns.map((txn, index) => {
                    const isPending = txn.status === 'FAILED' || txn.status === 'PENDING';
                    return (
                      <div key={txn.id} className={`grid grid-cols-1 md:grid-cols-[100px_1fr_220px_150px_130px_240px] gap-4 p-6 items-center text-[13px] transition-colors duration-100 ${index < suspiciousTxns.length - 1 ? 'border-b border-grid-line' : ''} ${isPending ? 'bg-[#8B8371]/10 hover:bg-[#8B8371]/15' : 'hover:bg-[#EAEAEA]/30'}`}>
                        <span className="font-mono text-medium-concrete font-bold text-[12px]">{txn.transactionCode}</span>
                        <div className="space-y-1">
                          <div className="font-bold text-charcoal-black uppercase">{txn.sender || 'SYSTEM'}</div>
                          <div className="text-[10px] text-medium-concrete uppercase font-mono">→ {txn.receiver || 'SYSTEM'}</div>
                        </div>
                        <div className="text-[11px] font-bold text-error uppercase font-mono leading-relaxed">{txn.paymentMethod || 'Failed transaction detected'}</div>
                        <div>
                          <span className={`text-[10px] font-bold uppercase px-3 py-1 border rounded-none ${txn.status === 'APPROVED' ? 'border-success text-success bg-success/5' : txn.status === 'REJECTED' ? 'border-error text-error bg-error/5' : 'border-charcoal-black text-charcoal-black bg-stone-white'}`}>{txn.status}</span>
                        </div>
                        <div className="font-mono font-bold text-[15px] text-charcoal-black">${Number(txn.amount || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>
                        <div className="flex gap-2 justify-end">
                          {txn.status !== 'APPROVED' && txn.status !== 'REJECTED' ? (
                            <>
                              <button
                                onClick={() => void approveSuspicious(txn)}
                                disabled={suspiciousActionInProgress === txn.transactionCode}
                                className="px-3 py-1.5 border border-success hover:bg-success hover:text-stone-white text-success font-extrabold text-[10px] tracking-wider uppercase rounded-none transition-colors duration-100 disabled:opacity-40"
                              >
                                APPROVE
                              </button>
                              <button
                                onClick={() => void rejectSuspicious(txn)}
                                disabled={suspiciousActionInProgress === txn.transactionCode}
                                className="px-3 py-1.5 border border-error hover:bg-error hover:text-stone-white text-error font-extrabold text-[10px] tracking-wider uppercase rounded-none transition-colors duration-100 disabled:opacity-40"
                              >
                                REJECT
                              </button>
                            </>
                          ) : (
                            <span className="text-[11px] uppercase tracking-wider text-medium-concrete italic font-medium pr-4">SETTLED & LOGGED</span>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}

          {activeTab === 'logs' && (
            <div className="space-y-6">
              <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">SYSTEM AUDIT TERMINAL FEED (LIVE)</div>
              <div className="border border-grid-line bg-charcoal-black text-stone-white p-8 font-mono text-[12px] space-y-3 leading-relaxed rounded-none min-h-[400px]">
                <div className="text-medium-concrete border-b border-stone-white/10 pb-2 mb-4 uppercase tracking-widest text-[10px]">ARCHIVAL AUDIT FEED ──── OUTPUT TERMINAL</div>
                {loading ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">LOADING AUDIT LOGS…</div>
                ) : error ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-error">{error}</div>
                ) : auditLogs.length === 0 ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">NO AUDIT LOGS AVAILABLE</div>
                ) : (
                  auditLogs.map((log) => (
                    <div key={log.id} className="flex gap-4">
                      <span className="text-medium-concrete">[{formatDate(log.createdAt)}]</span>
                      <span className={(String(log.action ?? '').includes('DELETE') || (log.description || '').toLowerCase().includes('purged')) ? 'text-error font-bold' : 'text-stone-white/90'}>
                        {log.userName ? `${log.userName}: ` : ''}{log.description}
                      </span>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="space-y-6">
              <div className="text-[11px] uppercase tracking-[0.2em] text-medium-concrete font-bold">SYSTEM NOTIFICATIONS</div>
              <div className="border border-grid-line bg-stone-white p-8 rounded-none space-y-6">
                {notificationLoadError ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-error">{notificationLoadError}</div>
                ) : notifications.length === 0 ? (
                  <div className="py-20 text-center uppercase tracking-[0.2em] text-medium-concrete">NO NOTIFICATIONS AVAILABLE</div>
                ) : (
                  <div className="space-y-4">
                    {notifications.map((notification) => (
                      <div key={notification.id} className={`border border-grid-line p-5 ${notification.read ? 'bg-stone-white' : 'bg-concrete-gray/20'}`}>
                        <div className="text-[13px] uppercase tracking-[0.2em] text-charcoal-black font-bold">{notification.title || 'System notice'}</div>
                        <div className="mt-2 text-[15px] leading-relaxed text-charcoal-black/80">{notification.content}</div>
                        <div className="mt-4 flex items-center justify-between text-[10px] uppercase tracking-[0.25em] text-medium-concrete">
                          <span>{formatDate(notification.createdAt)}</span>
                          <span className="font-bold uppercase">{notification.read ? 'READ' : 'UNREAD'}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        <div className="border-t border-grid-line pt-6 mt-12 flex justify-between items-center text-[10px] uppercase tracking-[0.25em] text-medium-concrete font-bold font-mono">
          <span>OPERATOR HOST: WALLET-MGMT-NODE-TOKYO</span>
          <span>SYSTEM TIME: {new Date().toISOString().substring(0, 10)} {new Date().toLocaleTimeString()}</span>
        </div>
      </main>
    </div>
  );
}
