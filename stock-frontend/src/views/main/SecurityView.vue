<script setup xmlns="http://www.w3.org/1999/html">

import {ElMessage} from "element-plus";
import {reactive, ref} from "vue";
import {Delete, Lock, Plus, Refresh, Switch} from "@element-plus/icons-vue";
import {get, logout, post} from "@/net";
import router from "@/router";

import {useStore} from "@/store";

const store = useStore()

const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid

const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: '',
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.new_password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const emailForm = reactive({
  email: store.user.email,
  code: ''
})

const coldTime = ref(0)
const isEmailValid = ref(true)

const onEmailValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

const validateEmail = () => {
  coldTime.value = 60
  let handle;
  get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`, () => {
    ElMessage.success(`验证码已发送到邮箱: ${emailForm.email}，请注意查收`)
    handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}

function modifyEmail() {
  post('/api/user/modify-email', emailForm, () => {
    ElMessage.success('邮件修改成功')
    logout(() => router.push('/'))
  })
}

const rules = {
  password: [
    { required: true, message: '请输入原来的密码', trigger: 'blur' },
  ],
  new_password: [
    { required: true, message: '请输入新的密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
  ],
  new_password_repeat: [
    { required: true, message: '请重复输入新的密码', trigger: 'blur' },
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ]
}

function resetPassword() {
  formRef.value.validate(isValid => {
    if(isValid) {
      post('/api/user/change-password', form, () => {
        ElMessage.success('密码修改成功，请重新登录!')
        logout(() => router.push('/'))
      })
    }
  })
}
</script>

<template>
  <div style="display: flex;gap: 10px">
    <div style="flex: 50%">
      <div class="info-card">
        <div class="title"><i class="fa-solid fa-lock"></i> 修改密码</div>
        <el-divider style="margin: 10px 0"/>
        <el-form @validate="onValidate" :model="form" :rules="rules"
                 ref="formRef" style="margin: 20px" label-width="100">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" v-model="form.password"
                      :prefix-icon="Lock" placeholder="当前密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" v-model="form.new_password"
                      :prefix-icon="Lock" placeholder="新密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" v-model="form.new_password_repeat"
                      :prefix-icon="Lock" placeholder="重复新密码" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button :icon="Switch" @click="resetPassword"
                       type="success" :disabled="!valid">立即重置密码</el-button>
          </div>
        </el-form>
      </div>
    </div>
    <div class="info-card" style="flex: 50%; padding-bottom: 54px">
      <div class="title"><i class="fa-regular fa-envelope"></i> 电子邮件设置</div>
      <el-divider style="margin: 10px 0"/>
      <el-form :model="emailForm" label-position="top" :rules="rules"
               ref="emailFormRef" @validate="onEmailValidate" style="margin: 0 10px 10px 10px">
        <el-form-item label="电子邮件" prop="email">
          <el-input v-model="emailForm.email"/>
        </el-form-item>
        <el-form-item>
          <el-row style="width: 100%" :gutter="10">
            <el-col :span="18">
              <el-input placeholder="请获取验证码" v-model="emailForm.code"/>
            </el-col>
            <el-col :span="6">
              <el-button type="success" @click="validateEmail" style="width: 100%;"
                         :disabled="!isEmailValid || coldTime > 0">
                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
        <div>
          <el-button @click="modifyEmail" :disabled="!emailForm.email"
                     :icon="Refresh" type="success">保存电子邮件</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.info-card {
  border-radius: 7px;
  padding: 15px 20px;
  background-color: var(--el-bg-color);
  height: fit-content;

  .title {
    font-size: 18px;
    font-weight: bold;
    color: dodgerblue;
  }
}

.account-card {
  border-radius: 5px;
  background-color: var(--el-bg-color-page);
  padding: 10px;
  display: flex;
  align-items: center;
  text-align: left;
  margin: 10px 0;
}

:deep(.el-drawer) {
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body) {
  padding: 0;
}
</style>