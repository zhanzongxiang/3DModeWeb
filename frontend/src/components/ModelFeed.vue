<template>
  <section class="space-y-6">
    <article v-if="showBanner" class="surface-panel overflow-hidden p-3 md:p-4">
      <div class="grid gap-3 md:grid-cols-[65%_35%]">
        <div class="relative">
          <img
            v-if="bannerImage"
            :src="bannerImage"
            :alt="bannerTitle"
            class="h-64 w-full rounded-card object-cover md:h-[330px]"
            loading="eager"
            decoding="async"
          />
          <div v-else class="flex h-64 items-center justify-center rounded-card bg-gray-100 text-muted md:h-[330px]">
            推荐 Banner
          </div>
          <div class="absolute bottom-4 left-4 rounded-full bg-white/90 px-3 py-1 text-[12px] font-semibold text-primary-text">
            推荐 · {{ bannerType }}
          </div>
        </div>

        <div class="rounded-card bg-gradient-to-br from-[#e7ffe7] via-[#ebffee] to-[#f5fff5] p-4">
          <div class="mb-3">
            <p class="text-[14px] font-semibold text-primary-text">探索更多</p>
            <p class="mt-1 text-[12px] text-muted">MakerLab / 创客宝库 / 社区精选</p>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <button class="rounded-card bg-white/90 px-3 py-4 text-left text-[12px] font-semibold text-primary-text">MakerLab</button>
            <button class="rounded-card bg-white/90 px-3 py-4 text-left text-[12px] font-semibold text-primary-text">创客宝库</button>
            <button class="rounded-card bg-white/90 px-3 py-4 text-left text-[12px] font-semibold text-primary-text">社区活动</button>
            <button class="rounded-card bg-white/90 px-3 py-4 text-left text-[12px] font-semibold text-primary-text">设计课堂</button>
          </div>
        </div>
      </div>
    </article>

    <section>
      <p v-if="error" class="mb-4 text-sm text-red-500">{{ error }}</p>

      <div v-if="loading" class="card-grid">
        <div v-for="i in 9" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else class="card-grid">
        <ModelCard v-for="item in list.items" :key="item.id" :item="item" />
      </div>
    </section>

    <footer class="surface-panel flex items-center justify-between p-4">
      <button class="btn-secondary" :disabled="list.page <= 1" @click="refresh(list.page - 1)">上一页</button>
      <p class="text-[12px] text-muted">第 {{ list.page }} 页，共 {{ totalPages }} 页</p>
      <button class="btn-secondary" :disabled="list.page >= totalPages" @click="refresh(list.page + 1)">
        下一页
      </button>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import ModelCard from "./ModelCard.vue";
import { fetchModels } from "../api/modules/model";
import type { ModelListData } from "../types";
import { optimizeImageUrl } from "../utils/image";

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
const bannerTitle = computed(() => bannerModel.value?.name ?? "Parrot Puzzle");
const bannerType = computed(() => bannerModel.value?.type ?? "推荐");
const bannerImage = computed(() => {
  if (!bannerModel.value) {
    return "";
  }
  try {
    const urls = JSON.parse(bannerModel.value.imageUrls);
    const firstUrl = Array.isArray(urls) && urls.length ? String(urls[0]) : "";
    return optimizeImageUrl(firstUrl, { appendWebpQuery: true });
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
    const category = typeFilter.value;
    const params = {
      page,
      size: list.size,
      type: category !== "全部" && category !== "推荐" && category !== "热门" ? category : undefined,
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
