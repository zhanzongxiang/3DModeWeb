import http, { unwrap } from "../http";
import type { ApiResponse, CollectionItem, CollectionToggleData } from "../../types";

export async function toggleCollection(modelId: number) {
  const res = await http.post<ApiResponse<CollectionToggleData>>("/api/collections/toggle", { modelId });
  return unwrap(res.data);
}

export async function getCollectionStatus(modelId: number) {
  const res = await http.get<ApiResponse<CollectionToggleData>>("/api/collections/status", { params: { modelId } });
  return unwrap(res.data);
}

export async function listMyCollections() {
  const res = await http.get<ApiResponse<CollectionItem[]>>("/api/collections/me");
  return unwrap(res.data);
}
