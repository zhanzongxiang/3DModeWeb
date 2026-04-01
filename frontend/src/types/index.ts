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
  orgId?: number;
  roles?: string[];
  permissions?: string[];
}

export interface RegisterRequest {
  username: string;
  password: string;
  captchaToken?: string;
}

export interface ForgotPasswordCodeRequest {
  username: string;
  captchaToken?: string;
}

export interface ForgotPasswordCodeData {
  expiresInSeconds: number;
  devCode?: string;
}

export interface ForgotPasswordResetRequest {
  username: string;
  code: string;
  newPassword: string;
  captchaToken?: string;
}

export interface AccountSecurityProfileData {
  username: string;
  email?: string;
  emailVerified: number;
  mobile?: string;
  mobileVerified: number;
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
  printLayerHeight?: number;
  printInfill?: number;
  printSupport?: number;
  printMaterial?: string;
  licenseType?: string;
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
  printLayerHeight?: number;
  printInfill?: number;
  printSupport?: number;
  printMaterial?: string;
  licenseType?: string;
}

export interface OssUploadData {
  url: string;
}

export interface MakeItem {
  id: number;
  modelId: number;
  userId: number;
  imageUrl: string;
  description?: string;
  createTime: string;
}

export interface CollectionToggleData {
  collected: boolean;
}

export interface CollectionItem {
  id: number;
  userId: number;
  modelId: number;
  createTime: string;
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface OrgNode {
  id: number;
  parentId: number;
  name: string;
  code?: string;
  leaderUserId?: number;
  status: number;
  sort: number;
  ancestors: string;
  children: OrgNode[];
}

export interface RoleItem {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: number;
  dataScopeType: string;
  isSystem: number;
  menuIds: number[];
  apiIds: number[];
  customOrgIds: number[];
}

export interface AdminUserItem {
  id: number;
  username: string;
  nickname?: string;
  realName?: string;
  mobile?: string;
  email?: string;
  status: number;
  orgId?: number;
  lastLoginTime?: string;
  createTime: string;
  updateTime: string;
  roleIds: number[];
  roleCodes: string[];
}

export interface MenuResourceItem {
  id: number;
  parentId: number;
  name: string;
  path?: string;
  component?: string;
  permCode?: string;
  sort: number;
  visible: number;
  status: number;
}

export interface ApiResourceItem {
  id: number;
  name: string;
  path: string;
  method: string;
  permCode: string;
  status: number;
}

export interface AdminCurrentUser {
  userId: number;
  username: string;
  orgId?: number;
  roles: string[];
  permissions: string[];
}
