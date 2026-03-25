<template>
  <section class="mx-auto max-w-md">
    <div class="surface-panel p-8">
      <h1 class="font-display text-3xl font-bold text-text-main">创建账户</h1>
      <p class="mt-2 text-sm text-text-sub">注册后即可上传你的 3D 模型资源。</p>

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

        <button :disabled="loading" class="btn-primary w-full">
          {{ loading ? "注册中..." : "创建账户" }}
        </button>

        <p v-if="message" class="text-sm text-brand-green">{{ message }}</p>
        <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { register } from "../api/modules/auth";

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
  loading.value = true;
  try {
    await register({ username: form.username, password: form.password });
    message.value = "注册成功，请前往登录";
  } catch (e) {
    error.value = e instanceof Error ? e.message : "注册失败";
  } finally {
    loading.value = false;
  }
}
</script>
