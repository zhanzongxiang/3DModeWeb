import http, { unwrap } from "../http";
import type { ApiResponse, MakeItem } from "../../types";

export async function listMakesByModel(modelId: number) {
  const res = await http.get<ApiResponse<MakeItem[]>>("/api/makes", { params: { modelId } });
  return unwrap(res.data);
}

export async function createMake(payload: { modelId: number; imageUrl: string; description?: string }) {
  const res = await http.post<ApiResponse<null>>("/api/makes", payload);
  return unwrap(res.data);
}
