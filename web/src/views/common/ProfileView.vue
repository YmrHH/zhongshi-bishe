<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMe, updateProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const phone = ref('')
const oldPassword = ref('')
const newPassword = ref('')

onMounted(async () => {
  const profile = (await fetchMe()).data.data
  userStore.setProfile(profile)
  phone.value = profile.phone || ''
})

async function submit() {
  loading.value = true
  try {
    const res = await updateProfile({
      phone: phone.value,
      oldPassword: oldPassword.value || undefined,
      newPassword: newPassword.value || undefined
    })
    userStore.setProfile(res.data.data)
    oldPassword.value = ''
    newPassword.value = ''
    ElMessage.success('个人资料已更新')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '更新失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">公共 / 个人中心</div>
    <h1 class="title">个人中心</h1>
    <div class="page-card">
      <el-descriptions :column="2" border style="margin-bottom:24px">
        <el-descriptions-item label="用户名">{{ userStore.profile?.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userStore.profile?.roleLabel }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userStore.profile?.realName }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ userStore.profile?.orgName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px" style="max-width:480px">
        <el-form-item label="联系电话">
          <el-input v-model="phone" />
        </el-form-item>
        <el-form-item label="原密码">
          <el-input v-model="oldPassword" type="password" show-password placeholder="修改密码时填写" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
