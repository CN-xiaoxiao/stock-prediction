<script setup>
import {watch, reactive, ref, computed} from "vue";
import {get, post} from "@/net/index.js";
import {Delete, DeleteFilled, Star, StarFilled} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useClipboard} from "@vueuse/core";
import StockDailyHistoryForFavorite from "@/components/stockDailyHistoryForFavorite.vue";
import StockPredictHistoryForFavorite from "@/components/stockPredictHistoryForFavorite.vue";



const props = defineProps({
  stockBasic: Object,
  update: Function
})

const emits = defineEmits(['delete'])

const details = reactive({
  truly: {list: []},
  predict: { list: []}
})
let body = {
  tsCode: "",
  date: ""
};
const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
const recentTrulyData = reactive({})
const recentPredictData = reactive({})

watch(() => props.stockBasic.tsCode, value => {
  if (value !== "" && value !=='' && value !== null && value !== undefined) {
    details.truly = {list: []}
    body.tsCode = value
    body.date = formatDate(new Date())
    post('/api/stock/daily', body, data => {
      Object.assign(details.truly, data)
      let len = details.truly.list.length
      recentTrulyData.value = details.truly.list[len-1]
    }, () => {
      details.truly.list = []
      recentTrulyData.value = {}
    })
    get(`/api/stock/predict-list?tsCode=${value}`, data => {
      Object.assign(details.predict.list, data)
      let len = details.predict.list.length
      recentPredictData.value = details.predict.list[len-1]
    }, () => {
      details.predict.list = []
      recentPredictData.value = {}
    })
    body = {}
  }
}, { immediate: true})

const isUpChange = computed(() => recentTrulyData.value.change > 0)
const isUpPctChg = computed(() => recentTrulyData.value.pctChg >0)
const isUpOpen = computed(() => recentPredictData.value.open > recentTrulyData.value.open)
const isUpClose = computed(() => recentPredictData.value.close > recentTrulyData.value.close)
const isUpHigh = computed(() => recentPredictData.value.high > recentTrulyData.value.high)
const isUpLow = computed(() => recentPredictData.value.high > recentTrulyData.value.high)
const isUpVol = computed(() => recentPredictData.value.vol > recentTrulyData.value.vol)

function favoriteStock() {
  ElMessageBox.confirm('您确定要这样删除该股票吗？', '删除股票', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    get(`/api/stock/favorite-delete?tsCode=${props.stockBasic.tsCode}`, () => {
      emits('delete')
      props.update()
      ElMessage.success('股票已成功移除')
    })
  }).catch(()=>{})
}
const {copy} = useClipboard()
const copyIp = ip => copy(ip).then(() => ElMessage.success('成功复制股票代码到剪切板'))

</script>

<template>
  <div class="stock-details">
    <div style="display: flex;justify-content: space-between">
      <div class="title">
        <i class="fa-solid fa-money-bill-trend-up"></i>
        股票基础信息
      </div>
      <div>
        <el-button :icon="DeleteFilled" type="danger" style="margin-left: 0;"
                   @click="favoriteStock" plain text>移出收藏夹</el-button>
      </div>
    </div>
    <el-divider style="margin: 10px 0;"/>
    <div class="details-list">
      <div>
        <span>股票名称</span>
        <span>{{stockBasic.name}}</span>&nbsp;
      </div>
      <div>
        <span>股票代码</span>
        <span>{{stockBasic.symbol}}</span>&nbsp;
        <i class="fa-solid fa-copy interact-item" style="color: dodgerblue" @click.stop="copyIp(stockBasic.symbol)"></i>
      </div>
      <div>
        <span>市场类型</span>
        <span>{{stockBasic.market}}</span>&nbsp;
      </div>
      <div>
        <span>地域</span>
        <span>{{stockBasic.area}}</span>&nbsp;
      </div>
      <div>
        <span>上市日期</span>
        <span>{{stockBasic.listDate}}</span>
      </div>
      <div>
        <span>所属行业</span>
        <span>{{stockBasic.industry}}</span>
      </div>
      <div>
        <span>实控人名称</span>
        <span>{{stockBasic.actName}}</span>
      </div>
      <div>
        <span>实控人企业性质</span>
        <span>{{stockBasic.actEntType}}</span>
      </div>
    </div>
    <div class="title" style="margin-top: 20px">
      <i class="fa-regular fa-calendar"></i>
      最近交易信息
    </div>
    <el-divider style="margin: 10px 0;"/>
    <div style="min-height: 200px" v-loading="!details.truly.list.length">
      <div style="display: flex; justify-content: space-between" v-if="details.truly.list.length" >
        <div class="details-list">
          <div>
            <span>交易日期</span>
            <span>{{recentTrulyData.value.tradeDate}}</span>
          </div>
          <div>
            <span>开盘价/收盘价(元)</span>
            <span>{{recentTrulyData.value.open}} / {{recentTrulyData.value.close}}</span>
          </div>
          <div>
            <span>昨收价(前复权)</span>
            <span>{{recentTrulyData.value.preClose}}</span>
          </div>
          <div>
            <span>涨幅额</span>
            <span :style="{'color': isUpChange ? 'red' : 'green'}">{{recentTrulyData.value.change+`%`}}</span>
          </div>
          <div>
            <span>涨跌幅</span>
            <span :style="{'color': isUpPctChg ? 'red' : 'green'}">{{recentTrulyData.value.pctChg+`%`}}</span>
          </div>
          <div>
            <span>最高价/最低价(元)</span>
            <span>{{recentTrulyData.value.high+` / `+recentTrulyData.value.low}}</span>
          </div>
          <div>
            <span>成交量(手)</span>
            <span>{{recentTrulyData.value.vol}}</span>
          </div>
          <div>
            <span>成交额(千元)</span>
            <span>{{recentTrulyData.value.amount}}</span>
          </div>
        </div>
      </div>
      <el-empty style="height: 180px" v-else description="暂无数据" />
    </div>
    <div class="title" style="margin-top: 20px">
      <i class="fa-solid fa-chart-line"></i>
      最近预测信息
    </div>
    <el-divider style="margin: 10px 0;"/>
    <div style="min-height: 180px">
      <div style="display: flex; justify-content: space-between" v-if="details.predict.list.length">
        <div class="details-list">
          <div>
            <span>交易日期</span>
            <span>{{recentPredictData.value.tradeDate}}</span>
          </div>
          <div>
            <span>开盘价(元)</span>
            <span :style="{'color': isUpOpen ? 'red' : 'green'}">{{recentPredictData.value.open.toFixed(2)}}</span>
          </div>
          <div>
            <span>收盘价(元)</span>
            <span :style="{'color': isUpClose ? 'red' : 'green'}">{{recentPredictData.value.close.toFixed(2)}}</span>
          </div>
          <div>
            <span>最高价(元)</span>
            <span :style="{'color': isUpHigh ? 'red' : 'green'}">{{recentPredictData.value.high.toFixed(2)}}</span>
          </div>
          <div>
            <span>最低价(元)</span>
            <span :style="{'color': isUpLow ? 'red' : 'green'}">{{recentPredictData.value.low.toFixed(2)}}</span>
          </div>
          <div>
            <span>成交量(手)</span>
            <span :style="{'color': isUpVol ? 'red' : 'green'}">{{recentPredictData.value.vol.toFixed(2)}}</span>
          </div>

        </div>
      </div>
      <el-empty style="height: 180px" v-else description="暂无数据" />
    </div>
    <div class="title" style="margin-top: 20px">
      <i class="fa-solid fa-gauge-high"></i>
      历史交易信息
    </div>
    <el-divider style="margin: 10px 0;"/>
    <div style="min-height: 200px" v-loading="!details.truly.list.length">
      <stock-daily-history-for-favorite style="margin-top: 20px" :data="details.truly.list"/>
    </div>
    <div class="title" style="margin-top: 20px">
      <i class="fa-solid fa-chart-line"></i>
      近期预测信息
    </div>
    <el-divider style="margin: 10px 0;"/>
    <div style="min-height: 200px" v-loading="!details.truly.list.length">
      <stock-predict-history-for-favorite v-if="details.predict.list.length" :data="details.predict.list"/>
      <el-empty style="height: 180px" v-else description="暂无数据" />
    </div>
  </div>
</template>

<style scoped>
.stock-details {
  height: 100%;
  padding: 20px;

  .title {
    color: dodgerblue;
    font-size: 18px;
    font-weight: bold;
  }

  .details-list {
    font-size: 14px;

    & div {
      margin-bottom: 10px;

      & span:first-child {
        color: rgb(128, 128, 128);
        font-size: 13px;
        font-weight: normal;
        width: 120px;
        display: inline-block;
      }

      & span {
        font-weight: bold;
      }
    }
  }
}
</style>