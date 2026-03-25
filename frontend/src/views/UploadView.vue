<template>
  <section class="grid gap-6 xl:grid-cols-[1fr_320px]">
    <form class="surface-panel space-y-5 p-6" @submit.prevent="submit">
      <h1 class="font-display text-3xl font-bold text-text-main">发布模型</h1>
      <p class="text-sm text-text-sub">填写模型信息并上传展示图，支持 OSS 自动生成链接。</p>

      <div class="grid gap-4 md:grid-cols-2">
        <div>
          <label class="label">模型名称</label>
          <input v-model="form.name" class="input" placeholder="例如：未来机甲战士" />
        </div>
        <div>
          <label class="label">作品名</label>
          <input v-model="form.artworkName" class="input" placeholder="例如：机械纪元计划" />
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-2">
        <div>
          <label class="label">模型类型</label>
          <input v-model="form.type" class="input" placeholder="角色 / 场景 / 道具" />
        </div>
        <div>
          <label class="label">网盘链接</label>
          <input v-model="form.diskLink" class="input" placeholder="夸克网盘分享链接" />
        </div>
      </div>

      <div>
        <label class="label">收费设置</label>
        <select v-model.number="form.isFree" class="input">
          <option :value="1">免费</option>
          <option :value="0">付费 / 条件解锁</option>
        </select>
      </div>

      <div class="space-y-3">
        <label class="label">展示图片</label>
        <div class="space-y-2">
          <input
            v-for="(_, index) in imageUrls"
            :key="index"
            v-model="imageUrls[index]"
            class="input"
            placeholder="图片 URL"
          />
          <button type="button" class="btn-secondary" @click="imageUrls.push('')">+ 添加图片链接</button>
        </div>
        <div class="rounded-2xl border border-neutral-border bg-neutral-bg p-3">
          <input type="file" accept="image/*" @change="onFileSelect" />
          <p class="mt-2 text-xs text-text-sub">可直接上传到 OSS 自动生成 URL</p>
        </div>
      </div>

      <button :disabled="loading" class="btn-primary w-full">
        {{ loading ? "发布中..." : "确认发布" }}
      </button>

      <p v-if="message" class="text-sm text-brand-green">{{ message }}</p>
      <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
    </form>

    <aside class="surface-panel h-fit p-6">
      <h2 class="font-display text-xl font-bold text-text-main">发布提示</h2>
      <ul class="mt-4 space-y-2 text-sm text-text-sub">
        <li>1. 模型类型建议使用统一词汇，便于检索。</li>
        <li>2. `imageUrls` 会以 JSON 字符串提交到后端。</li>
        <li>3. 付费模型请在网盘链接中标注获取条件。</li>
        <li>4. 登录页已预留行为验证码 Token 字段。</li>
      </ul>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { createModel } from "../api/modules/model";
import { uploadImage } from "../api/modules/oss";

const loading = ref(false);
const message = ref("");
const error = ref("");
const imageUrls = ref<string[]>([""]);
const form = reactive({
  name: "",
  artworkName: "",
  type: "",
  diskLink: "",
  isFree: 1,
});

async function onFileSelect(event: Event) {
  const input = event.target as HTMLInputElement;
  if (!input.files?.length) {
    return;
  }
  const file = input.files[0];
  try {
    const data = await uploadImage(file);
    imageUrls.value = imageUrls.value.filter(Boolean);
    imageUrls.value.push(data.url);
    message.value = "图片已上传到 OSS";
  } catch (e) {
    error.value = e instanceof Error ? e.message : "图片上传失败";
  } finally {
    input.value = "";
  }
}

async function submit() {
  message.value = "";
  error.value = "";
  if (!form.name || !form.artworkName || !form.type || !form.diskLink) {
    error.value = "请完整填写表单";
    return;
  }
  const validImageUrls = imageUrls.value.map((x) => x.trim()).filter(Boolean);
  if (!validImageUrls.length) {
    error.value = "请至少提供一张图片";
    return;
  }

  loading.value = true;
  try {
    await createModel({
      name: form.name,
      artworkName: form.artworkName,
      type: form.type,
      diskLink: form.diskLink,
      isFree: form.isFree,
      imageUrls: JSON.stringify(validImageUrls),
    });
    message.value = "发布成功";
    form.name = "";
    form.artworkName = "";
    form.type = "";
    form.diskLink = "";
    form.isFree = 1;
    imageUrls.value = [""];
  } catch (e) {
    error.value = e instanceof Error ? e.message : "发布失败";
  } finally {
    loading.value = false;
  }
}
</script>
