<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <h3>My Profile</h3>
      </template>
      <el-form :model="form" label-width="120px" @submit.prevent="handleSave">
        <el-form-item label="Username">
          <el-input :model-value="authStore.user?.username" disabled />
        </el-form-item>
        <el-form-item label="Nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="Avatar URL">
          <el-input v-model="form.avatarUrl" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit">Save</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { put } from '@/services/http'

const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  nickname: '',
  avatarUrl: '',
})

onMounted(() => {
  if (authStore.user) {
    form.nickname = authStore.user.username
  }
})

async function handleSave() {
  loading.value = true
  try {
    await put('/users/profile', form)
    ElMessage.success('Profile updated')
  } catch (e: any) {
    ElMessage.error(e.message || 'Update failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 600px;
  margin: 24px auto;
}
</style>
