<script setup>
import {useRoute} from "vue-router";
import {ref,computed, reactive} from "vue";
import {get, post, put} from "@/net/index.js"
import {Search} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import StockDatils from "@/components/StockDatils.vue";

const list = ref([])
const route = useRoute()
const favoriteList = ref([])

const updateList = () => {
  if (route.name === 'main-main') {
    get('/api/stock/hot', data => {
      list.value = data
    })
  }
}
setInterval(updateList, 15000)

let tableData = ref([])

function updateFavoriteList() {
  if (route.name === 'main-main') {
    get('/api/stock/favorite', data => {
      favoriteList.value = data.favoriteList
    })
  }
}
updateFavoriteList()
tableData = computed(() => {
    updateList()
    return initTable()
})

const cellStyle= ({ row, column, rowIndex, columnIndex })=> {
  if (row[column.property] == null) {
    row[column.property] = '----'
  }
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)


const initTable = () => {
  total.value = list.value.length
  // 分页逻辑在这里体现
  let start = currentPage.value > 1 ? (currentPage.value - 1) * pageSize.value : 0
  let end = currentPage.value * pageSize.value
  // 将符合条件的数据赋值给 tableData
  tableData = list.value.slice(start, end)
  return tableData
}

const handleSizeChange = (val) => {
  currentPage.value = 1
  pageSize.value = val
  initTable()
}
const handleCurrentChange = (val) => {
  currentPage.value = val
  initTable()
}

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
  <div class="main-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-list"></i> 热门股票榜单</div>
        <div class="desc">数据来自“东方财富网”，榜单每30分钟更新一次，点击股票<br>即可查看详情，收藏后可在个人收藏界面查看预测分析结果。</div>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div style="display: flex; margin-top: 15px">
      <div class="main-table">
        <el-table
            :data="tableData"
            :cell-style="cellStyle"
            stripe
            empty-text="null"
            max-height="461"
            @row-click="viewDetails"
        >
          <el-table-column label="序号" align='center' width="60">
            <template #default="scope">
              {{ scope.$index +(currentPage - 1) * pageSize+ 1 }}
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
              <el-button link type="primary" size="small" @click.stop="viewDetails(scope.row)">
                详情
              </el-button>
              <el-button v-if="!isFavorite(scope.row)" link type="warning" size="small" @click="addFavorite(scope.row)">
                收藏</el-button>
              <el-button v-else disabled link  size="small">已收藏</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          background
          layout="sizes, prev, pager, next"
          :total=total
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 10px;"
      />
      </div>
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

.main-main {
  margin: 0 50px;
  .title {
    font-size: 22px;
  }

  .desc {
    font-size: 15px;
    color: grey;
  }

  .main-table {
    width: 70vw;
  }

  .main-search {
    width: 300px;
    margin-left: 10px;
    background-color: pink;
  }
}

</style>