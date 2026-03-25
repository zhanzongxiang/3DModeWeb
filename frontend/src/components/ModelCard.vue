<template>
  <article class="model-card">
    <img v-if="coverUrl" :src="coverUrl" :alt="item.name" class="model-cover" />
    <div v-else class="model-cover flex items-center justify-center text-xs text-text-sub">无预览图</div>

    <div class="p-4">
      <h3 class="truncate text-base font-semibold text-text-main">{{ item.name }}</h3>
      <p class="mt-1 truncate text-sm text-text-sub">{{ item.artworkName }}</p>

      <div class="mt-4 flex items-center justify-between gap-3">
        <div class="flex items-center gap-2">
          <img :src="authorAvatar" alt="author avatar" class="h-7 w-7 rounded-full bg-gray-100" />
          <span class="text-xs text-text-sub">{{ authorName }}</span>
        </div>

        <div class="flex items-center gap-2">
          <span class="tag" :class="item.isFree ? 'tag-free' : 'tag-paid'">
            {{ item.isFree ? "免费" : "付费" }}
          </span>
          <a :href="item.diskLink" target="_blank" rel="noreferrer" class="tag tag-link">夸克链接</a>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { ModelItem } from "../types";

const props = defineProps<{
  item: ModelItem;
}>();

const coverUrl = computed(() => {
  try {
    const urls = JSON.parse(props.item.imageUrls);
    return Array.isArray(urls) && urls.length > 0 ? String(urls[0]) : "";
  } catch {
    return "";
  }
});

const authorName = computed(() => `创作者 #${props.item.userId}`);
const authorAvatar = computed(
  () => `https://api.dicebear.com/9.x/shapes/svg?seed=modelhub-${props.item.userId}`,
);
</script>
