<script setup>

import StockPreviewCard from "@/components/StockPreviewCard.vue";
import {computed, reactive, ref} from "vue";
import {useRoute} from "vue-router";
import {get, post} from "@/net/index.js";
import StockDatilsAndPredict from "@/components/StockDatilsAndPredict.vue";
import UserAgreement from "@/components/userAgreement.vue";

const favorite = ref([])
const route = useRoute()

function updateList() {
  if (route.name === 'main-favorite') {
    get('/api/stock/favoriteList', data => {
      favorite.value = data
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

const tsCode = ref(null);
const index = ref(null);

function closeHandel() {
  detail.tsCode = ""
  detail.show = false
  detail.stockBasic = {}
  tsCode.value = null
  index.value = null
}

// 用户协议
const isAgreed = ref(false);
const showModal = ref(false);

function findIsAgreed() {
  if (route.name === 'main-favorite') {
    get('/api/user/agree', data => {
      isAgreed.value = data
    })
  }
}
findIsAgreed()
function saveUserAgreed() {
  if (route.name === 'main-favorite') {
    get('api/user/agree-agreement?isAgreed=true', () => {

    })
  }
}

const showAgreementModal = (code, idx) => {
  tsCode.value = code;
  index.value = idx;
  findIsAgreed()
  if (isAgreed.value === true) {
    displayStockDetails(tsCode.value, index.value)
  } else {
    showModal.value = true;
  }
};

const agreeAndClose = () => {
  isAgreed.value = true;
  showModal.value = false;
  saveUserAgreed()
  displayStockDetails(tsCode.value, index.value)
};

const closeModal = () => {
  showModal.value = false;
};
// 用户协议

</script>

<template>
  <div class="main-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-star"></i> 个人收藏夹</div>
        <div class="desc">在这里选择已收藏的股票后可查看预测分析结果，首次添加最迟需等待一天可得到预测结果。</div>
        <div style="font-size: 15px; color: red">注意：预测有风险，交易需谨慎！</div>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div style="display: flex; margin-top: 15px" class="card-list">
      <StockPreviewCard v-for="(item, index) in favoriteList" :basic-data="item"
                        @click="showAgreementModal(item.tsCode, index)"/>
    </div>
    <el-drawer size="520" :with-header="false" :show-close="false" v-model="detail.show"
               v-if="favoriteList.length" @close="closeHandel()">
      <stock-datils-and-predict :stock-basic="detail.stockBasic" @delete="updateList" :update="updateList"/>
    </el-drawer>
    <user-agreement
      v-if="showModal"
      @agree="agreeAndClose"
      @close="closeModal"
    />
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