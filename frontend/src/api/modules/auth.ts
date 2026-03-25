import http, { unwrap } from "../http";
import type { ApiResponse, LoginData, LoginRequest, RegisterRequest } from "../../types";

export async function register(data: RegisterRequest) {
  const res = await http.post<ApiResponse<null>>("/api/auth/register", data);
  return unwrap(res.data);
}

export async function login(data: LoginRequest) {
  const res = await http.post<ApiResponse<LoginData>>("/api/auth/login", data);
  return unwrap(res.data);
}

