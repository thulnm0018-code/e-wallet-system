import api from '../../api';
import type { ApiResponse, PaginatedResponse, AdminUser } from './adminTypes';

export async function fetchAdminUsers(
  page = 0,
  size = 20,
  keyword?: string,
  status?: string
): Promise<PaginatedResponse<AdminUser>> {

  const response = await api.get<
    ApiResponse<PaginatedResponse<AdminUser>>
  >('/admin/users', {
    params: {
      page,
      size,
      keyword,
      status,
    },
  });

  console.log('USERS API RESPONSE:', response);

  return response.data.data;
}


export async function deleteAdminUser(userId: number): Promise<void> {
  await api.delete<ApiResponse<void>>(`/users/admin/users/${userId}`);
}

export async function lockAdminUser(userId: number): Promise<void> {
  await api.put<ApiResponse<void>>(`/users/admin/users/${userId}/lock`);
}

export async function unlockAdminUser(userId: number): Promise<void> {
  await api.put<ApiResponse<void>>(`/users/admin/users/${userId}/unlock`);
}
