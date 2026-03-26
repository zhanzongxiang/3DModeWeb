<template>
  <div class="app-shell flex">
    <aside
      class="left-rail hidden shrink-0 flex-col overflow-hidden md:flex"
      :class="collapsed ? 'w-16' : 'w-[240px]'"
    >
      <div class="flex h-16 items-center gap-2 px-4">
        <button class="btn-secondary h-7 w-7 !p-0 text-[12px]" @click="collapsed = !collapsed">[]</button>
        <span v-if="!collapsed" class="text-[14px] font-semibold text-primary-text">3D Model Hub</span>
      </div>

      <nav class="space-y-1 px-3 py-2">
        <RouterLink class="rail-item" :class="collapsed ? 'justify-center' : ''" to="/">
          <span class="rail-icon">R</span>
          <span v-if="!collapsed">推荐</span>
        </RouterLink>
        <RouterLink class="rail-item" :class="collapsed ? 'justify-center' : ''" to="/hall">
          <span class="rail-icon">H</span>
          <span v-if="!collapsed">全部模型</span>
        </RouterLink>
        <RouterLink class="rail-item" :class="collapsed ? 'justify-center' : ''" to="/upload">
          <span class="rail-icon">U</span>
          <span v-if="!collapsed">上传</span>
        </RouterLink>
        <RouterLink
          v-if="authStore.isLoggedIn"
          class="rail-item"
          :class="collapsed ? 'justify-center' : ''"
          to="/collections"
        >
          <span class="rail-icon">C</span>
          <span v-if="!collapsed">我的收藏</span>
        </RouterLink>
        <RouterLink v-if="!authStore.isLoggedIn" class="rail-item" :class="collapsed ? 'justify-center' : ''" to="/login">
          <span class="rail-icon">L</span>
          <span v-if="!collapsed">登录</span>
        </RouterLink>
      </nav>

      <div class="mt-auto border-t border-soft-border px-3 py-3">
        <div class="flex items-center gap-2" :class="collapsed ? 'justify-center' : ''">
          <a class="rail-icon" href="https://github.com" target="_blank" rel="noreferrer">G</a>
          <a class="rail-icon" href="https://x.com" target="_blank" rel="noreferrer">X</a>
          <a class="rail-icon" href="https://discord.com" target="_blank" rel="noreferrer">D</a>
        </div>
        <p v-if="!collapsed" class="mt-2 text-center text-[12px] text-muted">© 2026 Model Hub</p>
      </div>
    </aside>

    <section class="flex min-w-0 flex-1 flex-col overflow-hidden">
      <header class="header-wrap">
        <div class="flex h-16 items-center gap-3 px-4 md:px-6">
          <RouterLink to="/" class="text-[14px] font-semibold text-primary-text md:hidden">Model Hub</RouterLink>

          <form class="w-full max-w-[820px]" @submit.prevent="submitSearch">
            <div class="search-box">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="M20 20L17 17"></path>
              </svg>
              <input
                v-model="keyword"
                class="search-input"
                placeholder="搜索模型名称 / 作品名 / 类型"
              />
              <button class="camera-btn" type="button">
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 7h3l2-2h6l2 2h3v12H4z"></path>
                  <circle cx="12" cy="13" r="3"></circle>
                </svg>
              </button>
            </div>
          </form>
        </div>

        <div class="flex items-center gap-2 overflow-x-auto px-4 pb-3 md:px-6">
          <button class="chip whitespace-nowrap" :class="activeType === '推荐' ? 'chip-active' : ''" @click="setType('推荐')">
            推荐
          </button>
          <button class="chip whitespace-nowrap" :class="activeType === '热门' ? 'chip-active' : ''" @click="setType('热门')">
            热门
          </button>
          <button class="chip whitespace-nowrap" :class="activeType === '角色' ? 'chip-active' : ''" @click="setType('角色')">
            角色
          </button>
          <button class="chip whitespace-nowrap" :class="activeType === '场景' ? 'chip-active' : ''" @click="setType('场景')">
            场景
          </button>
          <button class="chip whitespace-nowrap" :class="activeType === '道具' ? 'chip-active' : ''" @click="setType('道具')">
            道具
          </button>
          <button class="chip whitespace-nowrap" :class="activeType === '全部' ? 'chip-active' : ''" @click="setType('全部')">
            全部
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto px-4 py-6 md:px-6 md:py-8">
        <RouterView />
      </main>
    </section>
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
  if (type === "推荐") {
    router.push({ path: "/", query });
    return;
  }
  router.push({ path: "/hall", query });
}
</script>
