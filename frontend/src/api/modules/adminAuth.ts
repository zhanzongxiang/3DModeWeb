import http, { unwrap } from "../http";
import type { AdminCurrentUser, ApiResponse } from "../../types";

export async function fetchAdminMe() {
  const res = await http.get<ApiResponse<AdminCurrentUser>>("/api/admin/auth/me");
  return unwrap(res.data);
}
