<template>
  <article class="model-card">
    <div class="model-badge">{{ item.type || "模型" }}</div>
    <img
      v-if="coverUrl"
      :src="coverUrl"
      :alt="item.name"
      class="model-cover"
      loading="lazy"
      decoding="async"
    />
    <div v-else class="model-cover flex items-center justify-center bg-gray-100 text-[12px] text-muted">无预览图</div>

    <div class="p-3">
      <RouterLink :to="detailLink" class="line-clamp-2 text-[15px] font-medium leading-tight text-primary-text hover:text-brand">
        {{ item.name }}
      </RouterLink>

      <div class="mt-3 flex items-center justify-between gap-2">
        <div class="flex items-center gap-1.5">
          <img :src="authorAvatar" alt="author avatar" class="h-5 w-5 rounded-full bg-gray-100" loading="lazy" decoding="async" />
          <span class="max-w-[110px] truncate text-[12px] text-muted">{{ authorName }}</span>
        </div>

        <div class="flex items-center gap-2 text-[12px] text-gray-400">
          <div class="flex items-center gap-1">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14"></path>
              <path d="M19 12l-7 7-7-7"></path>
            </svg>
            <span>{{ downloads }}</span>
          </div>
          <div class="flex items-center gap-1">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 21s-6.7-4.3-9.2-8A5.6 5.6 0 0 1 12 5.4 5.6 5.6 0 0 1 21.2 13C18.7 16.7 12 21 12 21z"></path>
            </svg>
            <span>{{ likes }}</span>
          </div>
          <button
            class="inline-flex items-center gap-1 rounded-full border px-2 py-0.5 transition"
            :class="isCollected ? 'border-brand text-brand' : 'border-gray-200 text-gray-400'"
            :disabled="toggleLoading"
            @click="toggleFavorite"
          >
            ♥
          </button>
        </div>
      </div>
      <div v-if="licenseTag" class="mt-2 flex justify-end">
        <div class="license-dot" :title="`协议: ${licenseTag}`">CC</div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import type { ModelItem } from "../types";
import { getCollectionStatus, toggleCollection } from "../api/modules/collection";
import { useAuthStore } from "../stores/auth";
import { optimizeImageUrl } from "../utils/image";

const props = defineProps<{
  item: ModelItem;
}>();

const coverUrl = computed(() => {
  try {
    const urls = JSON.parse(props.item.imageUrls);
    const firstUrl = Array.isArray(urls) && urls.length > 0 ? String(urls[0]) : "";
    return optimizeImageUrl(firstUrl, { appendWebpQuery: true });
  } catch {
    return "";
  }
});

const authorName = computed(() => `创作者 #${props.item.userId}`);
const authorAvatar = computed(
  () => `https://api.dicebear.com/9.x/shapes/svg?seed=modelhub-${props.item.userId}`,
);
const likes = computed(() => 20 + (props.item.id % 73));
const downloads = computed(() => 100 + (props.item.id % 500));
const licenseTag = computed(() => props.item.licenseType?.trim() || "");
const isCollected = ref(false);
const toggleLoading = ref(false);
const authStore = useAuthStore();

const detailLink = computed(() => ({
  path: `/models/${props.item.id}`,
  query: {
    name: props.item.name,
    artwork: props.item.artworkName,
    printLayerHeight: String(props.item.printLayerHeight ?? ""),
    printInfill: String(props.item.printInfill ?? ""),
    printSupport: String(props.item.printSupport ?? ""),
    printMaterial: props.item.printMaterial ?? "",
    licenseType: props.item.licenseType ?? "",
  },
}));

onMounted(async () => {
  if (!authStore.isLoggedIn) {
    return;
  }
  try {
    const data = await getCollectionStatus(props.item.id);
    isCollected.value = data.collected;
  } catch {
  }
});

async function toggleFavorite(event: MouseEvent) {
  event.preventDefault();
  if (!authStore.isLoggedIn) {
    return;
  }
  if (toggleLoading.value) {
    return;
  }
  toggleLoading.value = true;
  try {
    const data = await toggleCollection(props.item.id);
    isCollected.value = data.collected;
  } catch {
  } finally {
    toggleLoading.value = false;
  }
}
</script>
