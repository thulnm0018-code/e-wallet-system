import api from '../../api';
import type { ApiResponse, AuditLogEntry } from './adminTypes';

export async function fetchAuditLogs(): Promise<AuditLogEntry[]> {
  const response = await api.get<ApiResponse<AuditLogEntry[]>>('/admin/logs');
  return response.data.data;
}
