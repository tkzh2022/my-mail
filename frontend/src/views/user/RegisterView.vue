<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2 class="register-title">Create Account</h2>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="Consumer" name="user">
          <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-position="top" @submit.prevent="handleRegister('user')">
            <el-form-item label="Username" prop="username">
              <el-input v-model="userForm.username" size="large" />
            </el-form-item>
            <el-form-item label="Email" prop="email">
              <el-input v-model="userForm.email" size="large" />
            </el-form-item>
            <el-form-item label="Phone" prop="phone">
              <el-input v-model="userForm.phone" size="large" />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input v-model="userForm.password" type="password" size="large" show-password />
            </el-form-item>
            <el-form-item label="Verification Code" prop="verificationCode">
              <div class="code-row">
                <el-input v-model="userForm.verificationCode" size="large" />
                <el-button size="large" :disabled="codeCooldown > 0" @click="sendCode">
                  {{ codeCooldown > 0 ? `${codeCooldown}s` : 'Send Code' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" native-type="submit" class="register-btn">
                Register
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="Merchant" name="merchant">
          <el-form ref="merchantFormRef" :model="merchantForm" :rules="merchantRules" label-position="top" @submit.prevent="handleRegister('merchant')">
            <el-form-item label="Username" prop="username">
              <el-input v-model="merchantForm.username" size="large" />
            </el-form-item>
            <el-form-item label="Shop Name" prop="shopName">
              <el-input v-model="merchantForm.shopName" size="large" />
            </el-form-item>
            <el-form-item label="Business License" prop="businessLicense">
              <el-input v-model="merchantForm.businessLicense" size="large" />
            </el-form-item>
            <el-form-item label="Phone" prop="phone">
              <el-input v-model="merchantForm.phone" size="large" />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input v-model="merchantForm.password" type="password" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" native-type="submit" class="register-btn">
                Register as Merchant
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="register-footer">
        Already have an account? <router-link to="/login">Login</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('user')
const loading = ref(false)
const codeCooldown = ref(0)
const userFormRef = ref<FormInstance>()
const merchantFormRef = ref<FormInstance>()

const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  verificationCode: '',
})

const merchantForm = reactive({
  username: '',
  shopName: '',
  businessLicense: '',
  phone: '',
  password: '',
})

const userRules: FormRules = {
  username: [{ required: true, min: 3, max: 50, message: '3-50 characters', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: 'Valid email required', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: 'Valid phone required', trigger: 'blur' }],
  password: [{ required: true, min: 8, max: 32, message: '8-32 characters', trigger: 'blur' }],
  verificationCode: [{ required: true, len: 6, message: '6-digit code', trigger: 'blur' }],
}

const merchantRules: FormRules = {
  username: [{ required: true, min: 3, max: 50, message: '3-50 characters', trigger: 'blur' }],
  shopName: [{ required: true, message: 'Shop name required', trigger: 'blur' }],
  businessLicense: [{ required: true, message: 'License required', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: 'Valid phone required', trigger: 'blur' }],
  password: [{ required: true, min: 8, max: 32, message: '8-32 characters', trigger: 'blur' }],
}

function sendCode() {
  codeCooldown.value = 60
  const timer = setInterval(() => {
    codeCooldown.value--
    if (codeCooldown.value <= 0) clearInterval(timer)
  }, 1000)
  ElMessage.success('Verification code sent')
}

async function handleRegister(type: string) {
  loading.value = true
  try {
    if (type === 'user') {
      await authStore.register(userForm)
    } else {
      await authStore.register(merchantForm)
    }
    ElMessage.success('Registration successful')
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.message || 'Registration failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;
}

.register-card {
  width: 480px;
}

.register-title {
  text-align: center;
  margin: 0;
  font-size: 24px;
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
}

.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
</style>
