import api from '../../api';
import type { ApiResponse, NotificationItem } from './adminTypes';

export async function fetchNotifications(): Promise<NotificationItem[]> {
  const response = await api.get<ApiResponse<NotificationItem[]>>('/notifications');
  return response.data.data;
}

export async function markNotificationRead(notificationId: number): Promise<void> {
  await api.patch<ApiResponse<void>>(`/notifications/${notificationId}/read`);
}

export async function markAllNotificationsRead(notificationIds: number[]): Promise<void> {
  await Promise.all(notificationIds.map((id) => api.patch<ApiResponse<void>>(`/notifications/${id}/read`)));
}
