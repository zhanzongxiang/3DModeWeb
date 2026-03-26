import { createRouter, createWebHistory } from "vue-router";
import RecommendView from "../views/RecommendView.vue";
import HallView from "../views/HallView.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import UploadView from "../views/UploadView.vue";
import ModelDetailView from "../views/ModelDetailView.vue";
import MyCollectionsView from "../views/MyCollectionsView.vue";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: RecommendView },
    { path: "/hall", component: HallView },
    { path: "/models/:id", component: ModelDetailView },
    { path: "/collections", component: MyCollectionsView, meta: { requiresAuth: true } },
    { path: "/login", component: LoginView },
    { path: "/register", component: RegisterView },
    { path: "/upload", component: UploadView, meta: { requiresAuth: true } },
  ],
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return "/login";
  }
  return true;
});

export default router;
