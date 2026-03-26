<template>
  <section class="grid gap-6 xl:grid-cols-[1fr_320px]">
    <form class="surface-panel space-y-5 p-6" @submit.prevent="submit">
      <h1 class="font-display text-3xl font-bold text-primary-text">发布模型</h1>
      <p class="text-sm text-muted">填写模型信息、打印参数和协议信息，支持 OSS 自动上传图片。</p>

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

      <el-form label-position="top" class="surface-panel border border-soft-border p-4 !shadow-none">
        <el-form-item label="推荐层高 (mm)">
          <el-input-number v-model="form.printLayerHeight" :min="0.05" :max="0.4" :step="0.05" :precision="2" />
        </el-form-item>

        <el-form-item label="填充率 (%)">
          <el-slider v-model="form.printInfill" :min="0" :max="100" :step="5" show-input />
        </el-form-item>

        <el-form-item label="是否需要支撑">
          <el-switch
            v-model="supportSwitch"
            inline-prompt
            active-text="需要"
            inactive-text="不需要"
            @change="onSupportChange"
          />
        </el-form-item>

        <el-form-item label="建议材质">
          <el-select v-model="form.printMaterial" placeholder="选择材质" class="w-full">
            <el-option label="PLA" value="PLA" />
            <el-option label="PETG" value="PETG" />
            <el-option label="ABS" value="ABS" />
            <el-option label="TPU" value="TPU" />
          </el-select>
        </el-form-item>

        <el-form-item label="版权协议">
          <el-select v-model="form.licenseType" placeholder="选择协议" class="w-full">
            <el-option v-for="item in licenseOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>

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
        <div class="rounded-card border border-soft-border bg-gray-50 p-3">
          <input type="file" accept="image/*" @change="onFileSelect" />
          <p class="mt-2 text-[12px] text-muted">可直接上传到 OSS 自动生成 URL</p>
        </div>
      </div>

      <button :disabled="loading" class="btn-primary w-full">
        {{ loading ? "发布中..." : "确认发布" }}
      </button>

      <p v-if="message" class="text-sm text-brand">{{ message }}</p>
      <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
    </form>

    <aside class="surface-panel h-fit p-6">
      <h2 class="font-display text-xl font-bold text-primary-text">发布提示</h2>
      <ul class="mt-4 space-y-2 text-sm text-muted">
        <li>1. 打印参数会用于详情页的“打印建议”面板。</li>
        <li>2. 协议信息会在模型卡片和详情页显示。</li>
        <li>3. `imageUrls` 以 JSON 字符串提交到后端。</li>
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
const supportSwitch = ref(false);
const licenseOptions = ["CC BY", "CC BY-NC", "CC BY-ND", "CC BY-SA"];

const form = reactive({
  name: "",
  artworkName: "",
  type: "",
  diskLink: "",
  isFree: 1,
  printLayerHeight: 0.2,
  printInfill: 20,
  printSupport: 0,
  printMaterial: "PLA",
  licenseType: "CC BY-NC",
});

function onSupportChange(value: boolean | string | number) {
  form.printSupport = value ? 1 : 0;
}

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
      printLayerHeight: form.printLayerHeight,
      printInfill: form.printInfill,
      printSupport: form.printSupport,
      printMaterial: form.printMaterial,
      licenseType: form.licenseType,
    });
    message.value = "发布成功";
    form.name = "";
    form.artworkName = "";
    form.type = "";
    form.diskLink = "";
    form.isFree = 1;
    form.printLayerHeight = 0.2;
    form.printInfill = 20;
    form.printSupport = 0;
    form.printMaterial = "PLA";
    form.licenseType = "CC BY-NC";
    supportSwitch.value = false;
    imageUrls.value = [""];
  } catch (e) {
    error.value = e instanceof Error ? e.message : "发布失败";
  } finally {
    loading.value = false;
  }
}
</script>
