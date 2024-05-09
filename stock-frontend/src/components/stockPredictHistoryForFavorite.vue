<script setup>
import * as echarts from "echarts"
import {onMounted, watch} from "vue";
import {defaultOption, doubleSeries, singleSeries} from "@/echarts";
import {onUnmounted} from "@vue/runtime-core";

const charts = []

const props = defineProps({
  data: Object
})

const localTimeLine = list => list.map(item => item.tradeDate)

function updateOpen(list) {
  const chart = charts[0]
  let data = [
    list.map(item => (item.open.toFixed(2))),
    list.map(item => (item.close.toFixed(2)))
  ]
  const option = defaultOption('开收盘(元)', localTimeLine(list))
  doubleSeries(option, ['开盘价', '收盘价'], data, [
    ['#72c4fe', '#72d5fe', '#2b6fd733'],
    ['#FC8F8FED', '#FFC1C1', 'rgba(192,242,253,0.2)']
  ])
  chart.setOption(option)
}

function updateHighAndLow(list) {
  const chart = charts[1]
  let data = [
    list.map(item => item.high.toFixed(2)),
    list.map(item => item.low.toFixed(2))
  ]
  const option = defaultOption('出价(元)', localTimeLine(list))
  doubleSeries(option, ['最高价', '最低价'], data, [
    ['#f6b66e', '#ffd29c', '#fddfc033'],
    ['#79c7ff', '#3cabf3', 'rgba(192,242,253,0.2)']
  ])
  chart.setOption(option);
}

function updateVlo(list) {
  const chart = charts[2]
  let data = list.map(item => (item.vol.toFixed(2)));
  const option = defaultOption('成交量(手)', localTimeLine(list))
  singleSeries(option, '成交量', data, ['#FFB6C1', '#FFE4E1', '#FFF0F5'])
  chart.setOption(option);
}


function initCharts() {
  const chartList = [
    document.getElementById('pre-open'),
    document.getElementById('pre-highAndLow'),
    document.getElementById('pre-vlo'),
  ]
  for (let i = 0; i < chartList.length; i++) {
    const chart = chartList[i]
    charts[i] = echarts.init(chart)
  }
}

function destroy() {
  const chartList = [
    document.getElementById('pre-open'),
    document.getElementById('pre-highAndLow'),
    document.getElementById('pre-vlo'),
  ]
  for (let i = 0; i < chartList.length; i++) {
    const chart = chartList[i]
    echarts.getInstanceByDom(chart).dispose()
  }
}

onMounted(() => {
  initCharts()
  watch(() => props.data, list => {
    updateOpen(list)
    updateHighAndLow(list)
    updateVlo(list)
  }, {immediate: true, deep: true})
})

</script>

<template>
  <div class="charts">
    <div id="pre-open" style="width: 222px;height: 170px"></div>
    <div id="pre-highAndLow" style="width: 222px;height: 170px"></div>
    <div id="pre-vlo" style="width: 222px;height: 170px"></div>
  </div>
</template>

<style scoped>
.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-gap: 20px;
}
</style>