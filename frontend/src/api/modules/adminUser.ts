import http, { unwrap } from "../http";
import type { AdminUserItem, ApiResponse, PageResult } from "../../types";

export async function listAdminUsers(params: {
  page?: number;
  size?: number;
  keyword?: string;
  orgId?: number;
  status?: number;
} = {}) {
  const res = await http.get<ApiResponse<PageResult<AdminUserItem>>>("/api/admin/users", { params });
  return unwrap(res.data);
}

export async function createAdminUser(payload: {
  username: string;
  password: string;
  nickname?: string;
  realName?: string;
  mobile?: string;
  email?: string;
  status?: number;
  orgId?: number;
  roleIds?: number[];
}) {
  const res = await http.post<ApiResponse<null>>("/api/admin/users", payload);
  return unwrap(res.data);
}

export async function updateAdminUser(
  id: number,
  payload: { nickname?: string; realName?: string; mobile?: string; email?: string; orgId?: number },
) {
  const res = await http.put<ApiResponse<null>>(`/api/admin/users/${id}`, payload);
  return unwrap(res.data);
}

export async function updateAdminUserStatus(id: number, status: number) {
  const res = await http.patch<ApiResponse<null>>(`/api/admin/users/${id}/status`, { status });
  return unwrap(res.data);
}

export async function resetAdminUserPassword(id: number, newPassword: string) {
  const res = await http.post<ApiResponse<null>>(`/api/admin/users/${id}/reset-password`, { newPassword });
  return unwrap(res.data);
}

export async function assignAdminUserRoles(id: number, roleIds: number[]) {
  const res = await http.put<ApiResponse<null>>(`/api/admin/users/${id}/roles`, { roleIds });
  return unwrap(res.data);
}
