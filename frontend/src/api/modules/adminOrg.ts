import http, { unwrap } from "../http";
import type { ApiResponse, OrgNode } from "../../types";

export async function listOrgTree() {
  const res = await http.get<ApiResponse<OrgNode[]>>("/api/admin/orgs/tree");
  return unwrap(res.data);
}

export async function createOrg(payload: {
  parentId?: number;
  name: string;
  code?: string;
  leaderUserId?: number;
  sort?: number;
  status?: number;
}) {
  const res = await http.post<ApiResponse<null>>("/api/admin/orgs", payload);
  return unwrap(res.data);
}

export async function updateOrg(
  id: number,
  payload: { parentId?: number; name: string; code?: string; leaderUserId?: number; sort?: number },
) {
  const res = await http.put<ApiResponse<null>>(`/api/admin/orgs/${id}`, payload);
  return unwrap(res.data);
}

export async function updateOrgStatus(id: number, status: number) {
  const res = await http.patch<ApiResponse<null>>(`/api/admin/orgs/${id}/status`, { status });
  return unwrap(res.data);
}

export async function deleteOrg(id: number) {
  const res = await http.delete<ApiResponse<null>>(`/api/admin/orgs/${id}`);
  return unwrap(res.data);
}
