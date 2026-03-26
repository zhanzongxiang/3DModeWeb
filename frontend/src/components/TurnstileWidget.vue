<template>
  <div class="space-y-2">
    <div
      ref="containerRef"
      class="min-h-[74px] rounded-card border border-soft-border bg-gray-50 px-2 py-2"
    ></div>
    <p v-if="error" class="text-[12px] text-red-500">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";

const props = defineProps<{
  siteKey?: string;
  modelValue: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();

type TurnstileApi = {
  render: (container: HTMLElement, options: Record<string, unknown>) => string;
  reset: (widgetId: string) => void;
  remove?: (widgetId: string) => void;
};

declare global {
  interface Window {
    turnstile?: TurnstileApi;
  }
}

const SCRIPT_ID = "cf-turnstile-script";
const SCRIPT_SRC = "https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit";

const containerRef = ref<HTMLElement | null>(null);
const widgetId = ref<string>("");
const error = ref("");
let destroyed = false;

function clearToken() {
  emit("update:modelValue", "");
}

function resetWidget() {
  if (!widgetId.value || !window.turnstile) {
    clearToken();
    return;
  }
  window.turnstile.reset(widgetId.value);
  clearToken();
}

async function loadScript() {
  if (window.turnstile) {
    return;
  }
  const existing = document.getElementById(SCRIPT_ID) as HTMLScriptElement | null;
  if (existing) {
    await waitForTurnstile();
    return;
  }
  await new Promise<void>((resolve, reject) => {
    const script = document.createElement("script");
    script.id = SCRIPT_ID;
    script.src = SCRIPT_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("script load failed"));
    document.head.appendChild(script);
  });
  await waitForTurnstile();
}

async function waitForTurnstile() {
  const maxRetry = 80;
  for (let i = 0; i < maxRetry; i += 1) {
    if (window.turnstile) {
      return;
    }
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
  throw new Error("turnstile api not ready");
}

function renderWidget() {
  if (!props.siteKey?.trim() || !containerRef.value || !window.turnstile) {
    return;
  }

  if (widgetId.value && window.turnstile.remove) {
    window.turnstile.remove(widgetId.value);
    widgetId.value = "";
  }
  clearToken();
  error.value = "";

  widgetId.value = window.turnstile.render(containerRef.value, {
    sitekey: props.siteKey,
    callback: (token: string) => {
      emit("update:modelValue", token);
      error.value = "";
    },
    "expired-callback": () => {
      clearToken();
    },
    "error-callback": () => {
      clearToken();
      error.value = "验证码加载失败，请刷新后重试。";
    },
  });
}

onMounted(async () => {
  if (!props.siteKey?.trim()) {
    return;
  }
  try {
    await loadScript();
    if (!destroyed) {
      renderWidget();
    }
  } catch {
    error.value = "验证码脚本加载失败，请检查网络或稍后再试。";
  }
});

watch(
  () => props.siteKey,
  () => {
    renderWidget();
  },
);

onBeforeUnmount(() => {
  destroyed = true;
  if (widgetId.value && window.turnstile?.remove) {
    window.turnstile.remove(widgetId.value);
  }
});

defineExpose({
  reset: resetWidget,
});
</script>
