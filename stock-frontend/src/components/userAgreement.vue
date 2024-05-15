<!-- UserAgreement.vue -->
<template>
  <el-dialog
      title="用户协议"
      v-model="visible"
      width="50%"
      @closed="$emit('close')"
      class="user-agreement-modal"
  >
    <div class="content">
      <p>请仔细阅读并同意用户协议。</p>
      <a href="#" @click.prevent="showAgreementText">查看用户协议</a>
      <div class="agreement-text" v-if="agreementText.length > 0">
        <pre>{{ agreementText }}</pre>
      </div>
    </div>
    <template #footer>
      <div class="actions">
        <el-button type="primary" @click="agree">同意</el-button>
        <el-button @click="close">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['agree', 'close']);

const visible = ref(true);

const agreementText = ref('')

const showAgreementText = () => {
  agreementText.value = `用户协议

这里是用户协议的具体内容,需要您仔细阅读并同意。您的使用行为将受到本协议的约束。
\n如果您不同意本协议的任何条款,请立即停止使用本服务。您的继续使用将被视为对本协议的完全接受。

本协议是由[公司名称]（以下简称"本公司"）与用户（以下简称"您"）之间就使用本公司提供的股票预测系统（以下简称"本系统"）所订立的协议。在您使用本系统之前,请仔细阅读并确认您已完全理解本协议的全部内容。一旦您开始使用本系统,即视为您已同意并接受本协议的全部条款。

一、免责声明
1. 本系统提供的股票预测结果仅供参考,最终投资决策由您自行承担。本公司不对您的投资行为或投资结果承担任何责任。
2. 本系统的预测结果可能存在错误、延误或不准确的情况,这可能会导致您的投资决策出现偏差。本公司不保证预测结果的准确性、及时性和完整性。
3. 您使用本系统产生的任何直接、间接、偶然、特殊或结果性损失,包括但不限于利润损失、数据损失或业务中断,本公司概不负责。
4. 本公司有权随时修改、暂停或终止本系统的运营,无需事先通知您或获得您的同意。

二、风险提示
1. 股票市场存在较大的不确定性和风险,您应当充分了解并自行评估相关风险。本公司无法保证您能从使用本系统获得收益。
2. 您使用本系统进行投资操作,可能会遭受亏损。您应当根据自身的风险承受能力谨慎决策,并承担相应的投资风险。
3. 本系统的预测结果可能存在偏差或错误,这可能会造成您的投资决策出现失误,给您带来损失。

三、其他条款
1. 本协议的解释和适用均受中华人民共和国法律管辖。
2. 本协议中的任何条款如果被认定为无效或不可执行,不影响其他条款的效力。
3. 本公司保留对本协议进行修改的权利,修改后的协议将通过本系统或其他渠道公布,您应当及时查阅并遵守。

您已充分理解并同意本协议的全部内容。
`
}

const agree = () => {
  emit('agree');
  visible.value = false;
};

const close = () => {
  emit('close');
  visible.value = false;
};
</script>

<style scoped>
.user-agreement-modal {
  font-size: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.content {
  padding: 30px;
  max-height: 345px;
}

.agreement-text {
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
  background-color: #f5f7fa;
  padding: 0 20px 20px 20px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
  letter-spacing: 1px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

.actions .el-button {
  margin-left: 10px;
}

.user-agreement-modal .content a {
  color: #409EFF;
  text-decoration: underline;
  cursor: pointer;
  font-size: 14px;
}

.user-agreement-modal .content a:hover {
  color: #66b1ff;
}

.user-agreement-modal ::-webkit-scrollbar {
  width: 8px;
}

.user-agreement-modal ::-webkit-scrollbar-track {
  background-color: #f5f7fa;
  border-radius: 4px;
}

.user-agreement-modal ::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 4px;
}

.user-agreement-modal ::-webkit-scrollbar-thumb:hover {
  background-color: #b3b8c2;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>