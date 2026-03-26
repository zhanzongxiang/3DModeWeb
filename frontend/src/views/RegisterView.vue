<template>
  <section class="mx-auto max-w-md">
    <div class="surface-panel p-8">
      <h1 class="font-display text-3xl font-bold text-primary-text">创建账号</h1>
      <p class="mt-2 text-sm text-muted">注册后即可上传你的 3D 模型资源。</p>

      <form class="mt-6 space-y-4" @submit.prevent="submit">
        <div>
          <label class="label">用户名</label>
          <input v-model="form.username" class="input" placeholder="3-32 位字符" />
        </div>
        <div>
          <label class="label">密码</label>
          <input v-model="form.password" type="password" class="input" placeholder="至少 6 位" />
        </div>
        <div>
          <label class="label">确认密码</label>
          <input v-model="confirmPassword" type="password" class="input" placeholder="再次输入密码" />
        </div>

        <div v-if="turnstileEnabled">
          <label class="label">行为验证</label>
          <TurnstileWidget
            ref="turnstileRef"
            v-model="captchaToken"
            :site-key="turnstileSiteKey"
          />
        </div>

        <button :disabled="loading" class="btn-primary w-full">
          {{ loading ? "注册中..." : "创建账号" }}
        </button>

        <p class="text-[12px] text-muted">
          已有账号？
          <RouterLink class="text-brand hover:underline" to="/login">去登录</RouterLink>
        </p>
        <p v-if="message" class="text-sm text-brand">{{ message }}</p>
        <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import TurnstileWidget from "../components/TurnstileWidget.vue";
import { register } from "../api/modules/auth";

type TurnstileWidgetExpose = {
  reset: () => void;
};

const turnstileSiteKey = (import.meta.env.VITE_TURNSTILE_SITE_KEY as string | undefined)?.trim() ?? "";
const turnstileEnabled = computed(() => Boolean(turnstileSiteKey));
const turnstileRef = ref<TurnstileWidgetExpose | null>(null);
const captchaToken = ref("");

const loading = ref(false);
const error = ref("");
const message = ref("");
const confirmPassword = ref("");
const form = reactive({
  username: "",
  password: "",
});

async function submit() {
  message.value = "";
  error.value = "";
  if (!form.username || !form.password) {
    error.value = "请填写用户名和密码";
    return;
  }
  if (form.password !== confirmPassword.value) {
    error.value = "两次输入的密码不一致";
    return;
  }
  if (turnstileEnabled.value && !captchaToken.value) {
    error.value = "请先完成行为验证码校验";
    return;
  }

  loading.value = true;
  try {
    await register({
      username: form.username.trim(),
      password: form.password,
      captchaToken: captchaToken.value || undefined,
    });
    message.value = "注册成功，请前往登录";
    form.username = "";
    form.password = "";
    confirmPassword.value = "";
    if (turnstileEnabled.value) {
      turnstileRef.value?.reset();
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : "注册失败";
    if (turnstileEnabled.value) {
      turnstileRef.value?.reset();
    }
  } finally {
    loading.value = false;
  }
}
</script>
