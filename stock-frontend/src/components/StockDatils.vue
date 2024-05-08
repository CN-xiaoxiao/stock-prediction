<script setup>
import {watch, reactive, ref} from "vue";
import {get, post} from "@/net/index.js";
import {Delete, Star, StarFilled} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {useClipboard} from "@vueuse/core";
import StockDailyHistory from "@/components/stockDailyHistory.vue";
import App from "@/App.vue";


const props = defineProps({
  stockBasic: Object
})

const details = reactive({
  truly: {list: []},
  predict: {}
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

watch(() => props.stockBasic.tsCode, value => {
  if (value !== "") {
    details.truly = {list: []}
    body.tsCode = value
    body.date = formatDate(new Date())
    post('/api/stock/daily', body, data => {
      Object.assign(details.truly, data)
      let len = details.truly.list.length
      recentTrulyData.value = details.truly.list[len-1]
      console.log(recentTrulyData.value)
    })
    body = {}
  }
}, { immediate: true})

function favoriteStock() {

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
        <el-button :icon="Star" type="danger" style="margin-left: 0;"
                   @click="favoriteStock" plain text>收藏此股票</el-button>
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
            <span>开盘价/收盘价</span>
            <span>{{recentTrulyData.value.open}} / {{recentTrulyData.value.close}}</span>
          </div>
          <div>
            <span>昨收价(前复权)</span>
            <span>{{recentTrulyData.value.preClose}}</span>
          </div>
          <div>
            <span>涨幅额</span>
            <span>{{recentTrulyData.value.change+`%`}}</span>
          </div>
          <div>
            <span>涨跌幅</span>
            <span>{{recentTrulyData.value.pctChg+`%`}}</span>
          </div>
          <div>
            <span>最高价/最低价</span>
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
    </div>
    <div class="title" style="margin-top: 20px">
      <i class="fa-solid fa-gauge-high"></i>
      历史交易信息
    </div>
    <el-divider style="margin: 10px 0;"/>
    <div style="min-height: 200px" v-loading="!details.truly.list.length">
      <stock-daily-history style="margin-top: 20px" :data="details.truly.list"/>
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