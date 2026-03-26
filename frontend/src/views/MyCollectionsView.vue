<template>
  <section class="space-y-5">
    <div class="surface-panel p-5">
      <h1 class="text-[18px] font-semibold text-primary-text">我的收藏</h1>
      <p class="mt-1 text-[12px] text-muted">这里展示你收藏过的模型 ID，后续可扩展为完整卡片视图。</p>
    </div>

    <div class="surface-panel p-5">
      <p v-if="loading" class="text-[12px] text-muted">加载中...</p>
      <p v-if="!loading && !items.length" class="text-[12px] text-muted">暂无收藏。</p>

      <div class="space-y-2">
        <RouterLink
          v-for="item in items"
          :key="item.id"
          :to="`/models/${item.modelId}`"
          class="flex items-center justify-between rounded-card border border-soft-border px-3 py-2 text-[13px] text-primary-text"
        >
          <span>模型 #{{ item.modelId }}</span>
          <span class="text-[12px] text-muted">{{ item.createTime }}</span>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { listMyCollections } from "../api/modules/collection";
import type { CollectionItem } from "../types";

const loading = ref(false);
const items = ref<CollectionItem[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    items.value = await listMyCollections();
  } catch {
    items.value = [];
  } finally {
    loading.value = false;
  }
});
</script>
