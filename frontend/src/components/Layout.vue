<template>
  <div class="page-bg min-h-screen">
    <aside
      class="fixed inset-y-0 left-0 z-40 hidden flex-col border-r border-neutral-border bg-white md:flex"
      :class="collapsed ? 'w-20' : 'w-64'"
    >
      <div class="flex h-16 items-center justify-between border-b border-neutral-border px-4">
        <RouterLink to="/" class="font-display text-base font-bold text-text-main" :class="collapsed ? 'hidden' : ''">
          Model Hub
        </RouterLink>
        <button class="btn-secondary h-8 w-8 !p-0 text-xs" @click="collapsed = !collapsed">
          {{ collapsed ? ">" : "<" }}
        </button>
      </div>

      <nav class="flex-1 space-y-1 px-3 py-4">
        <RouterLink class="side-item" :class="collapsed ? 'justify-center' : ''" to="/">
          <span class="side-icon">R</span>
          <span v-if="!collapsed">推荐</span>
        </RouterLink>
        <RouterLink class="side-item" :class="collapsed ? 'justify-center' : ''" to="/hall">
          <span class="side-icon">E</span>
          <span v-if="!collapsed">发现</span>
        </RouterLink>
        <RouterLink class="side-item" :class="collapsed ? 'justify-center' : ''" to="/upload">
          <span class="side-icon">U</span>
          <span v-if="!collapsed">上传</span>
        </RouterLink>
        <RouterLink
          v-if="!authStore.isLoggedIn"
          class="side-item"
          :class="collapsed ? 'justify-center' : ''"
          to="/login"
        >
          <span class="side-icon">L</span>
          <span v-if="!collapsed">登录</span>
        </RouterLink>
      </nav>

      <div class="space-y-2 border-t border-neutral-border px-3 py-3">
        <div class="flex gap-2" :class="collapsed ? 'justify-center' : ''">
          <a class="social-dot" href="https://github.com" target="_blank" rel="noreferrer">G</a>
          <a class="social-dot" href="https://x.com" target="_blank" rel="noreferrer">X</a>
          <a class="social-dot" href="https://discord.com" target="_blank" rel="noreferrer">D</a>
        </div>
        <p v-if="!collapsed" class="text-center text-xs text-text-sub">© 2026 Model Hub</p>
      </div>
    </aside>

    <div class="transition-all duration-200" :class="collapsed ? 'md:pl-20' : 'md:pl-64'">
      <header class="header-nav sticky top-0 z-30">
        <div class="page-container py-3">
          <div class="flex items-center gap-3">
            <RouterLink to="/" class="brand-title md:hidden">Model Hub</RouterLink>

            <form class="mx-auto w-full max-w-[760px]" @submit.prevent="submitSearch">
              <input
                v-model="keyword"
                class="search-input"
                placeholder="搜索模型名称 / 作品名 / 类型"
              />
            </form>

            <nav class="hidden items-center gap-2 lg:flex">
              <button class="tab-link" :class="activeType === '全部' ? 'tab-link-active' : ''" @click="setType('全部')">
                全部
              </button>
              <button class="tab-link" :class="activeType === '角色' ? 'tab-link-active' : ''" @click="setType('角色')">
                角色
              </button>
              <button class="tab-link" :class="activeType === '场景' ? 'tab-link-active' : ''" @click="setType('场景')">
                场景
              </button>
            </nav>
          </div>
        </div>
      </header>

      <main class="page-container py-6 md:py-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const collapsed = ref(false);
const keyword = ref("");

const activeType = computed(() => String(route.query.type ?? "全部"));

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = String(value ?? "");
  },
  { immediate: true },
);

function submitSearch() {
  const query: Record<string, string> = {};
  const text = keyword.value.trim();
  if (text) {
    query.keyword = text;
  }
  if (activeType.value !== "全部") {
    query.type = activeType.value;
  }
  router.push({ path: "/hall", query });
}

function setType(type: string) {
  const query: Record<string, string> = {};
  const text = keyword.value.trim();
  if (text) {
    query.keyword = text;
  }
  if (type !== "全部") {
    query.type = type;
  }
  router.push({ path: "/hall", query });
}
</script>
