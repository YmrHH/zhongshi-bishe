<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ username: 'enterprise', password: '123456' })
const loading = ref(false)

const demos = [
  { username: 'dispatcher', label: '调度员 dispatcher / 123456' },
  { username: 'auditor', label: '审核员 auditor / 123456' },
  { username: 'enterprise', label: '企业 enterprise / 123456' },
  { username: 'technician', label: '技术人员 technician / 123456' }
]

async function onSubmit() {
  loading.value = true
  try {
    const res = await login(form.value.username, form.value.password)
    const data = res.data.data
    userStore.setToken(data.token)
    userStore.setProfile({ ...data, id: 0 })
    await userStore.loadProfile()
    ElMessage.success('登录成功')
    router.push(data.homePath)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '登录失败')
  } finally {
    loading.value = false
  }
}

function fillDemo(username: string) {
  form.value.username = username
  form.value.password = '123456'
}
</script>

<template>
  <div class="login-page">
    <div class="panel">
      <h1>广州生产力促进中心</h1>
      <h2>中试服务管理系统</h2>
      <el-form @submit.prevent="onSubmit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
      </el-form>
      <div class="demo">
        <p>演示账号（点击填充）：</p>
        <el-tag
          v-for="d in demos"
          :key="d.username"
          class="tag"
          effect="plain"
          @click="fillDemo(d.username)"
        >{{ d.label }}</el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eef3f8 0%, #dbeafe 100%);
}
.panel {
  width: 420px; background: #fff; border-radius: 18px; padding: 40px;
  box-shadow: 0 8px 32px rgba(31, 45, 61, 0.08);
}
h1 { margin: 0; font-size: 22px; color: #303133; }
h2 { margin: 8px 0 28px; font-size: 16px; color: #909399; font-weight: normal; }
.demo { margin-top: 24px; font-size: 13px; color: #606266; }
.tag { margin: 4px 4px 0 0; cursor: pointer; }
</style>
