import api from '../../api';
import type { ApiResponse, AdminTransaction } from './adminTypes';

export async function fetchSuspiciousActivities(): Promise<AdminTransaction[]> {
  const response = await api.get<ApiResponse<AdminTransaction[]>>('/admin/monitoring/suspicious');
  return response.data.data;
}

export async function approveSuspiciousTransaction(transactionCode: string): Promise<void> {
  await api.post<ApiResponse<void>>(`/admin/monitoring/suspicious/${transactionCode}/approve`);
}

export async function rejectSuspiciousTransaction(transactionCode: string): Promise<void> {
  await api.post<ApiResponse<void>>(`/admin/monitoring/suspicious/${transactionCode}/reject`);
}
