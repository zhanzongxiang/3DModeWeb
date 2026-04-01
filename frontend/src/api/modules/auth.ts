import http, { unwrap } from "../http";
import type {
  AccountSecurityProfileData,
  ApiResponse,
  ForgotPasswordCodeData,
  ForgotPasswordCodeRequest,
  ForgotPasswordResetRequest,
  LoginData,
  LoginRequest,
  RegisterRequest,
} from "../../types";

export async function register(data: RegisterRequest) {
  const res = await http.post<ApiResponse<null>>("/api/auth/register", data);
  return unwrap(res.data);
}

export async function login(data: LoginRequest) {
  const res = await http.post<ApiResponse<LoginData>>("/api/auth/login", data);
  return unwrap(res.data);
}

export async function sendForgotPasswordCode(data: ForgotPasswordCodeRequest) {
  const res = await http.post<ApiResponse<ForgotPasswordCodeData>>("/api/auth/forgot-password/code", data);
  return unwrap(res.data);
}

export async function resetPasswordByCode(data: ForgotPasswordResetRequest) {
  const res = await http.post<ApiResponse<null>>("/api/auth/forgot-password/reset", data);
  return unwrap(res.data);
}

export async function getAccountSecurityProfile() {
  const res = await http.get<ApiResponse<AccountSecurityProfileData>>("/api/auth/security/profile");
  return unwrap(res.data);
}

export async function changePassword(data: { oldPassword: string; newPassword: string }) {
  const res = await http.post<ApiResponse<null>>("/api/auth/change-password", data);
  return unwrap(res.data);
}

export async function sendContactVerificationCode(data: { type: "EMAIL" | "MOBILE"; target: string; captchaToken?: string }) {
  const res = await http.post<ApiResponse<ForgotPasswordCodeData>>("/api/auth/contact-verification/code", data);
  return unwrap(res.data);
}

export async function verifyContactCode(data: { type: "EMAIL" | "MOBILE"; target: string; code: string }) {
  const res = await http.post<ApiResponse<null>>("/api/auth/contact-verification/verify", data);
  return unwrap(res.data);
}

export async function cancelAccount(data: { password: string }) {
  const res = await http.post<ApiResponse<null>>("/api/auth/cancel-account", data);
  return unwrap(res.data);
}
