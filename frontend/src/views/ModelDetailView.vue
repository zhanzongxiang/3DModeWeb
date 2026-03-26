<template>
  <section class="space-y-5">
    <div class="surface-panel p-5">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h1 class="text-[18px] font-semibold text-primary-text">{{ modelName }}</h1>
          <p class="mt-1 text-[12px] text-muted">{{ artworkName }}</p>
        </div>
        <RouterLink class="btn-secondary" to="/hall">返回列表</RouterLink>
      </div>
    </div>

    <PrintSpec
      :print-layer-height="printLayerHeight"
      :print-infill="printInfill"
      :print-support="printSupport"
      :print-material="printMaterial"
    />

    <div class="surface-panel p-5">
      <h2 class="text-[14px] font-semibold text-primary-text">协议信息</h2>
      <p class="mt-2 text-[12px] text-muted">授权协议：{{ licenseType || "-" }}</p>
    </div>

    <div class="surface-panel p-5">
      <h2 class="text-[16px] font-semibold text-primary-text">打印实物墙</h2>
      <p class="mt-1 text-[12px] text-muted">上传你的打印成品图与心得，帮助其他创作者快速复现。</p>

      <div class="mt-4 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <article v-for="item in makes" :key="item.id" class="rounded-card border border-soft-border bg-gray-50 p-3">
          <img
            :src="makeImage(item.imageUrl)"
            alt="make image"
            class="h-36 w-full rounded-card object-cover"
            loading="lazy"
            decoding="async"
          />
          <p class="mt-2 text-[12px] text-primary-text">{{ item.description || "无描述" }}</p>
          <p class="mt-1 text-[11px] text-muted">用户 #{{ item.userId }}</p>
        </article>
        <div v-if="!makes.length" class="rounded-card border border-dashed border-soft-border bg-gray-50 p-4 text-[12px] text-muted">
          暂无作品秀，欢迎上传第一条。
        </div>
      </div>
    </div>

    <div class="surface-panel p-5">
      <h2 class="text-[14px] font-semibold text-primary-text">上传作品秀</h2>
      <div class="mt-3 space-y-3">
        <input v-model="makeForm.imageUrl" class="input" placeholder="作品图 URL" />
        <textarea
          v-model="makeForm.description"
          class="w-full rounded-card border border-soft-border bg-white px-3 py-2 text-[13px] text-primary-text outline-none"
          rows="3"
          placeholder="写一点打印心得（可选）"
        ></textarea>
        <div class="flex items-center gap-3">
          <input type="file" accept="image/*" @change="onMakeFileSelect" />
          <button class="btn-primary" :disabled="uploading" @click="submitMake">
            {{ uploading ? "提交中..." : "提交作品秀" }}
          </button>
        </div>
        <p v-if="msg" class="text-[12px] text-brand">{{ msg }}</p>
        <p v-if="err" class="text-[12px] text-red-500">{{ err }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import PrintSpec from "../components/PrintSpec.vue";
import { createMake, listMakesByModel } from "../api/modules/make";
import { uploadImage } from "../api/modules/oss";
import type { MakeItem } from "../types";
import { optimizeImageUrl } from "../utils/image";

const route = useRoute();
const modelId = computed(() => Number(route.params.id));
const modelName = computed(() => String(route.query.name ?? `模型 #${route.params.id}`));
const artworkName = computed(() => String(route.query.artwork ?? ""));
const printLayerHeight = computed(() => String(route.query.printLayerHeight ?? ""));
const printInfill = computed(() => String(route.query.printInfill ?? ""));
const printSupport = computed(() => String(route.query.printSupport ?? ""));
const printMaterial = computed(() => String(route.query.printMaterial ?? ""));
const licenseType = computed(() => String(route.query.licenseType ?? ""));

const makes = ref<MakeItem[]>([]);
const uploading = ref(false);
const msg = ref("");
const err = ref("");
const makeForm = reactive({
  imageUrl: "",
  description: "",
});

function makeImage(url: string) {
  return optimizeImageUrl(url, { appendWebpQuery: true });
}

onMounted(() => {
  loadMakes();
});

async function loadMakes() {
  if (!Number.isFinite(modelId.value) || modelId.value <= 0) {
    return;
  }
  try {
    makes.value = await listMakesByModel(modelId.value);
  } catch {
    makes.value = [];
  }
}

async function onMakeFileSelect(event: Event) {
  const input = event.target as HTMLInputElement;
  if (!input.files?.length) {
    return;
  }
  try {
    const data = await uploadImage(input.files[0]);
    makeForm.imageUrl = data.url;
    msg.value = "作品图片已上传";
    err.value = "";
  } catch (e) {
    err.value = e instanceof Error ? e.message : "上传失败";
  } finally {
    input.value = "";
  }
}

async function submitMake() {
  msg.value = "";
  err.value = "";
  if (!makeForm.imageUrl.trim()) {
    err.value = "请先提供作品图 URL";
    return;
  }
  if (!Number.isFinite(modelId.value) || modelId.value <= 0) {
    err.value = "模型 ID 无效";
    return;
  }
  uploading.value = true;
  try {
    await createMake({
      modelId: modelId.value,
      imageUrl: makeForm.imageUrl.trim(),
      description: makeForm.description.trim(),
    });
    makeForm.imageUrl = "";
    makeForm.description = "";
    msg.value = "作品秀提交成功";
    await loadMakes();
  } catch (e) {
    err.value = e instanceof Error ? e.message : "提交失败";
  } finally {
    uploading.value = false;
  }
}
</script>
