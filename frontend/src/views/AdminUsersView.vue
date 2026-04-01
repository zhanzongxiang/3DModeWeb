<template>
  <section class="space-y-5">
    <div class="surface-panel p-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-[18px] font-semibold text-primary-text">用户管理</h1>
          <p class="mt-1 text-[12px] text-muted">平台级用户列表、状态管理、角色分配与密码重置。</p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <el-input v-model="keyword" class="w-[220px]" clearable placeholder="用户名/昵称/姓名" @keyup.enter="loadData" />
          <el-select v-model="statusFilter" class="w-[120px]" clearable placeholder="状态">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button round @click="loadData">搜索</el-button>
          <el-button type="success" round @click="openCreate">新增用户</el-button>
        </div>
      </div>
    </div>

    <div class="surface-panel p-5">
      <el-table :data="tableData" size="small" v-loading="loading">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="roleCodes" label="角色" min-width="160">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag v-for="code in row.roleCodes" :key="code" size="small">{{ code }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orgId" label="组织ID" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "启用" : "停用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170" />
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" round type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" round @click="openRoles(row)">分配角色</el-button>
              <el-button size="small" round :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
                {{ row.status === 1 ? "停用" : "启用" }}
              </el-button>
              <el-button size="small" round type="danger" @click="resetPassword(row)">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :current-page="page"
          :page-size="size"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="Boolean(editingId)" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.mobile" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="组织">
          <el-select v-model="form.orgId" class="w-full" clearable>
            <el-option v-for="org in orgOptions" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingId" label="角色">
          <el-select v-model="form.roleIds" class="w-full" multiple>
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="success" round :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rolesVisible" title="分配角色" width="520px">
      <el-form label-width="90px">
        <el-form-item label="角色列表">
          <el-select v-model="roleAssignIds" class="w-full" multiple>
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="rolesVisible = false">取消</el-button>
        <el-button type="success" round :loading="assigningRoles" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { assignAdminUserRoles, createAdminUser, listAdminUsers, resetAdminUserPassword, updateAdminUser, updateAdminUserStatus } from "../api/modules/adminUser";
import { listRoles } from "../api/modules/adminRole";
import { listOrgTree } from "../api/modules/adminOrg";
import type { AdminUserItem, OrgNode, RoleItem } from "../types";

const loading = ref(false);
const submitting = ref(false);
const assigningRoles = ref(false);
const dialogVisible = ref(false);
const rolesVisible = ref(false);
const editingId = ref<number | null>(null);
const roleAssignUserId = ref<number | null>(null);

const tableData = ref<AdminUserItem[]>([]);
const roleOptions = ref<RoleItem[]>([]);
const orgOptions = ref<{ id: number; name: string }[]>([]);
const roleAssignIds = ref<number[]>([]);

const page = ref(1);
const size = ref(10);
const total = ref(0);
const keyword = ref("");
const statusFilter = ref<number | undefined>();

const form = reactive({
  username: "",
  password: "",
  nickname: "",
  realName: "",
  mobile: "",
  email: "",
  orgId: undefined as number | undefined,
  roleIds: [] as number[],
});

onMounted(async () => {
  await Promise.all([loadData(), loadOptions()]);
});

async function loadOptions() {
  const [roles, orgTree] = await Promise.all([
    listRoles({ page: 1, size: 200 }),
    listOrgTree(),
  ]);
  roleOptions.value = roles.items.filter((item) => item.status === 1);
  orgOptions.value = flattenOrgTree(orgTree).map((item) => ({ id: item.id, name: item.name }));
}

async function loadData() {
  loading.value = true;
  try {
    const data = await listAdminUsers({
      page: page.value,
      size: size.value,
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value,
    });
    tableData.value = data.items;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function onPageChange(current: number) {
  page.value = current;
  loadData();
}

function resetForm() {
  form.username = "";
  form.password = "";
  form.nickname = "";
  form.realName = "";
  form.mobile = "";
  form.email = "";
  form.orgId = undefined;
  form.roleIds = [];
}

function openCreate() {
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: AdminUserItem) {
  editingId.value = row.id;
  form.username = row.username;
  form.password = "";
  form.nickname = row.nickname ?? "";
  form.realName = row.realName ?? "";
  form.mobile = row.mobile ?? "";
  form.email = row.email ?? "";
  form.orgId = row.orgId;
  form.roleIds = [...row.roleIds];
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.username.trim()) {
    ElMessage.error("用户名不能为空");
    return;
  }
  if (!editingId.value && !form.password.trim()) {
    ElMessage.error("密码不能为空");
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateAdminUser(editingId.value, {
        nickname: form.nickname.trim() || undefined,
        realName: form.realName.trim() || undefined,
        mobile: form.mobile.trim() || undefined,
        email: form.email.trim() || undefined,
        orgId: form.orgId,
      });
      ElMessage.success("用户更新成功");
    } else {
      await createAdminUser({
        username: form.username.trim(),
        password: form.password.trim(),
        nickname: form.nickname.trim() || undefined,
        realName: form.realName.trim() || undefined,
        mobile: form.mobile.trim() || undefined,
        email: form.email.trim() || undefined,
        orgId: form.orgId,
        roleIds: form.roleIds,
        status: 1,
      });
      ElMessage.success("用户创建成功");
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    submitting.value = false;
  }
}

function openRoles(row: AdminUserItem) {
  roleAssignUserId.value = row.id;
  roleAssignIds.value = [...row.roleIds];
  rolesVisible.value = true;
}

async function submitRoles() {
  if (!roleAssignUserId.value) {
    return;
  }
  assigningRoles.value = true;
  try {
    await assignAdminUserRoles(roleAssignUserId.value, roleAssignIds.value);
    ElMessage.success("角色分配成功");
    rolesVisible.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    assigningRoles.value = false;
  }
}

async function toggleStatus(row: AdminUserItem) {
  try {
    await updateAdminUserStatus(row.id, row.status === 1 ? 0 : 1);
    ElMessage.success("状态更新成功");
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function resetPassword(row: AdminUserItem) {
  try {
    const { value } = await ElMessageBox.prompt(`请输入用户 ${row.username} 的新密码`, "重置密码", {
      inputType: "password",
      inputValue: "",
      inputPattern: /^.{6,128}$/,
      inputErrorMessage: "密码长度需为 6-128",
    });
    await resetAdminUserPassword(row.id, value);
    ElMessage.success("密码重置成功");
  } catch (error) {
    if ((error as Error).message !== "cancel") {
      ElMessage.error((error as Error).message);
    }
  }
}

function flattenOrgTree(list: OrgNode[]): OrgNode[] {
  const result: OrgNode[] = [];
  for (const item of list) {
    result.push(item);
    if (item.children?.length) {
      result.push(...flattenOrgTree(item.children));
    }
  }
  return result;
}
</script>
