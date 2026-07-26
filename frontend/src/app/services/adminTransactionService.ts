import api from '../../api';
import type { ApiResponse, PaginatedResponse, AdminTransaction } from './adminTypes';

export async function fetchAdminTransactions(
  page = 0,
  size = 20,
  status: string = 'all'
): Promise<PaginatedResponse<AdminTransaction>> {

  const response = await api.get<
    ApiResponse<PaginatedResponse<AdminTransaction>>
  >('/admin/transactions', {
    params: {
      page,
      size,
      status: status === 'all' ? undefined : status,
    },
  });

  console.log('TX API RESPONSE:', response);

  return response.data.data;
}
export async function approveAdminDepositRequest(transactionId: number): Promise<void> {
  await api.post<ApiResponse<void>>(`/admin/transactions/${transactionId}/approve`);
}

export async function rejectAdminDepositRequest(transactionId: number): Promise<void> {
  await api.post<ApiResponse<void>>(`/admin/transactions/${transactionId}/reject`);
}
