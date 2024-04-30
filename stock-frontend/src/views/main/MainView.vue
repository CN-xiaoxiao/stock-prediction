<script setup>
import {useRoute} from "vue-router";
import {ref,computed} from "vue";
import {get} from "@/net/index.js"
import {Search} from "@element-plus/icons-vue";

const list = ref([])
const route = useRoute()

const updateList = () => {
  if (route.name === 'main-main') {
    get('/api/stock/hot', data => {
      list.value = data
    })
  }
}
setInterval(updateList, 10000)

let tableData = ref([])

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
</script>

<template>
  <div class="main-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-list"></i> 热门股票榜单</div>
        <div class="desc">数据来自“东方财富网”，榜单每30分钟更新一次，点击股票即可查看详情。</div>
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
            :row-class-name="tableRowClassName"
            max-height="461"
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
      <div class="main-search">
        <div style="margin-left: 5px;">
          <el-input
            v-model="input1"
            style="width: 200px"
            placeholder="请输入股票代码"
            :prefix-icon="Search"/>
          <el-button type="primary" plain style="margin-left: 10px">搜索</el-button>
        </div>
        <el-divider style="margin: 10px 0"/>
        <div style="height: 450px; margin-top: 15px;">
          123
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
    width: 64vw;
    margin-right: 50px;
  }

  .main-search {
    width: 300px;
    margin-left: 10px;
    background-color: pink;
  }
}

</style>