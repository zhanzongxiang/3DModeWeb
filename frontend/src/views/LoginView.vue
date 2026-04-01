<template>
  <section class="mx-auto max-w-md">
    <div class="surface-panel p-8">
      <h1 class="font-display text-3xl font-bold text-primary-text">登录账号</h1>
      <p class="mt-2 text-sm text-muted">登录后可发布模型与管理资源。</p>

      <form class="mt-6 space-y-4" @submit.prevent="submit">
        <div>
          <label class="label">用户名</label>
          <input v-model="form.username" class="input" placeholder="输入用户名" />
        </div>
        <div>
          <label class="label">密码</label>
          <input v-model="form.password" type="password" class="input" placeholder="输入密码" />
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
          {{ loading ? "登录中..." : "立即登录" }}
        </button>

        <p class="text-[12px] text-muted">
          还没有账号？
          <RouterLink class="text-brand hover:underline" to="/register">去注册</RouterLink>
        </p>
        <p class="text-[12px] text-muted">
          忘记密码？
          <RouterLink class="text-brand hover:underline" to="/forgot-password">重置密码</RouterLink>
        </p>
        <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import TurnstileWidget from "../components/TurnstileWidget.vue";
import { login } from "../api/modules/auth";
import { useAuthStore } from "../stores/auth";

type TurnstileWidgetExpose = {
  reset: () => void;
};

const router = useRouter();
const authStore = useAuthStore();

const turnstileSiteKey = (import.meta.env.VITE_TURNSTILE_SITE_KEY as string | undefined)?.trim() ?? "";
const turnstileEnabled = computed(() => Boolean(turnstileSiteKey));
const turnstileRef = ref<TurnstileWidgetExpose | null>(null);
const captchaToken = ref("");

const loading = ref(false);
const error = ref("");
const form = reactive({
  username: "",
  password: "",
});

async function submit() {
  error.value = "";
  if (!form.username || !form.password) {
    error.value = "请填写用户名和密码";
    return;
  }
  if (turnstileEnabled.value && !captchaToken.value) {
    error.value = "请先完成行为验证码校验";
    return;
  }

  loading.value = true;
  try {
    const data = await login({
      username: form.username.trim(),
      password: form.password,
      captchaToken: captchaToken.value || undefined,
    });
    authStore.setAuth({
      token: data.token,
      userId: data.userId,
      username: data.username,
      orgId: data.orgId,
      roles: data.roles ?? [],
      permissions: data.permissions ?? [],
    });
    await router.push("/hall");
  } catch (e) {
    error.value = e instanceof Error ? e.message : "登录失败";
    if (turnstileEnabled.value) {
      turnstileRef.value?.reset();
    }
  } finally {
    loading.value = false;
  }
}
</script>
