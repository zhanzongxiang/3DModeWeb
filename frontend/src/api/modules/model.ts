import http, { unwrap } from "../http";
import type { ApiResponse, CreateModelRequest, ModelListData } from "../../types";

export async function fetchModels(params: {
  page: number;
  size: number;
  type?: string;
  name?: string;
  artwork_name?: string;
}) {
  const res = await http.get<ApiResponse<ModelListData>>("/api/models", { params });
  return unwrap(res.data);
}

export async function createModel(payload: CreateModelRequest) {
  const res = await http.post<ApiResponse<null>>("/api/models", payload);
  return unwrap(res.data);
}

