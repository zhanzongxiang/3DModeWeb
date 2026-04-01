<template>
  <section class="space-y-5">
    <div class="surface-panel p-5">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-[18px] font-semibold text-primary-text">组织管理</h1>
          <p class="mt-1 text-[12px] text-muted">树形组织结构管理，支持新增、编辑、启停用与删除。</p>
        </div>
        <el-button type="success" round @click="openCreate()">新增组织</el-button>
      </div>
    </div>

    <div class="surface-panel p-5">
      <el-table :data="flatRows" row-key="id" v-loading="loading" size="small">
        <el-table-column prop="name" label="名称" min-width="180">
          <template #default="{ row }">
            <span :style="{ paddingLeft: `${row.level * 20}px` }">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="编码" min-width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "启用" : "停用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" round @click="openCreate(row.id)">加下级</el-button>
              <el-button size="small" round type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button
                size="small"
                round
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? "停用" : "启用" }}
              </el-button>
              <el-button size="small" round type="danger" @click="removeOrg(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑组织' : '新增组织'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级组织">
          <el-select v-model="form.parentId" class="w-full" placeholder="请选择上级组织">
            <el-option :value="0" label="根组织" />
            <el-option
              v-for="opt in parentOptions"
              :key="opt.id"
              :value="opt.id"
              :label="`${'　'.repeat(opt.level)}${opt.name}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="组织名称">
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="组织编码">
          <el-input v-model="form.code" maxlength="64" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="success" round :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { createOrg, deleteOrg, listOrgTree, updateOrg, updateOrgStatus } from "../api/modules/adminOrg";
import type { OrgNode } from "../types";

type FlatOrgRow = OrgNode & { level: number };

const loading = ref(false);
const submitting = ref(false);
const tree = ref<OrgNode[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);

const form = reactive({
  parentId: 0,
  name: "",
  code: "",
  sort: 0,
});

const flatRows = computed(() => flattenTree(tree.value));
const parentOptions = computed(() => flatRows.value.filter((item) => item.id !== editingId.value));

onMounted(() => {
  loadData();
});

async function loadData() {
  loading.value = true;
  try {
    tree.value = await listOrgTree();
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.parentId = 0;
  form.name = "";
  form.code = "";
  form.sort = 0;
}

function openCreate(parentId = 0) {
  editingId.value = null;
  resetForm();
  form.parentId = parentId;
  dialogVisible.value = true;
}

function openEdit(row: FlatOrgRow) {
  editingId.value = row.id;
  form.parentId = row.parentId ?? 0;
  form.name = row.name;
  form.code = row.code ?? "";
  form.sort = row.sort ?? 0;
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.name.trim()) {
    ElMessage.error("请输入组织名称");
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateOrg(editingId.value, {
        parentId: form.parentId,
        name: form.name.trim(),
        code: form.code.trim() || undefined,
        sort: form.sort,
      });
      ElMessage.success("组织更新成功");
    } else {
      await createOrg({
        parentId: form.parentId,
        name: form.name.trim(),
        code: form.code.trim() || undefined,
        sort: form.sort,
        status: 1,
      });
      ElMessage.success("组织创建成功");
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  } finally {
    submitting.value = false;
  }
}

async function toggleStatus(row: FlatOrgRow) {
  try {
    await updateOrgStatus(row.id, row.status === 1 ? 0 : 1);
    ElMessage.success("状态更新成功");
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message);
  }
}

async function removeOrg(row: FlatOrgRow) {
  try {
    await ElMessageBox.confirm(`确认删除组织「${row.name}」？`, "提示", { type: "warning" });
    await deleteOrg(row.id);
    ElMessage.success("删除成功");
    await loadData();
  } catch (error) {
    if ((error as Error).message !== "cancel") {
      ElMessage.error((error as Error).message);
    }
  }
}

function flattenTree(list: OrgNode[], level = 0): FlatOrgRow[] {
  const result: FlatOrgRow[] = [];
  for (const item of list) {
    result.push({ ...item, level });
    if (item.children?.length) {
      result.push(...flattenTree(item.children, level + 1));
    }
  }
  return result;
}
</script>
