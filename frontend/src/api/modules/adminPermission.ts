import http, { unwrap } from "../http";
import type { ApiResourceItem, ApiResponse, MenuResourceItem } from "../../types";

export async function listMenuResources() {
  const res = await http.get<ApiResponse<MenuResourceItem[]>>("/api/admin/permissions/menus");
  return unwrap(res.data);
}

export async function listApiResources() {
  const res = await http.get<ApiResponse<ApiResourceItem[]>>("/api/admin/permissions/apis");
  return unwrap(res.data);
}
