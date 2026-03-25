import http, { unwrap } from "../http";
import type { ApiResponse, OssUploadData } from "../../types";

export async function uploadImage(file: File) {
  const formData = new FormData();
  formData.append("file", file);
  const res = await http.post<ApiResponse<OssUploadData>>("/api/oss/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return unwrap(res.data);
}

