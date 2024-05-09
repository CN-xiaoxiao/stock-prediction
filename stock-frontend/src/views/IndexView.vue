<script setup>
import {logout} from "@/net";
import router from "@/router/index.js";
import {Back, Moon, Search, Star, Sunny} from "@element-plus/icons-vue";
import {useDark} from "@vueuse/core";
import {reactive, ref} from "vue"
import TabItem from "@/components/TabItem.vue";
import {useRoute} from "vue-router";
import {useStore} from "@/store";
import {get, post} from "@/net/index.js"
import StockDatils from "@/components/StockDatils.vue";
import StockSearch from "@/components/stockSearch.vue";

const store = useStore()
const dark = ref(useDark())

const tabs = [
  {id: 1, name: '首页', route: 'main-main'},
  {id: 2, name: '收藏', route: 'main-favorite'},
  {id: 3, name: '安全', route: 'main-security'}
]

const route = useRoute()
const defaultIndex = ()=>{
  for (let tab of tabs) {
    if (route.name === tab.route)
      return tab.id
  }
  return 1
}

const searchList = reactive({
  total: 0,
  list: []
})

const tab = ref(defaultIndex())

function userLogout() {
  logout(() => router.push('/'))
}

function pushToFavorite() {
  tab.value = 2
  input.search = ""
  searchList.total = 0
  searchList.list = []
  router.push('/favorite')
}

function changePage(item) {
  tab.value = item.id
  input.search = ""
  searchList.total = 0
  searchList.list = []
  router.push({name: item.route})
}
const input = reactive({
  search: ""
})

const detail = reactive({
  show: false
})

const page = reactive({
  pageNum: 1,
  pageSize: 20
})

function search() {
  if (input.search !== "") {
    get(`/api/stock/query?pageNum=${page.pageNum}&pageSize=${page.pageSize}&query=${input.search}`, (data) => {
      searchList.total = data.total
      searchList.list = data.list
    })
    detail.show = true
  }
}

function handleClose() {
  searchList.total = 0
  searchList.list = []
}

</script>

<template>
  <el-container class="main-container">
    <el-header class="main-header">
      <el-image style="height: 30px" src="https://element-plus.org/images/element-plus-logo.svg"/>
      <div style="margin-left:200px;margin-right:auto;">
        <el-input
            v-model="input.search"
            size="large"
            style="width: 300px"
            :prefix-icon="Search"
            placeholder="请输入股票代码或股票名称"/>
      </div>
      <div>
        <el-button size="large" style="margin-left: 5px"
                   @click="search()">搜索</el-button>
      </div>
      <div class="tabs">
        <tab-item v-for="item in  tabs" :name="item.name"
                  :active="item.id === tab" @click="changePage(item)"/>
        <el-switch style="margin: 0 20px"
                   v-model = "dark" active-color="#424242"
                   :active-action-icon = "Moon"
                   :inactive-action-icon = "Sunny"/>
        <div style="text-align: right;line-height: 16px;margin-right: 10px">
          <div>
          <el-tag v-if="store.isAdmin" type="success" size="small">管理员</el-tag>
          <el-tag v-else size="small">用户会员</el-tag>
                    {{store.user.username}}
          </div>
          <div style="font-size: 13px;color: grey">{{store.user.email}}</div>
        </div>
        <el-dropdown>
          <el-avatar class="avatar"
                     src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"/>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="pushToFavorite">
                <el-icon><Star/></el-icon>
                我的收藏
              </el-dropdown-item>
              <el-dropdown-item @click="userLogout">
                <el-icon><Back/></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component"/>
        </transition>
      </router-view>
      <el-drawer size="600" direction="btt" @close="handleClose()"
                 :with-header="false" :show-close="false" v-model="detail.show" v-if="searchList.list.length">
        <stock-search :stock-list="searchList.list"/>
      </el-drawer>
    </el-main>
  </el-container>
</template>

<style scoped>
.main-container {
  height: 100vh;
  width: 100vw;

  .main-header {
    height: 55px;
    background-color: var(--el-bg-color);
    border-bottom: solid 1px var(--el-border-color);
    display: flex;
    align-items: center;

    .tabs {
      height: 55px;
      gap: 10px;
      flex: 1px;
      display: flex;
      align-items: center;
      justify-content: right;
    }
  }

  .main-content {
    height: 100%;
    background-color: #f5f5f5;
  }
}

.dark .main-container .main-content {
  background-color: #232323;
}
:deep(.el-drawer) {
  padding: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body) {
  padding: 0;
}
</style>