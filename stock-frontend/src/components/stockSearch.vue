<script setup>
import {reactive, ref} from "vue";
import {get, put} from "@/net/index.js";
import {ElMessage} from "element-plus";
import StockDatils from "@/components/StockDatils.vue";

const props = defineProps({
  stockList: []
})

const favoriteList = ref([])

function updateFavoriteList() {
  get('/api/stock/favorite', data => {
    favoriteList.value = data.favoriteList
  })
}
updateFavoriteList()

const cellStyle= ({ row, column, rowIndex, columnIndex })=> {
  if (row[column.property] == null) {
    row[column.property] = '----'
  }
}
let tableData = ref([])

tableData.value = props.stockList

function addFavorite(row) {
  let tsCode = row.tsCode
  put(`/api/stock/favorite?tsCode=${tsCode}`, null, () => {
    ElMessage.success(`已收藏股票: ${row.symbol}[${row.name}]`)
    updateFavoriteList()
  }, (message) => {
    ElMessage.warning(message)
  })
}

function isFavorite(row) {
  let flag = false;
  for (let i = 0; i < favoriteList.value.length; i++) {
    if (favoriteList.value[i] === row.tsCode) {
      flag = true;
      break
    }
  }
  return flag;
}
const detail = reactive({
  show: false,
  stockBasic: {}
})

function viewDetails(row) {
  detail.show = true
  detail.stockBasic = row
}

</script>

<template>
  <div class="search-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-square-poll-horizontal"></i> 搜索结果</div>
        <div class="desc">点击股票即可查看详情，收藏后可在个人收藏界面查看预测分析结果。</div>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div class="search-table" style="margin-left: auto; margin-right: auto; width: 80%">
      <el-table
          :data="tableData"
          :cell-style="cellStyle"
          stripe
          empty-text="null"
          max-height="480"
      >
        <el-table-column label="序号" align='center' width="60">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="symbol" label="股票代码" width="90" />
        <el-table-column prop="name" label="股票名称" width="90" />
        <el-table-column prop="area" label="地域" width="90"/>
        <el-table-column prop="industry" label="所属行业" width="90" />
        <el-table-column prop="cnspell" label="拼音缩写" width="90" />
        <el-table-column prop="market" label="市场类型"  width="90" />
        <el-table-column prop="listDate" label="上市日期" width="90" />
        <el-table-column prop="actName" label="实控人名称" width="180" />
        <el-table-column prop="actEntType" label="企业性质" width="90" />
        <el-table-column fixed="right" label="操作" width="110">
          <template v-slot="scope">
            <el-button link type="primary" size="small" @click="viewDetails(scope.row)">
              详情
            </el-button>
            <el-button v-if="!isFavorite(scope.row)" link type="warning" size="small" @click="addFavorite(scope.row)">
              收藏</el-button>
            <el-button v-else disabled link  size="small">已收藏</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-drawer size="520" :with-header="false" :show-close="false" v-model="detail.show" v-if="tableData.length">
      <StockDatils :stock-basic="detail.stockBasic"/>
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
.search-main {
  margin: 0 50px;
  .title {
    font-size: 22px;
  }

  .desc {
    font-size: 15px;
    color: grey;
  }
}
</style>