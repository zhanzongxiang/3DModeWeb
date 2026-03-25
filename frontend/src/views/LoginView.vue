<template>
  <section class="mx-auto max-w-md">
    <div class="surface-panel p-8">
      <h1 class="font-display text-3xl font-bold text-text-main">登录账户</h1>
      <p class="mt-2 text-sm text-text-sub">登录后可发布模型与管理资源。</p>

      <form class="mt-6 space-y-4" @submit.prevent="submit">
        <div>
          <label class="label">用户名</label>
          <input v-model="form.username" class="input" placeholder="输入用户名" />
        </div>
        <div>
          <label class="label">密码</label>
          <input v-model="form.password" type="password" class="input" placeholder="输入密码" />
        </div>
        <div>
          <label class="label">行为验证码 Token（预留）</label>
          <input v-model="form.captchaToken" class="input" placeholder="如 Turnstile Token" />
        </div>

        <button :disabled="loading" class="btn-primary w-full">
          {{ loading ? "登录中..." : "立即登录" }}
        </button>

        <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { login } from "../api/modules/auth";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const error = ref("");
const form = reactive({
  username: "",
  password: "",
  captchaToken: "",
});

async function submit() {
  error.value = "";
  if (!form.username || !form.password) {
    error.value = "请填写用户名和密码";
    return;
  }
  loading.value = true;
  try {
    const data = await login({
      username: form.username,
      password: form.password,
      captchaToken: form.captchaToken,
    });
    authStore.setAuth({ token: data.token, userId: data.userId, username: data.username });
    await router.push("/hall");
  } catch (e) {
    error.value = e instanceof Error ? e.message : "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>
