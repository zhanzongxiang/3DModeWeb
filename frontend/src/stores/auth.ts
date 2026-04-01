import { defineStore } from "pinia";

const TOKEN_KEY = "model_hub_token";
const USERNAME_KEY = "model_hub_username";
const USER_ID_KEY = "model_hub_user_id";
const ORG_ID_KEY = "model_hub_org_id";
const ROLES_KEY = "model_hub_roles";
const PERMS_KEY = "model_hub_perms";

type AuthPayload = {
  token: string;
  userId: number;
  username: string;
  orgId?: number;
  roles?: string[];
  permissions?: string[];
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) ?? "",
    username: localStorage.getItem(USERNAME_KEY) ?? "",
    userId: Number(localStorage.getItem(USER_ID_KEY) ?? 0),
    orgId: Number(localStorage.getItem(ORG_ID_KEY) ?? 0),
    roles: JSON.parse(localStorage.getItem(ROLES_KEY) ?? "[]") as string[],
    permissions: JSON.parse(localStorage.getItem(PERMS_KEY) ?? "[]") as string[],
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    hasPermission: (state) => (code: string) => state.permissions.includes(code),
    isAdmin: (state) => state.roles.includes("SUPER_ADMIN") || state.permissions.includes("sys:auth:me"),
  },
  actions: {
    setAuth(payload: AuthPayload) {
      this.token = payload.token;
      this.userId = payload.userId;
      this.username = payload.username;
      this.orgId = payload.orgId ?? 0;
      this.roles = payload.roles ?? [];
      this.permissions = payload.permissions ?? [];
      localStorage.setItem(TOKEN_KEY, payload.token);
      localStorage.setItem(USERNAME_KEY, payload.username);
      localStorage.setItem(USER_ID_KEY, String(payload.userId));
      localStorage.setItem(ORG_ID_KEY, String(this.orgId));
      localStorage.setItem(ROLES_KEY, JSON.stringify(this.roles));
      localStorage.setItem(PERMS_KEY, JSON.stringify(this.permissions));
    },
    logout() {
      this.token = "";
      this.userId = 0;
      this.username = "";
      this.orgId = 0;
      this.roles = [];
      this.permissions = [];
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USERNAME_KEY);
      localStorage.removeItem(USER_ID_KEY);
      localStorage.removeItem(ORG_ID_KEY);
      localStorage.removeItem(ROLES_KEY);
      localStorage.removeItem(PERMS_KEY);
    },
  },
});
