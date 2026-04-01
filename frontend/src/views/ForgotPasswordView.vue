<template>
  <section class="mx-auto max-w-md">
    <div class="surface-panel p-8">
      <h1 class="font-display text-3xl font-bold text-primary-text">找回密码</h1>
      <p class="mt-2 text-sm text-muted">通过验证码重置账号密码。</p>

      <form class="mt-6 space-y-4" @submit.prevent="submitReset">
        <div>
          <label class="label">用户名</label>
          <input v-model="form.username" class="input" placeholder="输入用户名" />
        </div>
        <div class="flex items-center gap-2">
          <div class="flex-1">
            <label class="label">验证码</label>
            <input v-model="form.code" class="input" placeholder="输入验证码" />
          </div>
          <button type="button" class="btn-secondary mt-7 whitespace-nowrap" :disabled="sendingCode || cooldown > 0" @click="sendCode">
            {{ cooldown > 0 ? `${cooldown}s` : sendingCode ? "发送中" : "发送验证码" }}
          </button>
        </div>
        <div>
          <label class="label">新密码</label>
          <input v-model="form.newPassword" type="password" class="input" placeholder="输入新密码（至少6位）" />
        </div>
        <div>
          <label class="label">确认新密码</label>
          <input v-model="confirmPassword" type="password" class="input" placeholder="再次输入新密码" />
        </div>

        <div v-if="turnstileEnabled">
          <label class="label">行为验证</label>
          <TurnstileWidget ref="turnstileRef" v-model="captchaToken" :site-key="turnstileSiteKey" />
        </div>

        <button :disabled="loading" class="btn-primary w-full">
          {{ loading ? "提交中..." : "重置密码" }}
        </button>

        <p v-if="devCode" class="text-xs text-muted">开发验证码（仅开发环境）：{{ devCode }}</p>
        <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
        <p v-if="success" class="text-sm text-green-600">{{ success }}</p>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onUnmounted, reactive, ref } from "vue";
import TurnstileWidget from "../components/TurnstileWidget.vue";
import { resetPasswordByCode, sendForgotPasswordCode } from "../api/modules/auth";

type TurnstileWidgetExpose = { reset: () => void };

const turnstileSiteKey = (import.meta.env.VITE_TURNSTILE_SITE_KEY as string | undefined)?.trim() ?? "";
const turnstileEnabled = computed(() => Boolean(turnstileSiteKey));
const turnstileRef = ref<TurnstileWidgetExpose | null>(null);
const captchaToken = ref("");

const form = reactive({
  username: "",
  code: "",
  newPassword: "",
});
const confirmPassword = ref("");
const loading = ref(false);
const sendingCode = ref(false);
const cooldown = ref(0);
const devCode = ref("");
const error = ref("");
const success = ref("");

let timer: number | null = null;

function startCooldown(seconds = 60) {
  cooldown.value = seconds;
  if (timer) {
    window.clearInterval(timer);
  }
  timer = window.setInterval(() => {
    cooldown.value -= 1;
    if (cooldown.value <= 0 && timer) {
      window.clearInterval(timer);
      timer = null;
    }
  }, 1000);
}

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer);
  }
});

async function sendCode() {
  error.value = "";
  success.value = "";
  devCode.value = "";
  if (!form.username.trim()) {
    error.value = "请输入用户名";
    return;
  }
  if (turnstileEnabled.value && !captchaToken.value) {
    error.value = "请先完成行为验证码校验";
    return;
  }
  sendingCode.value = true;
  try {
    const data = await sendForgotPasswordCode({
      username: form.username.trim(),
      captchaToken: captchaToken.value || undefined,
    });
    devCode.value = data.devCode ?? "";
    success.value = "验证码已发送，请在有效期内完成重置";
    startCooldown(60);
  } catch (e) {
    error.value = e instanceof Error ? e.message : "发送验证码失败";
    if (turnstileEnabled.value) {
      turnstileRef.value?.reset();
    }
  } finally {
    sendingCode.value = false;
  }
}

async function submitReset() {
  error.value = "";
  success.value = "";
  if (!form.username.trim() || !form.code.trim() || !form.newPassword) {
    error.value = "请完整填写表单";
    return;
  }
  if (form.newPassword.length < 6) {
    error.value = "新密码长度不能小于 6 位";
    return;
  }
  if (form.newPassword !== confirmPassword.value) {
    error.value = "两次输入的新密码不一致";
    return;
  }
  if (turnstileEnabled.value && !captchaToken.value) {
    error.value = "请先完成行为验证码校验";
    return;
  }
  loading.value = true;
  try {
    await resetPasswordByCode({
      username: form.username.trim(),
      code: form.code.trim(),
      newPassword: form.newPassword,
      captchaToken: captchaToken.value || undefined,
    });
    success.value = "密码重置成功，请返回登录页登录";
  } catch (e) {
    error.value = e instanceof Error ? e.message : "密码重置失败";
    if (turnstileEnabled.value) {
      turnstileRef.value?.reset();
    }
  } finally {
    loading.value = false;
  }
}
</script>
