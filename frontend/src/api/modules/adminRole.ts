import http, { unwrap } from "../http";
import type { ApiResponse, PageResult, RoleItem } from "../../types";

export async function listRoles(params: { page?: number; size?: number; keyword?: string } = {}) {
  const res = await http.get<ApiResponse<PageResult<RoleItem>>>("/api/admin/roles", { params });
  return unwrap(res.data);
}

export async function getRoleDetail(id: number) {
  const res = await http.get<ApiResponse<RoleItem>>(`/api/admin/roles/${id}`);
  return unwrap(res.data);
}

export async function createRole(payload: {
  name: string;
  code: string;
  description?: string;
  status?: number;
  dataScopeType?: string;
}) {
  const res = await http.post<ApiResponse<null>>("/api/admin/roles", payload);
  return unwrap(res.data);
}

export async function updateRole(
  id: number,
  payload: { name: string; description?: string; dataScopeType?: string },
) {
  const res = await http.put<ApiResponse<null>>(`/api/admin/roles/${id}`, payload);
  return unwrap(res.data);
}

export async function updateRoleStatus(id: number, status: number) {
  const res = await http.patch<ApiResponse<null>>(`/api/admin/roles/${id}/status`, { status });
  return unwrap(res.data);
}

export async function updateRolePermissions(
  id: number,
  payload: { dataScopeType?: string; menuIds?: number[]; apiIds?: number[]; customOrgIds?: number[] },
) {
  const res = await http.put<ApiResponse<null>>(`/api/admin/roles/${id}/permissions`, payload);
  return unwrap(res.data);
}

export async function deleteRole(id: number) {
  const res = await http.delete<ApiResponse<null>>(`/api/admin/roles/${id}`);
  return unwrap(res.data);
}
