import { defineStore } from "pinia";

const TOKEN_KEY = "model_hub_token";
const USERNAME_KEY = "model_hub_username";
const USER_ID_KEY = "model_hub_user_id";

type AuthPayload = {
  token: string;
  userId: number;
  username: string;
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) ?? "",
    username: localStorage.getItem(USERNAME_KEY) ?? "",
    userId: Number(localStorage.getItem(USER_ID_KEY) ?? 0),
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    setAuth(payload: AuthPayload) {
      this.token = payload.token;
      this.userId = payload.userId;
      this.username = payload.username;
      localStorage.setItem(TOKEN_KEY, payload.token);
      localStorage.setItem(USERNAME_KEY, payload.username);
      localStorage.setItem(USER_ID_KEY, String(payload.userId));
    },
    logout() {
      this.token = "";
      this.userId = 0;
      this.username = "";
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USERNAME_KEY);
      localStorage.removeItem(USER_ID_KEY);
    },
  },
});

