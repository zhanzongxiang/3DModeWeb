<template>
  <section class="mx-auto max-w-3xl space-y-5">
    <div class="surface-panel p-6">
      <h1 class="text-[18px] font-semibold text-primary-text">账号安全</h1>
      <p class="mt-1 text-[12px] text-muted">支持修改密码、邮箱/手机验证和账号注销。</p>
    </div>

    <div class="surface-panel p-6">
      <h2 class="text-[16px] font-semibold text-primary-text">修改密码</h2>
      <form class="mt-4 space-y-3" @submit.prevent="submitChangePassword">
        <div>
          <label class="label">旧密码</label>
          <input v-model="changeForm.oldPassword" type="password" class="input" placeholder="输入旧密码" />
        </div>
        <div>
          <label class="label">新密码</label>
          <input v-model="changeForm.newPassword" type="password" class="input" placeholder="输入新密码" />
        </div>
        <div>
          <label class="label">确认新密码</label>
          <input v-model="changeForm.confirmPassword" type="password" class="input" placeholder="再次输入新密码" />
        </div>
        <button class="btn-primary" :disabled="savingPassword">{{ savingPassword ? "保存中..." : "保存新密码" }}</button>
      </form>
    </div>

    <div class="surface-panel p-6">
      <h2 class="text-[16px] font-semibold text-primary-text">邮箱/手机验证</h2>
      <div class="mt-4 grid gap-4 md:grid-cols-2">
        <div class="rounded-card border border-soft-border p-4">
          <p class="text-sm font-semibold text-primary-text">邮箱验证</p>
          <p class="mt-1 text-xs text-muted">当前：{{ profile.email || "未设置" }} / {{ profile.emailVerified ? "已验证" : "未验证" }}</p>
          <input v-model="emailTarget" class="input mt-3" placeholder="输入邮箱地址" />
          <div class="mt-3 flex gap-2">
            <button class="btn-secondary" :disabled="emailCooldown > 0" @click.prevent="sendContactCode('EMAIL')">
              {{ emailCooldown > 0 ? `${emailCooldown}s` : "发送验证码" }}
            </button>
            <input v-model="emailCode" class="input flex-1" placeholder="输入验证码" />
          </div>
          <button class="btn-primary mt-3" @click.prevent="verifyContact('EMAIL')">验证邮箱</button>
          <p v-if="emailDevCode" class="mt-2 text-xs text-muted">开发验证码：{{ emailDevCode }}</p>
        </div>

        <div class="rounded-card border border-soft-border p-4">
          <p class="text-sm font-semibold text-primary-text">手机验证</p>
          <p class="mt-1 text-xs text-muted">当前：{{ profile.mobile || "未设置" }} / {{ profile.mobileVerified ? "已验证" : "未验证" }}</p>
          <input v-model="mobileTarget" class="input mt-3" placeholder="输入手机号" />
          <div class="mt-3 flex gap-2">
            <button class="btn-secondary" :disabled="mobileCooldown > 0" @click.prevent="sendContactCode('MOBILE')">
              {{ mobileCooldown > 0 ? `${mobileCooldown}s` : "发送验证码" }}
            </button>
            <input v-model="mobileCode" class="input flex-1" placeholder="输入验证码" />
          </div>
          <button class="btn-primary mt-3" @click.prevent="verifyContact('MOBILE')">验证手机</button>
          <p v-if="mobileDevCode" class="mt-2 text-xs text-muted">开发验证码：{{ mobileDevCode }}</p>
        </div>
      </div>

      <div v-if="turnstileEnabled" class="mt-4">
        <label class="label">行为验证</label>
        <TurnstileWidget ref="turnstileRef" v-model="captchaToken" :site-key="turnstileSiteKey" />
      </div>
    </div>

    <div class="surface-panel p-6">
      <h2 class="text-[16px] font-semibold text-red-500">账号注销</h2>
      <p class="mt-1 text-[12px] text-muted">注销后账号将被停用，当前登录态会失效。</p>
      <form class="mt-3 flex flex-wrap items-center gap-2" @submit.prevent="submitCancel">
        <input v-model="cancelPassword" type="password" class="input max-w-[320px]" placeholder="输入当前密码确认注销" />
        <button class="btn-danger" :disabled="cancelling">{{ cancelling ? "注销中..." : "确认注销" }}</button>
      </form>
    </div>

    <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
    <p v-if="success" class="text-sm text-green-600">{{ success }}</p>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import TurnstileWidget from "../components/TurnstileWidget.vue";
import {
  cancelAccount,
  changePassword,
  getAccountSecurityProfile,
  sendContactVerificationCode,
  verifyContactCode,
} from "../api/modules/auth";
import { useAuthStore } from "../stores/auth";

type TurnstileWidgetExpose = { reset: () => void };

const authStore = useAuthStore();
const router = useRouter();
const turnstileSiteKey = (import.meta.env.VITE_TURNSTILE_SITE_KEY as string | undefined)?.trim() ?? "";
const turnstileEnabled = Boolean(turnstileSiteKey);
const turnstileRef = ref<TurnstileWidgetExpose | null>(null);
const captchaToken = ref("");

const profile = reactive({
  username: "",
  email: "",
  emailVerified: 0,
  mobile: "",
  mobileVerified: 0,
});

const changeForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const emailTarget = ref("");
const emailCode = ref("");
const emailDevCode = ref("");
const emailCooldown = ref(0);
const mobileTarget = ref("");
const mobileCode = ref("");
const mobileDevCode = ref("");
const mobileCooldown = ref(0);

const cancelPassword = ref("");
const savingPassword = ref(false);
const cancelling = ref(false);
const error = ref("");
const success = ref("");

let emailTimer: number | null = null;
let mobileTimer: number | null = null;

onMounted(async () => {
  await loadProfile();
});

async function loadProfile() {
  const data = await getAccountSecurityProfile();
  profile.username = data.username;
  profile.email = data.email || "";
  profile.emailVerified = data.emailVerified ?? 0;
  profile.mobile = data.mobile || "";
  profile.mobileVerified = data.mobileVerified ?? 0;
  emailTarget.value = profile.email;
  mobileTarget.value = profile.mobile;
}

function startCooldown(kind: "EMAIL" | "MOBILE") {
  const setValue = (value: number) => {
    if (kind === "EMAIL") {
      emailCooldown.value = value;
    } else {
      mobileCooldown.value = value;
    }
  };
  let timer = kind === "EMAIL" ? emailTimer : mobileTimer;
  setValue(60);
  if (timer) {
    window.clearInterval(timer);
  }
  timer = window.setInterval(() => {
    const current = kind === "EMAIL" ? emailCooldown.value : mobileCooldown.value;
    const next = current - 1;
    setValue(next);
    if (next <= 0 && timer) {
      window.clearInterval(timer);
      if (kind === "EMAIL") {
        emailTimer = null;
      } else {
        mobileTimer = null;
      }
    }
  }, 1000);
  if (kind === "EMAIL") {
    emailTimer = timer;
  } else {
    mobileTimer = timer;
  }
}

async function sendContactCode(type: "EMAIL" | "MOBILE") {
  error.value = "";
  success.value = "";
  const target = type === "EMAIL" ? emailTarget.value.trim() : mobileTarget.value.trim();
  if (!target) {
    error.value = type === "EMAIL" ? "请输入邮箱地址" : "请输入手机号";
    return;
  }
  if (turnstileEnabled && !captchaToken.value) {
    error.value = "请先完成行为验证码校验";
    return;
  }
  try {
    const data = await sendContactVerificationCode({ type, target, captchaToken: captchaToken.value || undefined });
    if (type === "EMAIL") {
      emailDevCode.value = data.devCode ?? "";
    } else {
      mobileDevCode.value = data.devCode ?? "";
    }
    startCooldown(type);
    success.value = "验证码已发送";
  } catch (e) {
    error.value = e instanceof Error ? e.message : "发送验证码失败";
    if (turnstileEnabled) {
      turnstileRef.value?.reset();
    }
  }
}

async function verifyContact(type: "EMAIL" | "MOBILE") {
  error.value = "";
  success.value = "";
  const target = type === "EMAIL" ? emailTarget.value.trim() : mobileTarget.value.trim();
  const code = type === "EMAIL" ? emailCode.value.trim() : mobileCode.value.trim();
  if (!target || !code) {
    error.value = "请填写目标地址和验证码";
    return;
  }
  try {
    await verifyContactCode({ type, target, code });
    success.value = "验证成功";
    await loadProfile();
  } catch (e) {
    error.value = e instanceof Error ? e.message : "验证失败";
  }
}

async function submitChangePassword() {
  error.value = "";
  success.value = "";
  if (!changeForm.oldPassword || !changeForm.newPassword || !changeForm.confirmPassword) {
    error.value = "请完整填写密码表单";
    return;
  }
  if (changeForm.newPassword.length < 6) {
    error.value = "新密码不能少于 6 位";
    return;
  }
  if (changeForm.newPassword !== changeForm.confirmPassword) {
    error.value = "两次输入的新密码不一致";
    return;
  }
  savingPassword.value = true;
  try {
    await changePassword({ oldPassword: changeForm.oldPassword, newPassword: changeForm.newPassword });
    changeForm.oldPassword = "";
    changeForm.newPassword = "";
    changeForm.confirmPassword = "";
    success.value = "密码修改成功";
  } catch (e) {
    error.value = e instanceof Error ? e.message : "密码修改失败";
  } finally {
    savingPassword.value = false;
  }
}

async function submitCancel() {
  error.value = "";
  success.value = "";
  if (!cancelPassword.value) {
    error.value = "请输入当前密码";
    return;
  }
  cancelling.value = true;
  try {
    await cancelAccount({ password: cancelPassword.value });
    authStore.logout();
    ElMessage.success("账号已注销");
    await router.push("/login");
  } catch (e) {
    error.value = e instanceof Error ? e.message : "账号注销失败";
  } finally {
    cancelling.value = false;
  }
}
</script>
