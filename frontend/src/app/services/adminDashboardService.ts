import api from '../../api';
import type { ApiResponse, AdminDashboardSummary, AdminDashboardStatsResponse, PaginatedResponse, AdminTransaction } from './adminTypes';

export async function fetchAdminDashboard(): Promise<AdminDashboardSummary> {
  const response = await api.get<ApiResponse<AdminDashboardSummary>>('/admin/dashboard');
  return response.data.data;
}

export async function fetchAdminDashboardStats(): Promise<AdminDashboardStatsResponse> {
  const response = await api.get<ApiResponse<AdminDashboardStatsResponse>>('/admin/dashboard/stats');
  return response.data.data;
}

export async function fetchAdminTransactionCount(status: string): Promise<number> {
  const response = await api.get<ApiResponse<PaginatedResponse<AdminTransaction>>>('/admin/transactions', {
    params: {
      page: 0,
      size: 1,
      status: status === 'all' ? undefined : status,
    },
  });
  return response.data.data.totalElements;
}
