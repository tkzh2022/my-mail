<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 class="login-title">Login</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="Account" prop="account">
          <el-input v-model="form.account" placeholder="Username / Email / Phone" size="large" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="Password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" native-type="submit" class="login-btn">
            Login
          </el-button>
        </el-form-item>
        <div class="login-footer">
          <router-link to="/register">Create Account</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  account: '',
  password: '',
})

const rules: FormRules = {
  account: [{ required: true, message: 'Please enter your account', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login({ account: form.account, password: form.password, loginType: 'password' })
    ElMessage.success('Login successful')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    ElMessage.error(e.message || 'Login failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
}

.login-title {
  text-align: center;
  margin: 0;
  font-size: 24px;
}

.login-btn {
  width: 100%;
}

.login-footer {
  text-align: center;
}
</style>
