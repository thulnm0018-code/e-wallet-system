export interface ApiResponse<T> {
  message?: string;
  data: T;
}

export interface PaginatedResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AdminDashboardSummary {
  totalUsers: number;
  activeUsers: number;
  lockedUsers: number;
  activeWallets: number;
  totalTransactions: number;
  totalVolume: string;
  totalRevenue: string;
  pendingReviews: number;
}

export interface AdminDashboardStatsResponse {
  dashboard: AdminDashboardSummary;
  redisTelemetry: {
    hitRate: number;
    memoryMb: number;
    connectedClients: number;
  };
  rabbitTelemetry: {
    incomingRate: number;
    ready: number;
    unacked: number;
    activeConsumers: number;
  };
  deployments: {
    apiGateway: string;
    authService: string;
    database: string;
    rabbitmq: string;
  };
}

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  userStatus: string;
  balance: number;
  address?: string;
  dateOfBirth?: string;
  createdAt: string;
  walletStatus?: string;
}

export interface AdminTransaction {
  id: number;
  transactionCode: string;
  sender: string | null;
  receiver: string | null;
  amount: number;
  type: string;
  status: string;
  paymentMethod?: string;
  approvedBy?: number;
  approvedAt?: string;
  createdAt: string;
}

export interface SuspiciousActivity {
  id: string;
  transactionCode: string;
  sender: string | null;
  receiver: string | null;
  ruleTriggered?: string;
  riskLevel?: string;
  createdAt: string;
  status: string;
}

export interface AuditLogEntry {
  id: number;
  userId?: number;
  userName?: string;
  action?: string;
  description?: string;
  createdAt?: string;
  entity?: string;
  status?: string;
  ipAddress?: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  content: string;
  read: boolean;
  createdAt: string;
  receiverUser?: string;
}
