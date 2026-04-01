<template>
  <section class="space-y-5">
    <div class="surface-panel p-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-[18px] font-semibold text-primary-text">角色管理</h1>
          <p class="mt-1 text-[12px] text-muted">维护角色、权限点和数据范围配置。</p>
        </div>
        <div class="flex items-center gap-2">
          <el-input v-model="keyword" placeholder="按角色名称/编码搜索" clearable class="w-[240px]" @keyup.enter="loadData" />
          <el-button round @click="loadData">搜索</el-button>
          <el-button type="success" round @click="openCreate">新增角色</el-button>
        </div>
      </div>
    </div>

    <div class="surface-panel p-5">
      <el-table :data="tableData" v-loading="loading" size="small">
        <el-table-column prop="name" label="角色名" min-width="130" />
        <el-table-column prop="code" label="编码" min-width="130" />
        <el-table-column prop="dataScopeType" label="数据范围" width="150" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "启用" : "停用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" round type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" round @click="openPermission(row)">授权</el-button>
              <el-button size="small" round :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
                {{ row.status === 1 ? "停用" : "启用" }}
              </el-button>
              <el-button size="small" round type="danger" @click="removeRole(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="540px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="角色名称">
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.code" maxlength="64" :disabled="Boolean(editingId)" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScopeType" class="w-full">
            <el-option label="ALL" value="ALL" />
            <el-option label="ORG_AND_CHILDREN" value="ORG_AND_CHILDREN" />
            <el-option label="ORG_ONLY" value="ORG_ONLY" />
            <el-option label="SELF" value="SELF" />
            <el-option label="CUSTOM_ORG" value="CUSTOM_ORG" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="success" round :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionVisible" title="角色授权配置" width="760px">
      <el-form :model="permissionForm" label-width="110px">
        <el-form-item label="数据范围">
          <el-select v-model="permissionForm.dataScopeType" class="w-full">
            <el-option label="ALL" value="ALL" />
            <el-option label="ORG_AND_CHILDREN" value="ORG_AND_CHILDREN" />
            <el-option label="ORG_ONLY" value="ORG_ONLY" />
            <el-option label="SELF" value="SELF" />
            <el-option label="CUSTOM_ORG" value="CUSTOM_ORG" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-select v-model="permissionForm.menuIds" multiple filterable class="w-full">
            <el-option v-for="item in menuOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="接口权限">
          <el-select v-model="permissionForm.apiIds" multiple filterable class="w-full">
            <el-option
              v-for="item in apiOptions"
              :key="item.id"
              :label="`${item.permCode} (${item.method} ${item.path})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="permissionForm.dataScopeType === 'CUSTOM_ORG'" label="自定义组织">
          <el-select v-model="permissionForm.customOrgIds" multiple filterable class="w-full">
            <el-option v-for="item in orgOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="permissionVisible = false">取消</el-button>
        <el-button type="success" round :loading="permissionSubmitting" @click="submitPermission">保存授权</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { createRole, deleteRole, getRoleDetail, listRoles, updateRole, updateRolePermissions, updateRoleStatus } from "../api/modules/adminRole";
import { listApiResources, listMenuResources } from "../api/modules/adminPermission";
import { listOrgTree } from "../api/modules/adminOrg";
import type { ApiResourceItem, MenuResourceItem, OrgNode, RoleItem } from "../types";

const loading = ref(false);
const submitting = ref(false);
const permissionSubmitting = ref(false);
const tableData = ref<RoleItem[]>([]);
const menuOptions = ref<MenuResourceItem[]>([]);
const apiOptions = ref<ApiResourceItem[]>([]);
const orgOptions = ref<{ id: number; name: string }[]>([]);
const keyword = ref("");
const page = ref(1);
const size = ref(10);
const total = ref(0);

const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const permissionVisible = ref(false);
const permissionRoleId = ref<number | null>(null);

const form = reactive({
  name: "",
  code: "",
  description: "",
  dataScopeType: "ORG_ONLY",
});

const permissionForm = reactive({
  dataScopeType: "ORG_ONLY",
  menuIds: [] as number[],
  apiIds: [] as number[],
  customOrgIds: [] as number[],
});

onMounted(async () => {
  await Promise.all([loadData(), loadPermissionOptions()]);
});

async function loadData() {
  loading.value = true;
  try {
    const data = await listRoles({ page: page.value, size: size.value, keyword: keyword.value.trim() || undefined });
    tableData.value = data.items;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

async function loadPermissionOptions() {
  const [menus, apis, orgTree] = await Promise.all([listMenuResources(), listApiResources(), listOrgTree()]);
  menuOptions.value = menus;
  apiOptions.value = apis;
  orgOptions.value = flattenOrgTree(orgTree).map((item) => ({ id: item.id, name: item.name }));
}

function onPageChange(current: number) {
  page.value = current;
  loadData();
}

function resetForm() {
  form.name = "";
  form.code = "";
  form.description = "";
  form.dataScopeType = "ORG_ONLY";
}

function openCreate() {
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: RoleItem) {
  editingId.value = row.id;
  form.name = row.name;
  form.code = row.code;
  form.description = row.description ?? "";
  form.dataScopeType = row.dataScopeType || "ORG_ONLY";
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.name.trim() || !form.code.trim()) {
    ElMessage.error("角色名称和编码不能为空");
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateRole(editingId.value, {
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        dataScopeType: form.dataScopeType,
      });
      ElMessage.success("角色更新成功");
    } else {
      await createRole({
        name: form.name.trim(),
        code: form.code.trim(),
        description: form.description.trim() || undefined,
        dataScopeType: form.dataScopeType,
        status: 1,
      });
      ElMessage.success("角色创建成功");
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    submitting.value = false;
  }
}

async function toggleStatus(row: RoleItem) {
  try {
    await updateRoleStatus(row.id, row.status === 1 ? 0 : 1);
    ElMessage.success("状态更新成功");
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function removeRole(row: RoleItem) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, "提示", { type: "warning" });
    await deleteRole(row.id);
    ElMessage.success("删除成功");
    await loadData();
  } catch (error) {
    if ((error as Error).message !== "cancel") {
      ElMessage.error((error as Error).message);
    }
  }
}

async function openPermission(row: RoleItem) {
  try {
    const detail = await getRoleDetail(row.id);
    permissionRoleId.value = row.id;
    permissionForm.dataScopeType = detail.dataScopeType || "ORG_ONLY";
    permissionForm.menuIds = [...(detail.menuIds ?? [])];
    permissionForm.apiIds = [...(detail.apiIds ?? [])];
    permissionForm.customOrgIds = [...(detail.customOrgIds ?? [])];
    permissionVisible.value = true;
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function submitPermission() {
  if (!permissionRoleId.value) {
    return;
  }
  permissionSubmitting.value = true;
  try {
    await updateRolePermissions(permissionRoleId.value, {
      dataScopeType: permissionForm.dataScopeType,
      menuIds: permissionForm.menuIds,
      apiIds: permissionForm.apiIds,
      customOrgIds: permissionForm.customOrgIds,
    });
    ElMessage.success("授权更新成功");
    permissionVisible.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    permissionSubmitting.value = false;
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
