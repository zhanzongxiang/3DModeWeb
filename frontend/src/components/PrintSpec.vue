<template>
  <section class="surface-panel p-5">
    <h2 class="text-[16px] font-semibold text-primary-text">打印建议</h2>
    <div class="mt-4 grid gap-3 sm:grid-cols-2">
      <div class="spec-item">
        <p class="spec-label">推荐层高</p>
        <p class="spec-value">{{ layerHeightText }}</p>
      </div>
      <div class="spec-item">
        <p class="spec-label">填充率</p>
        <p class="spec-value">{{ infillText }}</p>
      </div>
      <div class="spec-item">
        <p class="spec-label">支撑需求</p>
        <p class="spec-value">{{ supportText }}</p>
      </div>
      <div class="spec-item">
        <p class="spec-label">建议材质</p>
        <p class="spec-value">{{ materialText }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  printLayerHeight?: number | string;
  printInfill?: number | string;
  printSupport?: number | string;
  printMaterial?: string;
}>();

const layerHeightText = computed(() =>
  props.printLayerHeight !== undefined && props.printLayerHeight !== ""
    ? `${props.printLayerHeight} mm`
    : "-",
);
const infillText = computed(() =>
  props.printInfill !== undefined && props.printInfill !== "" ? `${props.printInfill}%` : "-",
);
const supportText = computed(() => {
  if (props.printSupport === undefined || props.printSupport === "") {
    return "-";
  }
  return Number(props.printSupport) === 1 ? "需要支撑" : "无需支撑";
});
const materialText = computed(() => props.printMaterial?.trim() || "-");
</script>
