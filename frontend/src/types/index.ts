export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginRequest {
  username: string;
  password: string;
  captchaToken?: string;
}

export interface LoginData {
  token: string;
  expiresAt: number;
  userId: number;
  username: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface ModelItem {
  id: number;
  userId: number;
  name: string;
  artworkName: string;
  type: string;
  imageUrls: string;
  diskLink: string;
  isFree: number;
  createTime: string;
}

export interface ModelListData {
  items: ModelItem[];
  total: number;
  page: number;
  size: number;
}

export interface CreateModelRequest {
  name: string;
  artworkName: string;
  type: string;
  imageUrls: string;
  diskLink: string;
  isFree: number;
}

export interface OssUploadData {
  url: string;
}

