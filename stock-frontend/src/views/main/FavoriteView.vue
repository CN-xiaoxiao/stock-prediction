<script setup>

import StockPreviewCard from "@/components/StockPreviewCard.vue";
import {computed, reactive, ref} from "vue";
import {useRoute} from "vue-router";
import {get} from "@/net/index.js";
import StockDatils from "@/components/StockDatils.vue";
import StockDatilsAndPredict from "@/components/StockDatilsAndPredict.vue";

const favorite = ref([])
const route = useRoute()

function updateList() {
  if (route.name === 'main-favorite') {
    get('/api/stock/favoriteList', data => {
      favorite.value = data
      console.log(favorite.value)
    })
  }
}
updateList()

const favoriteList = computed(() => {
  return favorite.value
})

const detail = reactive( {
  show: false,
  stockBasic: {},
  tsCode: ""
})

function displayStockDetails(tsCode, index) {
  detail.show = true
  detail.tsCode = tsCode
  detail.stockBasic = favoriteList.value[index]
}

function closeHandel() {
  detail.tsCode = ""
  detail.show = false
  detail.stockBasic = {}
}

</script>

<template>
  <div class="main-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-star"></i> 个人收藏夹</div>
        <div class="desc">在这里选择已收藏的股票后可查看预测分析结果</div>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div style="display: flex; margin-top: 15px" class="card-list">
      <StockPreviewCard v-for="(item, index) in favoriteList" :basic-data="item"
      @click="displayStockDetails(item.tsCode, index)"/>
    </div>
    <el-drawer size="520" :with-header="false" :show-close="false" v-model="detail.show"
               v-if="favoriteList.length" @close="closeHandel()">
      <stock-datils-and-predict :stock-basic="detail.stockBasic" @delete="updateList" :update="updateList"/>
    </el-drawer>
  </div>

</template>

<style scoped>
:deep(.el-drawer) {
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body) {
  padding: 0;
}

.main-main {
  margin: 0 50px;
  .title {
    font-size: 22px;
  }

  .desc {
    font-size: 15px;
    color: grey;
  }
  .card-list {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
  }
}
</style>