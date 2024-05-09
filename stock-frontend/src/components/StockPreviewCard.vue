<script setup>

const props = defineProps({
  basicData: Object,
  update: Function
})

</script>

<template>
  <div class="instance-card">
    <div style="display: flex;justify-content: space-between">
      <div>
        <div class="name">
          股票名称:
          <span style="margin: 0 5px">{{ basicData.name }}</span>
        </div>
        <div>
          股票代码:
          <span>{{basicData.symbol}}</span>
        </div>
        <div>
          上市日期:
          <span>{{basicData.listDate}}</span>
        </div>
        <div>
          市场类型:
          <span>{{basicData.market}}</span>
        </div>
        <div>
          所属行业:
          <span>{{basicData.industry}}</span>
        </div>
        <div>
          地域:
          <span>{{basicData.area}}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dark .instance-card {
  color: #d9d9d9;
}

.interact-item {
  transition: .3s;
  &:hover {
    cursor: pointer;
    scale: 1.1;
    opacity: 0.8;
  }
}

.instance-card {
  width: 320px;
  padding: 15px;
  background-color: var(--el-bg-color);
  border-radius: 5px;
  box-sizing: border-box;
  color: #606060;
  transition: .3s;

  &:hover {
    cursor: pointer;
    scale: 1.02;
  }

  .name {
    font-size: 15px;
    font-weight: bold;
  }
}
</style>