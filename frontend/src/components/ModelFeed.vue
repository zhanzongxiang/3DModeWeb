<template>
  <section class="space-y-6">
    <article v-if="showBanner" class="surface-panel overflow-hidden p-0">
      <div class="grid gap-0 md:grid-cols-[1.2fr_1fr]">
        <div class="p-6 md:p-8">
          <p class="mb-3 text-sm font-semibold text-brand-green">推荐模型</p>
          <h1 class="text-3xl font-bold tracking-tight text-text-main md:text-4xl">
            {{ bannerTitle }}
          </h1>
          <p class="mt-3 text-sm text-text-sub">{{ bannerSubtitle }}</p>

          <div class="mt-6 flex items-center gap-3">
            <a :href="bannerLink" target="_blank" rel="noreferrer" class="btn-primary">
              查看模型
            </a>
            <RouterLink to="/hall" class="btn-secondary">浏览全部</RouterLink>
          </div>
        </div>

        <div class="bg-neutral-bg p-3">
          <img
            v-if="bannerImage"
            :src="bannerImage"
            :alt="bannerTitle"
            class="h-56 w-full rounded-2xl object-cover md:h-full"
          />
          <div v-else class="flex h-56 items-center justify-center rounded-2xl bg-white text-text-sub md:h-full">
            推荐位
          </div>
        </div>
      </div>
    </article>

    <div class="surface-panel p-5 md:p-6">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 class="text-xl font-bold text-text-main md:text-2xl">
            {{ showBanner ? "推荐模型列表" : "模型列表" }}
          </h2>
          <p class="mt-1 text-sm text-text-sub">共 {{ list.total }} 个模型，支持关键词和类型筛选。</p>
        </div>
        <div class="text-sm text-text-sub">当前第 {{ list.page }} / {{ totalPages }} 页</div>
      </div>
    </div>

    <section>
      <p v-if="error" class="mb-4 text-sm text-red-500">{{ error }}</p>

      <div v-if="loading" class="card-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else class="card-grid">
        <ModelCard v-for="item in list.items" :key="item.id" :item="item" />
      </div>
    </section>

    <footer class="surface-panel flex items-center justify-between p-4">
      <button class="btn-secondary" :disabled="list.page <= 1" @click="refresh(list.page - 1)">上一页</button>
      <p class="text-sm text-text-sub">第 {{ list.page }} 页，共 {{ totalPages }} 页</p>
      <button class="btn-secondary" :disabled="list.page >= totalPages" @click="refresh(list.page + 1)">
        下一页
      </button>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import ModelCard from "./ModelCard.vue";
import { fetchModels } from "../api/modules/model";
import type { ModelListData } from "../types";

defineProps<{
  showBanner: boolean;
}>();

const route = useRoute();
const loading = ref(false);
const error = ref("");
const keyword = ref("");
const typeFilter = ref("全部");

const list = reactive<ModelListData>({
  items: [],
  total: 0,
  page: 1,
  size: 20,
});

const totalPages = computed(() => Math.max(1, Math.ceil(list.total / list.size)));

const bannerModel = computed(() => (list.items.length ? list.items[0] : null));
const bannerTitle = computed(() => bannerModel.value?.name ?? "Parrot Puzzle 风格推荐位");
const bannerSubtitle = computed(
  () => bannerModel.value?.artworkName ?? "首页展示推荐模型 Banner，其他页面仅保留列表视图。",
);
const bannerLink = computed(() => bannerModel.value?.diskLink ?? "/hall");
const bannerImage = computed(() => {
  if (!bannerModel.value) {
    return "";
  }
  try {
    const urls = JSON.parse(bannerModel.value.imageUrls);
    return Array.isArray(urls) && urls.length ? String(urls[0]) : "";
  } catch {
    return "";
  }
});

watch(
  () => [route.query.keyword, route.query.type],
  () => {
    keyword.value = String(route.query.keyword ?? "").trim();
    typeFilter.value = String(route.query.type ?? "全部");
    refresh(1);
  },
  { immediate: true },
);

async function refresh(page = 1) {
  error.value = "";
  loading.value = true;
  try {
    const params = {
      page,
      size: list.size,
      type: typeFilter.value !== "全部" ? typeFilter.value : undefined,
      name: keyword.value || undefined,
      artwork_name: keyword.value || undefined,
    };
    const data = await fetchModels(params);
    list.items = data.items;
    list.page = data.page;
    list.size = data.size;
    list.total = data.total;
  } catch (e) {
    error.value = e instanceof Error ? e.message : "加载失败";
  } finally {
    loading.value = false;
  }
}
</script>
